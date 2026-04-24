import './HogaChart.css'
import { useState, useEffect } from 'react';
import useHoga from './useHoga';

export default function HogaChart({stock, onPriceSelect}) {
  const { sellOrders, buyOrders, maxQuantity, getBarWidth, totalSellQuantity, totalBuyQuantity,executions,closePrice,lastExecutionPrice } = useHoga(stock.stockCode)
  
  return (
    <div className="sa1m6r0" style={{ paddingLeft: "0px", paddingRight: "0px" }}>
      <div className="sa1m6r1">
        <div style={{ display: "flex", flexDirection: "column", flex: "1 1 0%", minHeight: "0px" }}>
          <div id="종목상세__호가" className="_1niv0g0" data-section-name="종목상세__호가" data-ignore-auto-section-prefix="true">
            <div className="_1niv0g1">
              <div style={{ position: "absolute", top: "calc(32px)", left: "50%" }}></div>
              <div className="_1ofr7z31"></div>
              <div className="_1oug70o0">
                <div className="_1oug70o1">
                  <div className="_1oug70o3 _1oug70o2">
                    <div className="_1oug70ow _1oug70ov"></div>
                    <div className="_1oug70oq _1oug70or _1oug70ou"></div>
                    <div className="_1oug70oz _1oug70oy"></div>
                  </div>
                  <div className="_1oug70o6 _1oug70o5">
                    <div className="_1oug70oa _1oug70o9 _1oug70oh">
                      <QuotesInfoKr stock={stock} />
                    </div>
                    <div className="_1oug70od _1oug70oc _1oug70oh">
                      <TradingStrengthKr executions={executions} />
                    </div>
                  </div>
                  <div className="_1oug70o8">
                    <SellOrderBook orders={sellOrders} maxQuantity={maxQuantity} getBarWidth={getBarWidth} openPrice={stock.openPrice} lastExecutionPrice={lastExecutionPrice} onPriceSelect={onPriceSelect} />
                    <div className="_1oug70o11"></div>
                    <BuyOrderBook orders={buyOrders} maxQuantity={maxQuantity} getBarWidth={getBarWidth} openPrice={stock.openPrice} lastExecutionPrice={lastExecutionPrice} onPriceSelect={onPriceSelect} />
                  </div>
                </div>
              </div>
              <HogaUnderBar totalSellQuantity={totalSellQuantity} totalBuyQuantity={totalBuyQuantity} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function SellOrderBook({ orders, maxQuantity, getBarWidth, openPrice, lastExecutionPrice, onPriceSelect }) {
  const [keyDownPrice, setKeyDownPrice] = useState(null);

  useEffect(() => {
    const handleMouseUp = () => setKeyDownPrice(null);
    window.addEventListener('mouseup', handleMouseUp);
    return () => window.removeEventListener('mouseup', handleMouseUp);
  }, []);

  return (
    <ul data-list-name="SellOrderBookKrComp" className="_1oug70of">
      {orders.map((order, i) => {
        const changeRate = openPrice ? ((order.price - openPrice) / openPrice * 100) : 0
        const isEqual = order.price === openPrice
        const isUp = changeRate > 0  // >= 에서 > 로 변경

        const priceColor = isEqual
          ? "var(--wts-adaptive-grey700)"
          : isUp
            ? "var(--wts-adaptive-red500)"
            : "var(--wts-adaptive-blue500)"
        const changeRateStr = `${isUp ? '+' : ''}${changeRate.toFixed(2)}%`

        const isActive = order.price === keyDownPrice;

        return (
          <li key={i} className="hmbv031 hmbv030" role="button" tabIndex="0"
            onMouseDown={() => setKeyDownPrice(order.price)}
            onMouseUp={() => { setKeyDownPrice(null); onPriceSelect?.(order.price); }}
          >
            <div id="quote-row-quantity" className="_14zza80 _14zza84">
              <div className="_14zza86 _14zza8a" style={{ width: getBarWidth(order.quantity) }}>
                <span className="tw3v-1r5dc8g0 _1p5yqoh0 _14zza88"
                  style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                  {order.quantity.toLocaleString('ko-KR')}
                </span>
              </div>
            </div>
            <button id="quote-row-price" className={`dj9of22 ${order.price === lastExecutionPrice || isActive ? 'dj9of20' : ''}`}>
              <div></div>
              <div className="dj9of25 dj9of23">
                <span className="tw3v-1r5dc8g0 gvo66u1"
                  style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                  {order.price.toLocaleString('ko-KR')}
                </span>
                <span className="tw3v-1r5dc8g0 dj9of2e dj9of2c"
                  style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                  {changeRateStr}
                </span>
              </div>
              <div></div>
            </button>
            <div></div>
          </li>
        )
      })}
    </ul>
  );
}

function BuyOrderBook({ orders, maxQuantity, getBarWidth, openPrice, lastExecutionPrice, onPriceSelect }) {
  const [keyDownPrice, setKeyDownPrice] = useState(null);

  useEffect(() => {
    const handleMouseUp = () => setKeyDownPrice(null);
    window.addEventListener('mouseup', handleMouseUp);
    return () => window.removeEventListener('mouseup', handleMouseUp);
  }, []);

  return (
    <ul data-list-name="BuyOrderBookKrComp" className="_1oug70og">
      {orders.map((order, i) => {
         const changeRate = openPrice ? ((order.price - openPrice) / openPrice * 100) : 0
          const isEqual = order.price === openPrice
          const isUp = changeRate > 0 
          const priceColor = isEqual
            ? "var(--wts-adaptive-grey700)"
            : isUp
              ? "var(--wts-adaptive-red500)"
              : "var(--wts-adaptive-blue500)"
          const changeRateStr = `${isUp ? '+' : ''}${changeRate.toFixed(2)}%`

          const isActive = order.price === keyDownPrice;

        return (
          <li key={i} className="_1kcm3421 _1kcm3420" role="button" tabIndex="0"
            onMouseDown={() => setKeyDownPrice(order.price)}
            onMouseUp={() => { setKeyDownPrice(null); onPriceSelect?.(order.price); }}
          >
            <div></div>
            <button id="quote-row-price" className={`dj9of22 ${order.price === lastExecutionPrice || isActive ? 'dj9of20' : ''}`}>
              <div></div>
              <div className="dj9of25 dj9of23">
                <span className="tw3v-1r5dc8g0 gvo66u1"
                  style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                  {order.price.toLocaleString('ko-KR')}
                </span>
                <span className="tw3v-1r5dc8g0 dj9of2e dj9of2c"
                  style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                  {changeRateStr}
                </span>
              </div>
              <div></div>
            </button>
            <div id="quote-row-quantity" className="_14zza80 _14zza85">
              <div className="_14zza87 _14zza8b" style={{ width: getBarWidth(order.quantity) }}>
                <span className="tw3v-1r5dc8g0 _1p5yqoh0 _14zza89"
                  style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                  {order.quantity.toLocaleString('ko-KR')}
                </span>
              </div>
            </div>
          </li>
        )
      })}
    </ul>
  );
}

function HogaUnderBar({ totalSellQuantity = 0, totalBuyQuantity = 0 }) {
  const total = totalSellQuantity + totalBuyQuantity || 1
  const sellRatio = totalSellQuantity / total  // CSS flex 값으로 사용
  const buyRatio  = totalBuyQuantity  / total

  return (
    <div>
      <div className="_1hpof5wa">
        <div className="_1hpof5w1">
          {/* ✅ 퍼센트 비율로 게이지바 */}
          <span className="_1hpof5w3" style={{ "--_1hpof5w2": sellRatio * 100 }}></span>
          <span className="_1hpof5w5" style={{ "--_1hpof5w4": buyRatio  * 100 }}></span>
        </div>
        <div className="_1hpof5w6">
          <div className="_1hpof5w8">
            <span className="tw3v-1r5dc8g0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>판매대기</span>
            <span className="tw3v-1r5dc8g0 _1p5yqoh0 gvo66u0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
              {totalSellQuantity.toLocaleString('ko-KR')}
            </span>
          </div>
          <div className="_1hpof5w7">
            <div className="_1oug70op" style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "center", alignItems: "center" }}>
              <span className="tw3v-1r5dc8g0 gvo66u1"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>애프터마켓</span>
            </div>
          </div>
          <div className="_1hpof5w9">
            <span className="tw3v-1r5dc8g0 _1p5yqoh0 gvo66u0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
              {totalBuyQuantity.toLocaleString('ko-KR')}
            </span>
            <span className="tw3v-1r5dc8g0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>구매대기</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function QuotesInfoKr({ stock }) {
  return (
    <ul data-list-name="QuotesInfoKr" className="_1oug70oj">
      <li className="_1oug70ol">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>52주 최고</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
          {stock.highPrice?.toLocaleString('ko-KR')}
        </span>
      </li>
      <hr className="tw3v-5u17g30 _1oug70ok" />
      <li className="_1oug70ol">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>최고</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
          {stock.highPrice?.toLocaleString('ko-KR')}
        </span>
      </li>
      <li className="_1oug70ol">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>최저</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
          {stock.lowPrice?.toLocaleString('ko-KR')}
        </span>
      </li>
    </ul>
  );
}

function TradingStrengthKr({ executions = [] }) {
  const recent = executions.slice(0, 10)
 return (
    <ul data-list-name="TradingStrengthKr" className="_1oug70om">

      <li className="_1oug70on">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>체결강도</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>122%</span>
      </li>

      {recent.map((exec, i) => (
        <li key={i} className="_1oug70on">
          <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
            {exec.price?.toLocaleString('ko-KR')}
          </span>
          <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": exec.tradeType === 'BUY' ? "var(--wts-adaptive-red600)" : "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
            {exec.quantity?.toLocaleString('ko-KR')}
          </span>
        </li>
      ))}
    </ul>
  );
}