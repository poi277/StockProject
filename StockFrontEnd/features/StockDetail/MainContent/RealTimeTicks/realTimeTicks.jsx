import './realTimeTicks.css'
import useRealTimeTicks, { formatTime } from './useRealTimeTicks'

export default function RealTimeTicks({ stockCode }) {
  const { executions, setTickType, isRealtime } = useRealTimeTicks(stockCode)

  return (
    <div className="sa1m6r0">
      <div className="sa1m6r1">
        <div className="ro36d1" data-section-name="종목상세__일별실시간시세" data-ignore-auto-section-prefix="true">

          {/* 실시간/일별 탭 */}
          <div role="radiogroup" aria-required="false" dir="ltr"
            className="tw3v-1sni4y90 tw3v-1sni4y92 tw3v-1sni4y95 ro36d0"
            tabIndex={0} style={{ outline: "none", position: "relative" }} data-scrollable="false">

            <div className="tw3v-1sni4y97 tw3v-1sni4y99" style={{
              boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px",
              width: "50%",
              transform: isRealtime ? "translateX(0%)" : "translateX(100%)",
              position: "absolute",
              top: "2px",
              left: "0px",
              height: "calc(100% - 4px)",
              transition: "transform 0.2s ease-in-out",
              zIndex: 0
            }}></div>

            <button type="button" role="radio" aria-checked={isRealtime}
              onClick={() => setTickType('realtime')}
              style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}
              className="tw3v-1cq3gqg0 tw3v-1cq3gqg2" tabIndex={-1}>
              <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
                <div className="tw3v-1cq3gqg8">
                  <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb" aria-hidden="true"
                    style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": isRealtime ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>실시간</span>
                  <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb"
                    style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": isRealtime ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>실시간</span>
                </div>
              </div>
            </button>

            <button type="button" role="radio" aria-checked={!isRealtime}
              onClick={() => setTickType('daily')}
              style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}
              className="tw3v-1cq3gqg0 tw3v-1cq3gqg2" tabIndex={-1}>
              <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
                <div className="tw3v-1cq3gqg8">
                  <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb" aria-hidden="true"
                    style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": !isRealtime ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>일별</span>
                  <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb"
                    style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": !isRealtime ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>일별</span>
                </div>
              </div>
            </button>
          </div>

          {/* 테이블 */}
          <div data-testid="virtuoso-scroller" tabIndex={0}
            style={{ height: "100%", outline: "none", overflowY: "auto", position: "relative" }}>
            <div data-viewport-type="element" style={{ width: "100%", height: "100%", position: "absolute", top: "0px" }}>
              <table className="tw3v-kvawo28 tw3v-kvawo2a tw3v-kvawo2e" style={{ borderSpacing: "0px", overflowAnchor: "none" }}>
                <thead className="tw3v-4pu5o90" style={{ zIndex: "auto", position: "sticky", top: "0px" }}>
                  <tr className="auto-zebra-pattern">
                    <th className="tw3v-1apn5az0"><div className="tw3v-1apn5az2 tw3v-1apn5az1 tw3v-1apn5az4 tw3v-kvawo25"><div className="tw3v-1apn5azd">체결가</div></div></th>
                    <th className="tw3v-1apn5az0"><div className="tw3v-1apn5az2 tw3v-1apn5az1 tw3v-1apn5az4 tw3v-kvawo27"><div className="tw3v-1apn5azd">체결량 (주)</div></div></th>
                    <th className="tw3v-1apn5az0"><div className="tw3v-1apn5az2 tw3v-1apn5az1 tw3v-1apn5az4 tw3v-kvawo27"><div className="tw3v-1apn5azd">등락률</div></div></th>
                    <th className="tw3v-1apn5az0"><div className="tw3v-1apn5az2 tw3v-1apn5az1 tw3v-1apn5az4 tw3v-kvawo27"><div className="tw3v-1apn5azd">거래량 (주)</div></div></th>
                    <th className="tw3v-1apn5az0"><div className="tw3v-1apn5az2 tw3v-1apn5az1 tw3v-1apn5az4 tw3v-kvawo27"><div className="tw3v-1apn5azd">시간</div></div></th>
                  </tr>
                </thead>
                <tbody data-testid="virtuoso-item-list">
                  {executions.map((exec, i) => {
                    const isUp = exec.tradeType === 'BUY'
                    const quantityColor = isUp ? "var(--wts-adaptive-red600)" : "var(--wts-adaptive-blue600)"
                    const changeRateColor = exec.changeRate >= 0 ? "var(--wts-adaptive-red500)" : "var(--wts-adaptive-blue500)"
                    const changeRatePrefix = exec.changeRate >= 0 ? '+' : ''

                    return (
                      <tr key={i}
                        className={`${i === 0 ? 'manual-zebra-pattern' : ''} _1p5yqoh0 _1jdgwi01`}
                        data-index={i} style={{ overflowAnchor: "none" }}>
                        <td className="tw3v-mq48z20">
                          <div className="tw3v-mq48z22 tw3v-kvawo25 tw3v-mq48z25">
                            <div className="tw3v-mq48z2h">{exec.price?.toLocaleString('ko-KR')}원</div>
                          </div>
                        </td>
                        <td className="tw3v-mq48z20" style={{ color: quantityColor }}>
                          <div className="tw3v-mq48z22 tw3v-kvawo27 tw3v-mq48z25">
                            <div className="tw3v-mq48z2h">{exec.quantity?.toLocaleString('ko-KR')}</div>
                          </div>
                        </td>
                        <td className="tw3v-mq48z20">
                          <div className="tw3v-mq48z22 tw3v-kvawo27 tw3v-mq48z25">
                            <div className="tw3v-mq48z2h">
                              <span style={{ color: changeRateColor }}>
                                {changeRatePrefix}{exec.changeRate?.toFixed(2)}%
                              </span>
                            </div>
                          </div>
                        </td>
                        <td className="tw3v-mq48z20">
                          <div className="tw3v-mq48z22 tw3v-kvawo27 tw3v-mq48z25">
                            <div className="tw3v-mq48z2h">{exec.totalVolume?.toLocaleString('ko-KR')}</div>
                          </div>
                        </td>
                        <td className="tw3v-mq48z20" style={{ color: "var(--wts-adaptive-grey600)" }}>
                          <div className="tw3v-mq48z22 tw3v-kvawo27 tw3v-mq48z25">
                            <div className="tw3v-mq48z2h">{formatTime(exec.time)}</div>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </div>
    </div>
  )
}