import { Fragment } from "react"
import useAccountMoney from "./useAccountMoney"

export default function AccountMoney() {
    const { MONEY_ITEMS } = useAccountMoney()
    
    return (
        <div style={{ display: "flex", flexDirection: "column", gap: "0px", justifyContent: "normal", alignItems: "normal" }}>
            <div className="_1rg3zxe1">
                {MONEY_ITEMS.map((item, index) => (
                    <Fragment key={item.label}>
                        {index === 1 && <div className="_1rg3zxe2" style={{ "--_1rg3zxe0": "linear-gradient(180deg, color-mix(in srgb, var(--wts-adaptive-greyOpacity100) 0%, transparent) 0%, color-mix(in srgb, var(--wts-adaptive-greyOpacity200) 80%, transparent) 25%, color-mix(in srgb, var(--wts-adaptive-greyOpacity200) 80%, transparent) 75%, color-mix(in srgb, var(--wts-adaptive-greyOpacity100) 0%, transparent) 100%)" }}></div>}
                        <a type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka58 tw6g-1wkoka541 tw6g-1wkoka5g tw6g-1wkoka51b tw6g-1wkoka511 tw6g-1wkoka5r tw6g-1wkoka5p tw6g-1wkoka52c _1rg3zxe3" data-tds-wts-button href={item.href} style={{ borderRadius: item.borderRadius }}>
                            <span className="tw6g-1wkoka52g">
                                <div style={{ display: "flex", flexDirection: "column", gap: "0px", justifyContent: "normal", alignItems: "normal" }}>
                                    <div style={{ display: "flex", flexDirection: "row", gap: "2px", justifyContent: "normal", alignItems: "center" }}>
                                        <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-medium)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity600)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>{item.label}</span>
                                    </div>
                                    <span className="tw6g-1r5dc8g0 _60z0ev0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{item.value}</span>
                                </div>
                            </span>
                        </a>
                    </Fragment>
                ))}
            </div>
        </div>
    )
}