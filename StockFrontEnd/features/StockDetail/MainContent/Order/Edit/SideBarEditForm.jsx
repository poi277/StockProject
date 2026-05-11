import './SideBarEditForm.css'
import useSideBarEditOrder from './useSideBarEditOrder';

export default function SideBarEditForm() {

    const {
        editOpen,
        editTarget,
        closeEdit,
        price,
        setPrice,
        popoverRef,
        handleEditOrder,
    } = useSideBarEditOrder();

    if (!editOpen) return null;
    return (
        <>
            <div ref={popoverRef}>
                <div data-radix-popper-content-wrapper style={{ position: "fixed", left: "0px", top: "0px", transform: "translate(1219px, 731px)", minWidth: "max-content", "--radix-popper-transform-origin": "330px 0%", zIndex: 1, "--radix-popper-available-width": "1529px", "--radix-popper-available-height": "953px", "--radix-popper-anchor-width": "297px", "--radix-popper-anchor-height": "49px" }}>
                    <div data-side="left" data-align="start" data-state="open" role="dialog" id="radix-_r_5dk_" aria-labelledby="radix-_r_5di_" aria-describedby="radix-_r_5dj_" className="tw6g-gduhvu0 tw6g-1h3jdxo4 tw6g-1h3jdxo7 tw6g-1h3jdxo5 dgtq0b" data-overlay-type="Popover" data-section-name="Popover__EditOrderPopover" tabIndex={-1} style={{ "--tw6g-1h3jdxo0": "10px", "--radix-popover-content-transform-origin": "var(--radix-popper-transform-origin)", "--radix-popover-content-available-width": "var(--radix-popper-available-width)", "--radix-popover-content-available-height": "var(--radix-popper-available-height)", "--radix-popover-trigger-width": "var(--radix-popper-anchor-width)", "--radix-popover-trigger-height": "var(--radix-popper-anchor-height)", opacity: 1, transform: "none" }}>
                        <form onSubmit={(e) => {e.preventDefault();handleEditOrder(); }}className="xl0v5q1" style={{display: "flex", flexDirection: "column",  gap: "8px 0px", position: "relative"}}>
                            <input type="hidden" value="false" name="agreedOver100Million" />
                            <input type="hidden" value="NASO251118004" name="stockCode" />
                            <input type="hidden" value="buy" name="tradeType" />
                            <input type="hidden" value="NSQ" name="market" />
                            <input type="hidden" value="KRW" name="currencyMode" />
                            <input type="hidden" value="1466" name="exchangeRate" />
                            <input type="hidden" value="false" name="noAutoExchange" />
                            <input type="hidden" value="false" name="buyMaxQuantity" />
                            <input type="hidden" value="2026-05-11" name="orderDate" />
                            <input type="hidden" value="19" name="orderNo" />
                            <input type="hidden" value="8rPZaSPKSCmsdK0IW8hcd4v2cXG5iomtrZpKS+n9DWL/jj3fjfi/2aRhafR+9dphx0tTJw==" name="orderId" />
                            <div>
                                <div className="_13izhfo0">
                                    <div className="_13izhfo1">
                                        <span className="tw6g-1r5dc8g0" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                            {"구매"}
                                            {"가격"}
                                        </span>
                                        <fieldset className="xl0v5q5">
                                            <legend className="_9vo4o90">주문 타입</legend>
                                            <div role="radiogroup" aria-required="true" dir="ltr" className="tw6g-1sni4y90 tw6g-1sni4y92 tw6g-1sni4y95" tabIndex={0} style={{ outline: "none" }} data-scrollable="false">
                                                <div className="tw6g-1sni4y97 tw6g-1sni4y99" style={{ boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px", width: "101px", transform: "none" }}></div>

                                                <button type="button" role="radio" aria-checked="true" data-state="checked" value="00" className="tw6g-1cq3gqg0 tw6g-1cq3gqg2" data-seg-state="checked" data-tossinvest-log="SegmentedControl.Item" data-contents-label="지정가" data-contents-label-code="label" data-contents-value="지정가" data-content-tag="label" data-parent-name="OrderPriceType" tabIndex={0} data-radix-collection-item>
                                                    <div className="tw6g-1cq3gqg3 tw6g-1cq3gqg5">
                                                        <div className="tw6g-1cq3gqg8">
                                                            <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqgb" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>지정가</span>
                                                            <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqgb" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>지정가</span>
                                                        </div>
                                                    </div>
                                                </button>
                                                <input aria-hidden="true" tabIndex={-1} type="radio" value="00" defaultChecked name="orderPriceType" style={{ transform: "translateX(-100%)", position: "absolute", pointerEvents: "none", opacity: 0, margin: "0px", width: "101px", height: "28px" }} />

                                                <button type="button" role="radio" aria-checked="false" data-state="unchecked" value="03" className="tw6g-1cq3gqg0 tw6g-1cq3gqg2" data-seg-state="unchecked" data-tossinvest-log="SegmentedControl.Item" data-contents-label="시장가" data-contents-label-code="label" data-contents-value="시장가" data-content-tag="label" data-parent-name="OrderPriceType" tabIndex={-1} data-radix-collection-item>
                                                    <div className="tw6g-1cq3gqg3 tw6g-1cq3gqg5">
                                                        <div className="tw6g-1cq3gqg8">
                                                            <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqgb" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>시장가</span>
                                                            <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqgb" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>시장가</span>
                                                        </div>
                                                    </div>
                                                </button>
                                                <input aria-hidden="true" tabIndex={-1} type="radio" value="03" name="orderPriceType" style={{ transform: "translateX(-100%)", position: "absolute", pointerEvents: "none", opacity: 0, margin: "0px", width: "101px", height: "28px" }} />
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
                                                    {/* data-tds-wts-field-box-disabled 를 위 아래로 넣어야함 */}
                                                    <div data-tds-wts-field-box-content-variant="default" className="css-c8ze6m" style={{ "--wts-field-box-background-color": "var(--wts-adaptive-background)", "--wts-field-box-disabled-background-color": "var(--wts-adaptive-grey100)", "--wts-field-box-border-color": "var(--wts-adaptive-grey200)", "--wts-field-box-disabled-border-color": "var(--wts-adaptive-greyOpacity50)", "--wts-field-box-h-padding": "6px", "--wts-field-box-height": "32px", "--wts-field-box-font-size": "14px", "--wts-field-box-separator-height": "20px", "--wts-field-box-border-radius": "8px", "--wts-field-box-separator-margin": "0 4px", "--wts-field-box-box-shadow-color": "var(--wts-field-box-border-color)", "--wts-field-box-box-shadow-width": "1px", "--wts-field-box-hover-box-shadow-color": "var(--wts-adaptive-blue200)", "--wts-field-box-focus-box-shadow-color": "var(--wts-adaptive-blue500)", "--wts-field-box-content-left-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-right-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-hover-offset": "0px", "--wts-field-box-clear-content-margin-bottom": "var(--wts-field-box-content-margin-bottom, 0px)" }}>
                                                        <label className="tw6g-1r5dc8g0 _13izhfo5 _7wshe50" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>
                                                            <input value={price} onChange={(e) => setPrice(e.target.value)} data-tossinvest-log="InputWithSubText" data-contents-value="가격" data-content-tag="가격" data-parent-name="PriceFieldsSet" aria-required="true" id="trading-form-price" inputMode="numeric" maxLength="11" pattern="[0-9,|.]+" type="text" name="price" style={{ width: "89px" }} />
                                                            <span aria-hidden="true" className="_7wshe51" style={{ marginLeft: "auto", marginRight: "6px" }}>원</span>
                                                        </label>
                                                    </div>
                                                </div>
                                            </div>
                                            <span className="_1cx72gj1">
                                                <button type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka5a tw6g-1wkoka5d tw6g-1wkoka515 tw6g-1wkoka5v tw6g-1wkoka5r tw6g-1wkoka5j tw6g-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="마이너스 버튼" data-content-tag="마이너스_버튼" data-parent-name="PlusMinusButtons">
                                                    <span className="tw6g-1wkoka52g">
                                                        <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                            <svg enableBackground="new 0 0 16 16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="m 12.292 8.875 h -8.585 c -0.483 0 -0.875 -0.392 -0.875 -0.875 s 0.391 -0.875 0.875 -0.875 h 8.585 c 0.483 0 0.875 0.391 0.875 0.875 s -0.392 0.875 -0.875 0.875 Z" fill="#b0b8c1"></path>
                                                            </svg>
                                                        </span>
                                                        <span className="_9vo4o90">가격 내리기</span>
                                                    </span>
                                                </button>
                                                <div className="_1cx72gj2"></div>
                                                <button type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka5a tw6g-1wkoka5d tw6g-1wkoka515 tw6g-1wkoka5v tw6g-1wkoka5r tw6g-1wkoka5j tw6g-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="플러스 버튼" data-content-tag="플러스_버튼" data-parent-name="PlusMinusButtons">
                                                    <span className="tw6g-1wkoka52g">
                                                        <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
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
                                            <label className="tw6g-1r5dc8g0" htmlFor="trading-form-quantity" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>수량</label>
                                        </div>
                                        <div>
                                            <div className="_13izhfo2">
                                                <div className="_13izhfo3 css-1qo9j44" style={{ "--wts-form-field-template-columns": "auto", "--wts-form-field-addon-columns-start": "1", "--wts-field-box-container-display": "grid" }}>
                                                    <div data-tds-wts-field-box-disabled className="css-ghyw0v">
                                                        <div data-tds-wts-field-readonly data-tds-wts-field-box-disabled data-tds-wts-field-box-content-variant="default" disabled className="css-c8ze6m" style={{ "--wts-field-box-background-color": "var(--wts-adaptive-background)", "--wts-field-box-disabled-background-color": "var(--wts-adaptive-grey100)", "--wts-field-box-border-color": "var(--wts-adaptive-grey200)", "--wts-field-box-disabled-border-color": "var(--wts-adaptive-greyOpacity50)", "--wts-field-box-h-padding": "6px", "--wts-field-box-height": "32px", "--wts-field-box-font-size": "14px", "--wts-field-box-separator-height": "20px", "--wts-field-box-border-radius": "8px", "--wts-field-box-separator-margin": "0 4px", "--wts-field-box-box-shadow-color": "var(--wts-field-box-border-color)", "--wts-field-box-box-shadow-width": "1px", "--wts-field-box-hover-box-shadow-color": "var(--wts-adaptive-blue200)", "--wts-field-box-focus-box-shadow-color": "var(--wts-adaptive-blue500)", "--wts-field-box-content-left-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-right-padding": "var(--wts-field-box-h-padding)", "--wts-field-box-content-hover-offset": "0px", "--wts-field-box-clear-content-margin-bottom": "var(--wts-field-box-content-margin-bottom, 0px)" }}>
                                                            <label className="tw6g-1r5dc8g0 _13izhfo5 _7wshe50" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>
                                                                <input data-tossinvest-log="InputWithSubText" data-contents-value="수량" data-content-tag="수량" data-parent-name="QuantityFieldsSet" aria-required="true" className="none" id="trading-form-quantity" inputMode="numeric" maxLength="11" pattern="[0-9,|.]+" readOnly type="text" value="1" name="quantity" style={{ width: "12px" }} />
                                                                <span aria-hidden="true" className="_7wshe51" style={{ marginLeft: "auto", marginRight: "6px" }}>전량</span>
                                                            </label>
                                                        </div>
                                                    </div>
                                                </div>
                                                <span className="_1cx72gj1">
                                                    <button type="button" aria-disabled="true" className="tw6g-1wkoka52h tw6g-1wkoka5a tw6g-1wkoka5d tw6g-1wkoka515 tw6g-1wkoka5v tw6g-1wkoka5r tw6g-1wkoka5j tw6g-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="마이너스 버튼" data-content-tag="마이너스_버튼" data-parent-name="PlusMinusButtons">
                                                        <span className="tw6g-1wkoka52g">
                                                            <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                                <svg enableBackground="new 0 0 16 16" viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                                    <path d="m 12.292 8.875 h -8.585 c -0.483 0 -0.875 -0.392 -0.875 -0.875 s 0.391 -0.875 0.875 -0.875 h 8.585 c 0.483 0 0.875 0.391 0.875 0.875 s -0.392 0.875 -0.875 0.875 Z" fill="#b0b8c1"></path>
                                                                </svg>
                                                            </span>
                                                            <span className="_9vo4o90">수량 1주 빼기</span>
                                                        </span>
                                                    </button>
                                                    <div className="_1cx72gj2"></div>
                                                    <button type="button" aria-disabled="true" className="tw6g-1wkoka52h tw6g-1wkoka5a tw6g-1wkoka5d tw6g-1wkoka515 tw6g-1wkoka5v tw6g-1wkoka5r tw6g-1wkoka5j tw6g-1wkoka526 _1cx72gj0" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="플러스 버튼" data-content-tag="플러스_버튼" data-parent-name="PlusMinusButtons">
                                                        <span className="tw6g-1wkoka52g">
                                                            <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="true" role="presentation" style={{ height: "16px", width: "16px", minWidth: "16px", color: "var(--wts-adaptive-grey500)" }}>
                                                                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                                                    <path d="M8 13.068c-.427 0-.775-.348-.775-.775v-3.518h-3.518c-.427 0-.775-.348-.775-.775s.348-.775.775-.775h3.518v-3.518c0-.427.348-.775.775-.775s.775.348.775.775v3.518h3.517c.428 0 .775.348.775.775s-.348.775-.775.775h-3.517v3.517c0 .428-.348.776-.775.776z" fill="#b0b8c1"></path>
                                                                </svg>
                                                            </span>
                                                            <span className="_9vo4o90">수량 1주 더하기</span>
                                                        </span>
                                                    </button>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="tw6g-1e8fj1a2 tw6g-1e8fj1a0">
                                <div className="tw6g-1e8fj1a9">
                                    <div className="tw6g-1e8fj1aa tw6g-1e8fj1ad tw6g-1e8fj1ac tw6g-1e8fj1ag" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                                        <span className="tw6g-1e8fj1am">
                                            <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1">
                                                <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>총 주문 금액</span>
                                            </div>
                                        </span>
                                        <span className="tw6g-1e8fj1am tw6g-1e8fj1ao">
                                            <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1">
                                                <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>4,999원</span>
                                            </div>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div className="xl0v5qa xl0v5q9">
                                <button type="submit" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka58 tw6g-1wkoka541 tw6g-1wkoka5f tw6g-1wkoka519 tw6g-1wkoka5z tw6g-1wkoka5s tw6g-1wkoka5n tw6g-1wkoka52a" data-tds-wts-button data-tossinvest-log="Button" data-contents-label="수정하기" data-contents-label-code="수정하기" data-contents-value="수정하기" data-content-tag="수정하기" data-parent-name="SubmitComp">
                                    <span className="tw6g-1wkoka52g">수정하기</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <span data-radix-focus-guard tabIndex={0} style={{ outline: "none", opacity: 0, position: "fixed", pointerEvents: "none" }}></span>
        </>
    )
}