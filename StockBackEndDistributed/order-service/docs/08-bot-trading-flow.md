<a id="top"></a>

# 봇 거래 구조

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고합니다.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 주식 README | [README](../../../README.md) | 주문 서비스 README | [README](../README.md) |
| 설계 노트 | [Engineering Notes](../../../docs/ENGINEERING.md) | 데이터베이스 ERD | [Database Schema ERD](../../../docs/database-schema.md) |
| 주문 서비스 개요 | [주문 서비스 개요](00-order-service-overview.md) | 주문 API | [주문 API](01-order-api.md) |
| Kafka 주문 흐름 | [Kafka 주문 흐름](02-kafka-order-flow.md) | 정산/체결 이벤트 | [정산/체결 이벤트](03-settlement-and-trade-events.md) |
| 호가장 | [호가장](04-orderbook.md) | 매칭 엔진 | [매칭 엔진](05-matching-engine.md) |
| Candle 차트 흐름 | [Candle 차트 흐름](06-candle-chart-flow.md) | 실시간 발행 흐름 | [실시간 발행 흐름](07-websocket-flow.md) |
| Bot 거래 구조 | [Bot 거래 구조](08-bot-trading-flow.md) | 초기화/주기 작업 | [초기화/주기 작업](09-initialization-and-scheduler.md) |
| 주문 서비스 이슈 | [order-service 이슈](10-order-service-issues.md) |  |  |

## 목차

> [개요](#개요) ·
> [Bot을 만든 목적](#bot을-만든-목적) ·
> [봇 구성](#봇-구성) ·
> [데이터 구조](#데이터-구조) ·
> [Bot 전략](#bot-전략) ·
> [시장 상태 저장](#시장-상태-저장) ·
> [봇 초기화](#봇-초기화) ·
> [봇 주문 생성 과정](#봇-주문-생성-과정)

> [시장 상태 갱신](#시장-상태-갱신) ·
> [봇 흐름](#봇-흐름) ·
> [핵심 구현 파일](#핵심-구현-파일)

## 개요

order-service에는 시장 시뮬레이션을 위한 Bot 모델, 보유 주식, 캐시, 전략 클래스, 주문 실행 경로가 구현되어 있습니다.

## Bot을 만든 목적

Bot은 사용자가 직접 주문하지 않아도 호가장과 체결 흐름을 테스트할 수 있도록 만든 시장 참여자 모델입니다. order-service의 매칭 엔진은 상대 주문이 있어야 체결이 발생하므로, Bot은 매수/매도 주문을 생성해 호가장, 체결, 정산, Candle, WebSocket 흐름을 검증할 수 있게 합니다.

Bot은 실제 외부 시장을 예측하는 투자 알고리즘이 아니라, 프로젝트 내부에서 시장 상황을 흉내 내기 위한 시뮬레이션 구성입니다. 각 Bot은 보유 현금, 보유 주식, 종목별 시세 스냅샷, 시장 상태를 참고해 매수/매도/관망 중 하나를 선택합니다.

## 봇 구성

Bot 타입은 `EnumUtil.BotType`에 정의되어 있습니다.

- `INDIVIDUAL`
- `FOREIGN`
- `INSTITUTION`

각 타입별 전략 클래스가 존재합니다.

- `IndividualBot`
- `ForeignBot`
- `InstitutionBot`

공통 전략 기반은 `AbstractBot`에 구현되어 있습니다.

### 데이터 구조

```mermaid
erDiagram
    Bot ||--o{ BotHaveStock : owns

    Bot {
        string botId PK
        string botType
        bigint asset
    }

    BotHaveStock {
        bigint id PK
        string bot FK
        string stockCode
        int quantity
        double averagePrice
    }
```

Bot은 유형과 자산을 저장하고, 종목별 보유 수량과 평균 매입가는 `BotHaveStock`에 분리합니다. Bot 참조 컬럼은 명시적 `JoinColumn` 없이 `ManyToOne` 기본 규칙으로 생성되므로 실제 컬럼명은 JPA naming strategy를 따르며, 코드에 별도 인덱스는 선언되어 있지 않습니다.

### 테이블 비교

| 구분 | 테이블 | PK | FK | 역할 |
| --- | --- | --- | --- | --- |
| 시장 참여자 | `Bot` | 문자열 `botId` | 없음 | Bot 유형과 현금 자산 저장 |
| 종목별 보유 주식 | `BotHaveStock` | 자동 생성 `id` | `Bot` 참조(`ManyToOne`) | 보유 수량과 평균 매입가 저장 |

### 구현 위치

- Bot 엔티티: `features/Bot/Bot.java`
- Bot 보유 주식 엔티티: `features/Bot/BotHaveStock.java`

## Bot 전략

세 Bot은 모두 `AbstractBot`의 공통 흐름을 사용합니다. 공통 흐름은 종목별 시세를 조회하고, 시장 상태와 이동평균, Bot 자산, 전략 강도를 조합해 주문 여부를 결정합니다. 차이는 각 Bot이 매수/매도/관망 확률을 계산하는 방식과 주문 수량 범위, 기존 주문 취소 성향에 있습니다.

| Bot | 실제 코드 기준 성향 |
| --- | --- |
| `IndividualBot` | `MA5`, `MA20`, `MA60`과 시장 상태를 함께 확인합니다. 상승장에서는 매수 가중치가 커지고, 하락장에서는 매도 가중치가 커진다. 기본 주문 수량 범위가 작고, 호가가 멀어지거나 시장이 급변하면 주문 취소 확률이 상대적으로 높습니다. |
| `ForeignBot` | `MA20`, `MA60`을 중심으로 추세를 판단합니다. 상승장에서도 정배열과 현재가 위치에 따라 매수 강도를 조정하고, 하락장에서는 매도 가중치를 높입니다. 개인 Bot보다 주문 수량 범위가 크고, 취소 기준 호가 범위도 더 넓습니다. |
| `InstitutionBot` | `MA20`, `MA60`과 현재가 위치를 사용합니다. 상승 추세가 뚜렷하면 매수 가중치를 크게 높이고, 하락 추세가 강하면 매도 가중치를 높입니다. 주문 수량 범위가 가장 크고, 취소 기준이 넓어 기존 주문을 더 오래 유지하는 성향입니다. |

모든 전략은 최종적으로 `BUY`, `SELL`, `HOLD` 중 하나를 선택합니다. 선택 결과가 `BUY` 또는 `SELL`이면 `BotOrderService`가 주문용 `TradeDTO`를 만들어 order-service의 매칭 흐름으로 전달합니다.

## 시장 상태 저장

`MarketStateHolder`는 종목별 시장 상태를 저장합니다. 상태 값은 `BULL`, `BEAR`, `FLAT`이며, 각각 상승장, 하락장, 횡보장을 의미합니다.

시장 상태는 1분봉 캐시에 저장된 이동평균을 기준으로 계산합니다.

- `MA5 > MA20 > MA60`: `BULL`
- `MA5 < MA20 < MA60`: `BEAR`
- 그 외 또는 이동평균 데이터 부족: `FLAT`

또한 `MA5`와 `MA60`의 괴리율을 기준으로 시장 강도(`intensity`)를 계산합니다. Bot 전략은 이 시장 상태와 강도를 참고해 주문 수량, 가격, 매수/매도 확률을 조정합니다.

### 동작 순서

1. 최신 1분봉의 이동평균을 조회합니다.
2. `MA5`, `MA20`, `MA60` 배열로 시장 상태를 결정합니다.
3. 단기·장기 이동평균 괴리율로 시장 강도를 저장합니다.

### 핵심 코드

```java
MarketState state;
if (ma5 > ma20 && ma20 > ma60) {
    state = MarketState.BULL;
} else if (ma5 < ma20 && ma20 < ma60) {
    state = MarketState.BEAR;
} else {
    state = MarketState.FLAT;
}

double divergence = Math.abs((ma5 - ma60) / ma60 * 100);
int intensity;
if (divergence >= 3.0) intensity = 90;
else if (divergence >= 2.0) intensity = 75;
else if (divergence >= 1.0) intensity = 60;
else if (divergence >= 0.5) intensity = 45;
else intensity = 30;
stateMap.put(stockCode, state);
intensityMap.put(stockCode, intensity);
```

전략 클래스마다 추세 계산을 중복하지 않도록 이동평균 기반 시장 상태를 공통 저장소에서 계산합니다. 세 이동평균을 입력으로 방향과 강도를 만들며, 결과는 모든 Bot의 매수·매도·관망 확률에 사용됩니다.

### 구현 위치

- 시장 상태 계산: `features/Bot/MarketStateHolder.java`의 `updateMarketState()`

## 봇 초기화

`BotInit`은 서버 시작 시 Bot이 주문을 만들 수 있는 상태를 준비합니다.

초기화 흐름은 다음과 같습니다.

```mermaid
flowchart TD
    Start["서버 시작"] --> CreateBot["시장 참여자 생성 또는 조회"]
    CreateBot --> RegisterBot["시장 참여자 캐시 등록"]
    RegisterBot --> Strategy["유형별 거래 전략 준비"]
    Strategy --> InitStock["보유 주식 생성 또는 조회"]
    InitStock --> CopyMarket["봇이 사용할 주식 데이터 복사"]
    CopyMarket --> BotStockCache["봇이 사용할 주식 데이터 구성"]
    BotStockCache --> MarketState["시장 상태 계산"]
    MarketState --> Ready["주문 준비 완료"]
```

초기 Bot 자산과 보유 주식은 코드에 기본값으로 정의되어 있습니다. Bot이 처음 생성되면 초기 자산이 설정되고, 종목별 Bot 보유 주식이 없으면 현재 종목 캐시 기준으로 보유 주식을 생성합니다.

### 동작 순서

1. 저장된 Bot을 조회하거나 초기 자산으로 생성합니다.
2. Bot 유형별 전략 인스턴스를 캐시에 등록합니다.
3. 종목별 보유 주식과 시장 상태를 준비합니다.

### 핵심 코드

```java
private Bot getOrCreateBot(String botId, BotType botType) {
    return botRepository.findById(botId).orElseGet(() -> {
        Bot newBot = new Bot();
        newBot.setBotId(botId);
        newBot.setBotType(botType);
        newBot.setAsset(INITIAL_ASSET);
        return botRepository.save(newBot);
    });
}

private void initBotCache() {
    stockCache.getCache().forEach((stockCode, stock) -> {
        botStockCache.put(stockCode, stock.botCacheCopy());
        marketStateHolder.updateMarketState(stockCode);
    });
}
```

재시작할 때 신규 Bot만 기본 상태로 생성하기 위한 초기화 로직입니다. Bot ID와 유형을 입력으로 재사용하고, 종목 스냅샷 복사본과 시장 상태를 Bot 전용 캐시에 구성합니다.

### 구현 위치

- Bot과 시장 캐시 초기화: `features/Bot/BotInit.java`

## 봇 주문 생성 과정

Bot 주문은 시장 상태와 Bot 전략을 기준으로 생성됩니다. 주문 실행 경로는 `BotOrderService`에 구현되어 있습니다.

동작 흐름은 다음과 같습니다.

```mermaid
flowchart TD
    Market["시장 상태<br/>BULL / BEAR / FLAT"] --> Strategy["Bot 전략<br/>Individual / Foreign / Institution"]
    Strategy --> Decision["매수 / 매도 / 관망 결정"]
    Decision -->|BUY 또는 SELL| DTO["주문 데이터 생성"]
    Decision -->|HOLD| Hold["주문 생성 안 함"]
    DTO --> OrderService["주문 실행"]
    OrderService --> Matching["주문 매칭"]
    Matching --> Settlement["정산 이벤트 / 체결 이벤트"]
    Settlement --> Candle["차트 갱신"]
    Settlement --> WebSocket["실시간 변경 발행"]
```

일반 사용자 주문은 주문 처리 Kafka Topic(`order-topic`)을 통해 비동기로 처리합니다. Bot 주문은 매칭 경로로 직접 진입하지만, 이후 정산과 Candle 갱신, 실시간 발행은 같은 흐름을 사용합니다.

Bot이 생성한 주문은 별도의 호가장을 사용하지 않습니다. Bot 주문도 일반 사용자 주문과 동일한 종목별 호가장에 등록됩니다. 따라서 Bot 주문과 사용자 주문은 같은 시장 안에서 가격 우선, 시간 우선 규칙에 따라 함께 매칭됩니다.

즉 Bot은 별도의 매칭 시스템이 아니라 프로젝트의 동일한 시장에 참여하는 하나의 참가자로 동작합니다.

### 동작 순서

1. Bot 주문에 내부 음수 주문 ID와 주문 정보를 구성합니다.
2. 종목별 락을 획득합니다.
3. 일반 주문과 동일한 `processOrder()`로 직접 전달합니다.

### 핵심 코드

```java
public void placeOrder(String botId, String stockCode, String stockName,
        tradeType type, int price, int quantity) {
    stockLock.lock(stockCode);
    try {
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setOrderId(-Math.abs(java.util.UUID.randomUUID()
                .getMostSignificantBits()));
        tradeDTO.setStockCode(stockCode);
        tradeDTO.setUserId(botId);
        tradeDTO.setTradeType(type);
        tradeDTO.setTradePrice(price);
        tradeDTO.setQuantity(quantity);
        tradeDTO.setStockName(stockName);
        orderService.processOrder(tradeDTO);
    } catch (Exception e) {
        log.error("봇 주문 처리 실패: {} / {}", botId, e.getMessage(), e);
    } finally {
        stockLock.unlock(stockCode);
    }
}
```

사용자 주문과 같은 호가장과 처리 흐름을 재사용합니다. Bot 전략의 주문 정보를 DTO로 변환해 종목별 락 안에서 처리하며, 결과는 일반 체결과 동일하게  반영됩니다.

### 구현 위치

- Bot 주문 생성과 매칭 진입: `features/Bot/BotOrderService.java`의 `placeOrder()`

## 시장 상태 갱신

`BotScheduler`는 Bot 전략이 참고하는 시장 상태를 주기적으로 갱신합니다. 이 갱신 결과는 `MarketStateHolder`에 저장되고, Bot 전략은 종목별 `BULL`, `BEAR`, `FLAT` 상태와 시장 강도를 참고해 매수, 매도, 관망 결정을 내립니다.

시장 상태 갱신 흐름:

```mermaid
flowchart TD
    Scheduler["시장 상태 주기 갱신"] --> Holder["종목별 시장 상태 저장"]
    Holder --> CandleCache["1분봉 이동평균 조회"]
    CandleCache --> State["시장 상태 계산<br/>BULL / BEAR / FLAT"]
    State --> Intensity["시장 강도 계산"]
    Intensity --> Strategy["Bot 전략에서 참고"]
```

## 봇 흐름

```mermaid
flowchart TD
    Init["서버 시작 시 봇 초기화"] --> BotCache["시장 참여자/전략/보유 주식 준비"]
    Init --> BotStock["시장 데이터 복사"]
    BotStock --> Market["시장 상태 계산"]
    Scheduler["시장 상태 주기 갱신"] --> Market
    Market --> Strategy["Bot 전략이 시장 상태 참고"]
    Strategy --> Decision["BUY / SELL / HOLD"]
    Decision --> DTO["주문 데이터 생성"]
    DTO --> Process["주문 매칭 경로 진입"]
    Process --> Matching["주문 매칭"]
    Matching --> Settlement["정산 / 시세 / Candle / 실시간 발행 흐름"]
```

## 핵심 구현 파일

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/features/Bot`

| 파일 |
| --- |
| `Bot.java` |
| `BotHaveStock.java` |
| `BotInit.java` |
| `BotScheduler.java` |
| `AbstractBot.java` |
| `IndividualBot.java` |
| `ForeignBot.java` |
| `InstitutionBot.java` |
| `BotOrderService.java` |
| `MarketStateHolder.java` |

<div align="right">

[문서 맨 위로](#top)

</div>
