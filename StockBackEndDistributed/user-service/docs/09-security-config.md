<a id="top"></a>

# 보안과 설정

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고하세요.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 루트 README | [README](../../../README.md) | 서비스 README | [README](../README.md) |
| Engineering Notes | [Engineering Notes](../../../docs/ENGINEERING.md) | Database Schema ERD | [Database Schema ERD](../../../docs/database-schema.md) |
| 01 | [개요](01-overview.md) | 02 | [인증/JWT](02-auth-jwt.md) |
| 03 | [회원가입/프로필](03-user-register-profile.md) | 04 | [자산/주문 검증](04-user-asset-order-validation.md) |
| 05 | [Kafka 정산](05-settlement-kafka.md) | 06 | [관심종목](06-watchlist.md) |
| 07 | [실시간 연결](07-websocket.md) | 08 | [도메인 모델](08-domain-model.md) |
| 09 | [보안 설정](09-security-config.md) | 10 | [user-service 이슈](10-user-service-issues.md) |

## 목차

> [개요](#개요) ·
> [보안 필터 설정](#보안-필터-설정) ·
> [CORS](#cors) ·
> [JWT 설정](#jwt-설정)

> [Redis 설정](#redis-설정) ·
> [서비스 간 HTTP 연동 설정](#서비스-간-http-연동-설정) ·
> [미사용 웹 설정](#미사용-웹-설정) ·
> [Dockerfile](#dockerfile) ·
> [핵심 구현 파일](#핵심-구현-파일)
## 개요

user-service는 Spring Security 기반 stateless JWT 인증을 사용한다. CORS, JWT 필터, 인증 매니저, Redis, WebSocket, RestTemplate 설정이 별도 config 클래스로 분리되어 있다.



## 보안 필터 설정

주요 설정:

- CORS 활성화
- CSRF 비활성화
- `SessionCreationPolicy.STATELESS`
- JWT filter 등록
- form login 비활성화
- http basic 비활성화

허용 경로:

- `OPTIONS /**`
- `/`
- `/hello`
- `/user/register`
- `/stock/**`
- `/ws-user/**`
- `/auth/**`
- `/order/orderbook/*`
- `/actuator/**`

그 외 요청은 인증이 필요하다.

## CORS

`SecurityConfig.corsConfigurationSource()` (CORS 허용 정책을 구성하는 기능)는 다음 정책을 설정한다.

- allowed origin: `http://localhost:3000`
- allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- allowed headers: `*`
- allow credentials: `true`
- exposed headers: `Authorization`

## JWT 설정

`JwtProvider`는 설정 파일의 JWT secret과 만료 시간을 사용해 토큰을 생성한다.

토큰 claim:

- subject: userId
- `type`: `access` 또는 `refresh`

민감정보인 JWT secret 값은 문서에 기록하지 않는다. 환경 변수로 분리 필요하다.

## Redis 설정

`RedisConfig`는 `RedisStandaloneConfiguration`과 `LettuceConnectionFactory`를 구성하고, `RedisTemplate<String, String>`을 Bean으로 등록한다.

Redis 접속 정보는 설정 파일에 존재하지만 문서에 기록하지 않는다. 환경 변수로 분리 필요하다.

## 서비스 간 HTTP 연동 설정

`RestTemplateConfig` (서비스 간 HTTP 요청 도구를 등록하는 설정)는 stock-service 연동에 사용할 `RestTemplate` Bean을 등록한다.

## 미사용 웹 설정

`WebConfig.java`는 전체 코드가 주석 처리되어 있다. 실제 CORS 설정은 `SecurityConfig`에서 처리한다.

## Dockerfile

Dockerfile은 Gradle 빌드 이미지에서 jar를 생성한 뒤, `eclipse-temurin:17-jre` 런타임 이미지에서 실행한다.

실행 profile:

- `docker`
## 핵심 구현 파일

기준 경로

`StockBackEndDistributed/user-service/src/main`

| 파일 |
| --- |
| `java/Poi/Stock/config/SecurityConfig.java` |
| `java/Poi/Stock/config/JwtProvider.java` |
| `java/Poi/Stock/config/JwtAuthenticationFilter.java` |
| `java/Poi/Stock/config/CustomUserDetailsService.java` |
| `java/Poi/Stock/config/AuthenticationManagerConfig.java` |
| `java/Poi/Stock/config/RedisConfig.java` |
| `java/Poi/Stock/config/RestTemplateConfig.java` |
| `java/Poi/Stock/config/WebSocketConfig.java` |
| `java/Poi/Stock/config/WebConfig.java` |
| `resources/application-docker.properties` |

<div align="right">

[문서 맨 위로](#top)

</div>



