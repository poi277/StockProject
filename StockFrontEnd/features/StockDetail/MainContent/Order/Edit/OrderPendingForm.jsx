import { PriceForm, QuantityForm, SubmitButton } from "../Commonutil/OrderCommon";
import useOrderEdit from "./useOrderEdit";

export default function OrderPendingForm({tradeTypeTab,stockCode,selectedPrice, edit, setEdit,
        editTarget, setEditTarget,
        editPrice, setEditPrice,
        editQuantity, setEditQuantity,
        editPriceType, setEditPriceType,
        handleEditOpen,
        handleEditClose, orders,executeOrder}) {
  

    return (
        <div id="trade-pending-orders-section">
            <form id="new-order-form" onSubmit={(e) => { e.preventDefault(); executeOrder({}); }} className="xl0v5q1" method="post" data-gtm-form-interact-id="3">
                <input type="hidden" value="A005930" name="stockCode" />
                <input type="hidden" value="BUY" name="tradeType" />
                <input type="hidden" value="KSP" name="market" />
                <input type="hidden" value="KRW" name="currencyMode" />
                <input type="hidden" value="1481.4" name="exchangeRate" />
                <input type="hidden" value="false" name="marginTrading" />
                <input type="hidden" value="false" name="noAutoExchange" />
                <input type="hidden" value="false" name="buyMaxQuantity" />
                <input type="hidden" value={editPriceType ? "00" : "03"} name="orderPriceType" />
                <div className="xl0v5q2" id="trade-order-section">
                 {edit ? 
                 <OrderEditForm tradeTypeTab={tradeTypeTab} 
                 editPriceType={editPriceType} 
                 setEditPriceType={setEditPriceType} 
                 editPrice={editPrice}
                 setEditPrice={setEditPrice}
                 editQuantity={editQuantity}
                 setQuantity={setEditQuantity}
                 handleEditClose={handleEditClose}
                 editTarget={editTarget}
                    /> : 
                 <OrderPendingListForm orders={orders} handleEditOpen={handleEditOpen} />}
                </div>
            </form>
        </div>
    )
}


function OrderPendingListForm({ orders, handleEditOpen }) {
    const TRADE_LABEL = { BUY: '구매', SELL: '판매' };
    const TRADE_COLOR = { BUY: 'var(--wts-adaptive-red500)', SELL: 'var(--wts-adaptive-blue500)' };

    return (
        <div className="xl0v5qv">
            <div data-testid="virtuoso-scroller" data-virtuoso-scroller="true" tabIndex={0} style={{ height: "100%", outline: "none", overflowY: "auto", position: "relative" }}>
                <div data-viewport-type="element" style={{ width: "100%", height: "100%", position: "absolute", top: "0px" }}>
                    <div data-testid="virtuoso-item-list" style={{ boxSizing: "border-box", paddingTop: "0px", paddingBottom: "0px", marginTop: "0px" }}>
                        {orders.map((order) => (
                            <div key={order.orderId} data-index={order.id} style={{ overflowAnchor: "none" }}>
                                <div className="xl0v5qw">
                                    <div className="order-summary">
                                        <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                            <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": TRADE_COLOR[order.tradeType], "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{TRADE_LABEL[order.tradeType]} </span>
                                            {`${order.remainingQuantity}주`}
                                        </span>
                                        <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>주당 {order.tradePrice.toLocaleString('ko-KR')}원</span>
                                    </div>
                                    <div className="order-edit-buttons">
                                        <button onClick={() => handleEditOpen(order)} type="button" aria-disabled="false" className="tw3s-1wkoka52h tw3s-1wkoka59 tw3s-1wkoka5c tw3s-1wkoka513 tw3s-1wkoka5t tw3s-1wkoka5r tw3s-1wkoka5h tw3s-1wkoka524" data-tds-wts-button data-tossinvest-log="Button" data-contents-label="수정" data-contents-label-code="수정" data-contents-value="수정" data-content-tag="pendingOrder_correctionInProgress_진행중__수정" data-parent-name="PendingOrder">
                                            <span className="tw3s-1wkoka52g">수정</span>
                                        </button>
                                        <button type="button" aria-disabled="false" className="tw3s-1wkoka52h tw3s-1wkoka51 tw3s-1wkoka5c tw3s-1wkoka513 tw3s-1wkoka5t tw3s-1wkoka5r tw3s-1wkoka5h tw3s-1wkoka524" data-tds-wts-button data-tossinvest-log="Button" data-contents-label="취소" data-contents-label-code="취소" data-contents-value="취소" data-content-tag="pendingOrder_cancellInProgress_진행중_취소" data-parent-name="PendingOrder">
                                            <span className="tw3s-1wkoka52g">취소</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    )
}

function OrderEditForm({tradeTypeTab, editPriceType, setEditPriceType,editPrice,setEditPrice,editQuantity,setEditQuantity,handleEditClose,editTarget}) {
    const priceLabel = editTarget.tradeType === 'BUY' ? '구매' : '판매';
    const tradeColor = editTarget.tradeType === 'BUY' ? 'var(--wts-adaptive-red500)' : 'var(--wts-adaptive-blue500)';
    return (
        <>
            <input type="hidden" value="2026-04-26" name="orderDate" />
            <input type="hidden" value="287099" name="orderNo" />
            <input type="hidden" value="24450" name="upperLimit" />
            <input type="hidden" value="13180" name="lowerLimit" />
            <div className="tw3s-1e8fj1a2 tw3s-1e8fj1a0">
                <div className="tw3s-1e8fj1a9">
                    <div className="tw3s-1e8fj1aa tw3s-1e8fj1ad tw3s-1e8fj1ac tw3s-1e8fj1ag" style={{ gridTemplateColumns: "minmax(0px, 1fr)" }}>
                        <span className="tw3s-1e8fj1am">
                            <div className="tw3s-1ia8ofc0 tw3s-1ia8ofc1">
                                <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                    <span style={{ color: tradeColor }}>{priceLabel}</span>
                                    {` ${editQuantity}주`}
                                </span>
                                <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                    {"주당 "}
                                    {`${Number(editPrice.replace(/,/g, '').toLocaleString('ko-KR'))}원`}
                                </span>
                            </div>
                        </span>
                    </div>
                    <div className="tw3s-1e8fj1ap">
                        <button type="button" className="tw3s-emtxt715 tw3s-emtxt7n tw3s-emtxt7r tw3s-emtxt7z" aria-disabled="false" aria-label="주문 수정 닫기" data-theme="grey" data-variant="fill" data-mode="dark" data-tossinvest-log="IconButton" data-contents-value="주문 수정 닫기" data-content-tag="주문_수정_닫기" onClick={handleEditClose} data-parent-name="OrderInfo">
                            <span className="tw3s-17xiat90 tw3s-17xiat91" aria-hidden="false" role="presentation" style={{ height: "12px", width: "12px", minWidth: "12px" }}>
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="line-icon">
                                    <path fill="#BOB8C1" fillRule="evenodd" d="M 13.815 12 l 5.651 -5.651 a 1.2 1.2 0 0 0 -1.697 -1.698 l -5.651 5.652 l -5.652 -5.652 a 1.201 1.201 0 0 0 -1.697 1.698 L 10.421 12 l -5.652 5.651 a 1.202 1.202 0 0 0 0.849 2.049 c 0.307 0 0.614 -0.117 0.848 -0.351 l 5.652 -5.652 l 5.651 5.652 a 1.198 1.198 0 0 0 1.697 0 a 1.2 1.2 0 0 0 0 -1.698 L 13.815 12 Z" />
                                </svg>
                            </span>
                        </button>
                    </div>
                </div>
            </div>
            <PriceForm priceLabel={priceLabel} priceType={editPriceType} price={editPrice} setPrice={setEditPrice} setPriceType={setEditPriceType} />
            <QuantityForm isPending={true} quantity={editQuantity} setQuantity={setEditQuantity} />
            <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "flex-end", alignItems: "normal", paddingRight: "8px" }}>
                <div className="tw3s-93nb7ym tw3s-93nb7yh tw3s-93nb7ye tw3s-93nb7yl _1ov4tnc1" data-tds-wts-checkbox>
                    <div className="tw3s-93nb7yo">
                        <input data-tossinvest-log="Checkbox" data-parent-name="EditQuantity" id="radix-_r_276_" className="tw3s-93nb7yp" data-contents-value="수량 바꾸기" data-content-tag="수량_바꾸기" type="checkbox" />
                        <label className="tw3s-1r5dc8g0" data-tossinvest-log="Txt" data-contents-label="[object Object]" data-contents-label-code="label" data-parent-name="Checkbox" htmlFor="radix-_r_276_" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                            <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>수량 바꾸기</span>
                        </label>
                    </div>
                </div>
            </div>
            <div className="tw3s-1e8fj1a2 tw3s-1e8fj1a0">
                <div className="tw3s-1e8fj1a9">
                    <div className="tw3s-1e8fj1aa tw3s-1e8fj1ad tw3s-1e8fj1ac tw3s-1e8fj1ag" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                        <span className="tw3s-1e8fj1am">
                            <div className="tw3s-1ia8ofc0 tw3s-1ia8ofc1">
                                <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>총 주문 금액</span>
                            </div>
                        </span>
                        <span className="tw3s-1e8fj1am tw3s-1e8fj1ao">
                            <div className="tw3s-1ia8ofc0 tw3s-1ia8ofc1">
                                <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{(Number(editPrice.replace(/,/g, '')) * Number(editQuantity)).toLocaleString('ko-KR')}원</span>
                            </div>
                        </span>
                    </div>
                </div>
            </div>
            <SubmitButton tradeTypeTab={tradeTypeTab} tradeType={editTarget.tradeType} />
        </>
    )
}

