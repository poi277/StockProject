import './HeaderProfile.css'
import useHeader from './useHeader';
export default function HeaderProfile({ onClose })
{

    const {user,handleLogout} = useHeader();

    return(
        <>
            <div data-radix-popper-content-wrapper style={{ position: "fixed", left: "0px", top: "0px", transform: "translate(1180px, 42px)", minWidth: "max-content", "--radix-popper-transform-origin": "100% 0px", zIndex: "auto", "--radix-popper-available-width": "1870px", "--radix-popper-available-height": "891px", "--radix-popper-anchor-width": "32px", "--radix-popper-anchor-height": "32px" }}>
                <div data-side="bottom" data-align="end" data-state="open" role="dialog" id="radix-_r_a_" aria-labelledby="radix-_r_8_" aria-describedby="radix-_r_9_" className="tw3s-gduhvu0 tw3s-1h3jdxo4 tw3s-1h3jdxo7 tw3s-1h3jdxo5 _1gwjmki4" tabIndex={-1} style={{ "--tw3s-1h3jdxo0": "8px", "--radix-popover-content-transform-origin": "var(--radix-popper-transform-origin)", "--radix-popover-content-available-width": "var(--radix-popper-available-width)", "--radix-popover-content-available-height": "var(--radix-popper-available-height)", "--radix-popover-trigger-width": "var(--radix-popper-anchor-width)", "--radix-popover-trigger-height": "var(--radix-popper-anchor-height)", opacity: 1, transform: "none" }}>
                    <div className="_1gwjmki7">
                        <header className="_1gwjmki5">
                            <div className="_1gwjmki8">
                                <div className="_1gwjmkia _1gwjmkic css-ry5kze">
                                    <div className="css-1vqadhc" style={{ width: "40px", height: "40px", borderRadius: "12px", backgroundColor: "transparent" }}>
                                        <img aria-hidden="true" draggable="false" className="css-zl34r3" src="https://static.toss.im/illusts/img-profile-emoji-09.png" style={{ "--asset-object-fit": "contain", "--asset-scale": "1", objectFit: "contain" }} />
                                    </div>
                                </div>
                            </div>
                            <div className="tw3s-1wmz9lv0">
                                <div className="tw3s-1wmz9lv1">
                                    <span className="tw3s-1r5dc8g0 tw3s-1pv95pb0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "17px" }}>허준찬</span>
                                </div>
                            </div>
                        </header>
                        <div className="_1qnnmir0 _1qnnmir2"></div>
                        <section className="ul8b8q0">
                            <ul role="menu" className="u18b8q1">
                                 <HeaderButton isLink={true} label="내 정보" tag="내_정보" />
                                 <HeaderButton isLink={true} label="내 커뮤니티 프로필" tag="내_커뮤니티_프로필" />
                                 <HeaderButton isLink={false} label="로그아웃" tag="로그아웃" onClick={handleLogout} />
                            </ul>
                        </section>
                    </div>
                </div>
            </div>
        </>
    )
}

function HeaderButton({ isLink, label, tag, onClick }) {
    return (
        <button className="ul8b8q2" role="menuitem" onClick={onClick}>
            <div className="tw3s-1e8fj1a2 tw3s-1e8fj1a0 tw3s-1e8fj1aj tw3s-1e8fj1ak ul8b8q3">
                <div className="tw3s-1e8fj1a9">
                    <HeaderBar label={label} />
                    {isLink ? <HeaderLink /> : null}
                </div>
            </div>
        </button>
    )
}

function HeaderBar({ label }) {
    return (
        <div className="tw3s-1e8fj1aa tw3s-1e8fj1ad tw3s-1e8fj1ab tw3s-1e8fj1af" style={{ gridTemplateColumns: "minmax(0px, 1fr)" }}>
            <span className="tw3s-1e8fj1am">
                <div className="tw3s-1ia8ofc0 tw3s-1ia8ofc1">
                    <span className="tw3s-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey0pacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{label}</span>
                </div>
            </span>
        </div>
    )
}

function HeaderLink()
{
    return(
        <div className="tw3s-1e8fj1ap">
            <div className="tw3s-1si66yh0">
                <span className="tw3s-17xiat90" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px" }}>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" className="line-icon">
                        <path d="M 5.75 13.4 c -0.23 0 -0.46 -0.09 -0.64 -0.26 a 0.9 0.9 0 0 1 0 -1.27 L 8.98 8 L 5.11 4.14 a 0.9 0.9 0 0 1 0 -1.27 a 0.9 0.9 0 0 1 1.27 0 l 4.5 4.5 a 0.9 0.9 0 0 1 0 1.27 l -4.5 4.5 c -0.17 0.17 -0.4 0.26 -0.63 0.26" fillRule="evenodd" clipRule="evenodd" fill="#b0b6c1" />
                    </svg>
                </span>
            </div>
        </div>
    )
}