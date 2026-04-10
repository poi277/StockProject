import "./StockTabSelection.css"

export default function StockTabSelection() {
  return (
    <div className="_1pn0hfp0" style={{ position: 'sticky', top: '81px', zIndex: 10 }}>
      <div dir="ltr" data-orientation="horizontal" className="tw3v-336bzib tw3v-336bzif">
        <div className="tw3v-336bzix">
          <div role="tablist" aria-orientation="horizontal" className="tw3v-336bzih" tabIndex="0" data-orientation="horizontal" style={{ outline: 'none' }} data-scrollable="false">
            <div className="tw3v-336bziw tw3v-336bziv" style={{ width: '86px', transform: 'none' }}></div>
            
            {/* 차트·호가 탭 (Active) */}
            <button type="button" role="tab" aria-selected="true" aria-controls="radix-_r_4l6_-content-order" data-state="active" id="radix-_r_4l6_-trigger-order" data-tossinvest-log="Tab.Item" data-contents-value="차트 · 호가" data-content-tag="get" className="tw3v-336bzit tw3v-336bzin _1pn0hfp1" tabIndex="-1" data-orientation="horizontal" data-radix-collection-item="">
              <div style={{ position: 'relative' }}>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii tw3v-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  차트 · 호가
                </span>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii" data-tds-wts-tab-item="true" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  차트 · 호가
                </span>
              </div>
            </button>

            {/* 나머지 탭 버튼들 (Inactive) */}
            <button type="button" role="tab" aria-selected="false" aria-controls="radix-_r_4l6_-content-analytics" data-state="inactive" id="radix-_r_4l6_-trigger-analytics" data-tossinvest-log="Tab.Item" data-contents-value="종목정보" data-content-tag="get" className="tw3v-336bzit tw3v-336bzin _1pn0hfp1" tabIndex="-1" data-orientation="horizontal" data-radix-collection-item=""> 
                <div style={{ position: 'relative' }}>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii tw3v-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  종목정보
                </span>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii" data-tds-wts-tab-item="true" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  종목정보
                </span>
              </div>
              </button>
            <button type="button" role="tab" aria-selected="false" aria-controls="radix-_r_4l6_-content-news" data-state="inactive" id="radix-_r_4l6_-trigger-news" data-tossinvest-log="Tab.Item" data-contents-value="뉴스 · 공시" data-content-tag="get" className="tw3v-336bzit tw3v-336bzin _1pn0hfp1" tabIndex="-1" data-orientation="horizontal" data-radix-collection-item="">
                <div style={{ position: 'relative' }}>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii tw3v-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  뉴스 · 공시
                </span>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii" data-tds-wts-tab-item="true" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  뉴스 · 공시
                </span>
              </div>
            </button>
            <button type="button" role="tab" aria-selected="false" aria-controls="radix-_r_4l6_-content-transaction-status" data-state="inactive" id="radix-_r_4l6_-trigger-transaction-status" data-tossinvest-log="Tab.Item" data-contents-value="거래현황" data-content-tag="get" className="tw3v-336bzit tw3v-336bzin _1pn0hfp1" tabIndex="-1" data-orientation="horizontal" data-radix-collection-item="">
                <div style={{ position: 'relative' }}>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii tw3v-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  거래현황
                </span>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii" data-tds-wts-tab-item="true" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  거래현황
                </span>
              </div>
            </button>
            <button type="button" role="tab" aria-selected="false" aria-controls="radix-_r_4l6_-content-community" data-state="inactive" id="radix-_r_4l6_-trigger-community" data-tossinvest-log="Tab.Item" data-contents-value="커뮤니티" data-content-tag="get" className="tw3v-336bzit tw3v-336bzin _1pn0hfp1" tabIndex="-1" data-orientation="horizontal" data-radix-collection-item="">
                <div style={{ position: 'relative' }}>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii tw3v-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  커뮤니티
                </span>
                <span className="tw3v-1r5dc8g0 tw3v-336bzij tw3v-336bzii" data-tds-wts-tab-item="true" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': 1.45, '--tds-wts-font-size': '14px' }}>
                  커뮤니티
                </span>
              </div>
            </button>
          </div>
        </div>
      </div>

      {/* 화면 편집 버튼 영역 */}
      <div>
        <div className="_1f14oz20">
          <button type="button" tabIndex="0" aria-disabled="false" aria-controls="radix-_r_6eq_" className="tw3v-1wkoka52h tw3v-1wkoka52h tw3v-1wkoka5a tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528 tw3v-1wkoka53j _1pd1ca40" data-tds-wts-button="true" data-tossinvest-log="Button" data-contents-label="화면 편집" data-contents-label-code="화면 편집" data-contents-value="화면 편집" data-content-tag="화면_편집" data-parent-name="CustomScreenButton" data-dismiss-chart-order-dialog="true" id="radix-_r_4lt_" aria-haspopup="menu" aria-expanded="false" data-state="closed" data-tossinvest-priority-log="Dropdown.Trigger">
            <span className="tw3v-1wkoka52g">화면 편집</span>
            <div className="tw3v-1wkoka532 tw3v-1wkoka52s tw3v-1wkoka51z">
              <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px' }}>
                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                  <path d="m3.691 5.746 4.309 4.355 4.309-4.355" fill="none" stroke="#8f959e" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
                </svg>
              </span>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}