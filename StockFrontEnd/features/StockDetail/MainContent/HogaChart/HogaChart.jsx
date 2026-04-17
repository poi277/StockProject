import './HogaChart.css'

// 매도 데이터
const sellOrders = [
  { price: 261000, change: '+29.85%', quantity: 296120, priceColor: 'var(--wts-adaptive-red500)' },
  { price: 255000, change: '+26.86%', quantity: 180000, priceColor: 'var(--wts-adaptive-red500)' },
];

// 매수 데이터
const buyOrders = [
  { price: 207000, change: '+2.98%',  quantity: 232232, priceColor: 'var(--wts-adaptive-red500)' },
  { price: 200000, change: '-0.50%',  quantity: 150000, priceColor: 'var(--wts-adaptive-blue500)' },
];

// 매도/매수 전체 최대값 기준으로 비율 계산
const maxQuantity = Math.max(
  ...sellOrders.map(o => o.quantity),
  ...buyOrders.map(o => o.quantity)
);

function getBarWidth(quantity) {
  return `calc(${(quantity / maxQuantity) * 100}% - 8px)`;
}

export default function HogaChart() {
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
                      <QuotesInfoKr />
                    </div>
                    <div className="_1oug70od _1oug70oc _1oug70oh">
                      <TradingStrengthKr />
                    </div>
                  </div>
                  <div className="_1oug70o8">
                    <SellOrderBook orders={sellOrders} maxQuantity={maxQuantity} />
                    <div className="_1oug70o11"></div>
                    <BuyOrderBook orders={buyOrders} maxQuantity={maxQuantity} />
                  </div>
                </div>
              </div>
              <HogaUnderBar />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function SellOrderBook({ orders, maxQuantity }) {
  return (
    <ul data-list-name="SellOrderBookKrComp" className="_1oug70of">
      {orders.map((order, i) => (
        <li key={i} data-tossinvest-log="li" data-parent-name="SellOrderBookKrComp"
          className="hmbv031 hmbv030" role="button" tabIndex="0">
          <div id="quote-row-quantity" className="_14zza80 _14zza84">
            <div className="_14zza86 _14zza8a" style={{ width: getBarWidth(order.quantity) }}>
              <span className="tw3v-1r5dc8g0 _1p5yqoh0 _14zza88"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                {order.quantity.toLocaleString('ko-KR')}
              </span>
            </div>
          </div>
          <button id="quote-row-price" className="dj9of22">
            <div></div>
            <div className="dj9of25 dj9of23">
              <span className="tw3v-1r5dc8g0 gvo66u1"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": order.priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                {order.price.toLocaleString('ko-KR')}
              </span>
              <span className="tw3v-1r5dc8g0 dj9of2e dj9of2c"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": order.priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                {order.change}
              </span>
            </div>
            <div></div>
          </button>
          <div></div>
        </li>
      ))}
    </ul>
  );
}

function BuyOrderBook({ orders, maxQuantity }) {
  return (
    <ul data-list-name="BuyOrderBookKrComp" className="_1oug70og">
      {orders.map((order, i) => (
        <li key={i} data-tossinvest-log="li" data-parent-name="BuyOrderBookKrComp"
          className="_1kcm3421 _1kcm3420" role="button" tabIndex="0">
          <div></div>
          <button id="quote-row-price" className="dj9of22">
            <div></div>
            <div className="dj9of25 dj9of23">
              <span className="tw3v-1r5dc8g0 gvo66u1"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": order.priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                {order.price.toLocaleString('ko-KR')}
              </span>
              <span className="tw3v-1r5dc8g0 dj9of2e dj9of2c"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": order.priceColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                {order.change}
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
      ))}
    </ul>
  );
}

function HogaUnderBar() {
  return (
    <div>
      <div className="_1hpof5wa">
        <div className="_1hpof5w1">
          <span className="_1hpof5w3" style={{ "--_1hpof5w2": "133314" }}></span>
          <span className="_1hpof5w5" style={{ "--_1hpof5w4": "46445" }}></span>
        </div>
        <div className="_1hpof5w6">
          <div className="_1hpof5w8">
            <span className="tw3v-1r5dc8g0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>판매대기</span>
            <span className="tw3v-1r5dc8g0 _1p5yqoh0 gvo66u0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>133,314</span>
          </div>
          <div className="_1hpof5w7">
            <div className="_1oug70op" style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "center", alignItems: "center" }}>
              <span className="tw3v-1r5dc8g0 gvo66u1"
                style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>애프터마켓</span>
            </div>
          </div>
          <div className="_1hpof5w9">
            <span className="tw3v-1r5dc8g0 _1p5yqoh0 gvo66u0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>46,445</span>
            <span className="tw3v-1r5dc8g0"
              style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>구매대기</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function QuotesInfoKr() {
  return (
    <ul data-list-name="QuotesInfoKr" className="_1oug70oj">
      <li className="_1oug70ol">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>52주 최고</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>1,117,000</span>
      </li>
      <hr className="tw3v-5u17g30 _1oug70ok" />
      <li className="_1oug70ol">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>최고</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>1,043,000</span>
      </li>
      <li className="_1oug70ol">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>최고</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>1,043,000</span>
      </li>
    </ul>
  );
}

function TradingStrengthKr() {
  return (
    <ul data-list-name="TradingStrengthKr" className="_1oug70om">
      <li className="_1oug70on">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>체결강도</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>122%</span>
      </li>
      <li className="_1oug70on">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>1,032,000</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>21</span>
      </li>
      <li className="_1oug70on">
        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>1,032,000</span>
        <span className="tw3v-1r5dc8g0 gvo66u0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-red600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>21</span>
      </li>
    </ul>
  );
}