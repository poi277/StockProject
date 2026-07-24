<a id="top"></a>

# user-service 트러블슈팅

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고합니다.

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

> [개요](#개요) 

## 개요

이 문서는 `user-service`에서 해결한 트러블슈팅을 정리합니다.


http 동기 호출

주문 생성·정정·취소는 주문 처리 전에 자산 예약이나 복구 결과를 확인해야 하므로 `order-service`가 `user-service`를 HTTP로 동기 호출합니다. 비동기 Kafka를 사용하면 검증 결과보다 주문 처리가 먼저 진행될 수 있어, 즉시 성공 여부가 필요한 검증 경로와 최종 체결 결과를 반영하는 정산 경로를 분리했습니다.

반면 정산은 이미 확정된 체결 결과를 자산에 반영하는 후속 작업이므로 Kafka 이벤트로 처리합니다. 이 구분을 통해 주문의 선행 조건은 동기 응답으로 보장하고, 정산은 서비스 간 결합을 낮춘 비동기 흐름으로 유지합니다.




<div align="right">

[문서 맨 위로](#top)

</div>
