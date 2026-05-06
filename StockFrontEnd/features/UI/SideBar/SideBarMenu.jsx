import useSideBarMenu from "./useSideBarMenu"

export default function SideBarMenu() {
    const {NAV_ITEMS, ICON_BUTTONS } = useSideBarMenu()

    return (
        <div className="gxe9ll0 _1kestwgs _1kestwg4 gxe9ll2 _17lx8ak1" data-section-name="탭바" data-tabster='{"mover":{"cyclic":false,"direction":1,"memorizeCurrent":true,"tabbable":true}}' style={{ display: "flex", flexDirection: "column", gap: "0px", justifyContent: "normal", alignItems: "center", visibility: "visible" }}>
            <i tabIndex={0} role="none" data-tabster-dummy aria-hidden="true" style={{ position: "fixed", height: "1px", width: "1px", opacity: 0.001, zIndex: -1, contentVisibility: "hidden", top: "0px", left: "0px" }}></i>
            <nav className="gxe9ll4">
                <button data-tossinvest-log="button" data-contents-value="리모콘 닫기" data-content-tag="collapsed_리모콘_열기_리모콘_닫기" data-parent-name="CollapseToggle" aria-label="리모콘 닫기" className="gxe9ll5 gxe9ll7" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" style={{ width: "24px", height: "48px" }}>
                    <div className="gxe9ll6">
                        <span className="tw6g-17xiat90 tw6g-17xiat91 _2v0cg80 _1kestwgl" aria-hidden="true" role="presentation" style={{ height: "26px", width: "26px", minWidth: "26px", color: "inherit" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><g fill="#b0b8c1" fillRule="evenodd"><path d="m6.353 17.45c-.2 0-.5-.1-.6-.3-.4-.4-.4-.9 0-1.3l3.8-3.8-3.9-3.9c-.4-.4-.4-.9 0-1.3s.9-.4 1.3 0l4.5 4.5c.4.4.4.9 0 1.3l-4.5 4.5c-.2.2-.4.3-.6.3" /><path d="m13.247 17.45c-.2 0-.5-.1-.6-.3-.4-.4-.4-.9 0-1.3l3.8-3.8-3.9-3.9c-.4-.4-.4-.9 0-1.3s.9-.4 1.3 0l4.5 4.5c.4.4.4.9 0 1.3l-4.5 4.5c-.2.2-.4.3-.6.3" /></g></svg>
                        </span>
                    </div>
                </button>

                {NAV_ITEMS.map((item, index) =>
                    item.hr ? <hr key={index} className="gxe9ll8" /> : (
                        <a key={item.label} className={`tw6g-1r5dc8g0 gxe9ll5 gxe9ll7${item.active ? " active" : ""}`} data-tossinvest-log="Txt" data-contents-label={item.label} data-contents-label-code="title" data-contents-value={item.label} data-content-tag={item.tag} data-parent-name="LNBRail" href={item.href} style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>
                            <div className="gxe9ll6">
                                <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="true" role="presentation" style={{ height: "20px", width: "20px", minWidth: "20px", color: "inherit" }}>
                                    {item.icon}
                                </span>
                            </div>
                            <span>{item.label}</span>
                        </a>
                    )
                )}
            </nav>
            <div aria-hidden="true" style={{ flex: "1 1 0%", minHeight: "48px" }}></div>
            <div>
                <button className="tw6g-1r5dc8g0 gxe9ll5" data-tossinvest-log="Txt" data-contents-value="의견" data-content-tag="의견" data-parent-name="SurveyButton" aria-haspopup="dialog" aria-expanded="false" aria-controls="radix-_r_12_" data-state="closed" data-contents-label="[object Object]" data-contents-label-code="child" data-tossinvest-priority-log="Popover.Trigger" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>
                    <div className="gxe9ll6">
                        <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="true" role="presentation" style={{ height: "20px", width: "20px", minWidth: "20px", color: "inherit" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><g fill="#b8bdc5"><path d="m14.625 13.276 7.875-5.467v-1.009c0-1.657-1.343-3-3-3h-15c-1.657 0-3 1.343-3 3v1.009l7.875 5.467c2.34 1.717 3.986.82 5.25 0z" /><path d="m14.625 13.276c-1.264.82-2.91 1.717-5.25 0l-7.875-5.467v9.391c0 1.657 1.343 3 3 3h15c1.657 0 3-1.343 3-3v-9.391z" opacity=".6" /></g></svg>
                        </span>
                    </div>
                    <span className="tw6g-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-regular)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "12px" }}>의견</span>
                </button>
                <div style={{ flex: "0 0 auto", height: "12px" }}></div>
                <div style={{ display: "flex", flexDirection: "column", gap: "8px", justifyContent: "normal", alignItems: "center" }}>
                    {ICON_BUTTONS.map((btn) => (
                        <button key={btn.label} className="tw6g-emtxt715 tw6g-emtxt7o tw6g-emtxt7y tw6g-emtxt712 _1xoykgb0" aria-disabled="false" aria-label={btn.label} data-theme="grey" data-variant="weak" data-mode="dark" data-tossinvest-log={btn.log} data-contents-value={btn.label} data-content-tag={btn.tag} data-parent-name={btn.parent} data-state="closed">
                            <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="false" role="presentation" style={{ height: "18px", width: "18px", minWidth: "18px" }}>
                                {btn.icon}
                            </span>
                        </button>
                    ))}
                </div>
            </div>
            <i tabIndex={0} role="none" data-tabster-dummy aria-hidden="true" style={{ position: "fixed", height: "1px", width: "1px", opacity: 0.001, zIndex: -1, contentVisibility: "hidden", top: "0px", left: "0px" }}></i>
        </div>
    )
}