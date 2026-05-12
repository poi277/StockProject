import { useEffect, useRef } from 'react';
import useCancelStore from '../../../../../store/cancelStore';
import './SideBarCancelForm.css'

export default function SideBarCancelForm() {
    const { cancelOpen, cancelType, cancelTarget, closeCancel, executeCancel } = useCancelStore();
    const popoverRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (popoverRef.current && !popoverRef.current.contains(e.target)) {
                closeCancel();
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    if (!cancelOpen || cancelType !== 'sidebar') return null;

    return (
        <>
            <div ref={popoverRef}>
                <div data-radix-popper-content-wrapper style={{ position: "fixed", left: "0px", top: "0px", transform: "translate(1299px, 823px)", minWidth: "max-content", "--radix-popper-transform-origin": "250px 0%", zIndex: 1, "--radix-popper-available-width": "1529px", "--radix-popper-available-height": "913px", "--radix-popper-anchor-width": "297px", "--radix-popper-anchor-height": "49px" }}>
                    <div data-side="left" data-align="start" data-state="open" role="dialog" id="radix-_r_16d_" aria-labelledby="radix-_r_16b_" aria-describedby="radix-_r_16c_" className="tw6g-gduhvu0 tw6g-1h3jdxo4 tw6g-1h3jdxo6 tw6g-1h3jdxo5 dgtq08" tabIndex={-1} style={{ "--tw6g-1h3jdxo0": "10px", "--radix-popover-content-transform-origin": "var(--radix-popper-transform-origin)", "--radix-popover-content-available-width": "var(--radix-popper-available-width)", "--radix-popover-content-available-height": "var(--radix-popper-available-height)", "--radix-popover-trigger-width": "var(--radix-popper-anchor-width)", "--radix-popover-trigger-height": "var(--radix-popper-anchor-height)", opacity: 1, transform: "none" }}>
                        <div className="tw6g-1r5dc8g0" id="radix-_r_16b_" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-bold)", "--tds-wts-foreground-color": "var(--wts-adaptive-grey800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "15px" }}>
                            {cancelTarget?.stockName} {cancelTarget?.tradeType === 'BUY' ? '구매' : '판매'}를 취소할까요?
                        </div>
                        <div style={{ flex: "0 0 auto", height: "16px" }}></div>
                        <button onClick={executeCancel} type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka50 tw6g-1wkoka541 tw6g-1wkoka5f tw6g-1wkoka519 tw6g-1wkoka5z tw6g-1wkoka5s tw6g-1wkoka5n tw6g-1wkoka52a tw6g-1h3jdxoa tw6g-1h3jdxob" data-tds-wts-button data-tds-wts-popover-button>
                            <span className="tw6g-1wkoka52g">취소하기</span>
                        </button>
                    </div>
                </div>
            </div>
            <span data-radix-focus-guard tabIndex={0} style={{ outline: "none", opacity: 0, position: "fixed", pointerEvents: "none" }}></span>
        </>
    )
}