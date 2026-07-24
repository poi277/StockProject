<a id="top"></a>

# 외부 시세 연동 사용 중단

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고합니다.

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

> [개요](#개요) · [현재 시세 흐름](#현재-시세-흐름) · [남아 있는 조회 API](#남아-있는-조회-api) · [핵심 구현 파일](#핵심-구현-파일) · [관련 문서](#관련-문서)

## 개요

외부 시장 데이터 연동은 현재 주요 시세 흐름에서 사용하지 않습니다. 내부 캐시 데이터와 외부 데이터 사이의 불일치를 피하기 위해 체결 이벤트와 저장된 Candle을 기준으로 시세를 구성합니다.

## 현재 시세 흐름

현재가와 거래량은 프로젝트 내부 체결 결과를 기준으로 갱신합니다.

### 동작 순서

1. 서비스 시작 시 저장된 종목과 Candle로 초기 시세를 복구합니다.
2. 주문 서비스에서 체결 결과를 수신합니다.
3. 체결 가격과 수량을 실시간 시세에 반영합니다.
4. 갱신한 현재가와 체결 내역을 사용자 화면에 전달합니다.

### 핵심 코드

```java
public void applyTradeExecutions(List<TradeExecution> executions) {
    if (executions == null || executions.isEmpty()) {
        return;
    }
    String stockCode = executions.get(0).getStockCode();
    StockRealTimeSnapshot snapshot = stockCache.get(stockCode);
    if (snapshot == null) {
        return;
    }

    // 생략: 체결 가격·거래량·등락률 반영과 WebSocket 발행
}
```

체결 목록을 입력받아 대상 종목의 갱신을 진행하며, 결과는 캐시와 WebSocket에 반영됩니다.

### 구현 위치

- 초기 시세 복구: `init/StockScheduler.java`
- 체결 반영: `features/Stock/StockService.java`
- 실시간 발행: `features/webSocket/WebSocketService.java`

## 남아 있는 조회 API

외부 시세는 주요 가격 갱신 흐름에서 사용하지 않지만 직접 조회하는 Controller는 남아 있습니다.

### 동작 순서

1. 외부 시장의 현재가를 조회하는 REST API(`GET /api/kis/stock/price/{code}`)를 호출합니다.
2. 외부 시장의 매수·매도 호가를 조회하는 REST API(`GET /api/kis/stock/ask/{code}`)를 호출합니다.
3. 응답은 외부 시세 연동 Service에서 받아 그대로 반환합니다.

### 핵심 코드

```java
@GetMapping("/price/{code}")
public CurrentPriceResponse price(@PathVariable("code") String code) {
    return kisStockService.getCurrentPrice(code);
}

@GetMapping("/ask/{code}")
public AskingPriceResponse ask(@PathVariable("code") String code) {
    return kisStockService.getAskingPrice(code);
}
```

현재가와 호가 조회를 별도 API로 남겨둔 경계입니다. 종목 코드를 입력받아 KIS 응답 DTO를 반환하지만, 이 결과는 내부 실시간 시세 캐시를 변경하지 않습니다.

### 구현 위치

- 외부 시세 요청: `features/Stock/KisStockController.java`
- 외부 연동: `config/kis/KisStockService.java`

## 핵심 구현 파일

기준 경로: `StockBackEndDistributed/stock-service/src/main/java/Poi/Stock`

| 파일 |
| --- |
| `init/StockScheduler.java` |
| `features/Stock/StockService.java` |
| `features/webSocket/WebSocketService.java` |
| `features/Stock/KisStockController.java` |
| `config/kis/KisStockService.java` |

## 관련 문서

- [실시간 시세](03-realtime-stock-cache.md)
- [체결 처리](04-kafka-trade-execution.md)
- [주기 작업](06-scheduler.md)

<div align="right">

[문서 맨 위로](#top)

</div>



