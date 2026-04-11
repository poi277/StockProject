'use client';

import { useState, useRef, useCallback, useEffect } from 'react';
import './MainContent.css';
import '@/tossCss/toss-layout.css';

// 초기 레이아웃 상수
const INIT = {
  // 세로 구분선 X 위치
  splitV1: 759,   // 왼쪽 | 중앙
  splitV2: 1089,  // 중앙 | 오른쪽

  // 가로 구분선 Y 위치
  splitH_left: 395,   // 차트 | 커뮤니티
  splitH_mid: 562,    // 호가 | 개인외국인기관
  splitH_right: 340,  // 일반주문 | 보유주식

  totalH: 727,
  GAP: 10,
};

export default function StockContent() {
  const containerRef = useRef(null);

  const [layout, setLayout] = useState({
    splitV1: INIT.splitV1,
    splitV2: INIT.splitV2,
    splitH_left: INIT.splitH_left,
    splitH_mid: INIT.splitH_mid,
    splitH_right: INIT.splitH_right,
    totalH: INIT.totalH,
  });

  const dragging = useRef(null); // { type, startX, startY, startLayout }

  const onMouseDown = useCallback((e, type) => {
    e.preventDefault();
    dragging.current = {
      type,
      startX: e.clientX,
      startY: e.clientY,
      startLayout: { ...layout },
    };
  }, [layout]);

  useEffect(() => {
    const onMouseMove = (e) => {
      if (!dragging.current) return;
      const { type, startX, startY, startLayout } = dragging.current;
      const dx = e.clientX - startX;
      const dy = e.clientY - startY;
      const G = INIT.GAP;

      setLayout(prev => {
        const next = { ...prev };

        if (type === 'v1') {
          // 왼쪽 | 중앙 구분선
          next.splitV1 = Math.max(200, Math.min(startLayout.splitV1 + dx, startLayout.splitV2 - 200));
        } else if (type === 'v2') {
          // 중앙 | 오른쪽 구분선
          next.splitV2 = Math.max(startLayout.splitV1 + 200, Math.min(startLayout.splitV2 + dx, 1600));
        } else if (type === 'h_left') {
          next.splitH_left = Math.max(100, Math.min(startLayout.splitH_left + dy, startLayout.totalH - 100));
        } else if (type === 'h_mid') {
          next.splitH_mid = Math.max(100, Math.min(startLayout.splitH_mid + dy, startLayout.totalH - 100));
        } else if (type === 'h_right') {
          next.splitH_right = Math.max(100, Math.min(startLayout.splitH_right + dy, startLayout.totalH - 100));
        }

        return next;
      });
    };

    const onMouseUp = () => {
      dragging.current = null;
    };

    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);
    return () => {
      window.removeEventListener('mousemove', onMouseMove);
      window.removeEventListener('mouseup', onMouseUp);
    };
  }, []);

  const { splitV1, splitV2, splitH_left, splitH_mid, splitH_right, totalH } = layout;
  const G = INIT.GAP;

  // 각 컬럼 너비
  const w1 = splitV1;
  const w2 = splitV2 - splitV1 - G;
  const w3 = 1460 - splitV2 - G; // 전체 너비 가정 1460

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
                  <div className="tw3v-1ftc7zl0"></div>
                </div>

                {/* 2. 호가 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: 0, left: `${splitV1 + G}px`,
                  width: `${w2}px`, height: `${splitH_mid}px`
                }}>
                  <input type="hidden" value='{"typeId":"quote","title":"호가","minWidth":325,"minHeight":300,"hide":"false"}' />
                  <div className="tw3v-1ftc7zl0"></div>
                </div>

                {/* 3. 일반주문 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: 0, left: `${splitV2 + G}px`,
                  width: `${w3}px`, height: `${splitH_right}px`
                }}>
                  <input type="hidden" value='{"typeId":"orderForm","title":"일반주문","minWidth":300,"minHeight":300,"hide":"false"}' />
                  <div className="tw3v-1ftc7zl0"></div>
                </div>

                {/* 4. 보유주식 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: `${splitH_right + G}px`, left: `${splitV2 + G}px`,
                  width: `${w3}px`, height: `${totalH - splitH_right - G}px`
                }}>
                  <input type="hidden" value='{"typeId":"myStockInformation","title":"보유 주식","hide":"false"}' />
                  <div className="tw3v-1ftc7zl0"></div>
                </div>

                {/* 5. 개인·외국인·기관 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: `${splitH_mid + G}px`, left: `${splitV1 + G}px`,
                  width: `${w2}px`, height: `${totalH - splitH_mid - G}px`
                }}>
                  <input type="hidden" value='{"typeId":"investorTrend","title":"개인 · 외국인 · 기관","hide":"false"}' />
                  <div className="tw3v-1ftc7zl0"></div>
                </div>

                {/* 6. 커뮤니티 */}
                <div className="_14b51l92" style={{
                  position: 'absolute', top: `${splitH_left + G}px`, left: 0,
                  width: `${w1}px`, height: `${totalH - splitH_left - G}px`
                }}>
                  <input type="hidden" value='{"typeId":"community","title":"커뮤니티","minWidth":300,"minHeight":300,"hide":"false"}' />
                  <div className="tw3v-1ftc7zl0"></div>
                </div>

                {/* ── 구분선들 ── */}

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