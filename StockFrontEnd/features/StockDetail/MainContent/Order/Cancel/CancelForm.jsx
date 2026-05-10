'use client'

import useCancelStore from "../../../../../store/cancelStore";

export default function CancelForm() {
    const { cancelOpen, cancelType, closeCancel, executeCancel } = useCancelStore();
    if (!cancelOpen || cancelType !== 'order') return null;

    return (
        <>
            <div className="tw6g-1ahjvge0">
                <div className="tw6g-1ahjvge5 tw6g-1ahjvge7" data-tossinvest-log="Overlay" data-parent-name="AlertDialogContent" style={{ opacity: 1 }} data-aria-hidden="true" aria-hidden="true"></div>
                <div className="tw6g-1ahjvge1" role="alertdialog" id="radix-_r_rq_" aria-describedby="radix-_r_rs_" aria-labelledby="radix-_r_rr_" data-state="open" data-overlay-type="AlertDialog" data-section-name="AlertDialog__ConfirmDialog" tabIndex={-1} style={{ opacity: 1, transform: "none", pointerEvents: "auto" }}>
                    <div className="tw6g-1ahjvge2">
                        <div className="tw6g-1r5dc8g0" id="radix-_r_rr_" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "17px" }}>구매를 취소할까요?</div>
                    </div>
                    <div className="tw6g-1ahjvge4">
                        <div dir="ltr" style={{ position: "relative", "--radix-scroll-area-corner-width": "0px", "--radix-scroll-area-corner-height": "0px", overflow: "hidden", height: "inherit" }}>
                            <style>{`[data-radix-scroll-area-viewport]{scrollbar-width:none;-ms-overflow-style:none;-webkit-overflow-scrolling:touch;}[data-radix-scroll-area-viewport]::-webkit-scrollbar{display:none}`}</style>
                            <div data-radix-scroll-area-viewport className="tw6g-15ui9mk2" style={{ overflow: "hidden scroll" }}>
                                <div style={{ minWidth: "100%", display: "table" }}></div>
                            </div>
                        </div>
                    </div>
                    <div className="tw6g-1ahjvge3">
                        <button onClick={closeCancel} type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka58 tw6g-1wkoka541 tw6g-1wkoka5f tw6g-1wkoka519 tw6g-1wkoka5z tw6g-1wkoka5s tw6g-1wkoka5n tw6g-1wkoka52a" data-tds-wts-button data-tossinvest-log="AlertDialog.CancelButton" data-parent-name="ConfirmDialog" data-contents-label="닫기" data-contents-label-code="cancelText" data-contents-value="닫기" data-content-tag="cancelText">
                            <span className="tw6g-1wkoka52g">닫기</span>
                        </button>
                        <button onClick={() => executeCancel()} type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka50 tw6g-1wkoka541 tw6g-1wkoka5f tw6g-1wkoka519 tw6g-1wkoka5z tw6g-1wkoka5s tw6g-1wkoka5n tw6g-1wkoka52a" data-tds-wts-button data-tossinvest-log="AlertDialog.CancelButton" data-parent-name="ConfirmDialog" data-contents-label="취소하기" data-contents-label-code="confirmText" data-contents-value="취소하기" data-content-tag="confirmText">
                            <span className="tw6g-1wkoka52g">취소하기</span>
                        </button>
                    </div>
                </div>
            </div>
            <span data-radix-focus-guard tabIndex={0} style={{ outline: "none", opacity: 0, position: "fixed", pointerEvents: "none" }} data-aria-hidden="true" aria-hidden="true"></span>
        </>
    )
}