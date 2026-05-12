'use client';

import './MainContent.css';
import '@/tossCss/toss-layout.css';
import RealTimeTicks from './RealTimeTicks/realTimeTicks';
import HogaChart from './HogaChart/HogaChart';
import OrderForm from './Order/OrderForm';
import ChartForm from '../Chart/ChartForm';
import useMainContent from './useMainContent';
import HaveStock from './HaveStockDetail/HaveStock';

export default function StockContent({stock}) {
  const {
    containerRef,
    selectedPrice, setSelectedPrice,
    onMouseDown,
    splitV1, splitV2, splitH_left, splitH_mid, splitH_right, totalH,
    G, w1, w2, w3,
  } = useMainContent();

  return (
    <div className="_1gr0g6c7">
      <div className="_1gr0g6c0 _1gr0g6c1" id="stock-content">
        <div
          data-state="closed"
          style={{ position: 'relative', top: '0px', marginBottom: '0px' }}
        ></div>

        <div className="_1qqdlu30" style={{ overflow: 'clip', padding: '10px', margin: '-10px' }}>
          <div className="d16w490" ref={containerRef} style={{ height: `${totalH}px` }}>
            <div id="screen-edit-modal-portal" className="_1qqdlu34"></div>

            <div className="_14b51l90">
              <div className="_14b51l91">

                {/* 1. 차트 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: 0, left: 0,
                  width: `${w1}px`, height: `${splitH_left}px`
                }}>
                  <input type="hidden" value='{"typeId":"chart","title":"차트","hide":"false"}' />
                <div className="_1upatvo0">
                  <div className="tw3v-1ftc7zl0">
                     <MainContentForm/>
                     <ChartForm  stockCode = {stock.stockCode}/>
                    </div>
                  </div>
                </div>

                {/* 2. 호가 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: 0, left: `${splitV1 + G}px`,
                  width: `${w2}px`, height: `${splitH_mid}px`
                }}>
                  <input type="hidden" value='{"typeId":"quote","title":"호가","minWidth":325,"minHeight":300,"hide":"false"}' />
                  <div className="_1upatvo0">
                  <div className="tw3v-1ftc7zl0">
                     <MainContentForm/>
                      <HogaChart stock={stock} onPriceSelect={setSelectedPrice} />
                     </div>
                  </div>
                </div>

                {/* 3. 일반주문 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: 0, left: `${splitV2 + G}px`,
                  width: `${w3}px`, height: `${splitH_right}px`
                }}>
                  <input type="hidden" value='{"typeId":"orderForm","title":"일반주문","minWidth":300,"minHeight":300,"hide":"false"}' />
                   <div className="_1upatvo0">
                  <div className="tw3v-1ftc7zl0">
                    <MainContentForm/>
                    <OrderForm selectedPrice={selectedPrice} setSelectedPrice={setSelectedPrice} stockCode={stock.stockCode} />
                  </div>
                  </div>
                </div>

                {/* 4. 보유주식 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: `${splitH_right + G}px`, left: `${splitV2 + G}px`,
                  width: `${w3}px`, height: `${totalH - splitH_right - G}px`
                }}>
                  <input type="hidden" value='{"typeId":"myStockInformation","title":"보유 주식","hide":"false"}' />
                  <div className="tw3v-1ftc7zl0">
                    <MainContentForm/>
                    <HaveStock stockCode = {stock.stockCode}/>
                  </div>
                </div>

                {/* 5-1. 시세 */}
                <div className="_14b51l92" style={{ position: 'absolute', top: `${splitH_mid + G}px`, left: `${splitV1 + G}px`,  width: `${w2}px`, height: `${totalH - splitH_mid - G}px` }}>
                  <input type="hidden" value='{"typeId":"realtimeTicks","title":"시세","hide":"false"}' />
                    <div className="tw3v-1ftc7zl0">
                        <MainContentForm/>
                        <RealTimeTicks  stockCode = {stock.stockCode}/>
                    </div>
                </div>

                {/* 6. 커뮤니티 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: `${splitH_left + G}px`, left: 0,
                  width: `${w1}px`, height: `${totalH - splitH_left - G}px`
                }}>
                  <input type="hidden" value='{"typeId":"community","title":"커뮤니티","minWidth":300,"minHeight":300,"hide":"false"}' />
                  <div className="tw3v-1ftc7zl0">
                    <MainContentForm/>
                  </div>
                </div>

                {/* 세로 구분선 V1 (왼쪽) */}
                <div
                  role="separator"
                  className="_14b51l93 _14b51l94"
                  onMouseDown={(e) => onMouseDown(e, 'v1')}
                  style={{ position: 'absolute', top: 0, left: `${splitV1}px`, width: `${G}px`, height: `${totalH}px` }}
                >
                  <div className="d16w498" style={{ width: '100%', height: '100%', padding: '10px 0', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ background: 'linear-gradient(0deg, rgba(100,168,255,0) 0%, rgb(100,168,255) 50%, rgba(100,168,255,0) 100%)', flex: '1 1 0%', height: 0, width: '4px' }}></div>
                  </div>
                </div>

                {/* 세로 구분선 V2 (오른쪽) */}
                <div
                  role="separator"
                  className="_14b51l93 _14b51l94"
                  onMouseDown={(e) => onMouseDown(e, 'v2')}
                  style={{ position: 'absolute', top: 0, left: `${splitV2}px`, width: `${G}px`, height: `${totalH}px` }}
                >
                  <div className="d16w498" style={{ width: '100%', height: '100%', padding: '10px 0', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ background: 'linear-gradient(0deg, rgba(100,168,255,0) 0%, rgb(100,168,255) 50%, rgba(100,168,255,0) 100%)', flex: '1 1 0%', height: 0, width: '4px' }}></div>
                  </div>
                </div>

                {/* 가로 구분선 H_left (차트|커뮤니티) */}
                <div
                  role="separator"
                  className="_14b51l93 _14b51l95"
                  onMouseDown={(e) => onMouseDown(e, 'h_left')}
                  style={{ position: 'absolute', top: `${splitH_left}px`, left: 0, width: `${w1}px`, height: `${G}px` }}
                >
                  <div className="d16w498" style={{ width: '100%', height: '100%', padding: '0 10px', display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ background: 'linear-gradient(90deg, rgba(100,168,255,0) 0%, rgb(100,168,255) 50%, rgba(100,168,255,0) 100%)', flex: '1 1 0%', height: '4px', width: 0 }}></div>
                  </div>
                </div>

                {/* 가로 구분선 H_mid (호가|개인외국인기관) */}
                <div
                  role="separator"
                  className="_14b51l93 _14b51l95"
                  onMouseDown={(e) => onMouseDown(e, 'h_mid')}
                  style={{ position: 'absolute', top: `${splitH_mid}px`, left: `${splitV1 + G}px`, width: `${w2}px`, height: `${G}px` }}
                >
                  <div className="d16w498" style={{ width: '100%', height: '100%', padding: '0 10px', display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ background: 'linear-gradient(90deg, rgba(100,168,255,0) 0%, rgb(100,168,255) 50%, rgba(100,168,255,0) 100%)', flex: '1 1 0%', height: '4px', width: 0 }}></div>
                  </div>
                </div>

                {/* 가로 구분선 H_right (일반주문|보유주식) */}
                <div
                  role="separator"
                  className="_14b51l93 _14b51l95"
                  onMouseDown={(e) => onMouseDown(e, 'h_right')}
                  style={{ position: 'absolute', top: `${splitH_right}px`, left: `${splitV2 + G}px`, width: `${w3}px`, height: `${G}px` }}
                >
                  <div className="d16w498" style={{ width: '100%', height: '100%', padding: '0 10px', display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'center' }}>
                    <div style={{ background: 'linear-gradient(90deg, rgba(100,168,255,0) 0%, rgb(100,168,255) 50%, rgba(100,168,255,0) 100%)', flex: '1 1 0%', height: '4px', width: 0 }}></div>
                  </div>
                </div>
                
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function MainContentForm() {
  return (
    <div className="tw3v-1ftc7zl1">
      <div className="tw3v-1ftc7zl2">
        <div style={{ cursor: "move", touchAction: "none", userSelect: "none" }} >
          <div className="tw3v-1ftc7zl4" aria-label="패널 이동">
            <span className="rc5u-d3e6jc0 rc5u-d3e6jc2 rc5u-d3e6jc1" aria-hidden="false" role="presentation" style={{ "--standard-icon-size": "14px" }} >
              <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" className="line-icon">
                <g fill="#ADB7C1" fillRule="evenodd">
                  <path d="M 9.125 17.125 a 1.8 1.8 0 1 1 0 3.6 a 1.8 1.8 0 0 1 0 -3.6 M 10.925 12.125 c 0 1 -0.8 1.8 -1.8 1.8 s -1.8 -0.8 -1.8 -1.8 s 0.8 -1.8 1.8 -1.8 s 1.8 0.8 1.8 1.8 M 10.925 5.325 c 0 1 -0.8 1.8 -1.8 1.8 s -1.8 -0.8 -1.8 -1.8 s 0.8 -1.8 1.8 -1.8 s 1.8 0.8 1.8 1.8 M 15.125 17.125 a 1.8 1.8 0 1 1 0 3.6 a 1.8 1.8 0 0 1 0 -3.6 M 16.925 12.125 c 0 1 -0.8 1.8 -1.8 1.8 s -1.8 -0.8 -1.8 -1.8 s 0.8 -1.8 1.8 -1.8 s 1.8 0.8 1.8 1.8 M 16.925 5.325 c 0 1 -0.8 1.8 -1.8 1.8 s -1.8 -0.8 -1.8 -1.8 s 0.8 -1.8 1.8 -1.8 s 1.8 0.8 1.8 1.8"/>
                </g>
              </svg>
            </span>
          </div>
        </div>
        <div className="tw3v-13vbc5y0"> 
          <div className="tw3v-pmssay6" aria-hidden="true"> 
            <div role="tab" tabIndex={0} aria-disabled="false" aria-roledescription="sortable" aria-describedby="" aria-selected="true" className="tw3v-pmssay2" data-tab-id="bba3fb5f-b825-45bc-b69a-b3711c5d7ee8" data-selected="true" data-closable="true">
              <span className="tw3v-pmssay3">시세</span>
              <button type="button" className="tw3v-pmssay4" aria-label="탭 닫기">
                <span className="rc5u-d3e6jc0 rc5u-d3e6jc2 rc5u-d3e6jc1" aria-hidden="false" role="presentation" style={{ "--standard-icon-size": "14px" }}>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="line-icon">
                    <path fill="#BOB8C1" fillRule="evenodd" d="M 13.815 12 l 5.651 -5.651 a 1.2 1.2 0 0 0 -1.697 -1.698 l -5.651 5.652 l -5.652 -5.652 a 1.201 1.201 0 0 0 -1.697 1.698 L 10.421 12 l -5.652 5.651 a 1.202 1.202 0 0 0 0.849 2.049 c 0.307 0 0.614 -0.117 0.848 -0.351 l 5.652 -5.652 l 5.651 5.652 a 1.198 1.198 0 0 0 1.697 0 a 1.2 1.2 0 0 0 0 -1.698 L 13.815 12z" />
                  </svg>
                </span>
              </button>
            </div>
          </div>
          <div role="tablist" className="tw3v-pmssay0">
            <div role="tab" tabIndex={0} aria-disabled="false" aria-roledescription="sortable" aria-describedby="DndDescribedBy-68" aria-selected="true" className="tw3v-pmssay2" data-tab-id="bba3fb5f-b825-45bc-b69a-b3711c5d7ee8" data-selected="true" data-closable="true">
              <span className="tw3v-pmssay3">시세</span>
              <button type="button" className="tw3v-pmssay4" aria-label="탭 닫기">
                <span className="rc5u-d3e6jc0 rc5u-d3e6jc2 rc5u-d3e6jc1" aria-hidden="false" role="presentation" style={{ "--standard-icon-size": "14px" }}>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="line-icon">
                    <path fill="#BOB8C1" fillRule="evenodd" d="M13.815 12 l 5.651 -5.651 a 1.2 1.2 0 0 0 -1.697 -1.698 l -5.651 5.652 l -5.652 -5.652 a 1.201 1.201 0 0 0 -1.697 1.698 L 10.421 12 l -5.652 5.651 a 1.202 1.202 0 0 0 0.849 2.049 c 0.307 0 0.614 -0.117 0.848 -0.351 l 5.652 -5.652 l 5.651 5.652 a 1.198 1.198 0 0 0 1.697 0 a 1.2 1.2 0 0 0 0 -1.698 L 13.815 12z" />
                  </svg>
                </span>
              </button>
            </div>
          </div>
          <div id="DndDescribedBy-68" style={{ display: "none" }}> </div>
          <div id="DndLiveRegion-68" role="status" aria-live="assertive" aria-atomic="true" style={{ position: "fixed", top: 0, left: 0, width: "1px", height: "1px", margin: "-1px", border: 0, padding: "0px", overflow: "hidden", clip: "rect(0px, 0px, 0px, 0px)", clipPath: "inset(100%)", whiteSpace: "nowrap" }}>
          </div>
          <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710 tw3v-ta8c3h1" aria-disabled="false" aria-label="탭 추가" data-theme="grey" data-variant="clear" data-mode="dark" aria-haspopup="menu" aria-expanded="false" data-state="closed">
            <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px" }}>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="line-icon">
                <path fill="#BOB8C1" d="M 20.318 10.8 h -7 v -7 a 1.2 1.2 0 1 0 -2.4 0 v 7 h -7 a 1.2 1.2 0 1 0 0 2.4 h 7 v 7 a 1.2 1.2 0 1 0 2.4 0 v -7 h 7 a 1.2 1.2 0 1 0 0 -2.4" fillRule="evenodd" />
              </svg>
            </span>
          </button>
          <div className="tw3v-1y1hl1j1">
            <button type="button" className="tw3v-1y1hl1j9" aria-haspopup="dialog" aria-expanded="false" aria-controls="radix-_r97d_" data-state="closed">
              <span className="tw3v-1y1hl1ja">0개 더보기</span>
              <span className="rc5u-d3e6jc0 rc5u-d3e6jc2 rc5u-d3e6jc1" aria-hidden="false" role="presentation" style={{ "--standard-icon-size": "14px" }}>
                <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg">
                  <path d="m3.691 5.746 4.309 4.355 4.309 -4.355" fill="none" stroke="#8f959e" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
                </svg>
              </span>
            </button>
          </div>
        </div>
      </div>
      <div className="tw3v-1ftc7zl3"></div>
    </div>
  )
}