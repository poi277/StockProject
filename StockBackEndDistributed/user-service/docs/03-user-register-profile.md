<a id="top"></a>

# 회원가입과 프로필

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고하세요.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 주식 README | [README](../../../README.md) | 사용자 서비스 README | [README](../README.md) |
| 설계 노트 | [Engineering Notes](../../../docs/ENGINEERING.md) | 데이터베이스 ERD | [Database Schema ERD](../../../docs/database-schema.md) |
| 개요 | [개요](01-overview.md) | 인증/JWT | [인증/JWT](02-auth-jwt.md) |
| 회원가입/프로필 | [회원가입/프로필](03-user-register-profile.md) | 자산/주문 검증 | [자산/주문 검증](04-user-asset-order-validation.md) |
| Kafka 정산 | [Kafka 정산](05-settlement-kafka.md) | 관심종목 | [관심종목](06-watchlist.md) |
| 실시간 연결 | [실시간 연결](07-websocket.md) | 도메인 모델 | [도메인 모델](08-domain-model.md) |
| 보안 설정 | [보안 설정](09-security-config.md) | 유저 서비스 이슈 | [user-service 이슈](10-user-service-issues.md) |

## 목차

> [개요](#개요) ·
> [회원가입 API](#회원가입-api)

> [프로필 API](#프로필-api) ·
> [도메인](#도메인) ·
> [주의](#주의) ·
> [핵심 구현 파일](#핵심-구현-파일)
## 개요

회원가입 기능은 사용자 ID 중복을 검사하고 비밀번호를 BCrypt로 암호화한 뒤 `StockUser` 엔티티를 저장합니다. 프로필 기능은 현재 인증된 사용자의 ID를 조회해 `ProfileDTO`로 반환합니다.


## 회원가입 API

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/user/register` | 신규 사용자 등록 | 불필요 |

요청 DTO:

- `id`
- `username`
- `password`

### 동작 순서

1. 사용자 ID 중복 여부 검사
2. `PasswordEncoder`로 비밀번호 암호화
3. `StockUser` 생성
4. 사용자 정보 저장

### 핵심 코드

```java
public void registerUser(UserRegisterDto dto) {
    try {
        if (userRepository.existsById(dto.getId())) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다");
        }
        StockUser user = new StockUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception e) {
        throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다", e);
    }
}
```

평문 비밀번호 저장과 사용자 ID 충돌을 차단하기 위한 등록 로직입니다. 회원가입 DTO를 입력으로 중복 여부를 확인하고 BCrypt 비밀번호를 가진 사용자 엔티티를 저장합니다.

### 구현 위치

- 사용자 등록: `features/User/UserService.java`의 `registerUser()`

## 프로필 API

| Method | Path | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/profile/` | 현재 사용자 프로필 조회 | 필요 |

프로필 조회는 인증 정보에서 사용자 ID를 확인한 뒤 사용자 정보를 조회해 `ProfileDTO`로 반환합니다.
구현은 `ProfileService.getProfile()` (프로필 응답 생성 기능)에서 담당합니다.

현재 `ProfileDTO`에는 `id`만 포함됩니다.

### 동작 순서

1. 인증 정보에서 현재 사용자 ID를 확인합니다.
2. 사용자 저장소에서 계정을 조회합니다.
3. 외부에 공개할 필드만 `ProfileDTO`로 반환합니다.

### 핵심 코드

```java
public ProfileDTO getProfile(String userId) {
    StockUser user = stockUserRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    return new ProfileDTO(user.getId());
}
```

사용자 엔티티 전체가 API 응답으로 노출되지 않도록 프로필 조회 결과를 DTO로 제한합니다. 인증 사용자 ID를 입력으로 계정을 조회하고 현재 공개 대상인 ID만 반환합니다.

### 구현 위치

- 프로필 조회와 DTO 변환: `features/Profile/ProfileService.java`의 `getProfile()`

## 도메인

회원가입으로 저장되는 사용자 데이터는 다음과 같습니다. `holdings`와 `watchLists`는 테이블 컬럼이 아니라 연관 엔티티 컬렉션입니다.

| 구분 | 필드 | 역할 |
| --- | --- | --- |
| 사용자 식별 | `id` | `StockUser` 테이블의 PK |
| 기본 정보 | `username`, `password` | 사용자 이름과 암호화된 비밀번호 |
| 자산 | `Asset`, `availableAsset` | 총 보유 자산과 주문 가능 현금 |
| 연관 데이터 | `holdings`, `watchLists` | 보유 주식과 관심종목의 1:N 컬렉션 |

## 주의

회원가입 시 코드에서는 총 보유 자산(`Asset`)과 주문 가능 현금(`availableAsset`) 초기값을 직접 설정하지 않습니다. 이 부분은 `10-user-service-issues.md`에 개선 필요 항목으로 정리합니다.

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



