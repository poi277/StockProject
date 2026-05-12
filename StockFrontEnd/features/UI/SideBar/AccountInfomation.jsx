import useAccountInfomation from "./useAccountInfomation";

export default function AccountInfomation() {
    const {CURRENCY_ITEMS } = useAccountInfomation();

    return (
        <div className="_1ovjuf10" style={{ display: "flex", flexDirection: "row", gap: "6px", justifyContent: "space-between", alignItems: "normal" }}>
            <section>
                <div className="_1ovjuf12">
                    <span className="list-row__icon css-1o27cbr">
                        <div className="css-6w615j" style={{ clipPath: "path('M 0 11.568 C 0 2.376 2.376 0 11.568 0 H 12.432 C 21.624 0 24 2.376 24 11.568 V 12.432 C 24 21.624 21.624 24 12.432 24 H 11.568 C 2.376 24 0 21.624 0 12.432 V 11.568 Z')" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <foreignObject x="0" y="0" width="24" height="24" className="css-uwwqev">
                                    <span className="css-z55afh">
                                        <span role="presentation" style={{ lineHeight: 0, display: "inline-block", height: "16px", width: "16px", minWidth: "16px" }}>
                                            <img alt="" draggable="false" loading="lazy" width="16" height="16" decoding="async" data-nimg="1" srcSet="https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Ficons%2Fpng%2F4x%2Ficon-toss-logo-simple.png?width=16&height=16 1x, https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Ficons%2Fpng%2F4x%2Ficon-toss-logo-simple.png?width=32&height=32 2x" src="https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Ficons%2Fpng%2F4x%2Ficon-toss-logo-simple.png?width=32&height=32" style={{ color: "transparent", width: "100%" }} />
                                        </span>
                                    </span>
                                </foreignObject>
                            </svg>
                        </div>
                    </span>
                    <span className="tw6g-1r5dc8g0 _60z0ev1 _60z0ev2 _60z0ev0" style={{ flex: "1 1 0%", "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>기본계좌</span>
                </div>
                <select className="tw6g-oyza72o" aria-hidden="true" tabIndex={-1} defaultValue="1">
                    <option value="1"></option>
                </select>
            </section>
            <AccountInfomationButton CURRENCY_ITEMS={CURRENCY_ITEMS} />
        </div>
    )
}

function AccountInfomationButton({ CURRENCY_ITEMS }) {
    return (
        <div className="_1ovjuf13">
            <div role="radiogroup" aria-required="false" dir="ltr" className="tw6g-1sni4y90 tw6g-1sni4y91 tw6g-1sni4y95 _1ovjuf16" aria-label="통화변경 달러" tabIndex={0} style={{ outline: "none" }} data-scrollable="false">
                <div className="tw6g-1sni4y97 tw6g-1sni4y98" style={{ boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px", width: "20px", transform: "translateX(20px)" }}></div>
                {CURRENCY_ITEMS.map((item) => (
                    <button key={item.label} type="button" role="radio" aria-checked={item.checked} data-state={item.state} value={item.value} className="tw6g-1cq3gqg0 tw6g-1cq3gqg2" data-seg-state={item.state} tabIndex={item.tabIndex} data-radix-collection-item>
                        <div className="tw6g-1cq3gqg3 tw6g-1cq3gqg4">
                            <span className="tw6g-17xiat90 tw6g-17xiat91 tw6g-1cq3gqge" aria-hidden="false" role="presentation" style={{ height: "12px", width: "12px", minWidth: "12px", color: item.color }}>
                                {item.icon}
                            </span>
                        </div>
                    </button>
                ))}
            </div>

            <button type="button" className="tw6g-emtxt715 tw6g-emtxt7n tw6g-emtxt7u tw6g-emtxt710" tabIndex={0} aria-disabled="false" aria-label="framer-tds-desktop-icon-button-label-icon-dots-mono" data-theme="grey" data-variant="fill" data-mode="dark" id="radix-_r_pq_" aria-haspopup="menu" aria-expanded="false" data-state="closed">
                <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px" }}>
                    <svg fill="none" height="24" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
                        <path clipRule="evenodd" d="m5.23 14c-.26271-.0001-.52284-.0519-.76552-.1525-.24269-.1006-.46319-.248-.6489-.4338-.18572-.1858-.33302-.4064-.4335-.6491-.10047-.2427-.15215-.5029-.15208-.7656.00006-.2627.05187-.5228.15247-.7655.10059-.2427.248-.4632.43381-.6489s.40638-.333.64912-.4335.50289-.15217.7656-.1521c.53056.00013 1.03934.211 1.41442.5863.37507.3752.58571.8841.58558 1.4147-.00014.5306-.21103 1.0393-.58629 1.4144s-.88415.5857-1.41471.5856zm6.771 0c-.5304 0-1.0391-.2107-1.4142-.5858s-.5858-.8838-.5858-1.4142.2107-1.0391.5858-1.4142.8838-.5858 1.4142-.5858 1.0391.2107 1.4142.5858.5858.8838.5858 1.4142-.2107 1.0391-.5858 1.4142-.8838.5858-1.4142.5858zm6.77 0c-.2627-.0001-.5228-.0519-.7655-.1525s-.4632-.248-.6489-.4338-.333-.4064-.4335-.6491-.1522-.5029-.1521-.7656.0519-.5228.1525-.7655.248-.4632.4338-.6489.4064-.333.6491-.4335.5029-.15217.7656-.1521c.5306.00013 1.0393.211 1.4144.5863.3751.3752.5857.8841.5856 1.4147s-.211 1.0393-.5863 1.4144-.8841.5857-1.4147.5856z" fill="#8b95a1" fillRule="evenodd" />
                    </svg>
                </span>
            </button>
        </div>
    )
}