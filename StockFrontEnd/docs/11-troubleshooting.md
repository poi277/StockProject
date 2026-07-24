<a id="top"></a>

# 프론트엔드 트러블슈팅

## 문서 포털

상세 구현, API, 아키텍처와 트러블슈팅 정보는 아래 문서에서 확인할 수 있습니다.

| 분류 | 문서 | 분류 | 문서 |
| --- | --- | --- | --- |
| 주식 README | [README](../../README.md) | 주식 거래 플랫폼 README | [README](../README.md) |
| 설계 노트 | [Engineering Notes](../../docs/ENGINEERING.md) | 데이터베이스 ERD | [Database Schema ERD](../../docs/database-schema.md) |
| 인증 | [인증](01-auth.md) | 종목 목록 | [종목 목록](02-stock-list.md) |
| 종목 상세 | [종목 상세](03-stock-detail.md) | 차트 | [차트](04-chart.md) |
| 주문 | [주문](05-order.md) | 호가/체결 | [호가/체결](06-orderbook-execution.md) |
| 자산 | [자산](07-user-asset.md) | 관심종목 | [관심종목](08-watchlist.md) |
| 실시간 연결 | [실시간 연결](09-websocket.md) | 프론트엔드 이슈 | [프론트엔드 이슈](10-frontend-issues.md) |

## 목차

> [개요](#개요) · [Candle 업데이트](#candle-업데이트) · [시간과 주기 정규화](#시간과-주기-정규화) · [스크롤과 뷰포트](#스크롤과-뷰포트) · [표시 형식과 조회 범위](#표시-형식과-조회-범위) · [주문 정정](#주문-정정) · [핵심 구현 파일](#핵심-구현-파일) · [관련 문서](#관련-문서)

## 개요

실시간 Candle, 과거 데이터 조회와 주문 정정 과정에서 해결한 문제를 정리합니다. 각 항목은 증상, 원인과 적용한 해결 방법만 간단히 설명합니다.

## Candle 업데이트

Whitespace와 실시간 Candle이 같은 시간축을 사용할 때 업데이트 순서와 데이터 존재 여부를 구분해야 합니다.

### 해결 순서

1. Whitespace 뒤의 기존 Candle은 `series.update(data, true)`로 수정합니다.
2. 장 시작 전 일봉처럼 Candle이 없을 수 있는 주기는 새 데이터 추가를 허용합니다.
3. 진행 Candle은 같은 시간 구간에 병합하고 완료 Candle은 확정합니다.
4. 그룹봉은 Open을 유지하고 High·Low·Close와 거래량을 누적합니다.

### 핵심 코드

```js
const mergedCandle = {
    ...prev,
    high: Math.max(prev.high, normalizedCandle.high),
    low: Math.min(prev.low, normalizedCandle.low),
    close: normalizedCandle.close,
    buyQty: (prev.buyQty ?? 0) + (normalizedCandle.buyQty ?? 0),
    sellQty: (prev.sellQty ?? 0) + (normalizedCandle.sellQty ?? 0),
    movingAverages: normalizedCandle.movingAverages ?? prev.movingAverages,
};

this._candles = [
    ...candles.slice(0, idx),
    mergedCandle,
    ...candles.slice(idx + 1),
];
```

같은 시간 구간의 메시지를 새 Candle로 추가하면 시간축 중복과 잘못된 OHLC가 생기므로 기존 구간과 병합합니다. 이전 Candle과 완료 Candle을 입력으로 Open은 유지하고 High·Low·Close와 거래량을 합산합니다. 병합 결과는 같은 인덱스를 교체해 차트와 이동평균 계산의 확정 데이터가 됩니다.

### 구현 위치

- Candle 병합과 그룹화: `features/StockDetail/Chart/useChart.js`
- 차트 업데이트: `features/StockDetail/Chart/ChartComponent.jsx`
- 실시간 구독: `util/websocket/useCandleSocket.js`

## 시간과 주기 정규화

선택 주기와 시간대가 일치하지 않으면 Candle이 잘못된 슬롯에 들어갑니다. 현재 주기를 명시적으로 전달하고 KST 기준으로 시간을 정규화합니다.

### 해결 순서

1. 모든 차트 데이터 변환에 현재 주기를 전달합니다.
2. 수신한 시간 문자열이 비어 있지 않은지 확인한 뒤 Candle에 적용합니다.
3. KST 기준 시·분으로 분봉 슬롯을 계산합니다.
4. 주기 변경 시 이전 진행 Candle을 새 주기로 다시 계산하지 않습니다.
5. Whitespace 간격도 현재 주기로 생성합니다.

### 핵심 코드

```js
switch (candleType) {
    case 'THREE_MINUTE':
        minutes = Math.floor(minutes / 3) * 3;
        break;
    case 'FIVE_MINUTE':
        minutes = Math.floor(minutes / 5) * 5;
        break;
    case 'TEN_MINUTE':
        minutes = Math.floor(minutes / 10) * 10;
        break;
    case 'HOUR':
        minutes = 0;
        break;
    // 생략: 2·3·4시간봉과 일·주·월·연 주기 정규화
}
```

서버 메시지 시각을 그대로 사용하면 같은 구간의 Candle이 서로 다른 키로 저장될 수 있어 주기별 시작 시각으로 정규화합니다. KST 시·분과 Candle 주기를 입력으로 3·5·10분 또는 시간 단위 슬롯을 계산합니다. 반환된 시각은 진행·완료 Candle을 찾고 병합하는 공통 키가 됩니다.

### 구현 위치

- 시간 정규화: `features/StockDetail/Chart/useChart.js`
- 데이터 변환: `features/StockDetail/Chart/ChartComponent.jsx`
- 주기 상태: `store/chartButtonStore.js`

## 스크롤과 뷰포트

과거 데이터를 추가하거나 오른쪽 끝을 제한할 때 사용자가 보던 위치를 유지해야 합니다. 실제 Candle 수와 전체 차트 데이터 길이를 분리해 계산합니다.

### 해결 순서

1. 과거 조회 전후의 순수 Candle 수로 추가 개수를 계산합니다.
2. 추가한 개수만큼 Logical Range를 보정합니다.
3. Candle과 Whitespace를 포함한 전체 길이로 오른쪽 이동 범위를 제한합니다.
4. 범위 제한 직후에는 과거 데이터 조회를 잠시 차단합니다.
5. `fitContent()`는 최초 로딩에서만 실행해 줌 상태를 유지합니다.

### 핵심 코드

```js
applyCandles(event.candles, typeRef.current, candleSeries, maSeries, totalDataLengthRef);
if (currentRange && event.addedCount > 0) {
  timeScale.setVisibleLogicalRange({
    from: currentRange.from + event.addedCount,
    to: currentRange.to + event.addedCount,
  });
}
```

과거 데이터를 배열 앞에 추가하면 인덱스가 이동해 차트가 갑자기 다른 시점으로 점프하는 문제를 보정합니다. 기존 논리 범위와 실제 추가된 Candle 수를 입력으로 범위의 시작과 끝을 같은 수만큼 이동합니다. 보정 결과가 Lightweight Charts의 표시 범위에 적용되어 사용자가 보던 구간이 유지됩니다.

### 구현 위치

- 과거 조회: `features/StockDetail/Chart/useChart.js`
- 범위와 뷰포트: `features/StockDetail/Chart/ChartComponent.jsx`
- 뷰포트 저장: `store/chartButtonStore.js`

## 표시 형식과 조회 범위

시간축과 과거 조회 범위는 Candle 주기에 맞게 달라야 합니다.

### 동작 기준

1. 시간축은 `TickMarkType`에 따라 연도·월·일·시:분 형식으로 표시합니다.
2. 분봉은 분 단위, 시봉은 일 단위로 과거 범위를 계산합니다.
3. 일봉·주봉·월봉은 각각 30일·30주·12개월 범위를 사용합니다.

| Candle 주기 | 과거 조회 범위 |
| --- | ---: |
| 1분봉 | 120분 |
| 5분봉 | 600분 |
| 1시간봉 | 7일 |
| 일봉 | 30일 |
| 주봉 | 30주 |
| 월봉 | 12개월 |

### 핵심 코드

```js
if (type === 'DAY' || type === 'WEEK') {
  startTime.setDate(startTime.getDate() - 30);
} else if (type === 'MONTH') {
  startTime.setMonth(startTime.getMonth() - 12);
} else if (type === 'YEAR') {
  startTime.setFullYear(startTime.getFullYear() - 10);
} else {
  startTime.setMinutes(startTime.getMinutes() - 120);
}
```

모든 주기에 같은 조회 폭을 사용하면 분봉은 부족하고 장기 봉은 과도한 데이터를 가져오므로 주기별 범위를 분리합니다. Candle 유형과 조회 종료 시각을 입력으로 분·일·월·연 단위의 시작 시각을 계산합니다. 계산된 `startTime`과 `endTime`은 과거 Candle API에 전달되어 스크롤 한 번에 가져올 데이터 크기를 제한합니다.

### 구현 위치

- 조회 범위: `features/StockDetail/Chart/useChart.js`
- 시간축 형식: `features/StockDetail/Chart/ChartComponent.jsx`
- Candle API: `lib/candle.js`

## 주문 정정

부분 체결 주문의 수량 기준이 달라지는 문제를 막기 위해 정정 요청에서는 기존 주문 수량을 유지하고 가격만 변경합니다.

### 해결 순서

1. 정정 대상의 현재 가격을 입력 상태에 반영합니다.
2. 수량 입력과 증감 버튼을 비활성화합니다.
3. 기존 주문 수량과 변경 가격을 정정 요청에 전달합니다.

### 핵심 코드

```js
const handleEditOrder = async () => {
    if (!editTarget) return;

    const response = await editOrderApi(
        editTarget.orderId,
        editTarget.tradeType,
        editTarget.stockName,
        editTarget.stockCode,
        editTarget.quantity,
        Number(price)
    );

    if (response.success) {
        closeEdit();
    }
};
```

부분 체결 이후 화면에 남은 수량과 원래 주문 수량을 혼동하지 않도록 사이드바 정정은 가격만 변경합니다. 정정 대상과 새 가격을 입력으로 받되 요청 수량은 기존 `editTarget.quantity`를 유지합니다. API 성공 시 정정 화면을 닫고 실제 주문 목록 변화는 WebSocket 응답으로 반영합니다.

### 구현 위치

- 정정 화면: `features/StockDetail/MainContent/Order/Edit/SideBarEditForm.jsx`
- 정정 상태: `features/StockDetail/MainContent/Order/Edit/useSideBarEditOrder.js`
- 정정 API: `lib/order.js`

## 핵심 구현 파일

기준 경로: `StockFrontEnd`

| 파일 |
| --- |
| `features/StockDetail/Chart/ChartComponent.jsx` |
| `features/StockDetail/Chart/useChart.js` |
| `store/chartButtonStore.js` |
| `util/websocket/useCandleSocket.js` |
| `lib/candle.js` |
| `features/StockDetail/MainContent/Order/Edit/SideBarEditForm.jsx` |
| `features/StockDetail/MainContent/Order/Edit/useSideBarEditOrder.js` |
| `lib/order.js` |

## 관련 문서

- [차트](04-chart.md)
- [주문](05-order.md)
- [실시간 연결](09-websocket.md)
- [프론트엔드 이슈](10-frontend-issues.md)

<div align="right">[문서 맨 위로](#top)</div>
