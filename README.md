# 실시간 주식 거래 매칭 엔진

> **"주식 거래 시뮬레이션"**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-black.svg)](https://nextjs.org/)
[![Kafka](https://img.shields.io/badge/Kafka-7.5.0-black.svg)](https://kafka.apache.org/)

## 프로젝트 소개

실시간 주식 주문 매칭 엔진을 구현한 풀스택 프로젝트입니다.  
매수/매도 주문을 시간 우선 원칙으로 매칭하며, Kafka 기반 비동기 처리와 WebSocket 실시간 시세 제공을 합니다.  
모놀리스로 시작하여 MSA로 전환하는 전 과정을 직접 구현했습니다.
(보안상 aplication.properties는 제외하였습니다.)

## 주요 기능

### 1. 실시간 주문 매칭 엔진
- 시간 우선 원칙 기반 매수,매도 매칭
- 인메모리 OrderBook으로 고속 매칭 처리
- 부분 체결(PARTIAL), 완전 체결(COMPLETED) 지원

### 2. 실시간 시세 및 호가
- WebSocket(STOMP) 기반 호가창 및 체결 내역 실시간 처리

### 3. 자동 매매 봇
- MarketMaker 봇: 호가 유지
- Trend 봇: 현재가 매매
- 봇 간 거래는 체결 이력에서 제외

### 4. 매매 관리
- JWT 기반 사용자 인증와 redis 세션 기반 인증
- 체결 후 Kafka 이벤트 기반 자산,보유주식 정산

### 5. MSA 아키텍처
- order-service, user-service, stock-service 3개 서비스 분리
- Nginx API Gateway를 통한 단일 진입점 제공
- 서비스 간 HTTP 통신 및 Kafka 이벤트 기반 데이터 동기화

## 프로젝트 구조
- StockBackEnd : 백엔드의 모놀리스
- StockBackEndDistributed : MSA이후 백엔드
- StockBackEndMonoless : MSA이전 작업 백엔드의 백업용
- StockFrontEnd : 프론트엔드
- docker : 도커 실행 파일

백엔드
```
├── user-service (8081)      # 인증, 유저, 자산, 관심종목
│   ├── features/Auth        # JWT 인증
│   ├── features/User        # 유저 및 자산 관리
│   └── features/WatchList   # 관심종목
│
├── stock-service (8082)     # 주식 데이터, 현재가 캐시, KIS API
│   ├── features/Stock       # 주식 데이터 및 캐시
│   └── config/kis           # 한국투자증권 Open API 연동
│
├── order-service (8083)     # 주문, 매칭 엔진, 봇, Kafka, WebSocket
│   ├── features/Order       # 주문 처리 및 매칭
│   ├── features/Bot         # 자동 매매 봇
│   ├── features/kafka       # Kafka Producer/Consumer
│   └── features/Websocket   # 실시간 시세 전송
│
│
├── frontend (Next.js 16)    # 실시간 주식 거래 UI
│
└── docker/                  # 인프라 설정
    ├── docker-compose.yml       # 공통 (Kafka, Nginx)
    ├── docker-compose.prod.yml  # 전체 Docker 실행
    ├── nginx.dev.conf           # 개발용 (host.docker.internal)
    └── nginx.prod.conf          # 운영용 (컨테이너 이름)
```
프론트
```
├── StockFrontEnd            # 프론트엔드
│   ├── context              # 로그인 로직
│   ├── features             # 페이지 별 기능
│   ├── lib                  # 라우트
│   └── util                 # 유틸리티 파일
```

## 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 | 언어 |
| Spring Boot | 3.2.2 | 프레임워크 |
| Spring Security | - | JWT 인증 |
| Spring Data JPA | - | ORM |
| Spring WebSocket | - | 실시간 통신 |
| Apache Kafka | 7.5.0 | 비동기 메시지 처리 |
| Redis | - | Refresh Token 저장 |
| PostgreSQL (Supabase) | - | 데이터베이스 |

### Frontend
| 기술 | 버전 | 용도 |
|------|------|------|
| Next.js | 16 | 프레임워크 |
| STOMP.js | 7.3.0 | WebSocket 클라이언트 |
| SockJS | 1.6.1 | WebSocket Fallback |

### Infra
| 기술 | 용도 |
|------|------|
| Docker / Docker Compose | 컨테이너화 |
| Nginx | API Gateway, 리버스 프록시 |

## 실행 방법

### 개발 환경 (Eclipse + Docker)

```bash
# Kafka + Nginx만 Docker로 실행
docker compose up zookeeper kafka nginx

# 각 서비스는 Eclipse에서 직접 실행
# user-service  → 8081
# stock-service → 8082
# order-service → 8083
```

### 전체 Docker 실행

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build
```

### 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

## API 라우팅 (Nginx)

| 경로 | 서비스 |
|------|--------|
| `/auth/**`, `/user/**`, `/watch/**`, `/profile/**` | user-service |
| `/stock/**`, `/api/kis/**` | stock-service |
| `/order/**` | order-service |
| `/ws/**` | order-service (WebSocket) |

## 아키텍처

```
프론트(3000)
    ↓
Nginx(80) - API Gateway
    ├── /auth, /user, /watch  → user-service(8081)
    ├── /stock                → stock-service(8082)
    ├── /order                → order-service(8083)
    └── /ws                   → order-service(8083) WebSocket

order-service
    └── Kafka → settlement-topic → user-service (체결 정산)

stock-service
    └── HTTP → user-service (자산 조회)
```

## 트러블슈팅

### 1. OrderBook 동시성 문제
**문제**: 여러 주문이 동시에 들어올 때 OrderBook 데이터 불일치 발생
**원인**: 멀티스레드 환경에서 OrderBook 접근 시 race condition 발생
**해결**: 종목별 StockLock을 설계하여 같은 종목의 주문은 순차 처리되도록 구현

---

### 2. 봇 주문 DB 과적재 문제
**문제**: 봇끼리 체결 시 불필요한 DB 저장으로 과적재 발생
**원인**: 봇vs봇 체결도 유저와 동일하게 CompletedOrder, TradeHistory 저장
**해결**: isBot() 메서드로 봇 여부를 판별하여 봇vs봇 체결은 DB 저장 제외

---

### 3. 현재가 0 문제
**문제**: 매도 호가가 없을 때 현재가가 0으로 업데이트되어 봇이 멈춤
**원인**: 현재가를 매도 1호가(getSellfirstKey())로 계산하여 호가 없으면 0 반환
**해결**: 현재가를 마지막 체결가(LastExecutionPrice)로 변경하고 0이면 업데이트 제외

---

### 4. 매도 잔량 0 표시 버그
**문제**: 미체결/부분체결 시 매도 잔량이 0으로 표시
**원인**: matchLoop에서 미체결 주문을 OrderBook에 다시 추가하지 않음
**해결**: order.isCompleted()가 false일 때 book.addOrder(order) 호출 추가

---

### 5. 봇 스케줄러 미실행 문제
**문제**: @Scheduled 봇이 5초마다 실행되지 않고 멈춤
**원인**: processMatching의 @Transactional로 인해 insert가 즉시 커밋되지 않아 순서 꼬임
**해결**: updateStockPrice에서 currentPrice <= 0이면 업데이트 제외 처리

---

### 6. BotInitializer 초기화 순서 문제
**문제**: 서버 시작 시 BotHaveStock이 DB에 저장되지 않음
**원인**: StockCache 초기화 전에 BotInitializer가 먼저 실행됨
**해결**: @DependsOn("webSocketService")로 초기화 순서 보장

---