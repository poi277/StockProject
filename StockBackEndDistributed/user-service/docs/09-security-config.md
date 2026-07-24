<a id="top"></a>

# 보안과 설정

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
> [보안 필터 설정](#보안-필터-설정) ·
> [CORS](#cors) ·
> [JWT 설정](#jwt-설정)

> [Redis 설정](#redis-설정) ·
> [서비스 간 HTTP 연동 설정](#서비스-간-http-연동-설정) ·
> [미사용 웹 설정](#미사용-웹-설정) ·
> [Dockerfile](#dockerfile) ·
> [핵심 구현 파일](#핵심-구현-파일)
## 개요

user-service는 Spring Security 기반 stateless JWT 인증을 사용합니다. CORS, JWT 필터, 인증 매니저, Redis, WebSocket, RestTemplate 설정이 별도 config 클래스로 분리되어 있습니다.



## 보안 필터 설정

주요 설정:

- CORS 활성화
- CSRF 비활성화
- `SessionCreationPolicy.STATELESS`
- JWT filter 등록
- form login 비활성화
- http basic 비활성화

인증 없이 허용하는 경로:

- CORS 사전 요청을 처리하는 경로: `OPTIONS /**`
- 서비스 기본 진입 경로: `/`
- 연결 확인용 경로: `/hello`
- 신규 사용자를 등록하는 경로: `/user/register`
- 종목 관련 공개 요청 경로: `/stock/**`
- 사용자 WebSocket 연결 경로: `/ws-user/**`
- 로그인과 토큰 처리를 위한 인증 경로: `/auth/**`
- 초기 호가를 조회하는 공개 경로: `/order/orderbook/*`
- 서비스 상태를 점검하는 Actuator 경로: `/actuator/**`

그 외 요청은 인증이 필요합니다.

### 동작 순서

1. 세션과 기본 로그인 방식을 비활성화해 요청마다 JWT를 검증합니다.
2. 공개 경로와 인증이 필요한 경로를 분리합니다.
3. JWT 필터를 사용자명·비밀번호 필터보다 먼저 실행합니다.

### 핵심 코드

```java
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/", "/hello", "/user/register", "/stock/**",
                            "/ws-user/**", "/auth/**", "/order/orderbook/*",
                            "/api/kis/stock/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                    UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());
    return http.build();
}
```

사용자를 식별하기 위한 필터 체인입니다. 요청 경로와 `Authorization` header가 입력되며, 공개 경로가 아닌 요청은 JWT 검증 결과에 따라 SecurityContext에 인증 정보가 반영됩니다.

### 구현 위치

- 보안 필터 체인: `config/SecurityConfig.java`의 `filterChain()`

## CORS

`SecurityConfig.corsConfigurationSource()` (CORS 허용 정책을 구성하는 기능)는 다음 정책을 설정합니다.

- allowed origin: `http://localhost:3000`
- allowed methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- allowed headers: `*`
- allow credentials: `true`
- exposed headers: `Authorization`

### 동작 순서

1. 브라우저 요청을 허용할 origin과 HTTP method를 제한합니다.
2. 인증 정보를 포함한 교차 출처 요청을 허용합니다.
3. 응답의 `Authorization` header를 프런트엔드에서 읽을 수 있도록 노출합니다.

### 핵심 코드

```java
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

인증 응답을 사용할 수 있도록 허용 범위를 명시한 설정입니다. 요청 origin·method·header를 입력 조건으로 검사하고, 허용된 응답에서는 프런트엔드가 `Authorization` header를 읽을 수 있습니다.

### 구현 위치

- CORS 허용 정책: `config/SecurityConfig.java`의 `corsConfigurationSource()`

## JWT 설정

`JwtProvider`는 설정 파일의 JWT secret과 만료 시간을 사용해 토큰을 생성합니다.

토큰 claim:

- subject: userId
- `type`: `access` 또는 `refresh`

민감정보인 JWT secret 값은 문서에 기록하지 않습니다. 환경 변수로 분리 필요합니다.

### 동작 순서

1. 사용자 ID와 토큰 종류를 claim으로 구성합니다.
2. 종류별 만료 시간을 적용합니다.
3. 같은 비밀키로 서명한 JWT 문자열을 반환합니다.

### 핵심 코드

```java
public String createAccessToken(String userId) {
    return Jwts.builder().subject(userId).claim("type", "access")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessExp))
            .signWith(key).compact();
}

public String createRefreshToken(String userId) {
    return Jwts.builder().subject(userId).claim("type", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExp))
            .signWith(key).compact();
}
```

Access Token과 Refresh Token의 권한과 수명을 구분하면서도 사용자 식별 기준은 동일하게 유지하기 위한 로직입니다. 사용자 ID를 입력받아 종류와 만료 시간이 다른 서명 토큰을 만들며, 결과는 로그인 응답과 토큰 재발급 처리에 사용됩니다.

### 구현 위치

- JWT 생성: `config/JwtProvider.java`의 `createAccessToken()`, `createRefreshToken()`

## Redis 설정

`RedisConfig`는 `RedisStandaloneConfiguration`과 `LettuceConnectionFactory`를 구성하고, `RedisTemplate<String, String>`을 Bean으로 등록합니다.

## 서비스 간 HTTP 연동 설정

`RestTemplateConfig` (서비스 간 HTTP 요청 도구를 등록하는 설정)는 stock-service 연동에 사용할 `RestTemplate` Bean을 등록합니다.

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



