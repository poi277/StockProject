import './HaveStock.css'
import useHaveStock from './useHaveStock'

export default function HaveStock({stockCode}) {

    const { STOCK_INFO_ROWS, totalDiff, totalRate, stock } = useHaveStock({ stockCode });
    
    return (
        <div className="sa1m6r0" style={{ overflow: "auto" }}>
            <div className="sa1m6r1">
                <div className="_1ov4tnc0" data-section-name="종목상세__내주식" data-ignore-auto-section-prefix="true">
                    {stock && (
                        <>
                            <div style={{ display: "flex", flexFlow: "wrap", gap: "8px", justifyContent: "space-between", alignItems: "normal", padding: "4px" }}>
                                <h3 className="tw6g-1r5dc8g0 _60z0ev1 _60z0ev2 _60z0ev0" style={{ padding: "0px 4px", "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                    총 수익
                                    <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": totalDiff >= 0 ? "var(--wts-adaptive-red500)" : "var(--wts-adaptive-blue500)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                        {" " + (totalDiff > 0 ? "+" : "") + totalDiff.toLocaleString() + "원 (" + totalRate + "%)"}
                                    </span>
                                </h3>
                                <div style={{ display: "flex", flexDirection: "row", gap: "0px", justifyContent: "normal", alignItems: "normal", flexShrink: 0 }}>
                                    <label htmlFor="radix-_r_d4n_" className="tw6g-nkz6ce0 tw6g-nkz6ce1" aria-disabled="false" aria-checked="true" tabIndex={0}>
                                        <input data-tossinvest-log="Check" data-parent-name="Check" aria-hidden="true" id="radix-_r_d4n_" tabIndex={-1} className="tw6g-nkz6ce6" data-contents-value="수수료·세금 OFF" data-content-tag="nextIncludeStockExpense_수수료_세금" type="checkbox" defaultChecked />
                                        <span className="tw6g-17xiat90" aria-hidden="false" role="presentation" style={{ height: "12px", width: "12px", minWidth: "12px", color: "var(--wts-adaptive-blue500)" }}>
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" fillRule="evenodd"><path fill="#3180F2" d="M23 12c0 6.075-4.925 11-11 11S1 18.075 1 12 5.925 1 12 1s11 4.925 11 11" /><path stroke="#FFF" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7.5 11.676l3.416 3.416L16.5 9.508" /></g></svg>
                                        </span>
                                        <span className="tw6g-1r5dc8g0 tw6g-nkz6ce7" style={{ padding: "0px 1px", "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                                            <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-blue700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                                {"수수료·세금 "}<span className="sr-only">OFF</span>
                                            </span>
                                        </span>
                                    </label>
                                </div>
                            </div>

                            {STOCK_INFO_ROWS.map(({ label, value }) => (
                                <StockInfoRow key={label} label={label} value={value} />
                            ))}

                            <div>
                                <div className="tw6g-1e8fj1a2 tw6g-1e8fj1a0">
                                    <div className="tw6g-1e8fj1a9">
                                        <div className="tw6g-1e8fj1aa tw6g-1e8fj1ad tw6g-1e8fj1ab tw6g-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                                            <span className="tw6g-1e8fj1am">
                                                <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1" color="var(--wts-adaptive-grey700)">
                                                    <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>
                                                        수수료
                                                        <span className="t00n7r0" aria-haspopup="dialog" aria-expanded="false" aria-controls="radix-_r_d4q_" data-state="closed" data-tossinvest-log="RadixPopover.Trigger" data-contents-label="[object Object]" data-contents-label-code="child" data-parent-name="Commission" data-tossinvest-priority-log="Popover.Trigger" data-contents-value="hint tooltip icon" data-content-tag="hint_tooltip_icon">
                                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="143 -1757 2014 2014" style={{ width: "1em" }}><path shapeRendering="geometricPrecision" d="M645 121.5Q414-14 278.5-245T143-750 278.5-1255 645-1621.5 1150-1757 1655-1621.5 2021.5-1255 2157-750 2021.5-245 1655 121.5 1150 257 645 121.5ZM1590 8.5Q1786-104 1898-303T2010-750 1898-1197 1590-1508.5 1150-1621 710-1508.5 402-1197 290-750 402-303 710 8.5 1150 121 1590 8.5ZM1078-761.5Q1102-801 1169-844 1233-881 1260.5-917.5T1288-1005Q1288-1061 1246-1099.5T1135-1138Q1068-1138 1023.5-1102T973-1008H820Q828-1081 872.5-1140.5T987-1234 1139-1268Q1227-1268 1295-1234T1401-1141 1439-1009Q1439-930 1403.5-870.5T1291-761Q1249-736 1230.5-720.5T1204.5-688 1197-642V-550H1054V-657Q1054-722 1078-761.5ZM1051.5-279.5Q1021-310 1021-354T1051.5-428.5 1125-459Q1169-459 1200-428.5T1231-354 1200-279.5 1125-249Q1082-249 1051.5-279.5Z" fill="currentColor" /></svg>
                                                        </span>
                                                    </span>
                                                </div>
                                            </span>
                                            <span className="tw6g-1e8fj1am tw6g-1e8fj1ao">
                                                <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1" color="var(--wts-adaptive-grey700)">
                                                    <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>2원 예상</span>
                                                </div>
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    )
}

function StockInfoRow({ label, value }) {
    return (
        <div className="tw6g-1e8fj1a2 tw6g-1e8fj1a0">
            <div className="tw6g-1e8fj1a9">
                <div className="tw6g-1e8fj1aa tw6g-1e8fj1ad tw6g-1e8fj1ab tw6g-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr) minmax(0px, 1fr)" }}>
                    <span className="tw6g-1e8fj1am">
                        <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1" color="var(--wts-adaptive-grey700)">
                            <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{label}</span>
                        </div>
                    </span>
                    <span className="tw6g-1e8fj1am tw6g-1e8fj1ao">
                        <div className="tw6g-1ia8ofc0 tw6g-1ia8ofc1" color="var(--wts-adaptive-grey700)">
                            <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey700)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{value}</span>
                        </div>
                    </span>
                </div>
            </div>
        </div>
    )
}