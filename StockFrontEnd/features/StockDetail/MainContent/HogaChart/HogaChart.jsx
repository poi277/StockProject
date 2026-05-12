import './HogaChart.css'
import React, { useState, useEffect } from 'react';
import useHoga from './useHoga';

const PRICE_STYLE = (color) => ({ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": color, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" });
const RATE_STYLE = (color) => ({ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": color, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" });
const TEXT_STYLE = (weight, color, size = "12px") => ({ "--tds-wts-font-weight": `var(--tw-font-weight-${weight})`, "--tds-wts-foreground-color": color, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": size });

function getPriceColor(price, openPrice) {
  if (!openPrice || price === openPrice) return "var(--wts-adaptive-grey700)";
  return price > openPrice ? "var(--wts-adaptive-red500)" : "var(--wts-adaptive-blue500)";
}

function getChangeRateStr(price, openPrice) {
  if (!openPrice) return "0.00%";
  const rate = (price - openPrice) / openPrice * 100;
  return `${rate > 0 ? '+' : ''}${rate.toFixed(2)}%`;
}

export default function HogaChart({ stock, onPriceSelect }) {
  const { sellOrders, buyOrders, maxQuantity, getBarWidth, totalSellQuantity, totalBuyQuantity, executions, closePrice, lastExecutionPrice } = useHoga(stock.stockCode);

  return (
    <div className="sa1m6r0" style={{ paddingLeft: "0px", paddingRight: "0px" }}>
      <div className="sa1m6r1">
        <div style={{ display: "flex", flexDirection: "column", flex: "1 1 0%", minHeight: "0px" }}>
          <div id="종목상세__호가" className="_1niv0g0">
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
                    <div className="_1oug70oa _1oug70o9 _1oug70oh"><QuotesInfoKr stock={stock} /></div>
                    <div className="_1oug70od _1oug70oc _1oug70oh"><TradingStrengthKr executions={executions} /></div>
                  </div>
                  <div className="_1oug70o8">
                    <OrderBook type="sell" orders={sellOrders} getBarWidth={getBarWidth} openPrice={stock.openPrice} lastExecutionPrice={lastExecutionPrice} onPriceSelect={onPriceSelect} />
                    <div className="_1oug70o11"></div>
                    <OrderBook type="buy" orders={buyOrders} getBarWidth={getBarWidth} openPrice={stock.openPrice} lastExecutionPrice={lastExecutionPrice} onPriceSelect={onPriceSelect} />
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

function OrderBook({ type, orders, getBarWidth, openPrice, lastExecutionPrice, onPriceSelect }) {
  const [keyDownPrice, setKeyDownPrice] = useState(null);
  const isSell = type === 'sell';

  useEffect(() => {
    const handleMouseUp = () => setKeyDownPrice(null);
    window.addEventListener('mouseup', handleMouseUp);
    return () => window.removeEventListener('mouseup', handleMouseUp);
  }, []);

  return (
    <ul className={isSell ? "_1oug70of" : "_1oug70og"}>
      {orders.map((order, i) => {
        const priceColor = getPriceColor(order.price, openPrice);
        const changeRateStr = getChangeRateStr(order.price, openPrice);
        const isActive = order.price === keyDownPrice;

        const quantityDiv = (
          <div id="quote-row-quantity" className={`_14zza80 ${isSell ? '_14zza84' : '_14zza85'}`}>
            <div className={`${isSell ? '_14zza86 _14zza8a' : '_14zza87 _14zza8b'}`} style={{ width: getBarWidth(order.quantity) }}>
              <span className={`tw3v-1r5dc8g0 _1p5yqoh0 ${isSell ? '_14zza88' : '_14zza89'}`}
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": isSell ? "var(--wts-adaptive-blue600)" : "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                {order.quantity.toLocaleString('ko-KR')}
              </span>
            </div>
          </div>
        );

        const priceButton = (
          <button id="quote-row-price" className={`dj9of22 ${order.price === lastExecutionPrice || isActive ? 'dj9of20' : ''}`}>
            <div></div>
            <div className="dj9of25 dj9of23">
              <span className="tw3v-1r5dc8g0 gvo66u1" style={PRICE_STYLE(priceColor)}>{order.price.toLocaleString('ko-KR')}</span>
              <span className="tw3v-1r5dc8g0 dj9of2e dj9of2c" style={RATE_STYLE(priceColor)}>{changeRateStr}</span>
            </div>
            <div></div>
          </button>
        );

        return (
          <li key={i} className={isSell ? "hmbv031 hmbv030" : "_1kcm3421 _1kcm3420"} role="button" tabIndex="0"
            onMouseDown={() => setKeyDownPrice(order.price)}
            onMouseUp={() => { setKeyDownPrice(null); onPriceSelect?.(order.price); }}>
            {isSell ? <>{quantityDiv}{priceButton}<div></div></> : <><div></div>{priceButton}{quantityDiv}</>}
          </li>
        );
      })}
    </ul>
  );
}

function HogaUnderBar({ totalSellQuantity = 0, totalBuyQuantity = 0 }) {
  const total = totalSellQuantity + totalBuyQuantity || 1;
  const sellRatio = totalSellQuantity / total;
  const buyRatio = totalBuyQuantity / total;

  return (
    <div>
      <div className="_1hpof5wa">
        <div className="_1hpof5w1">
          <span className="_1hpof5w3" style={{ "--_1hpof5w2": sellRatio * 100 }}></span>
          <span className="_1hpof5w5" style={{ "--_1hpof5w4": buyRatio * 100 }}></span>
        </div>
        <div className="_1hpof5w6">
          <div className="_1hpof5w8">
            <span className="tw3v-1r5dc8g0" style={TEXT_STYLE("medium", "var(--wts-adaptive-grey0pacity600)")}>판매대기</span>
            <span className="tw3v-1r5dc8g0 _1p5yqoh0 gvo66u0" style={TEXT_STYLE("semibold", "var(--wts-adaptive-blue600)")}>{totalSellQuantity.toLocaleString('ko-KR')}</span>
          </div>
          <div className="_1hpof5w7">
            <div className="_1oug70op" style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "center", alignItems: "center" }}>
              <span className="tw3v-1r5dc8g0 gvo66u1" style={TEXT_STYLE("semibold", "var(--wts-adaptive-grey700)")}>애프터마켓</span>
            </div>
          </div>
          <div className="_1hpof5w9">
            <span className="tw3v-1r5dc8g0 _1p5yqoh0 gvo66u0" style={TEXT_STYLE("semibold", "var(--wts-adaptive-red600)")}>{totalBuyQuantity.toLocaleString('ko-KR')}</span>
            <span className="tw3v-1r5dc8g0" style={TEXT_STYLE("medium", "var(--wts-adaptive-grey0pacity600)")}>구매대기</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function QuotesInfoKr({ stock }) {
  const ITEMS = [
    { label: '52주 최고', value: stock.highPrice, color: "var(--wts-adaptive-grey600)" },
    { label: '최고', value: stock.highPrice, color: "var(--wts-adaptive-red600)" },
    { label: '최저', value: stock.lowPrice, color: "var(--wts-adaptive-blue600)" },
  ];

  return (
    <ul className="_1oug70oj">
      {ITEMS.map(({ label, value, color }, i) => (
        <React.Fragment key={label}>
          {i === 1 && <hr className="tw3v-5u17g30 _1oug70ok" />}
          <li className="_1oug70ol">
            <span className="tw3v-1r5dc8g0" style={TEXT_STYLE("medium", "var(--wts-adaptive-grey600)")}>{label}</span>
            <span className="tw3v-1r5dc8g0 gvo66u0" style={TEXT_STYLE("medium", color)}>{value?.toLocaleString('ko-KR')}</span>
          </li>
        </React.Fragment>
      ))}
    </ul>
  );
}

function TradingStrengthKr({ executions = [] }) {
  return (
    <ul className="_1oug70om">
      <li className="_1oug70on">
        <span className="tw3v-1r5dc8g0" style={TEXT_STYLE("semibold", "var(--wts-adaptive-grey800)")}>체결강도</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={TEXT_STYLE("semibold", "var(--wts-adaptive-red600)")}>122%</span>
      </li>
      {executions.slice(0, 10).map((exec, i) => (
        <li key={i} className="_1oug70on">
          <span className="tw3v-1r5dc8g0" style={TEXT_STYLE("medium", "var(--wts-adaptive-grey700)")}>{exec.price?.toLocaleString('ko-KR')}</span>
          <span className="tw3v-1r5dc8g0 gvo66u0" style={TEXT_STYLE("medium", exec.tradeType === 'BUY' ? "var(--wts-adaptive-red600)" : "var(--wts-adaptive-blue600)")}>{exec.quantity?.toLocaleString('ko-KR')}</span>
        </li>
      ))}
    </ul>
  );
}