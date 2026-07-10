<a id="top"></a>

# stock-service 트러블슈팅

## 문서 포털

문서의 상세 구현, API, 아키텍처, 트러블슈팅은 아래 문서를 참고한다.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 루트 README | [README](../../../README.md) | 서비스 README | [README](../README.md) |
| Engineering Notes | [Engineering Notes](../../../docs/ENGINEERING.md) | Database Schema ERD | [Database Schema ERD](../../../docs/database-schema.md) |
| 01 | [개요](01-overview.md) | 02 | [종목 API](02-stock-api.md) |
| 03 | [실시간 시세 캐시](03-realtime-stock-cache.md) | 04 | [Kafka 체결 처리](04-kafka-trade-execution.md) |
| 05 | [실시간 연결](05-websocket.md) | 06 | [주기 작업](06-scheduler.md) |
| 07 | [Candle 구조](07-candle-structure.md) | 08 | [외부 시세 연동 사용 중단](08-external-market-data-disabled.md) |
| 09 | [도메인 모델](09-domain-model.md) | 10 | [stock-service 이슈](10-stock-service-issues.md) |
| 11 | [stock-service 트러블슈팅](11-troubleshooting.md) |  |  |

## 목차

> [개요](#개요) 

## 개요

이 문서는 `stock-service`에서 해결한 트러블슈팅을 정리한다.


## kafka의 경로 일치 및 객체 오류

orderservice에서 온 kafka는 json 파싱오류?로 인해
list<TradeExecution>으로 받지못하여 오류가 생기며 이를 해결할려했지만
가장 깔끔한 방법은 한번더 객체로 랩핑하는것이다.
그리고 두 서비스는 패키지명(경로)를 동일하게 해야 인식이 가능했다.

## 최근 30분의 거래 비율 조회

최근 30분의 데이터 조회라는 휘발성 데이터를 설계및 구현할때는
redis를 이용하여 최근 30분의 체결 후 데이터를 만들려고 하였지만.
관리가 까다로웠다.키값이 하나인 이상 판매,구매를 저장된 이상 최근 30분이 경계가 사라져 30분마다 아예 초기화해서 새로 집계하는 방법밖에 없었으며 이는 최근 30분이라는 니즈를 충족하지못하였다. 그래서 최근 30개의 분봉 차트를 가져와서 계산하는 수밖에 없었다.

## 시작시 복구
시작시 가장 최근 일봉의 데이터를 가져와서 적재하였지만 당일데이터는 일봉에 적재되지않기에 데이터 불일치가 생겼다. 일봉데이터를 가져온후 당일의 분봉데이터를 가져와 적재시켜야했다.



<div align="right">

[문서 맨 위로](#top)

</div>
