<a id="top"></a>

# StockFrontEnd

[![Next.js](https://img.shields.io/badge/Next.js-16.1.3-black?logo=nextdotjs)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-19.2.3-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-%5E5-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Zustand](https://img.shields.io/badge/Zustand-%5E5.0.13-443E38)](https://zustand-demo.pmnd.rs/)
[![STOMP.js](https://img.shields.io/badge/STOMP.js-%5E7.3.0-010101)](https://stomp-js.github.io/)
[![SockJS](https://img.shields.io/badge/SockJS-%5E1.6.1-010101)](https://github.com/sockjs/sockjs-client)
[![lightweight-charts](https://img.shields.io/badge/lightweight--charts-%5E5.1.0-2962FF)](https://tradingview.github.io/lightweight-charts/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-%5E4-06B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고하세요.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 루트 README | [README](../README.md) | 서비스 README | [README](README.md) |
| Engineering Notes | [Engineering Notes](../docs/ENGINEERING.md) | Database Schema ERD | [Database Schema ERD](../docs/database-schema.md) |
| 01 | [인증](docs/01-auth.md) | 02 | [종목 목록](docs/02-stock-list.md) |
| 03 | [종목 상세](docs/03-stock-detail.md) | 04 | [차트](docs/04-chart.md) |
| 05 | [주문](docs/05-order.md) | 06 | [호가/체결](docs/06-orderbook-execution.md) |
| 07 | [자산](docs/07-user-asset.md) | 08 | [관심종목](docs/08-watchlist.md) |
| 09 | [실시간 연결](docs/09-websocket.md) | 10 | [프론트엔드 이슈](docs/10-frontend-issues.md) |

## 목차

> [프로젝트 개요](#프로젝트-개요) ·
> [주요 구현 내용](#주요-구현-내용) ·
> [시스템 아키텍처](#시스템-아키텍처)

> [저장소 구조](#저장소-구조) ·
> [실행 방법](#실행-방법)

## 프로젝트 개요

Next.js 기반 주식 거래 시뮬레이션 프론트엔드입니다. 사용자 인증, 종목 목록/상세, 차트, 호가, 주문, 보유 자산, 관심종목, WebSocket 실시간 갱신 화면을 담당합니다.

프론트엔드는 초기 데이터는 REST API로 조회하고, 이후 바뀌는 주문/호가/체결/시세/Candle/자산 상태는 서비스별 WebSocket 구독으로 반영한다. 실시간 흐름을 이렇게 나눈 배경은 루트의 [Engineering Notes](../docs/ENGINEERING.md)에 정리했습니다.

## 주요 구현 내용

- 로그인, 회원가입, 토큰 쿠키 저장
- 종목 목록과 종목 상세 조회
- 실시간 현재가, 체결, 호가 WebSocket 구독
- Candle 차트 초기 조회와 실시간 Candle 반영
- 매수, 매도, 주문 정정, 주문 취소 UI
- 보유 현금/주식과 평가 정보 표시
- 관심종목 등록, 삭제, 목록 조회

## 시스템 아키텍처

```mermaid
flowchart TD
    App["앱 진입"] --> Providers["공통 상태 준비"]
    Providers --> Auth["로그인 상태 관리"]
    Providers --> OrderWS["주문/호가 실시간 연결"]
    Providers --> StockWS["시세/체결 실시간 연결"]
    Providers --> UserWS["사용자 자산 실시간 연결"]
    Providers --> Asset["계좌/주문 상태 통합"]
    App --> Pages["사용자 화면 표시"]
    Pages --> StockList["종목 탐색"]
    Pages --> StockDetail["종목 분석"]
    Pages --> Order["주문과 호가 확인"]
    Pages --> Watch["관심 종목 관리"]
```

## 저장소 구조

```text
StockFrontEnd/
├─ app/                 # Next.js App Router 페이지
├─ features/            # 화면별 기능 컴포넌트
├─ lib/                 # API 요청 래퍼
├─ store/               # Zustand 상태
├─ util/                # WebSocket 등 공통 유틸
└─ docs/                # 프론트엔드 상세 문서
```

## 실행 방법

```bash
npm install
npm run dev
```

기본 개발 서버는 `http://localhost:3000`에서 실행됩니다.

<div align="right">

[문서 맨 위로](#top)

</div>



