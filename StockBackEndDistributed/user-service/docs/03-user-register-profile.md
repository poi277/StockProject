<a id="top"></a>

# 회원가입과 프로필

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
> [핵심 구현 파일](#핵심-구현-파일) ·
> [회원가입 API](#회원가입-api)

> [프로필 API](#프로필-api) ·
> [도메인](#도메인) ·
> [주의](#주의)

## 개요

회원가입 기능은 사용자 ID 중복을 검사하고 비밀번호를 BCrypt로 암호화한 뒤 `StockUser` 엔티티를 저장한다. 프로필 기능은 현재 인증된 사용자의 ID를 조회해 `ProfileDTO`로 반환한다.


## 회원가입 API

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/user/register` | 신규 사용자 등록 | 불필요 |

요청 DTO:

- `id`
- `username`
- `password`

처리 흐름:

1. 사용자 ID 중복 여부 검사
2. `PasswordEncoder`로 비밀번호 암호화
3. `StockUser` 생성
4. 사용자 정보 저장

## 프로필 API

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/profile/` | 현재 사용자 프로필 조회 | 필요 |

프로필 조회는 인증 정보에서 사용자 ID를 확인한 뒤 사용자 정보를 조회해 `ProfileDTO`로 반환한다.
구현은 `ProfileService.getProfile()` (프로필 응답 생성 기능)에서 담당한다.

현재 `ProfileDTO`에는 `id`만 포함된다.

## 도메인

`StockUser` 주요 필드:

- `id`
- `username`
- `password`
- 총 보유 자산(`Asset`)
- 주문 가능 현금(`availableAsset`)
- `holdings`
- `watchLists`

## 주의

회원가입 시 코드에서는 총 보유 자산(`Asset`)과 주문 가능 현금(`availableAsset`) 초기값을 직접 설정하지 않는다. 이 부분은 `10-user-service-issues.md`에 개선 필요 항목으로 정리한다.

## 핵심 구현 파일

기준 경로

`StockBackEndDistributed/user-service/src/main/java/Poi/Stock`

| 파일 |
| --- |
| `features/User/UserController.java` |
| `features/User/UserService.java` |
| `features/User/StockUser.java` |
| `features/Profile/ProfileController.java` |
| `features/Profile/ProfileService.java` |
| `repository/StockUserRepository.java` |
| `DTO/user/UserRegisterDto.java` |
| `DTO/user/ProfileDTO.java` |
<div align="right">

[문서 맨 위로](#top)

</div>



