<a id="top"></a>

# 트러블슈팅

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고한다.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 루트 README | [README](../README.md) | 서비스 README | [프론트엔드](../StockFrontEnd/README.md) |
| Engineering Notes | [Engineering Notes](ENGINEERING.md) | Database Schema ERD | [Database Schema ERD](database-schema.md) |
| Troubleshooting | [Troubleshooting](TROUBLESHOOTING.md) | Docker | [Docker / 인프라](../docker/README.md) |
| user-service | [user-service](../StockBackEndDistributed/user-service/README.md) | stock-service | [stock-service](../StockBackEndDistributed/stock-service/README.md) |
| order-service | [order-service](../StockBackEndDistributed/order-service/README.md) |  |  |

## 목차

> [개요](#개요) ·
> [서비스별 트러블슈팅](#서비스별-트러블슈팅) ·
> [문서화 과정에서 정리한 문제](#문서화-과정에서-정리한-문제) ·
> [프로젝트를 통해 배운 점](#프로젝트를-통해-배운-점)

## 개요

이 문서는 프로젝트 전체 트러블슈팅의 진입점이다. 기존에 루트 문서에 모아 두었던 문제 해결 과정은 실제로 문제를 해결한 서비스의 `docs` 문서로 분리했다.

기능 설명은 각 서비스 기능 문서에 두고, 문제 상황과 원인, 해결 방법, 배운 점은 서비스별 트러블슈팅 문서에서 다룬다.

## 서비스별 트러블슈팅

| 서비스 | 문서 | 다루는 문제 |
| --- | --- | --- |
| order-service | [order-service 트러블슈팅](../StockBackEndDistributed/order-service/docs/11-troubleshooting.md) | 주문 처리 동시성, Kafka DLT, Candle 분리, Bot 자동 주문 제어 |
| stock-service | [stock-service 트러블슈팅](../StockBackEndDistributed/stock-service/docs/11-troubleshooting.md) | 외부 시세와 내부 체결 데이터 기준 불일치 |

현재 루트 트러블슈팅에서 분리할 user-service 전용 해결 사례는 별도로 정리하지 않았다. user-service의 현재 위험과 개선 필요 항목은 [user-service 이슈](../StockBackEndDistributed/user-service/docs/10-user-service-issues.md)에 둔다.

프론트엔드의 빌드, lint, 화면 구현 이슈는 [프론트엔드 이슈](../StockFrontEnd/docs/10-frontend-issues.md)에 둔다.

## 문서화 과정에서 정리한 문제

### 문제 상황

문서가 늘어나면서 Mermaid 다이어그램이 구현 파일과 함수 호출 중심으로 흐르거나, 문서 포털 링크와 표현 방식이 문서마다 달라질 수 있었다.

### 원인

서비스별 README와 `docs/` 문서가 각각 작성되면서 같은 기능을 다른 용어로 부르거나, 문서 포털 순서가 흔들릴 가능성이 있었다. 다이어그램도 시스템 동작보다 구현 구조를 직접 노출하기 쉬웠다.

### 해결 방법

문서 포털은 4열 표 구조를 유지하고, 공통 문서와 서비스 문서의 배치 순서를 맞춘다. Mermaid 라벨은 파일명과 함수명보다 사용자 요청, 상태 변경, 이벤트 전달처럼 시스템 동작 중심으로 작성한다. API, topic, 필드명처럼 실제 코드와 대응되는 식별자는 백틱으로 표시한다.

### 배운 점

문서는 코드 목록이 아니라 시스템 이해를 돕는 진입점이어야 한다. 설계 문서, 기능 문서, 이슈 문서, 트러블슈팅 문서의 역할을 나누면 구현 변경 이후에도 문서의 판단 기준을 유지할 수 있다.

## 프로젝트를 통해 배운 점

첫째, 실시간 거래 도메인은 기능보다 상태 전파가 더 어렵다. 주문 하나가 자산, 호가, 체결, 시세, Candle, 사용자 화면에 영향을 주기 때문에 이벤트 경계와 데이터 소유권을 먼저 정해야 한다.

둘째, MSA는 코드를 나누는 것만으로 완성되지 않는다. 서비스 경계를 나누면 Kafka topic, 장애 처리, 초기화 순서, 설정 관리, 문서 관리까지 같이 복잡해진다.

셋째, 캐시는 성능 도구이면서 동시에 일관성 문제를 만든다. OrderBook, StockCache, CandleCache, Redis 현재 Candle은 각각 빠른 조회와 실시간 반영을 위해 필요하지만, 서버 재시작과 DB 동기화 흐름을 함께 설계해야 한다.

넷째, 외부 데이터를 붙이는 것보다 데이터 기준을 하나로 유지하는 것이 더 중요할 때가 있다. KIS 연동은 기능 자체보다 내부 시뮬레이션 데이터와의 기준 불일치가 더 큰 문제였다.

마지막으로, 문서는 기능 목록만으로는 부족하다. 무엇을 만들었는지와 왜 그렇게 설계했는지를 분리해야 나중에 구현을 바꾸더라도 판단 기준을 잃지 않는다.

### 관련 문서 링크

- [Engineering Notes](ENGINEERING.md)
- [Documentation Index](DOCUMENTATION.md)
- [order-service 트러블슈팅](../StockBackEndDistributed/order-service/docs/11-troubleshooting.md)
- [stock-service 트러블슈팅](../StockBackEndDistributed/stock-service/docs/11-troubleshooting.md)

<div align="right">

[문서 맨 위로](#top)

</div>
