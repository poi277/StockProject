<a id="top"></a>

# stock-service 트러블슈팅

## 문서 포털

상세 구현, API, 아키텍처와 트러블슈팅 정보는 아래 문서에서 확인할 수 있습니다.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 주식 README | [README](../../../README.md) | 종목 서비스 README | [README](../README.md) |
| 설계 노트 | [Engineering Notes](../../../docs/ENGINEERING.md) | 데이터베이스 ERD | [Database Schema ERD](../../../docs/database-schema.md) |
| 개요 | [개요](01-overview.md) | 종목 API | [종목 API](02-stock-api.md) |
| 실시간 시세 캐시 | [실시간 시세 캐시](03-realtime-stock-cache.md) | Kafka 체결 처리 | [Kafka 체결 처리](04-kafka-trade-execution.md) |
| 실시간 연결 | [실시간 연결](05-websocket.md) | 주기 작업 | [주기 작업](06-scheduler.md) |
| Candle 구조 | [Candle 구조](07-candle-structure.md) | 외부 시세 연동 사용 중단 | [외부 시세 연동 사용 중단](08-external-market-data-disabled.md) |
| 도메인 모델 | [도메인 모델](09-domain-model.md) | 주식 서비스 이슈 | [stock-service 이슈](10-stock-service-issues.md) |

## 목차

> [개요](#개요) · [체결 메시지 변환](#체결-메시지-변환) · [최근 거래 통계](#최근-거래-통계) · [시작 시 시세 복구](#시작-시-시세-복구) · [핵심 구현 파일](#핵심-구현-파일) · [관련 문서](#관련-문서)

## 개요

체결 이벤트 변환, 최근 거래 통계와 서비스 시작 시 시세 복구 과정에서 해결한 문제를 정리합니다.

## 체결 메시지 변환

주문 서비스에서 전달한 체결 목록을 바로 변환하지 못해 시세 갱신이 중단되었습니다. 목록을 하나의 메시지 객체로 감싸고 두 서비스의 객체 구조를 맞춰 해결했습니다.

### 동작 순서

1. 주문 서비스가 체결 목록을 wrapper 객체로 전송합니다.
2. 종목 서비스가 같은 구조의 객체로 메시지를 변환합니다.
3. 내부 체결 목록을 종목별 시세 갱신에 전달합니다.

### 핵심 코드

```java
@KafkaListener(topics = "trade-execution-topic", groupId = "stock-service-group")
@Transactional
public void consumeTradeExecution(@Payload TradeExecutionList message) {
    try {
        stockService.applyTradeExecutions(message.getExecutions());
    } catch (Exception e) {
        log.error("시세 처리 실패: {}", e.getMessage());
        kafkaProducer.sendToExecutionDLT(message);
    }
}
```

Kafka 역직렬화 대상과 내부 처리 입력이 달라지는 문제를 피하기 위해 체결 목록을 `TradeExecutionList`로 감싼 메시지 경계를 사용합니다. wrapper를 입력받아 내부 목록만 시세 서비스에 전달하며, 변환이나 처리 실패 시 원본 wrapper를 DLT로 보냅니다.

### 구현 위치

- 메시지 수신: `features/kafka/TradeExecutionConsumer.java`
- 체결 목록: `object/TradeExecutionList.java`

## 최근 거래 통계

최근 30분이라는 이동 구간을 정확히 유지하기 위해 단순 누적 캐시 대신 저장된 분봉을 사용합니다. 현재 시각부터 30분 전까지의 Candle을 조회해 거래 통계를 계산합니다.

### 동작 순서

1. 현재 시각 기준 30분 전을 계산합니다.
2. 해당 구간에 거래가 있는 종목을 조회합니다.
3. 종목별 분봉의 매수·매도 수량과 거래대금을 합산합니다.
4. 목록 API에서 사용할 통계 상태를 갱신합니다.

### 핵심 코드

```java
LocalDateTime from = LocalDateTime.now().minusMinutes(30);
List<String> stockCodes = candleMinuteRepository.findDistinctStockCodes();
for (String stockCode : stockCodes) {
    try {
        List<CandleMinute> recent = candleMinuteRepository
                .findByStockCodeAndTimeAfter(stockCode, from);
        long buyQty = recent.stream()
                .mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0).sum();
        long sellQty = recent.stream()
                .mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0).sum();
        double tradeAmount = recent.stream()
                .mapToDouble(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0).sum();
        stockTradeStatusCache.put(
                stockCode, new StockTradeStatus(buyQty, sellQty, tradeAmount));
        // 생략: 종목별 통계 갱신 로그
    } catch (Exception e) {
        log.error("캐시 갱신 실패 - stockCode: {} error: {}",
                stockCode, e.getMessage());
    }
}
```

서비스 실행 시간과 무관하게 항상 최근 30분만 집계하려면 누적 메모리 값보다 시간 조건이 있는 Candle 조회가 필요합니다. 기준 시각과 종목 코드를 입력으로 거래 통계를 다시 계산하고, 목록 API가 사용하는 통계 캐시에 저장합니다.

### 구현 위치

- 통계 계산: `Scheduler/StockTradeStatsScheduler.java`
- 분봉 조회: `features/Candle/CandleMinuteRepository.java`

## 시작 시 시세 복구

최근 일봉만 사용하면 당일 체결이 반영되지 않아 재시작 전후 시세가 달라졌습니다. 전일 종가와 당일 분봉을 함께 조회해 최신 시세를 복구합니다.

### 동작 순서

1. 최근 일봉에서 전일 종가를 조회합니다.
2. 당일 분봉에서 현재가, 고가, 저가와 거래량을 조회합니다.
3. 두 데이터를 결합해 실시간 시세 스냅샷을 만듭니다.
4. 종목별 시세 상태에 저장합니다.

### 핵심 코드

```java
int currentPrice, highPrice, lowPrice; long totalVolume;

if (todayCandles.isEmpty()) {
    currentPrice = highPrice = lowPrice = yesterdayClose;
    totalVolume = 0L;
} else {
    currentPrice = todayCandles.get(todayCandles.size() - 1).getClose();
    highPrice = todayCandles.stream()
            .mapToInt(CandleMinute::getHigh).max().orElse(yesterdayClose);
    lowPrice = todayCandles.stream()
            .mapToInt(CandleMinute::getLow).min().orElse(yesterdayClose);
    totalVolume = todayCandles.stream()
            .mapToLong(CandleMinute::getTotalVolume).sum();
}

int changeAmount = currentPrice - yesterdayClose;
double changeRate = yesterdayClose == 0 ? 0.0
        : (changeAmount / (double) yesterdayClose) * 100;

StockRealTimeSnapshot snapshot = new StockRealTimeSnapshot(
        stockCode, stock.getStockName(), yesterdayClose,
        currentPrice, highPrice, lowPrice, totalVolume, changeAmount, changeRate);
stockCache.put(stockCode, snapshot);
```

재시작 시 전일 종가만 사용하면 이미 진행된 당일 거래가 사라지므로 당일 분봉의 마지막 값과 범위를 함께 복원합니다. 일봉과 당일 분봉을 입력으로 스냅샷을 재구성하고, 이후 REST 조회와 체결 반영이 사용하는 캐시에 저장합니다.

### 구현 위치

- 초기 복구: `init/StockScheduler.java`
- 일봉 조회: `features/Candle/CandleDayRepository.java`
- 분봉 조회: `features/Candle/CandleMinuteRepository.java`

## 핵심 구현 파일

기준 경로: `StockBackEndDistributed/stock-service/src/main/java/Poi/Stock`

| 파일 |
| --- |
| `features/kafka/TradeExecutionConsumer.java` |
| `object/TradeExecutionList.java` |
| `Scheduler/StockTradeStatsScheduler.java` |
| `init/StockScheduler.java` |
| `features/Candle/CandleMinuteRepository.java` |
| `features/Candle/CandleDayRepository.java` |

## 관련 문서

- [체결 처리](04-kafka-trade-execution.md)
- [주기 작업](06-scheduler.md)
- [Candle 구조](07-candle-structure.md)

<div align="right">

[문서 맨 위로](#top)

</div>
