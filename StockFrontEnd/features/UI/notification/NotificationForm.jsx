import './NotificationForm.css'
import useNotification from './useNotification'

export default function NotificationForm() {

    const { notifications } = useNotification()
    if (!notifications.length) return null;


    return (
        <div className="tw6g-mnwhon1 tw6g-mnwhon0" style={{ paddingTop: "52px" }}>
            <div className="tw6g-mnwhon4 tw6g-mnwhon3" role="section" style={{ position: "relative", height: "64px", width: "calc(24px + min(-48px + 100vw, 400px))", transform: "translateY(-12px)" }}>
                {notifications.map((notification, index) => (
                    <div key={notification.id} className="tw6g-mnwhon6" style={{ transformOrigin: "center top", zIndex: 10001 - index, top: "12px", transform: `translateY(${index * 52}px)`, opacity: 1, height: "40px" }}>
                        <div className="tw6g-ff6axk0 tw6g-ff6axk1" role="status">
                            <div className="tw6g-ff6axk6">
                                <span className="tw6g-17xiat90" aria-hidden="false" role="presentation" style={{ height: "18px", width: "18px", minWidth: "18px" }}>
                                    <svg enableBackground="new 0 0 144 144" viewBox="0 0 144 144" xmlns="http://www.w3.org/2000/svg">
                                        <path clipRule="evenodd" d="m138 72c0 36.449-29.551 66-66 66s-66-29.551-66-66 29.551-66 66-66 66 29.551 66 66" fill="#15c07e" fillRule="evenodd" />
                                        <path d="m45 70.055 20.496 20.496 33.504-33.504" fill="none" stroke="#fff" strokeLinecap="round" strokeLinejoin="round" strokeWidth="12" />
                                    </svg>
                                </span>
                            </div>
                            <div className="tw6g-ff6axk2 tw6g-ff6axk3">
                                <div className="tw6g-1r5dc8g0 tw6g-ff6axk5" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "rgba(255,255,255,1)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{notification.text}</div>
                            </div>
                        </div>
                    </div>
                ))}
                {/* <div className="tw6g-mnwhon6" style={{ transformOrigin: "center top", zIndex: 10000, top: "12px", transform: "translateY(52px)", opacity: 1, height: "40px" }}>
                        <div className="tw6g-ff6axk0 tw6g-ff6axk1" role="status">
                            <div className="tw6g-ff6axk6">
                                <span className="tw6g-17xiat90" aria-hidden="false" role="presentation" style={{ height: "18px", width: "18px", minWidth: "18px" }}>
                                    <svg enableBackground="new 0 0 144 144" viewBox="0 0 144 144" xmlns="http://www.w3.org/2000/svg">
                                        <path clipRule="evenodd" d="m138 72c0 36.449-29.551 66-66 66s-66-29.551-66-66 29.551-66 66-66 66 29.551 66 66" fill="#15c07e" fillRule="evenodd" />
                                        <path d="m45 70.055 20.496 20.496 33.504-33.504" fill="none" stroke="#fff" strokeLinecap="round" strokeLinejoin="round" strokeWidth="12" />
                                    </svg>
                                </span>
                            </div>
                            <div className="tw6g-ff6axk2 tw6g-ff6axk3">
                                <div className="tw6g-1r5dc8g0 tw6g-ff6axk5" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "rgba(255,255,255,1)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>구매 주문 취소</div>
                            </div>
                        </div>
                    </div> */}
            </div>
        </div>
    )
}