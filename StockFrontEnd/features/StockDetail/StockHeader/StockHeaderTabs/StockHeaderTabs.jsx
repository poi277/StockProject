import StockHeaderGrid from "./StockHeaderGrid";
import './StockHeaderTabs.css'
export default function StockHeaderTabs() {
  return (
    <>
      {/* 1. 상단 탭 리스트 영역 (차트, 호가 등 버튼들이 위치하는 곳) */}
      <div className="_8u2t3p4 _8u2t3p2 _8u2t3p3">
        <StockLeftHeaderScrollButton/>
      </div>

      {/* 2. 스크롤 루트 영역 (가로 스크롤 대응을 위한 컨테이너) */}
      <div className="_8u2t3p1" data-scroll-root="true">
        <StockHeaderGrid/>
      </div>

      {/* 3. 하단 장식 또는 경계선 영역 */}
      <div className="_8u2t3p5 _8u2t3p2 _8u2t3p3">
        <StockHeaderRightScrollButton/>
      </div>
    </>
  );

  function StockLeftHeaderScrollButton() {
    return(
      <button 
        className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710 tw3v-emtxt716" 
        aria-disabled="false" 
        aria-label="왼쪽으로 스크롤" 
        type="button"
        data-theme="grey"
        data-variant="clear"
        data-mode="dark"
        data-tossinvest-log="IconButton"
        data-contents-value="왼쪽으로 스크롤"
        data-content-tag="왼쪽으로_스크롤"
        data-parent-name="HorizontalScrollArea"
        style={{ cursor: 'pointer' }}
      >
        <span 
          className="tw3v-17xiat90 tw3v-17xiat91" 
          aria-hidden="false" 
          role="presentation" 
          style={{ height: '14px', width: '14px', minWidth: '14px' }}
        >
          {/* 이미지 내 SVG 경로 반영 */}
          <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
            <path 
              d="m4.069 8c0-.23.087-.46.263-.636l4.5-4.5c.226-.235.561-.331.877-.248.316.082.562.328.644.644.082.315-.012.651-.248.877l-3.864 3.864 3.864 3.864c.235.226.33.562.248.877-.082.316-.328.562-.644.644-.316.083-.651-.013-.877-.248l-4.5-4.5c-.168-.169-.263-.398-.263-.636" 
              fill="#b0b8c1" 
              fillRule="evenodd"
            />
          </svg>
        </span>
      </button>
    )}

 function StockHeaderRightScrollButton() {
  return(
      <button 
        className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710 tw3v-emtxt716" 
        aria-disabled="false" 
        aria-label="오른쪽으로 스크롤" 
        type="button"
        data-theme="grey"
        data-variant="clear"
        data-mode="dark"
        data-tossinvest-log="IconButton"
        data-contents-value="오른쪽으로 스크롤"
        data-content-tag="오른쪽으로_스크롤"
        data-parent-name="HorizontalScrollArea"
      >
        <span 
          className="tw3v-17xiat90 tw3v-17xiat91" 
          aria-hidden="false" 
          role="presentation" 
          style={{ height: '14px', width: '14px', minWidth: '14px' }}
        >
          {/* 오른쪽 화살표 아이콘 (class="line-icon") */}
          <svg 
            xmlns="http://www.w3.org/2000/svg" 
            viewBox="0 0 16 16" 
            className="line-icon"
          >
            <path 
              d="M5.75 13.4c-.23 0-.46-.09-.64-.26a.9.9 0 010-1.27L8.98 8 5.11 4.14a.9.9 0 010-1.27.9.9 0 011.27 0l4.5 4.5a.9.9 0 010 1.27l-4.5 4.5c-.17.17-.4.26-.63.26" 
              fillRule="evenodd" 
              clipRule="evenodd" 
              fill="#b0b8c1" 
            />
          </svg>
        </span>
      </button>
    )}
}