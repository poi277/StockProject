<a id="top"></a>

# 사용자 서비스 (user-service)

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Web](https://img.shields.io/badge/Spring%20Web-starter-6DB33F?logo=spring&logoColor=white)](https://spring.io/guides/gs/rest-service/)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-starter-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-starter-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-data-jpa)
[![Spring Data Redis](https://img.shields.io/badge/Spring%20Data%20Redis-starter-DC382D?logo=redis&logoColor=white)](https://spring.io/projects/spring-data-redis)
[![Spring Kafka](https://img.shields.io/badge/Spring%20Kafka-starter-6DB33F?logo=apachekafka&logoColor=white)](https://spring.io/projects/spring-kafka)
[![Spring WebSocket](https://img.shields.io/badge/Spring%20WebSocket-starter-6DB33F?logo=spring&logoColor=white)](https://docs.spring.io/spring-framework/reference/web/websocket.html)
[![JJWT](https://img.shields.io/badge/JJWT-0.12.5-000000)](https://github.com/jwtk/jjwt)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-driver-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고하세요.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 루트 README | [README](../../README.md) | 서비스 README | [README](README.md) |
| Engineering Notes | [Engineering Notes](../../docs/ENGINEERING.md) | Database Schema ERD | [Database Schema ERD](../../docs/database-schema.md) |
| 01 | [개요](docs/01-overview.md) | 02 | [인증/JWT](docs/02-auth-jwt.md) |
| 03 | [회원가입/프로필](docs/03-user-register-profile.md) | 04 | [자산/주문 검증](docs/04-user-asset-order-validation.md) |
| 05 | [Kafka 정산](docs/05-settlement-kafka.md) | 06 | [관심종목](docs/06-watchlist.md) |
| 07 | [실시간 연결](docs/07-websocket.md) | 08 | [도메인 모델](docs/08-domain-model.md) |
| 09 | [보안 설정](docs/09-security-config.md) | 10 | [user-service 이슈](docs/10-user-service-issues.md) |

## 목차

> [서비스 개요](#서비스-개요) ·
> [주요 구현 내용](#주요-구현-내용)

> [시스템 아키텍처](#시스템-아키텍처) ·
> [실행 방법](#실행-방법)

## 서비스 개요

사용자 계정, 인증, 자산, 관심종목, 주문 검증, 정산 처리를 담당하는 Spring Boot 서비스입니다.

user-service는 로그인과 JWT 발급을 처리하고, 보안토큰을 Redis에 저장합니다. 또한 order-service가 주문을 접수하기 전에 매수/매도 가능 여부를 검증하고, 체결 이후에는 Kafka `settlement-topic` 이벤트로 사용자 보유 현금과 보유 주식을 갱신합니다.

## 주요 구현 내용

- 회원가입, 로그인, JWT access token/refresh token 발급
- Redis 기반 refresh token 저장
- 사용자 프로필 조회/수정
- 보유 현금과 보유 주식 관리
- 주문 전 매수/매도 가능 여부 검증
- 주문 정정과 취소 시 예약 자산 복구
- Kafka `settlement-topic` 정산 이벤트 처리
- 관심종목 등록, 삭제, 조회
- 사용자 자산 변경 WebSocket 발행

| 영역 | 구현 내용 | 관련 문서 |
| --- | --- | --- |
| 인증 | 로그인, JWT 발급, refresh token Redis 저장 | [인증/JWT](docs/02-auth-jwt.md) |
| 회원/프로필 | 회원가입, 프로필 조회/수정 | [회원가입/프로필](docs/03-user-register-profile.md) |
| 자산 관리 | 보유 현금, 사용 가능 현금, 보유 주식 관리 | [자산/주문 검증](docs/04-user-asset-order-validation.md) |
| 주문 검증 | 매수 가능 금액과 매도 가능 수량 검증 | [자산/주문 검증](docs/04-user-asset-order-validation.md) |
| 정산 | Kafka `settlement-topic` 이벤트 소비 후 자산 반영 | [Kafka 정산](docs/05-settlement-kafka.md) |
| 관심종목 | 관심종목 등록, 삭제, 목록 조회 | [관심종목](docs/06-watchlist.md) |
| 웹소켓 | 사용자 자산/보유 주식 변경 발행 | [실시간 연결](docs/07-websocket.md) |
| 도메인 모델 | 사용자, 보유 주식, 관심종목 엔티티 구조 | [도메인 모델](docs/08-domain-model.md) |

## 시스템 아키텍처

```mermaid
flowchart TD
    Client["사용자 화면"] --> Auth["로그인과 JWT 발급"]
    Client --> UserAPI["사용자 정보와 자산 요청"]
    Client --> Watch["관심종목 요청"]
    Auth --> Redis["Refresh Token 저장"]
    UserAPI --> DB["사용자 데이터 저장"]
    Watch --> DB
    Order["주문 서비스"] --> Validate["주문 가능 여부 검증 요청"]
    Validate --> Asset["자산과 보유 수량 전송"]
    Kafka["정산 이벤트 수신"] --> Settlement["체결 결과 정산"]
    Settlement --> AssetUpdate["사용자 자산/보유주식 갱신"]
    AssetUpdate --> DB
    AssetUpdate --> WS["자산 변경 실시간 발행"]
    WS --> Client
```

- 로그인 흐름은 JWT를 발급하고 refresh token을 Redis에 저장한다.
- 사용자 정보와 자산 요청은 PostgreSQL의 사용자 데이터를 기준으로 처리한다.
- 관심종목 흐름은 사용자별 종목 등록, 삭제, 조회를 담당한다.
- 정산 흐름은 Kafka `settlement-topic` 이벤트를 받아 자산과 보유 주식에 반영한다.
- 자산 또는 보유 주식 변경은 사용자 화면으로 실시간 발행된다.

## 실행 방법

```bash
.\gradlew.bat bootRun
```

`application-docker.properties` 기준 서비스 포트는 `8081`입니다. DB, Redis, JWT 등 민감 설정 값은 문서에 기록하지 않으며 환경 변수로 분리 필요합니다.

<div align="right">

[문서 맨 위로](#top)

</div>



