import useOrderSideBar from "./useOrderSideBar"
import './OrderSideBar.css'
import useCancelStore from "../../../store/cancelStore";

export default function OrderSideBar() {

    const { TAB_ITEMS, orders, setOrders } = useOrderSideBar();

    return (
        <section data-section-name="주문내역">
            <OrderHistory />
            <div className="_1hld45d0" style={{ display: "flex", flexDirection: "column", overflow: "visible", height: "210px", marginTop: "0px", marginBottom: "12px", opacity: 1 }}>
                <div style={{ display: "flex", flexDirection: "column", flex: "1 1 0%" }}>
                    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0px 12px", marginTop: "8px" }}>
                        <div dir="ltr" data-orientation="horizontal" className="tw6g-336bzib tw6g-336bzif">
                            <div className="tw6g-336bzix">
                                <div role="tablist" aria-orientation="horizontal" className="tw6g-336bzih" tabIndex={0} data-orientation="horizontal" style={{ outline: "none", overflow: "hidden" }}>
                                    <div className="tw6g-336bziw tw6g-336bziv" style={{ width: "48px", transform: "none" }}></div>
                                    {TAB_ITEMS.map((tab) => (
                                        <button key={tab.label} type="button" role="tab" aria-selected={tab.selected} aria-controls={`radix-_r_19_-content-${tab.controls}`} data-state={tab.state} id={`radix-_r_19_-trigger-${tab.controls}`} data-tossinvest-log="Tab.Item" data-contents-label={tab.label} data-contents-label-code={tab.label} data-contents-value={tab.label} data-content-tag={tab.label} className="tw6g-336bzit tw6g-336bzin" tabIndex={-1} data-orientation="horizontal" data-radix-collection-item>
                                            <div style={{ position: "relative" }}>
                                                <span className="tw6g-1r5dc8g0 tw6g-336bzij tw6g-336bzii tw6g-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{tab.label}</span>
                                                <span className="tw6g-1r5dc8g0 tw6g-336bzij tw6g-336bzii" data-tds-wts-tab-item={tab.selected ? "true" : "false"} style={{ "--tds-wts-font-weight": `var(--tw-font-weight-${tab.activeWeight})`, "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{tab.label}</span>
                                            </div>
                                        </button>
                                    ))}
                                </div>
                            </div>
                        </div>
                        <div>
                            <button type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka51 tw6g-1wkoka5d tw6g-1wkoka515 tw6g-1wkoka5v tw6g-1wkoka5r tw6g-1wkoka5j tw6g-1wkoka526" data-tds-wts-button data-tossinvest-log="Button" data-contents-label="전체 취소" data-contents-label-code="전체 취소" data-contents-value="전체 취소" data-content-tag="전체_취소" data-parent-name="CancelAllButton">
                                <span className="tw6g-1wkoka52g">전체 취소</span>
                            </button>
                        </div>
                    </div>
                    <div style={{ flex: "0 0 auto", height: "8px" }}></div>
                    <div style={{ flex: "1 1 0%", padding: "0px 8px" }}>
                        <section style={{ height: "100%", display: "flex", flexDirection: "column" }} data-section-name="대기">
                            <div data-content-container="true" style={{ height: "100%" }}>
                                <div data-testid="virtuoso-scroller" data-virtuoso-scroller="true" tabIndex={0} style={{ height: "100%", outline: "none", overflowY: "auto", position: "relative" }}>
                                    <div data-viewport-type="element" style={{ width: "100%", height: "100%", position: "absolute", top: "0px" }}>
                                        <div data-testid="virtuoso-item-list" style={{ boxSizing: "border-box", paddingTop: "0px", paddingBottom: "0px", marginTop: "0px" }}>
                                            <OrderList orders={orders} />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                </div>
            </div>
        </section>
    )
}

function OrderHistory() {
    return (
        <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "space-between", alignItems: "center", height: "24px", padding: "0px 16px", marginTop: "0px" }}>
            <a data-tossinvest-log="Link" data-contents-label="[object Object]" data-contents-label-code="header" data-contents-value="주문내역" data-content-tag="derivedTitleText" data-parent-name="HeaderWithSummary" className="_1hld45d1" href="/account/orders">
                <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "normal", alignItems: "center" }}>
                    <div style={{ display: "flex", flexDirection: "row", gap: "4px", justifyContent: "normal", alignItems: "center", height: "18px" }}>
                        <span className="tw6g-1r5dc8g0" size="15" style={{ lineHeight: "1.45", "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>주문내역</span>
                    </div>
                    <span className="tw6g-17xiat90 tw6g-17xiat91 _1h1d45d2" aria-hidden="false" role="presentation" style={{ height: "22px", width: "22px", minWidth: "22px", color: "var(--wts-adaptive-grey700)" }}>
                        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path d="m10.379 17.043c-.205 0-.409-.078-.565-.234-.312-.312-.312-.818 0-1.131l3.677-3.678-3.677-3.678c-.312-.312-.312-.819 0-1.131s.819-.312 1.131 0l4.242 4.243c.312.312.312.819 0 1.131l-4.242 4.243c-.156.156-.361.234-.566.234z" fill="#b0b8c1" />
                        </svg>
                    </span>
                </div>
            </a>
            <div style={{ display: "flex", flexDirection: "row", gap: "6px", justifyContent: "space-between", alignItems: "center" }}>
                <button className="tw6g-emtxt715 tw6g-emtxt7p tw6g-emtxt7u tw6g-emtxt710" aria-disabled="false" aria-label="주문내역 접기" data-theme="grey" data-variant="clear" data-mode="dark" data-tossinvest-log="IconButton" data-contents-label="주문내역 접기" data-contents-value="주문내역 접기" data-content-tag="derivedTitleText_openStatus_open_접기_펼치기" data-parent-name="HeaderWithSummary" aria-hidden="true" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" style={{ marginRight: "-6px" }}>
                    <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", transform: "rotate(180deg)" }}>
                        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path d="m12.002 7.275c.345 0 .69.13.954.395l6.75 6.75c.352.339.497.841.372 1.315-.123.474-.492.843-.966.966-.472.123-.977-.018-1.315-.372l-5.796-5.796-5.796 5.796c-.339.352-.843.495-1.315.372-.474-.123-.843-.492-.966-.966-.124-.474.02-.977.372-1.315l6.75-6.75c.254-.252.597-.395.954-.395" fill="#8f959e" fillRule="evenodd" />
                            <path d="m0 0h24v24h-24z" fill="none" />
                        </svg>
                    </span>
                </button>
            </div>
        </div>
    )
}



function OrderList({ orders }) {
    const { openCancel } = useCancelStore();

    return (
        <>
            {orders?.map((order) => (
                <div key={order.orderId} data-index="0" data-known-size="49" data-item-index="0" style={{ overflowAnchor: "none" }}>
                    <div>
                        <div>
                            <a data-tossinvest-log="Link" data-contents-value={order.stockName} data-content-tag="displayName" data-parent-name="AllItem" className="dgtq01" href={`/stock/${order.stockCode}`}>
                                <div className="tw6g-1e8fj1a2 tw6g-1e8fj1a0 tw6g-1e8fj1aj tw6g-1e8fj1ak dgtq02 dgtq00">
                                    <div className='tw6g-1e8fj1a3 tw6g-1e8fj1a7 tw6g-1e8fj1a6'>
                                        <span role="presentation" className="tw6g-m6rqix2 tw6g-m6rqix0" style={{ lineHeight: 0, display: "inline-block", height: "30px", width: "30px" }}>
                                            <img
                                                alt=""
                                                draggable="false"
                                                loading="lazy"
                                                width="30"
                                                height="30"
                                                decoding="async"
                                                data-nimg="1"
                                                srcSet={`https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-${order.stockCode}.png?width=32&height=32 1x, https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-${order.stockCode}.png?width=64&height=64 2x`}
                                                src={`https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-${order.stockCode}.png?width=64&height=64`}
                                                style={{ color: "transparent", width: "100%" }}
                                            />
                                        </span>
                                    </div>
                                    <div className="tw6g-1e8fj1a9">
                                        <div className="tw6g-1e8fj1aa tw6g-1e8fj1ad tw6g-1e8fj1ab tw6g-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                                            <span className="tw6g-1e8fj1am">
                                                <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1">
                                                    <span className="tw6g-1r5dc8g0 dgtq0a" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                                        {order.stockName}
                                                    </span>
                                                </div>
                                            </span>
                                            <span className="tw6g-1e8fj1am tw6g-1e8fj1ao">
                                                <div className="xl0v5qw" style={{ height: "auto" }}>
                                                    <div className="dgtq06 order-edit-buttons">
                                                        <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); }} type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka59 tw6g-1wkoka5c tw6g-1wkoka513 tw6g-1wkoka5t tw6g-1wkoka5r tw6g-1wkoka5h tw6g-1wkoka524" data-tds-wts-button>
                                                            <span className="tw6g-1wkoka52g">수정</span>
                                                        </button>
                                                        <button onClick={(e) => { e.preventDefault(); e.stopPropagation(); openCancel(order, 'sidebar'); }} type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka51 tw6g-1wkoka5c tw6g-1wkoka513 tw6g-1wkoka5t tw6g-1wkoka5r tw6g-1wkoka5h tw6g-1wkoka524" data-tds-wts-button>
                                                            <span className="tw6g-1wkoka52g">취소</span>
                                                        </button>
                                                    </div>
                                                </div>
                                                <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1 dgtq07">
                                                    <span className="tw6g-1r5dc8g0 dgtq0a" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                                        {"주당 " + order.tradePrice.toLocaleString() + "원"}
                                                    </span>
                                                    <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": order.tradeType === "BUY" ? "var(--wts-adaptive-red600)" : "var(--wts-adaptive-blue600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                                                        {order.remainingQuantity + "주 "}
                                                        {order.tradeType === "BUY" ? "구매" : "판매"}
                                                    </span>
                                                </div>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </div>
                    </div>
                </div>
            ))}
        </>
    );
}