<a id="top"></a>

# 주문 서비스 개요

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
> [전체 구조](#전체-구조) ·
> [주요 디렉터리](#주요-디렉터리) ·
> [설정 주의](#설정-주의)

> [핵심 구현 파일](#핵심-구현-파일)

## 개요

주문 서비스는 주문 접수부터 매칭, 체결 후속 처리까지 주식 거래의 전체 흐름을 담당합니다. 호가장과 Candle을 갱신하고, 사용자 자산과 종목 시세에 반영할 체결 결과를 다른 서비스로 전달합니다.

현재 코드 기준 주요 책임은 다음과 같습니다.

- 사용자 주문 접수, 정정, 취소 API 제공
- user-service를 통한 주문 가능 여부와 정정/취소 가능 여부 검증
- 주문 이벤트를 전달하는 Kafka Topic(`order-topic`) 기반 비동기 처리
- 종목별 메모리 호가장 관리
- 가격/시간 우선 방식의 주문 매칭
- 체결 결과를 user-service 정산 이벤트와 stock-service 체결 이벤트로 발행
- 체결 데이터를 기반으로 진행 중인 Candle을 Redis에 갱신
- 과거 Candle 조회 REST API와 실시간 Candle 발행 제공
- 서버 시작 시 종목, 호가장, Candle 캐시, Bot 캐시 초기화
- Bot 모델과 주문 실행 경로 제공


## 주문 서비스 전체 구조

```mermaid
flowchart LR
    Client["사용자 화면"] --> OrderAPI["주문 요청 접수"]
    OrderAPI --> UserService["주문 가능 여부 검증"]
    OrderAPI --> KafkaProducer["주문 이벤트 발행"]
    KafkaProducer --> OrderTopic["주문 이벤트 대기열"]
    OrderTopic --> KafkaConsumer["주문 이벤트 수신"]
    KafkaConsumer --> Matching["주문 매칭"]
    Matching --> OrderBook["메모리 호가장 갱신"]
    Matching --> DB[("주문/체결 결과 저장")]
    Matching --> Settlement["체결 결과 이벤트 발행"]
    Settlement --> UserSettlement["정산 이벤트 전달"]
    Settlement --> StockExecution["시세 반영 이벤트 전달"]
    Matching --> Candle["현재 Candle 갱신"]
    Candle --> Redis[("진행 중인 Candle 저장")]
    Matching --> WebSocket["주문/호가 변경 실시간 발행"]
    Candle --> WebSocket
    WebSocket --> Client
```

## 주요 디렉터리

기준 경로

`src/main/java/Poi/Stock`

| 디렉터리 | 설명 |
| --- | --- |
| `features/Order` | 주문 API, 주문 서비스, 호가장, 체결 처리 |
| `features/kafka` | 주문 이벤트 수신과 체결 후속 이벤트 발행 |
| `features/Candle` | Candle 조회, 진행 데이터 저장, 주기 작업 |
| `features/Websocket` | 호가·주문 상태·Candle 실시간 발행 |
| `features/Bot` | Bot 엔티티, 캐시, 전략, 주문 실행 경로 |
| `init` | 서버 시작 시 캐시와 호가장 초기화 |
| `config` | 보안, JWT, Redis, WebSocket 설정 |

## 설정 주의

`application-docker.properties`에는 DB, Redis, JWT 등 민감 설정이 포함되어 있습니다. 해당 값은 문서에 직접 기록하지 않고, 운영 환경에서는 환경 변수로 분리 필요합니다.

## 핵심 구현 파일

기준 경로

`StockBackEndDistributed/order-service/src/main`

| 파일 |
| --- |
| `java/Poi/Stock/OrderServiceApplication.java` |
| `java/Poi/Stock/features/Order/OrderController.java` |
| `java/Poi/Stock/features/Order/OrderService.java` |
| `java/Poi/Stock/features/Order/OrderTradeService.java` |
| `java/Poi/Stock/features/kafka/KafkaConsumer.java` |
| `java/Poi/Stock/features/Candle/CandleService.java` |
| `java/Poi/Stock/features/Websocket/WebSocketService.java` |
| `resources/application-docker.properties` |

<div align="right">

[문서 맨 위로](#top)

</div>



