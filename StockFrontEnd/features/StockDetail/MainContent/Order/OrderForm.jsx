import { useState } from 'react'
import './OrderForm.css'

export default function OrderForm()
{
    const [tradeType, setTradeType] = useState('buy')

    return(
        <div className="sa1m6r0">
            <div className="sa1m6r1">
                <div style={{ display: "flex", flexDirection: "column", height: "100%", overflow: "auto" }}>
                    <div style={{ flex: "1 1 0", minHeight: "0px" }}>
                        <div id="trade-order-section" data-section-name="종목상세__주문하기" data-ignore-auto-section-prefix="true">
                           <OrderType tradeType={tradeType} setTradeType={setTradeType}/>
                           <OrderStatus tradeType={tradeType} />
                           <OrderQuantityForm tradeType={tradeType} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

function OrderType({ tradeType, setTradeType }) {
  const isBuy = tradeType === 'buy';
  const isSell = tradeType === 'sell';
  const isPending = tradeType === 'pending-orders';

  // 구매: red, 판매: blue, 대기: grey (상태에 따른 부모 컨테이너 색상 클래스)
  const colorClass = isBuy ? 'xl0v5qg' : isSell ? 'xl0v5qg-sell' : 'xl0v5qg-pending';

  /**
   * 이동 로직 설명:
   * 1. width: "33.333%" -> 전체의 1/3 너비
   * 2. transform:
   * - 구매(buy): translateX(0%) -> 가장 왼쪽
   * - 판매(sell): translateX(100%) -> 중앙 (자기 너비만큼 1번 이동)
   * - 대기(pending-orders): translateX(200%) -> 오른쪽 (자기 너비만큼 2번 이동)
   */
  const getTranslateValue = () => {
    if (isBuy) return 'translateX(0%)';
    if (isSell) return 'translateX(100%)';
    if (isPending) return 'translateX(200%)';
    return 'none';
  };

  return (
    <div className="xl0v5qc">
      <div
        role="radiogroup"
        aria-required="false"
        dir="ltr"
        className={`tw3v-1sni4y90 tw3v-1sni4y92 tw3v-1sni4y95 xl0v5qf xl0v5qk xl0v5ql ${colorClass}`}
        tabIndex="0"
        style={{ outline: "none", position: "relative" }} // 부모에 relative 추가
        data-scrollable="false"
      >
        {/* 배경 슬라이더 (움직이는 하얀색 바) */}
        <div
          className="tw3v-1sni4y97 tw3v-1sni4y99"
          style={{
            boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px",
            width: "33.333%", // 1/3 너비
            transform: getTranslateValue(), // 위치 이동
            position: "absolute",
            top: "2px", // 상하 여백 조정 (필요시)
            left: "0px",
            height: "calc(100% - 4px)", // 높이 조정 (필요시)
            transition: "transform 0.2s ease-in-out", // 부드러운 이동 효과
            zIndex: 0
          }}
        ></div>

        {/* 구매 버튼 */}
        <button
          type="button"
          role="radio"
          aria-checked={isBuy}
          className={`tw3v-1cq3gqg0 tw3v-1cq3gqg2 xl0v5qn ${isBuy ? 'xl0v5qo' : 'xl0v5qr'}`}
          style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}
          onClick={() => setTradeType('buy')}
        >
          <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
            <div className="tw3v-1cq3gqg8">
              <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>구매</span>
            </div>
          </div>
        </button>

        {/* 판매 버튼 */}
        <button
          type="button"
          role="radio"
          aria-checked={isSell}
          className={`tw3v-1cq3gqg0 tw3v-1cq3gqg2 xl0v5qn ${isSell ? 'xl0v5qp' : 'xl0v5qr'}`}
          style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}
          onClick={() => setTradeType('sell')}
        >
          <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
            <div className="tw3v-1cq3gqg8">
              <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>판매</span>
            </div>
          </div>
        </button>

        {/* 대기 버튼 */}
        <button
          type="button"
          role="radio"
          aria-checked={isPending}
          className={`tw3v-1cq3gqg0 tw3v-1cq3gqg2 xl0v5qn ${isPending ? 'xl0v5qp' : 'xl0v5qr'}`}
          style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}
          onClick={() => setTradeType('pending-orders')}
        >
          <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
            <div className="tw3v-1cq3gqg8">
              <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>대기</span>
            </div>
          </div>
        </button>
      </div>
    </div>
  );
}

function OrderStatus({ tradeType })
{
    return(
        <div className="fjdgoj0">
            <label className="tw3v-1r5dc8g0 fjdgoj1" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>주문 유형</label>
            <div className="fjdgoj2 css-1qo9j44" style={{ "--wts-form-field-template-columns": "auto", "--wts-form-field-addon-columns-start": "1", "--wts-field-box-container-display": "grid" }}>
                <div type="button" id="radix-_r_kul_" aria-haspopup="menu" aria-expanded="false" data-state="closed" data-tossinvest-log="DropdownMenu.Trigger" data-parent-name="OrderMethodSelect" tabIndex="0" data-tossinvest-priority-log="Dropdown.Trigger" data-contents-value="주문 유형 선택" data-content-tag="주문_유형_선택" className="css-ghyw0v">
                    <div data-tds-wts-field-box-content-variant="default" className="_13izhfo4 css-c8ze6m" style={{ "--wts-field-box-background-color": "var(--wts-adaptive-background)", "--wts-field-box-disabled-background-color": "var(--wts-adaptive-grey100)", "--wts-field-box-border-color": "var(--wts-adaptive-grey200)", "--wts-field-box-disabled-border-color": "var(--wts-adaptive-greyOpacity50)", "--wts-field-box-h-padding": "6px", "--wts-field-box-height": "32px", "--wts-field-box-font-size": "14px", "--wts-field-box-separator-height": "20px", "--wts-field-box-border-radius": "8px", "--wts-field-box-separator-margin": "0 4px", "--wts-field-box-box-shadow-color": "var(--wts-field-box-border-color)", "--wts-field-box-box-shadow-width": "1px", "--wts-field-box-hover-box-shadow-color": "var(--wts-adaptive-blue200)", "--wts-field-box-focus-box-shadow-color": "var(--wts-adaptive-blue500)", "--wts-field-box-content-left-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-right-padding": "0px", "--wts-field-box-content-hover-offset": "5px", "--wts-field-box-content-left": "0px", "--wts-field-box-clear-content-margin-bottom": "var(--wts-field-box-content-margin-bottom, 0px)" }}>
                        <span className="tw3v-1r5dc8g0 _13izhfo5 _7wshe50" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>일반 주문</span>
                        <span className="css-109kmiu" style={{ "--wts-field-box-addon-padding": "4px" }}>
                            <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey400)" }}>
                                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                    <path d="m3.691 5.746 4.309 4.355 4.309-4.355" fill="none" stroke="#8f959e" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8"></path>
                                </svg>
                            </span>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    )
}

function OrderQuantityForm({tradeType})
{
    const [priceType, setPriceType] = useState('limit') // 'limit' = 지정가, 'market' = 시장가
    const isLimit = priceType === 'limit'
    const isSell = tradeType === 'sell'
    const priceLabel = isSell ? '판매' : '구매'
    const buttonLabel = isSell ? '판매하기' : '구매하기'
    const buttonClass = isSell
        ? "tw3v-1wkoka52h tw3v-1wkoka54 tw3v-1wkoka541 tw3v-1wkoka5f tw3v-1wkoka519 tw3v-1wkoka5z tw3v-1wkoka5s tw3v-1wkoka5n tw3v-1wkoka52b"
        : "tw3v-1wkoka52h tw3v-1wkoka54 tw3v-1wkoka541 tw3v-1wkoka5f tw3v-1wkoka519 tw3v-1wkoka5z tw3v-1wkoka5s tw3v-1wkoka5n tw3v-1wkoka52a"

    return(
        <form id="new-order-form" action="/api/v2/wts/trading/order/create/direct" className="xl0v5q1" method="post" data-gtm-form-interact-id="3">
            <input type="hidden" value="A005930" name="stockCode" />
            <input type="hidden" value="buy" name="tradeType" />
            <input type="hidden" value="KSP" name="market" />
            <input type="hidden" value="KRW" name="currencyMode" />
            <input type="hidden" value="1481.4" name="exchangeRate" />
            <input type="hidden" value="false" name="marginTrading" />
            <input type="hidden" value="false" name="noAutoExchange" />
            <input type="hidden" value="false" name="buyMaxQuantity" />
            <input type="hidden" value={isLimit ? "00" : "03"} name="orderPriceType" />
            <div className="xl0v5q2" id="trade-order-section">
                <div>
                    <input type="hidden" value="268000" name="UpperLimit" />
                    <input type="hidden" value="145000" name="LowerLimit" />
                    <div className="_13izhfo0">
                        <input type="hidden" value="00" name="orderPriceType" />
                        <div className="_13izhfo1">
                            <span className="tw3v-1r5dc8g0" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                {priceLabel}
                                가격
                            </span>
                            <fieldset className="xl0v5q5">
                                <legend className="_9vo4o90">매매 가격</legend>
                                <div role="radiogroup" aria-required="true" dir="ltr" className="tw3v-1sni4y90 tw3v-1sni4y92 tw3v-1sni4y95" data-skip="true" tabIndex="0" style={{ outline: "none" }} data-scrollable="false">
                                    <div className="tw3v-1sni4y97 tw3v-1sni4y99" style={{ boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px", width: "50%",transform: isLimit ? "translateX(0%)" : "translateX(100%)", position: "absolute", top: "2px", left: "0px", height: "calc(100% - 4px)", transition: "transform 0.2s ease-in-out", zIndex: 0}}></div>
                                    <button onClick={() => setPriceType('limit')} style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }} type="button" role="radio" aria-checked="true" data-state="checked" value="00" className="tw3v-1cq3gqg0 tw3v-1cq3gqg2" data-seg-state="checked" data-tossinvest-log="SegmentedControl.Item" data-contents-label="지정가" data-contents-label-code="orderPriceTypeName" data-contents-value="지정가" data-content-tag="orderPriceTypeName" tabIndex="-1" data-radix-collection-item>
                                        <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
                                            <div className="tw3v-1cq3gqg8">
                                                 {/* greyOpacity800->600으로 */}
                                                <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": priceType === 'limit' ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>지정가</span>
                                                <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color":priceType === 'limit' ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>지정가</span>
                                            </div>
                                        </div>
                                    </button>
                                    <input aria-hidden="true" tabIndex="-1" type="radio" value="00" checked name="orderPriceType" onChange={() => {}} style={{ transform: "translateX(-100%)", position: "absolute", pointerEvents: "none", opacity: "0", margin: "0px", width: "123px", height: "28px" }} />
                                    <button onClick={() => setPriceType('market')} style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}  type="button" role="radio" aria-checked="false" data-state="unchecked" value="03" className="tw3v-1cq3gqg0 tw3v-1cq3gqg2" data-seg-state="unchecked" data-tossinvest-log="SegmentedControl.Item" data-contents-label="시장가" data-contents-label-code="orderPriceTypeName" data-contents-value="시장가" data-content-tag="orderPriceTypeName" tabIndex="-1" data-radix-collection-item>
                                        <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
                                            <div className="tw3v-1cq3gqg8">
                                                <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": priceType === 'market' ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>시장가</span>
                                                <span className="tw3v-1r5dc8g0 tw3v-1cq3gqg9 tw3v-1cq3gqgb" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color":priceType === 'market' ? "var(--wts-adaptive-greyOpacity800)" : "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>시장가</span>
                                            </div>
                                        </div>
                                    </button>
                                    <input aria-hidden="true" tabIndex="-1" type="radio" value="03" name="orderPriceType" onChange={() => {}} style={{ transform: "translateX(-100%)", position: "absolute", pointerEvents: "none", opacity: "0", margin: "0px", width: "123px", height: "28px" }} />
                                </div>
                            </fieldset>
                        </div>
                        <div className="_13izhfo1">
                            <div>
                                <label className="_9vo4o90" htmlFor="trading-form-price">가격</label>
                            </div>
                            <div className="_13izhfo2">
                                <div className="_13izhfo3 css-1qo9j44" style={{ "--wts-form-field-template-columns": "auto", "--wts-form-field-addon-columns-start": "1", "--wts-field-box-container-display": "grid" }}>
                                    <div className="css-ghyw0v">
                                        <div data-tds-wts-field-box-content-variant="default" className="css-c8ze6m" style={{ "--wts-field-box-background-color": "var(--wts-adaptive-background)", "--wts-field-box-disabled-background-color": "var(--wts-adaptive-grey100)", "--wts-field-box-border-color": "var(--wts-adaptive-grey200)", "--wts-field-box-disabled-border-color": "var(--wts-adaptive-greyOpacity50)", "--wts-field-box-h-padding": "6px", "--wts-field-box-height": "32px", "--wts-field-box-font-size": "14px", "--wts-field-box-separator-height": "20px", "--wts-field-box-border-radius": "8px", "--wts-field-box-separator-margin": "0 4px", "--wts-field-box-box-shadow-color": "var(--wts-field-box-border-color)", "--wts-field-box-box-shadow-width": "1px", "--wts-field-box-hover-box-shadow-color": "var(--wts-adaptive-blue200)", "--wts-field-box-focus-box-shadow-color": "var(--wts-adaptive-blue500)", "--wts-field-box-content-left-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-right-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-hover-offset": "0px", "--wts-field-box-clear-content-margin-bottom": "var(--wts-field-box-content-margin-bottom, 0px)" }}>
                                            <label className="tw3v-1r5dc8g0 _13izhfo5 _7wshe50" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>
                                                <input data-tossinvest-log="InputWithSubText" data-contents-value="가격" data-content-tag="가격" data-parent-name="PriceFieldsSet" aria-required="true" id="trading-form-price" inputMode="numeric" maxLength="11" pattern="[0-9,|.]+" type="text" defaultValue="214,500" name="price" style={{ width: "58px" }} />
                                                <span aria-hidden="true" className="_7wshe51" style={{ marginLeft: "auto", marginRight: "6px" }}>원</span>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                                <span className="_1cx72gj1">
                                    <button type="button" tabIndex="-1" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka5a tw3v-1wkoka5d tw3v-1wkoka515 tw3v-1wkoka5v tw3v-1wkoka5r tw3v-1wkoka5j tw3v-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="마이너스 버튼" data-content-tag="마이너스_버튼" data-parent-name="PlusMinusButtons">
                                        <span className="tw3v-1wkoka52g">
                                            <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                <svg enableBackground="new 0 0 16 16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                  <path d="m 12.292 8.875 h -8.585 c -0.483 0 -0.875 -0.392 -0.875 -0.875 s 0.391 -0.875 0.875 -0.875 h 8.585 c 0.483 0 0.875 0.391 0.875 0.875 s -0.392 0.875 -0.875 0.875 Z" fill="#b0b8c1"></path>
                                                </svg>
                                            </span>
                                            <span className="_9vo4o90">가격 내리기</span>
                                        </span>
                                    </button>
                                    <div className="_1cx72gj2"></div>
                                    <button type="button" tabIndex="-1" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka5a tw3v-1wkoka5d tw3v-1wkoka515 tw3v-1wkoka5v tw3v-1wkoka5r tw3v-1wkoka5j tw3v-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="플러스 버튼" data-content-tag="플러스_버튼" data-parent-name="PlusMinusButtons">
                                        <span className="tw3v-1wkoka52g">
                                            <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                   <path d="M8 13.068c-.427 0-.775-.348-.775-.775v-3.518h-3.518c-.427 0-.775-.348-.775-.775s.348-.775.775-.775h3.518v-3.518c0-.427.348-.775.775-.775s.775.348.775.775v3.518h3.517c.428 0 .775.348.775.775s-.348.775-.775.775h-3.517v3.517c0 .428-.348.776-.775.776z" fill="#b0b8c1"></path>
                                                </svg>
                                            </span>
                                            <span className="_9vo4o90">가격 올리기</span>
                                        </span>
                                    </button>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
                <div>
                    <div className="_13izhfo0">
                        <div className="_13izhfo1">
                            <div>
                                <label className="tw3v-1r5dc8g0" htmlFor="trading-form-quantity" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>수량</label>
                            </div>
                            <div>
                                <div className="_13izhfo2">
                                <div className="_13izhfo3 css-1qo9j44" style={{ "--wts-form-field-template-columns": "auto", "--wts-form-field-addon-columns-start": "1", "--wts-field-box-container-display": "grid" }}>
                                    <div className="css-ghyw0v">
                                        <div data-tds-wts-field-box-content-variant="default" className="css-c8ze6m" style={{ "--wts-field-box-background-color": "var(--wts-adaptive-background)", "--wts-field-box-disabled-background-color": "var(--wts-adaptive-grey100)", "--wts-field-box-border-color": "var(--wts-adaptive-grey200)", "--wts-field-box-disabled-border-color": "var(--wts-adaptive-greyOpacity50)", "--wts-field-box-h-padding": "6px", "--wts-field-box-height": "32px", "--wts-field-box-font-size": "14px", "--wts-field-box-separator-height": "20px", "--wts-field-box-border-radius": "8px", "--wts-field-box-separator-margin": "0 4px", "--wts-field-box-box-shadow-color": "var(--wts-field-box-border-color)", "--wts-field-box-box-shadow-width": "1px", "--wts-field-box-hover-box-shadow-color": "var(--wts-adaptive-blue200)", "--wts-field-box-focus-box-shadow-color": "var(--wts-adaptive-blue500)", "--wts-field-box-content-left-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-right-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-hover-offset": "0px", "--wts-field-box-clear-content-margin-bottom": "var(--wts-field-box-content-margin-bottom, 0px)" }}>
                                            <label className="tw3v-1r5dc8g0 _13izhfo5 _7wshe50" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>
                                                <input data-tossinvest-log="InputWithSubText" data-contents-value="수량" data-content-tag="수량" data-parent-name="QuantityFieldsSet" aria-required="true" className="" id="trading-form-quantity" inputMode="numeric" maxLength="11" pattern="[0-9,|.]+" type="text" defaultValue="" name="quantity" style={{ width: "80px" }} placeholder="최대 5주 가능" />
                                                <span aria-hidden="true" className="_7wshe51" style={{ marginLeft: "auto", marginRight: "6px" }}></span>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                                <span className="_1cx72gj1">
                                    <button type="button" tabIndex="-1" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka5a tw3v-1wkoka5d tw3v-1wkoka515 tw3v-1wkoka5v tw3v-1wkoka5r tw3v-1wkoka5j tw3v-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="마이너스 버튼" data-content-tag="마이너스_버튼" data-parent-name="PlusMinusButtons">
                                        <span className="tw3v-1wkoka52g">
                                            <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                <svg enableBackground="new 0 0 16 16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                  <path d="m 12.292 8.875 h -8.585 c -0.483 0 -0.875 -0.392 -0.875 -0.875 s 0.391 -0.875 0.875 -0.875 h 8.585 c 0.483 0 0.875 0.391 0.875 0.875 s -0.392 0.875 -0.875 0.875 Z" fill="#b0b8c1"></path>
                                                </svg>
                                            </span>
                                            <span className="_9vo4o90">수량 내리기</span>
                                        </span>
                                    </button>
                                    <div className="_1cx72gj2"></div>
                                    <button type="button" tabIndex="-1" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka5a tw3v-1wkoka5d tw3v-1wkoka515 tw3v-1wkoka5v tw3v-1wkoka5r tw3v-1wkoka5j tw3v-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="플러스 버튼" data-content-tag="플러스_버튼" data-parent-name="PlusMinusButtons">
                                        <span className="tw3v-1wkoka52g">
                                            <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                   <path d="M8 13.068c-.427 0-.775-.348-.775-.775v-3.518h-3.518c-.427 0-.775-.348-.775-.775s.348-.775.775-.775h3.518v-3.518c0-.427.348-.775.775-.775s.775.348.775.775v3.518h3.517c.428 0 .775.348.775.775s-.348.775-.775.775h-3.517v3.517c0 .428-.348.776-.775.776z" fill="#b0b8c1"></path>
                                                </svg>
                                            </span>
                                            <span className="_9vo4o90">수량 올리기</span>
                                        </span>
                                    </button>
                                </span>
                                </div>
                                <div className="xl0v5q7">
                                    <button type="button" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka58 tw3v-1wkoka541 tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="10%" data-content-tag="isMaxButton_최대_times" data-parent-name="PercentageSelectorCommon" style={{ flex: "1 1 0%", paddingRight: "0px", paddingLeft: "0px" }}>
                                        <span className="tw3v-1wkoka52g">10%</span>
                                    </button>
                                    <button type="button" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka58 tw3v-1wkoka541 tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="25%" data-content-tag="isMaxButton_최대_times" data-parent-name="PercentageSelectorCommon" style={{ flex: "1 1 0%", paddingRight: "0px", paddingLeft: "0px" }}>
                                        <span className="tw3v-1wkoka52g">25%</span>
                                    </button>
                                    <button type="button" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka58 tw3v-1wkoka541 tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="50%" data-content-tag="isMaxButton_최대_times" data-parent-name="PercentageSelectorCommon" style={{ flex: "1 1 0%", paddingRight: "0px", paddingLeft: "0px" }}>
                                        <span className="tw3v-1wkoka52g">50%</span>
                                    </button>
                                    <button type="button" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka58 tw3v-1wkoka541 tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="최대" data-content-tag="isMaxButton_최대_times" data-parent-name="PercentageSelectorCommon" style={{ flex: "1 1 0%", paddingRight: "0px", paddingLeft: "0px" }}>
                                        <span className="tw3v-1wkoka52g">최대</span>
                                    </button>
                                    <button type="button" className="tw3v-emtxt715 tw3v-emtxt7o tw3v-emtxt7w tw3v-emtxt711" tabIndex={0} aria-disabled="false" aria-label="통화 설정" data-theme="grey" data-variant="weak" data-mode="dark" id="radix-_rIkh_" aria-haspopup="menu" aria-expanded="false" data-state="closed" data-tossinvest-log="DropdownMenu.Trigger" data-parent-name="OnlyDollarSettingDropdown" data-tossinvest-priority-log="Dropdown.Trigger" data-contents-value="통화 설정" data-content-tag="통화_설정" style={{ width: "32px", height: "32px", flex: "0 1 auto" }}>
                                        <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px" }}>
                                        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path d="m 22 14.85 l -1 -0.8 c -0.5 -0.5 -0.8 -1.2 -0.8 -1.9 s 0.3 -1.4 0.8 -1.9 l 0.9 -1 c 0.4 -0.4 0.5 -1 0.2 -1.5 l -1.3 -2.2 c -0.3 -0.5 -0.8 -0.7 -1.4 -0.6 l -1.3 0.3 c -0.7 0.2 -1.5 0.1 -2.1 -0.3 s -1.1 -0.9 -1.3 -1.6 l -0.4 -1.3 c 0 -0.6 -0.5 -1 -1 -1 h -2.5 c -0.6 0 -1 0.4 -1.2 0.9 l -0.4 1.4 c -0.2 0.6 -0.6 1.2 -1.2 1.5 c -0.1 0 -0.1 0.1 -0.2 0.1 c -0.6 0.3 -1.3 0.4 -1.9 0.3 l -1.4 -0.4 c -0.5 -0.1 -1.1 0.1 -1.4 0.6 l -1.3 2.2 c -0.3 0.5 -0.2 1.1 0.2 1.5 l 0.9 1 c 0.5 0.5 0.8 1.2 0.8 1.9 s -0.3 1.4 -0.8 1.9 l -0.5 0.5 c -0.6 0.7 -0.8 1.7 -0.3 2.5 l 0.9 1.6 c 0.3 0.5 0.8 0.7 1.4 0.6 l 1.4 -0.4 c 0.6 -0.2 1.3 -0.1 1.9 0.3 c 0.1 0 0.1 0.1 0.2 0.1 c 0.6 0.3 1 0.9 1.2 1.5 l 0.4 1.4 c 0.2 0.5 0.6 0.9 1.2 0.9 h 2.5 c 0.6 0 1 -0.4 1.2 -0.9 l 0.4 -1.4 c 0.2 -0.6 0.6 -1.2 1.2 -1.5 c 0.1 0 0.1 -0.1 0.2 -0.1 c 0.6 -0.3 1.3 -0.4 1.9 -0.3 l 1.4 0.4 c 0.5 0.1 1.1 -0.1 1.4 -0.6 l 1.3 -2.2 c 0.3 -0.5 0.2 -1.1 -0.2 -1.5 Z m -10 0.9 c -2 0 -3.7 -1.6 -3.7 -3.7 s 1.6 -3.7 3.7 -3.7 s 3.7 1.6 3.7 3.7 s -1.7 3.7 -3.7 3.7 Z" fill="#b0b8c1" fillRule="evenodd" />
                                        </svg>
                                        </span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div>
                    <div className="tw3v-1e8fj1a2 tw3v-1e8fj1a0">
                        <div className="tw3v-1e8fj1a9">
                            <div className="tw3v-1e8fj1aa tw3v-1e8fj1ad tw3v-1e8fj1ab tw3v-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr)" }}>
                            <span className="tw3v-1e8fj1am">
                                <hr className="tw3v-5u17g30" />
                            </span>
                            </div>
                        </div>
                    </div>
                        <div className="tw3v-1e8fj1a2 tw3v-1e8fj1a0" style={{ paddingRight: "8px" }}>
                            <div className="tw3v-1e8fj1a9">
                                <div className="tw3v-1e8fj1aa tw3v-1e8fj1ad tw3v-1e8fj1ab tw3v-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr)" }}>
                                    <span className="tw3v-1e8fj1am">
                                        <div className="tw3v-1ia8ofc0 tw3v-1ia8ofc1">
                                            <span className="tw3v-1r5dc8g0 _1aatj5e0" data-contents-value="미수거래 (현금 50%) [object Object]" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                            미수거래 (현금 50%)
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="143 -1757 2014 2014" style={{ width: "1em" }}>
                                                <path shapeRendering="geometricPrecision" d="M 645 121.5 Q 414 -14 278.5 -245 T 143 -750 T 278.5 -1255 T 645 -1621.5 T 1150 -1757 T 1655 -1621.5 T 2021.5 -1255 T 2157 -750 T 2021.5 -245 T 1655 121.5 T 1150 257 T 645 121.5 Z M 1590 8.5 Q 1786 -104 1898 -303 T 2010 -750 T 1898 -1197 T 1590 -1508.5 T 1150 -1621 T 710 -1508.5 T 402 -1197 T 290 -750 T 402 -303 T 710 8.5 T 1150 121 T 1590 8.5 Z M 1078 -761.5 Q 1102 -801 1169 -844 Q 1233 -881 1260.5 -917.5 T 1288 -1005 Q 1288 -1061 1246 -1099.5 T 1135 -1138 Q 1068 -1138 1023.5 -1102 T 973 -1008 H 820 Q 828 -1081 872.5 -1140.5 T 987 -1234 T 1139 -1268 Q 1227 -1268 1295 -1234 T 1401 -1141 T 1439 -1009 Q 1439 -930 1403.5 -870.5 T 1291 -761 Q 1249 -736 1230.5 -720.5 T 1204.5 -688 T 1197 -642 V -550 H 1054 V -657 Q 1054 -722 1078 -761.5 Z M 1051.5 -279.5 Q 1021 -310 1021 -354 T 1051.5 -428.5 T 1125 -459 Q 1169 -459 1200 -428.5 T 1231 -354 T 1200 -279.5 T 1125 -249 Q 1082 -249 1051.5 -279.5 Z" fill="currentColor" />
                                            </svg>
                                            </span>
                                        </div>
                                    </span>
                                </div>
                                <div className="tw3v-1e8fj1ap">
                                    <div data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-contents-value="미수거래 토글" data-content-tag="미수거래_토글" data-parent-name="MarginTradeSwitchComp">
                                        <div className="tw3v-3n2xt3a tw3v-3n2xt3i tw3v-3n2xt32 width" data-tossinvest-log="Toggle" data-contents-value="미수거래 ON" data-content-tag="isMarginTradeActiveRemoteValue_OFF_ON_미수거래" data-parent-name="MarginToggle">
                                            <button type="button" data-tossinvest-log="slots.button" data-parent-name="Toggle" className="tw3v-3n2xt3b" aria-pressed="false" data-tds-wts-toggle-checked="false" data-tds-wts-toggle-disabled="false" style={{ width: "16px" }}>
                                            <div className="tw3v-3n2xt3d" data-tds-wts-toggle-handler style={{ transform: "translateX(0px)" }}></div>
                                            </button>
                                            <input data-tossinvest-log="slots.hiddenInput" data-parent-name="Toggle" aria-hidden="true" id="radix-_rIqq_" tabIndex="-1" aria-checked="false" className="tw3v-3n2xt3c" type="checkbox" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div>
                            <div className="tw3v-1e8fj1a2 tw3v-1e8fj1a0">
                                <div className="tw3v-1e8fj1a9">
                                <div className="tw3v-1e8fj1aa tw3v-1e8fj1ad tw3v-1e8fj1ab tw3v-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                                    <span className="tw3v-1e8fj1am">
                                    <div className="tw3v-1ia8ofc0 tw3v-1ia8ofc1" color="var(--wts-adaptive-grey800)" style={{ fontWeight: 700 }}>
                                        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>구매가능 금액</span>
                                    </div>
                                    </span>
                                    <span className="tw3v-1e8fj1am tw3v-1e8fj1ao">
                                    <div className="tw3v-1ia8ofc0 tw3v-1ia8ofc1">
                                        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>693,147원</span>
                                    </div>
                                    </span>
                                </div>
                                </div>
                            </div>
                        </div>
                    <div className="tw3v-1e8fj1a2 tw3v-1e8fj1a0">
                         <div className="tw3v-1e8fj1a9">
                            <div className="tw3v-1e8fj1aa tw3v-1e8fj1ad tw3v-1e8fj1ab tw3v-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                                <span className="tw3v-1e8fj1am">
                                    <div className="tw3v-1ia8ofc0 tw3v-1ia8ofc1" color="var(--wts-adaptive-grey800)" style={{ fontWeight: 700 }}>
                                        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>총 주문 금액</span>
                                    </div>
                                </span>
                                <span className="tw3v-1e8fj1am tw3v-1e8fj1ao">
                                    <div className="tw3v-1ia8ofc0 tw3v-1ia8ofc1">
                                        <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>0원</span>
                                    </div>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
                    <div className="xl0v5qb xl0v5q9">
                        <button
                            type="submit"
                            aria-disabled="false"
                            className={isSell ? "tw3v-1wkoka52h tw3v-1wkoka50 tw3v-1wkoka541 tw3v-1wkoka5f tw3v-1wkoka519 tw3v-1wkoka5z tw3v-1wkoka5s tw3v-1wkoka5n tw3v-1wkoka52a" : "tw3v-1wkoka52h tw3v-1wkoka54 tw3v-1wkoka541 tw3v-1wkoka5f tw3v-1wkoka519 tw3v-1wkoka5z tw3v-1wkoka5s tw3v-1wkoka5n tw3v-1wkoka52a"}
                            data-tds-wts-button
                            data-tossinvest-log="Button"
                            data-contents-label={isSell ? "판매하기" : "구매하기"}
                            data-contents-label-code="buttonText"
                            data-contents-value={isSell ? "판매하기" : "구매하기"}
                            data-content-tag="buttonText"
                            data-parent-name="SubmitComp"
                            style={{ backgroundColor: "var(--wts-button-background-color)" }}
                        >
                            <span className="tw3v-1wkoka52g">{isSell ? "판매하기" : "구매하기"}</span>
                        </button>
                    </div>
            </div>
        </form>
    )
}