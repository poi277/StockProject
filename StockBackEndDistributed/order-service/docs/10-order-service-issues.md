<a id="top"></a>

# 주문 서비스 문제점 및 개선 필요 항목

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고하세요.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 루트 README | [README](../../../README.md) | 서비스 README | [README](../README.md) |
| Engineering Notes | [Engineering Notes](../../../docs/ENGINEERING.md) | Database Schema ERD | [Database Schema ERD](../../../docs/database-schema.md) |
| 00 | [주문 서비스 개요](00-order-service-overview.md) | 01 | [주문 API](01-order-api.md) |
| 02 | [Kafka 주문 흐름](02-kafka-order-flow.md) | 03 | [정산/체결 이벤트](03-settlement-and-trade-events.md) |
| 04 | [호가장](04-orderbook.md) | 05 | [매칭 엔진](05-matching-engine.md) |
| 06 | [Candle 차트 흐름](06-candle-chart-flow.md) | 07 | [실시간 발행 흐름](07-websocket-flow.md) |
| 08 | [Bot 거래 구조](08-bot-trading-flow.md) | 09 | [초기화/주기 작업](09-initialization-and-scheduler.md) |
| 10 | [order-service 이슈](10-order-service-issues.md) |  |  |

## 목차

> [민감 설정 분리 필요](#민감-설정-분리-필요) ·
> [웹소켓 인증 검증 부족](#웹소켓-인증-검증-부족) ·
> [주문 정정 처리 경로 불일치](#주문-정정-처리-경로-불일치) ·
> [일괄 취소와 예약 복구 흐름 확인 필요](#일괄-취소와-예약-복구-흐름-확인-필요) ·
> [봇 자동 주문 스케줄 비활성](#봇-자동-주문-스케줄-비활성)

> [사용자 주문과 봇 주문 처리 경로 차이](#사용자-주문과-봇-주문-처리-경로-차이) ·
> [캔들 스케줄러 서비스 컴파일 경고](#캔들-스케줄러-서비스-컴파일-경고) ·
> [인코딩 깨짐](#인코딩-깨짐) ·
> [프로젝트 루트의 JVM 오류 로그](#프로젝트-루트의-jvm-오류-로그)

## 민감 설정 분리 필요

`application-docker.properties`에 DB, Redis, JWT 등 민감 설정이 포함되어 있다. 문서에는 값을 기록하지 않는다.

개선 방향:

- 환경 변수로 분리 필요
- 배포 환경별 secret 관리 방식 도입 필요

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main`

| 파일 |
| --- |
| `resources/application-docker.properties` |
| `java/Poi/Stock/config/RedisConfig.java` |
| `java/Poi/Stock/config/JwtProvider.java` |

## 웹소켓 인증 검증 부족

WebSocket CONNECT에서 클라이언트가 전달한 `userId` 헤더를 그대로 Principal로 설정한다. REST API의 JWT 인증과 달리 WebSocket 연결 단계에서 토큰 검증이 보이지 않는다.

개선 방향:

- WebSocket CONNECT 시 JWT 검증
- user destination 사용 시 인증 사용자와 Principal 일치 검증

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/config`

| 파일 |
| --- |
| `WebSocketConfig.java` |
| `StompPrincipal.java` |

## 주문 정정 처리 경로 불일치

일반 주문은 Kafka `order-topic`을 통해 종목별 락을 잡고 처리된다. 반면 주문 정정은 `OrderService.stockEdit`에서 Kafka를 거치지 않고 직접 처리된다.

개선 방향:

- 주문 정정도 Kafka 기반으로 일관 처리할지 검토
- 종목별 락 적용 여부 명확화
- 일반 주문과 정정 주문의 저장/정산/WebSocket 흐름 차이 점검

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/features`

| 파일 |
| --- |
| `Order/OrderController.java` |
| `Order/OrderService.java` |
| `kafka/KafkaConsumer.java` |

## 일괄 취소와 예약 복구 흐름 확인 필요

단일 주문 취소 흐름은 user-service의 예약 복구 API와 연결된다. 그러나 일괄 취소와 범위 이탈 주문 취소 흐름은 메모리 호가장과 DB 삭제 중심이며 user-service 예약 복구 연결이 보이지 않는다.
단일 주문 취소 구현은 `cancelOrder()` (주문 취소와 예약 복구를 연결하는 기능)에서 담당한다.

이 메서드들이 실제 사용자 주문에 사용되면 예수금 또는 보유 수량 예약 상태와 불일치가 생길 수 있다.

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/features`

| 파일 |
| --- |
| `Order/OrderCancelService.java` |
| `Bot/BotService.java` |

## 사용자 주문과 봇 주문 처리 경로 차이

일반 사용자 주문은 Kafka를 거쳐 처리된다. Bot 주문은 Kafka 흐름을 거치지 않고 주문 매칭 처리로 직접 연결된다.
구현은 `BotOrderService` (Bot 주문 생성 기능)와 `OrderService.processOrder()` (주문 매칭 처리 기능)에서 확인된다.

개선 방향:

- Bot 주문도 Kafka로 통일할지 검토
- 직접 연결 유지 시 락, 실패 처리, DLT, 관측 가능성 차이를 문서화

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/features`

| 파일 |
| --- |
| `Bot/BotOrderService.java` |
| `Order/OrderService.java` |
| `Lock/StockLock.java` |

## 캔들 스케줄러 서비스 컴파일 경고

`compileJava`는 성공하지만 `CandleSchedulerService`에서 unchecked 또는 unsafe operation 경고가 발생한다.

개선 방향:

- 제네릭 타입 캐스팅 구간 점검
- `-Xlint:unchecked`로 상세 경고 확인

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/features/Candle`

| 파일 |
| --- |
| `CandleSchedulerService.java` |

## 인코딩 깨짐

일부 주석과 메시지의 한글이 깨져 있어 코드 이해와 유지보수가 어렵다.

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service/src/main/java/Poi/Stock/features`

| 파일 |
| --- |
| `Order/OrderService.java` |
| `Bot/*` |
| `Candle/*` |

## 프로젝트 루트의 JVM 오류 로그

서비스 루트에 `hs_err_pid16064.log` 파일이 존재한다. 소스 기능과 직접 관련된 문서는 아니지만, 저장소에 포함할 필요가 있는지 검토가 필요하다.

핵심 구현 파일:

기준 경로

`StockBackEndDistributed/order-service`

| 파일 |
| --- |
| `hs_err_pid16064.log` |

<div align="right">

[문서 맨 위로](#top)

</div>



