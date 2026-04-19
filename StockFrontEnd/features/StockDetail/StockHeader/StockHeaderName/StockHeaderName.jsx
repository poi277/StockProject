import './StockHeaderName.css'

export default function StockHeaderName({stock}) {
  console.log(stock)
  return (
      <div className="ia3qp43">
      {/* 1. 로고 이미지 영역 */}
      <div className="tw3v-ig4sg23 tw3v-ig4sg24" style={{ backgroundColor: 'var(--wts-adaptive-greyOpacity100)', '--tw3v-ig4sg20': '20px', '--tw3v-ig4sg21': '20px', '--tw3v-ig4sg22': '50%' }}>
        <div className="tw3v-ig4sg27 tw3v-ig4sg2a" style={{ transform: 'scale(1)' }}>
          <img 
            referrerPolicy="no-referrer" 
            draggable="false" 
            alt="logo" 
            loading="lazy" 
            width="20" 
            height="20"
            decoding="async"
            data-naimg="1"
            src="https://images.tossinvest.com/https%3A%2F%2Fstatic.toss.im%2Fpng-icons%2Fsecurities%2Ficn-sec-fill-NASOVDJMJ-E0.png?width=48&height=48" 
            style={{ color: 'transparent' }} 
          />
        </div>
      </div>

      {/* 2. 종목명 및 티커 영역 */}
      <div className="ia3qp44">
        <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', 
            '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>
          {stock.stockName}
        </span>
        <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)',
             '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>
          {stock.stockCode}
        </span>
      </div>

      {/* 3. 메모 작성 버튼 영역 */}
      <div className="_196hb70" style={{transform: 'none', transformOrigin: '50% 50% 0px' }}>
        <button 
          className="tw3v-nxr2vw0 tw3v-nxr2vwa tw3v-nxr2vwg tw3v-nxr2vw4" 
          aria-disabled="false" 
          tabIndex="0" 
          type="button" 
          data-tds-wts-chip-variant="default"
          style={{ cursor: 'pointer' }}
        >
          <span className="tw3v-nxr2vw1 contents">
            메모작성
          </span>
        </button>
      </div>

      {/* 4. 경고 공지 영역 (상장폐지 위험 등) */}
      <div className="ia3qp46">
        <StockNoticeButton />
      </div>
    </div>
  );
}

function StockNoticeButton() {
  return (
    <div className="_3wzkxk0">
      <button 
        className="tw3v-nxr2vw0 tw3v-nxr2vwa tw3v-nxr2vwe tw3v-nxr2vw4 _3wzkxk2 _3wzkxk4" 
        aria-disabled="false" 
        tabIndex="0" 
        type="button"
        data-tds-wts-chip-variant="default"
        data-tossinvest-log="Chip"
        data-contents-value="이 주식은 상장폐지될 위험이 있어요"
        data-content-tag="notice_title"
        data-parent-name="StockNoticesV2"
        style={{ cursor: 'pointer' }}
      >
        <span className="tw3v-17xiat94 tw3v-17xiat92" style={{ display: 'flex' }}>
          <span 
            className="tw3v-17xiat90" 
            aria-hidden="false" 
            role="presentation" 
            style={{ height: '12px', width: '12px', minWidth: '12px', color: 'var(--wts-adaptive-greyOpacity500)' }}
          >
            {/* 이미지 [image_7ac7e1.png]의 SVG 아이콘 경로 완벽 반영 */}
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="">
              <g fill="none" fillRule="evenodd">
                <path 
                  fill="#EE4452" 
                  d="M10.268 2.913L.74 19.416c-.771 1.333.192 3 1.73 3h21.53c1.539 0 2.502-1.667 1.73-3L13.734 2.913c-.771-1.333-2.694-1.333-3.465 0"
                />
                <path 
                  fill="#FFF" 
                  d="M12 16.583a1.2 1.2 0 100 2.4 1.2 1.2 0 000-2.4m0-1.683a.9.9 0 01-.9-.9V9a.9.9 0 011.8 0v5a.9.9 0 01-.9.9"
                />
              </g>
            </svg>
          </span>
        </span>
        
        {/* 경고 문구 텍스트 */}
        <span className="tw3v-nxr2vw1 contents">
          <span>이 주식은 상장폐지될 위험이 있어요</span>
        </span>
      </button>
    </div>
  );
}