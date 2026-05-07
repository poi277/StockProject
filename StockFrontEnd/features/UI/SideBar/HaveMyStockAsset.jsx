import useHaveMyStockAsset from "./useHaveMyStockAssect"

export default function HaveMyStockAsset() {
    const { SEGMENT_ITEMS, totalInvestment, totalDiff, totalRate, stocks, haveStocks } = useHaveMyStockAsset()

    return (
        <div className="_1oe23q53" data-section-name="내투자">
            <HaveMyStockMoney totalInvestment={totalInvestment} totalDiff={totalDiff} totalRate={totalRate} />
            <HaveMyStock SEGMENT_ITEMS={SEGMENT_ITEMS} stocks={stocks} haveStocks={haveStocks} />
        </div>
    )
}

function HaveMyStockMoney({ totalInvestment,totalDiff,totalRate }) {
    const isPositive = totalDiff >= 0;
    const diffColor = isPositive ? "var(--wts-adaptive-red500)" : "var(--wts-adaptive-blue500)";

    return (
        <section>
            <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "space-between", alignItems: "center", height: "24px" }}>
                <a data-tossinvest-log="Link" data-contents-label="[object Object]" data-contents-label-code="header" data-contents-value="내 투자" data-content-tag="derivedTitleText" data-parent-name="HeaderWithSummary" className="_1h1d45d1" href="/investment-portfolio">
                    <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "normal", alignItems: "center" }}>
                        <span className="tw6g-1r5dc8g0" size="15" data-state="closed" data-tossinvest-log="TooltipLegacy.Trigger" data-contents-value="숨긴 종목 확인하기" data-content-tag="숨긴_종목_확인하기" data-parent-name="HideAssetNudgeTooltip" style={{ lineHeight: "1.45", "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>내 투자</span>
                        <span className="tw6g-17xiat90 tw6g-17xiat91 _1h1d45d2" aria-hidden="false" role="presentation" style={{ height: "22px", width: "22px", minWidth: "22px", color: "var(--wts-adaptive-grey700)" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <path d="m10.379 17.043c-.205 0-.409-.078-.565-.234-.312-.312-.312-.818 0-1.131l3.677-3.678-3.677-3.678c-.312-.312-.312-.819 0-1.131s.819-.312 1.131 0l4.242 4.243c.312.312.312.819 0 1.131l-4.242 4.243c-.156.156-.361.234-.566.234z" fill="#b0b8c1" />
                            </svg>
                        </span>
                    </div>
                </a>
                <div style={{ display: "flex", flexDirection: "row", gap: "6px", justifyContent: "space-between", alignItems: "center" }}>
                    <button className="tw6g-emtxt715 tw6g-emtxt7p tw6g-emtxt7u tw6g-emtxt710" aria-disabled="false" aria-label="내 투자 접기" data-theme="grey" data-variant="clear" data-mode="dark" data-tossinvest-log="IconButton" data-contents-label="내 투자 접기" data-contents-value="내 투자 접기" data-content-tag="derivedTitleText_openStatus_open_접기_펼치기" data-parent-name="HeaderWithSummary" aria-hidden="true" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" style={{ marginRight: "-6px" }}>
                        <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", transform: "none" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <path d="m12.002 7.275c.345 0 .69.13.954.395l6.75 6.75c.352.339.497.841.372 1.315-.123.474-.492.843-.966.966-.472.123-.977-.018-1.315-.372l-5.796-5.796-5.796 5.796c-.339.352-.843.495-1.315.372-.474-.123-.843-.492-.966-.966-.124-.474.02-.977.372-1.315l6.75-6.75c.254-.252.597-.395.954-.395" fill="#8f959e" fillRule="evenodd" />
                                <path d="m0 0h24v24h-24z" fill="none" />
                            </svg>
                        </span>
                    </button>
                </div>
            </div>
            <div className="_1h1d45d0" style={{ display: "flex", flexDirection: "column", height: "auto", marginTop: "0px", marginBottom: "12px", opacity: 1 }}>
                <a data-tossinvest-log="Link" data-contents-value="770,145원" data-content-tag="formatPrice" data-parent-name="AssetsSummary" className="_11fusij0 tw6g-gduhvu0" href="/investment-portfolio">
                    <div style={{ display: "flex", flexDirection: "column", gap: "0px", justifyContent: "normal", alignItems: "normal", width: "fit-content" }}>
                        <span className="tw6g-1r5dc8g0 _1p5yqoh0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "24px" }}>
                            {totalInvestment.toLocaleString()}원
                        </span>
                        <span className="tw6g-1r5dc8g0 _1p5yqoh0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": diffColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                            {(totalDiff > 0 ? "+" : "") + totalDiff.toLocaleString() + "원"}
                            {" ("}
                            {(totalDiff > 0 ? "+" : "") + totalRate}
                            {"%)"}
                        </span>
                    </div>
                </a>
            </div>
        </section>
    )
}

function HaveMyStock({ SEGMENT_ITEMS, stocks }) {
    return (
        <div style={{ display: "flex", flexDirection: "column", gap: "0px", justifyContent: "normal", alignItems: "normal", flex: "1 1 0%", minHeight: "0px" }}>
            <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "space-between", alignItems: "center" }}>
                <button type="button" tabIndex={0} aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka5a tw6g-1wkoka5e tw6g-1wkoka518 tw6g-1wkoka5v tw6g-1wkoka5r tw6g-1wkoka5m tw6g-1wkoka529 tw6g-1wkoka53k" data-tds-wts-button data-tossinvest-log="Button" data-contents-value="가나다 순" data-content-tag="isUserConfigMode_ShareHoldingsSortingRuleMap_직접_설정하기_ShareHoldingsSortingRuleMap_sortMode" data-parent-name="Tabs" id="radix-_r_pf_" aria-haspopup="menu" aria-expanded="false" data-state="closed" data-tossinvest-priority-log="Dropdown.Trigger" style={{ marginLeft: "-6px" }}>
                    <span className="tw6g-1wkoka52g">가나다 순</span>
                    <div className="tw6g-1wkoka532 tw6g-1wkoka52t tw6g-1wkoka51z">
                        <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px" }}>
                            <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                                <path d="m3.691 5.746 4.309 4.355 4.309-4.355" fill="none" stroke="#8f959e" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
                            </svg>
                        </span>
                    </div>
                </button>

                <div role="radiogroup" aria-required="false" dir="ltr" className="tw6g-1sni4y90 tw6g-1sni4y91 tw6g-1sni4y95" aria-label="평가금에서 현재가로" data-tossinvest-log="SegmentedControl" data-parent-name="Tabs" data-skip="true" tabIndex={0} style={{ outline: "none" }} data-scrollable="false">
                    <div className="tw6g-1sni4y97 tw6g-1sni4y98" style={{ boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px", width: "43px", transform: "translateX(45px)" }}></div>
                    {SEGMENT_ITEMS.map((item) => (
                        <button key={item.label} type="button" role="radio" aria-checked={item.checked} data-state={item.state} value={item.value} className="tw6g-1cq3gqg0 tw6g-1cq3gqg2" data-seg-state={item.state} data-tossinvest-log="SegmentedControl.Item" data-contents-label={item.label} data-contents-label-code={item.label} data-contents-value={item.label} data-content-tag={item.label} data-parent-name="Tabs" tabIndex={-1} data-radix-collection-item>
                            <div className="tw6g-1cq3gqg3 tw6g-1cq3gqg4">
                                <div className="tw6g-1cq3gqg8">
                                    <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqga" aria-hidden="true" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>{item.label}</span>
                                    <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqga" style={{ "--tds-wts-font-weight": `var(--tw-font-weight-${item.activeWeight})`, "--tds-wts-foreground-color": item.activeColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>{item.label}</span>
                                </div>
                            </div>
                        </button>
                    ))}
                </div>
            </div>
            <div style={{ flex: "0 0 auto", height: "12px" }}></div>
            <div className="_1a6f6te0 deq21q0" style={{ margin: "0px -8px" }}>
                <div style={{ height: "107.375px", width: "100%", position: "relative" }}>
                    <HaveMyStockList stocks={stocks} />
                </div>
            </div>
            <div id="DndDescribedBy-8" style={{ display: "none" }}> ... </div>
            <div id="DndLiveRegion-8" role="status" aria-live="assertive" aria-atomic="true" style={{ position: "fixed", width: "1px", height: "1px", margin: "-1px", border: "0px", padding: "0px", overflow: "hidden", clip: "rect(0px, 0px, 0px, 0px)", clipPath: "inset(100%)", whiteSpace: "nowrap" }}></div>
        </div>
    )
}
function HaveMyStockList({ stocks = [] }) {

    return (
        <ul data-tabster='{"mover":{"cyclic":false,"direction":1,"memorizeCurrent":true}}' style={{ position: "absolute", top: "0px", left: "0px", width: "100%", transform: "translateY(0px)" }}>
            {stocks?.map((stock, index) => {
                const isPositive = stock.diff >= 0;
                const changeColor = isPositive
                    ? "var(--wts-adaptive-red500)"
                    : "var(--wts-adaptive-blue500)";

                return (
                    <div key={stock.stockCode} data-index={index}>
                        <a
                            data-tossinvest-log="StockRow"
                            data-contents-value={stock.stockName}
                            data-content-tag="item_stockName"
                            data-parent-name="EvaluatedAmountListRow"
                            tabIndex={0}
                            className=""
                            role="button"
                            aria-disabled="false"
                            aria-roledescription="sortable"
                            aria-describedby="DndDescribedBy-8"
                            data-state="closed"
                            href={`/stocks/${stock.stockCode}/order`}
                        >
                            <div className="tw6g-1e8fj1a2 tw6g-1e8fj1a0 tw6g-1e8fj1aj tw6g-1e8fj1ak" style={{ transition: "none", visibility: "visible" }}>
                                <div className="tw6g-1e8fj1a3 tw6g-1e8fj1a8 tw6g-1e8fj1a6">
                                    <div data-nosnippet="true" className="favgr63 favgr60">
                                        <div className="c3f3of0 favgr6c favgr69">
                                            <img
                                                alt="logo"
                                                loading="lazy"
                                                width="30"
                                                height="30"
                                                decoding="async"
                                                data-nimg="1"
                                                srcSet={`https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-${stock.stockCode}-E0.png?width=32&height=32 1x, https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-${stock.stockCode}-E0.png?width=64&height=64 2x`}
                                                src={`https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-${stock.stockCode}-E0.png?width=64&height=64`}
                                                style={{ color: "transparent" }}
                                            />
                                        </div>
                                        <span className="favgr6u favgr6r favgr6q"></span>
                                    </div>
                                </div>
                                <div className="tw6g-1e8fj1a9">
                                    <div className="tw6g-1e8fj1aa tw6g-1e8fj1ad tw6g-1e8fj1ac tw6g-1e8fj1ag" style={{ gridTemplateColumns: "minmax(0px, 1fr) max-content" }}>
                                        <span className="tw6g-1e8fj1am">
                                            <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1">
                                                <span className="tw6g-1r5dc8g0 fmiok60" data-contents-label="종목명" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                                    <span>{stock.stockName}</span>
                                                </span>
                                                <span className="tw6g-1r5dc8g0" data-contents-label="현재가" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                                                    {stock.quantity + "주"}
                                                </span>
                                            </div>
                                        </span>
                                        <span className="tw6g-1e8fj1am tw6g-1e8fj1ao">
                                            <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1 _1p5yqoh0">
                                                <span className="tw6g-1r5dc8g0" data-contents-label="거래량" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                                    {stock.evaluatedAmount.toLocaleString() + "원"}
                                                </span>
                                                <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": changeColor, "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                                                    {(stock.diff > 0 ? "+" : "") + stock.diff.toLocaleString() + "원 (" + stock.rate + "%)"}
                                                </span>
                                            </div>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                );
            })}
        </ul>
    );
}