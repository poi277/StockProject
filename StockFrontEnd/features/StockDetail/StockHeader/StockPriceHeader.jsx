import StockHeaderName from "./StockHeaderName/StockHeaderName";
import StockHeaderPrice from "./StockHeaderName/StockHeaderPrice";
import StockHeaderTabs from "./StockHeaderTabs/StockHeaderTabs";
import '../../../tossCss/toss-layout.css'
import './StockPriceHeader.css'
//css완료중
export default function StockPriceHeader() {
  return (
    /* 이미지 최상단: div class="ia3qp41" */
    <div 
      className="ia3qp41" style={{ display: 'flex', flexDirection: 'row', gap: '0px', justifyContent: 'normal', alignItems: 'center' }}
    >
      {/* 1. 종목명/아이콘 영역: div class="ia3qp42" */}
      <div className="ia3qp42" style={{ display: 'flex' }}>
        {/* 1. 종목 아이콘/로고 영역: div class="ia3qp43" */}
          <StockHeaderName/>
        {/* 2. 숨겨진 구분선 혹은 스페이서: div class="_1sivumi7" */}
        <div className="_1sivumi7" style={{ visibility: 'hidden'}}></div>
        {/* 3. 종목가격: div class="_1sivumi0 _1sivumi2" */}
          <StockHeaderPrice/>
      </div>

      {/* 2. 가격/등락 정보 영역: div class="_8u2t3p0" */}
      <div className="_8u2t3p0" style={{ display: 'flex' }}>
          <StockHeaderTabs/>
      </div>

      <div className="njzdl36" >
      {/* 1. 지정가 알림 설정 버튼 */}
            <button 
              className="tw3v-emtxt715 tw3v-emtxt7o tw3v-emtxt7t tw3v-emtxt710" 
              aria-disabled="false" 
              aria-label="지정가 알림 설정" 
              data-theme="grey" 
              data-variant="weak" 
              data-mode="dark" 
              data-tossinvest-log="IconButton" 
              data-contents-value="지정가 알림 설정" 
              data-content-tag="지정가_알림_설정" 
              aria-haspopup="dialog" 
              aria-expanded="false" 
              aria-controls="radix-_r_4fr_" 
              data-state="closed" 
              data-contents-label="[object Object]" 
              data-contents-label-code="child" 
              data-parent-name="PopoverTrigger$1" 
              data-tossinvest-priority-log="Popover.Trigger"
              type="button"
            >
              <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px' }}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" className="line-icon">
                  <path fill="#B0B8C1" d="M10.58 13.294a2.355 2.355 0 11-4.71 0h4.71zm-7.738-6.81a5.384 5.384 0 015.742-5.372c2.86.186 5.025 2.69 5.025 5.557v2.728l.916 1.586a.686.686 0 01-.594 1.03H2.52a.686.686 0 01-.594-1.03l.916-1.586V6.484z" fillRule="evenodd" />
                </svg>
              </span>
            </button>

            {/* 2. 관심 종목 해제하기/설정하기 버튼 */}
            <button 
              className="tw3v-emtxt715 tw3v-emtxt7o tw3v-emtxt7y tw3v-emtxt712" 
              aria-disabled="false" 
              aria-label="관심 종목 해제하기" 
              data-theme="grey" 
              data-variant="weak" 
              data-mode="dark" 
              data-tossinvest-log="IconButton" 
              data-contents-value="관심 종목 해제하기" 
              data-content-tag="isWatching_관심_종목_해제하기_관심_종목_설정하기" 
              data-parent-name="WatchActionGroupSelect" 
              data-state="closed" 
              data-tossinvest-priority-log="Tooltip.Trigger"
              type="button"
            >
              <span className="tw3v-17xiat90" aria-hidden="false" role="presentation" style={{ height: '12px', width: '12px', minWidth: '12px' }}>
                <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path d="m22.223 5.572c-1.107-1.842-2.963-2.94-4.966-2.94-2.969 0-4.549 1.865-5.257 3.062-.708-1.197-2.288-3.062-5.257-3.062-2.003 0-3.858 1.099-4.966 2.94-1.329 2.211-1.317 5.047.031 7.586 1.973 3.714 6.359 6.977 8.798 8.59.424.28.908.421.1394.421s.97-.141 1.394-.421c2.438-1.613 6.825-4.876 8.798-8.59 1.349-2.539 1.36-5.375.031-7.586z" fill="#e9323e" />
                </svg>
              </span>
            </button>

            <div role="radiogroup" ria-required="false" 
              dir="ltr" 
              className="tw3v-1sni4y90 tw3v-1sni4y92 tw3v-1sni4y95" 
              aria-label="통화변경" 
              data-tossinvest-log="CurrencyToggleButton" 
              data-parent-name="Actions" 
              data-skip="true" 
              data-contents-value="통화 전환" 
              data-content-tag="통화_전환" 
              tabIndex="0" 
              style={{ outline: 'none', dataScrollable: 'false' }}
            >
              {/* 토글 배경 슬라이더 애니메이션 div */}
              <div className="tw3v-1sni4y97 tw3v-1sni4y99" style={{ boxShadow: 'rgba(0, 0, 0, 0.15) 0px 1px 3px 0px', width: '28px', transform: 'translateX(28px)' }}></div>

              {/* 1. 왼쪽 버튼 (달러) */}
              <button 
                type="button" 
                role="radio" 
                aria-checked="false" 
                data-state="unchecked" 
                value="left" 
                className="tw3v-1cq3ggq0 tw3v-1cq3ggq2" 
                data-seg-state="unchecked" 
                data-tossinvest-log="SegmentedControl.Item" 
                data-contents-value="달러" 
                data-content-tag="달러" 
                data-parent-name="CurrencyToggleButton" 
                tabIndex="-1" 
                data-radix-collection-item=""
              >
                <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5" style={{ display: 'flex' }}>
                  <span className="tw3v-17xiat90 tw3v-17xiat91 tw3v-1cq3gqge" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px', color: 'var(--wts-adaptive-greyOpacity400)' }}>
                    <svg enableBackground="new 0 0 24 24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path 
                          d="m 10.719 18.885 c -2.329 -0.372 -4.041 -1.691 -4.424 -3.658 l 2.744 -0.755 c 0.404 1.382 1.648 2.169 3.36 2.169 c 1.467 0 2.552 -0.776 2.552 -1.829 c 0 -0.851 -0.553 -1.351 -1.691 -1.595 l -2.956 -0.627 c -2.393 -0.51 -3.722 -1.903 -3.722 -3.903 c 0 -2.042 1.68 -3.605 4.137 -4.009 v -2.328 h 2.531 v 2.339 c 2.116 0.351 3.69 1.606 4.222 3.456 l -2.658 0.723 c -0.255 -1.159 -1.446 -1.978 -2.924 -1.978 c -1.51 0 -2.541 0.712 -2.541 1.776 c 0 0.659 0.638 1.255 1.648 1.467 l 2.988 0.627 c 2.403 0.5 3.722 1.914 3.722 3.913 c 0 2.286 -1.733 3.903 -4.456 4.254 v 2.722 h -2.531 v -2.764 Z" 
                          style={{ fill: 'rgb(176, 184, 193)' }} 
                        />
                    </svg>
                  </span>
                </div>
              </button>

              {/* 2. 오른쪽 버튼 (원) - 현재 선택됨(checked) */}
              <button 
                type="button" 
                role="radio" 
                aria-checked="true" 
                data-state="checked" 
                value="right" 
                className="tw3v-1cq3ggq0 tw3v-1cq3ggq2" 
                data-seg-state="checked" 
                data-tossinvest-log="SegmentedControl.Item" 
                data-contents-value="원" 
                data-content-tag="원" 
                data-parent-name="CurrencyToggleButton" 
                tabIndex="0" 
                data-radix-collection-item=""
              >
                <div className="tw3v-1cq3ggq3 tw3v-1cq3gqg5" style={{ display: 'flex' }}>
                  <span className="tw3v-17xiat90 tw3v-17xiat91 tw3v-1cq3ggqe" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px', color: 'var(--wts-adaptive-greyOpacity800)' }}>
                    <svg enableBackground="new 0 0 24 24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <g fill="#b0b8c1">
                        <path d="m 7.363 9.498 c 0.671 0.253 1.438 0.38 2.299 0.38 s 1.621 -0.127 2.28 -0.38 s 1.172 -0.614 1.539 -1.083 c 0.38 -0.481 0.57 -1.032 0.57 -1.653 c 0 -0.633 -0.19 -1.184 -0.57 -1.653 c -0.367 -0.469 -0.88 -0.836 -1.539 -1.102 s -1.419 -0.399 -2.28 -0.399 s -1.628 0.133 -2.299 0.399 c -0.659 0.266 -1.172 0.633 -1.539 1.102 s -0.551 1.02 -0.551 1.653 c 0 0.621 0.184 1.172 0.551 1.653 c 0.367 0.469 0.88 0.83 1.539 1.083 Z m 0.855 -3.724 c 0.38 -0.215 0.861 -0.323 1.444 -0.323 c 0.405 0 0.754 0.051 1.045 0.152 s 0.519 0.247 0.684 0.437 s 0.247 0.431 0.247 0.722 c 0 0.418 -0.184 0.741 -0.551 0.969 c -0.355 0.215 -0.83 0.323 -1.425 0.323 c -0.583 0 -1.064 -0.108 -1.444 -0.323 c -0.367 -0.228 -0.551 -0.551 -0.551 -0.969 c 0 -0.443 0.184 -0.773 0.551 -0.988 Z" />
                        <path d="m 13.044 13.222 v 1.729 h 3.306 v 1.387 h 2.527 v -13.338 h -2.527 v 10.222 Z"/>
                        <path d="m 8.731 18.276 v -2.394 h 2.451 v -3.303 c 0.115 -0.008 0.227 -0.013 0.342 -0.022 c 1.305 -0.114 2.584 -0.279 3.838 -0.494 l -0.171 -1.805 c -1.216 0.152 -2.483 0.266 -3.8 0.342 s -2.609 0.127 -3.876 0.152 c -1.267 0.013 -2.438 0.025 -3.515 0.038 l 0.323 2.014 c 1.051 -0.013 2.191 -0.032 3.42 -0.057 c 0.3 -0.006 0.608 -0.022 0.912 -0.033 v 2.351 h -2.451 v 3.211 v 0.551 v 1.482 h 13.072 v -2.033 Z" />
                      </g>
                    </svg>
                  </span>
                </div>
              </button>
            </div>
       </div>
    </div>
  );
}