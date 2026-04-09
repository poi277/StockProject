import React from 'react';

/**
 * 이미지 [image_af0608.png]의 3단 그리드 구조를 반영한 컴포넌트
 */
export default function StockHeaderGrid() {
  return (
    /* 최상위 Flex 컨테이너 */
    <div className="w65d0">
      
      {/* 1. 첫 번째 그리드 섹션 범위 */}
      <div className="w65d6" >
                {/* 1. 상단 라벨 영역: tw3v-1r5dc8g0 span 2개가 연속으로 위치 */}
              <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                1일 범위
              </span>
              <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                52주 범위
              </span>

              {/* 2. 첫 번째 데이터 행 (1일 최저가 / 52주 최저가) */}
              <span className="tw3v-1r5dc8g0 w65d7" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                <span>199,800원</span>
              </span>
              <span className="tw3v-1r5dc8g0 w65d7" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                <span>52,000원</span>
              </span>

              {/* 3. 시각적 범위 그래프 영역 (w65d3 flex 컨테이너) */}
              <div className="w65d3">
                <div className="w65d4" style={{ flex: '67.6871 1 0%' }}></div>
                <div className="w65d5"></div>
                <div className="w65d4" style={{ flex: '32.3129 1 0%' }}></div>
              </div>
              <div className="w65d3">
                <div className="w65d4" style={{ flex: '89.3768 1 0%' }}></div>
                <div className="w65d5"></div>
                <div className="w65d4" style={{ flex: '10.6232 1 0%' }}></div>
              </div>

              {/* 4. 하단 데이터 행 (1일 최고가 / 52주 최고가) */}
              <span className="tw3v-1r5dc8g0 w65d7" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                <span >214,500원</span>
              </span>
              <span className="tw3v-1r5dc8g0 w65d7" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                <span >228,500원</span>
              </span>
      </div>

      {/* 2. 두 번째 그리드 섹션 거래대금 체결강도 */}
      <div className="w65d1">
                {/* --- 거래대금 섹션 --- */}
                <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                  <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                    거래대금
                  </span>
                  <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                    <span>
                     1위
                    </span>
                    <span className='w65da'>▼</span>
                  </span>
                </div>

                {/* --- 체결강도 섹션 --- */}
                <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                  <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                    체결강도
                  </span>
                  <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                    124.58%
                  </span>
                </div>
              </div>

      {/* 3. 세 번째 그리드 섹 외국인 순매수/도  */}
      <div className="w65d1" >
              {/* --- 외국인 순매수 섹션 --- */}
              <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                  외국인 순매수
                </span>
                <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                  <span>
                    2위
                  </span>
                  <span className='w65da'>-</span>
                </span>
              </div>

              {/* --- 외국인 순매도 섹션 ($0로 선택된 영역) --- */}
              <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                  외국인 순매도
                </span>
                <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                  <span>100위 밖</span>
                </span>
              </div>
       </div>
      {/* 3. 네 번째 그리드 섹션 기관 순매수/도 */}
        <div className="w65d1">
                {/* --- 기관 순매수 섹션 --- */}
                <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                  <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                    기관 순매수
                  </span>
                  <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                    <span>
                      1위
                    </span>
                    <span className='w65da'>-</span>
                  </span>
                </div>

                {/* --- 기관 순매도 섹션 ($0로 선택된 영역) --- */}
                <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                  <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                    기관 순매도
                  </span>
                  <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                    <span>100위 밖</span>
                  </span>
                </div>
        </div>
        {/* 3. 다섯 번째 그리드 섹션 시총 */}
      <div className="w65d1" >
        {/* --- 시가총액 순위 섹션 --- */}
              <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                  시가총액 순위
                </span>
                <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                  <span>
                    1위
                  </span>
                  <span className='w65da'></span>
                </span>
              </div>

              {/* --- 시가총액 정보 섹션 --- */}
              <div className="w65dc w65db" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-parent-name="SectionWrapper" data-skip="true" style={{ display: 'grid' }}>
                <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                  시가총액
                </span>
                <span className="tw3v-1r5dc8g0 w65d2" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity700)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px', display: 'flex' }}>
                  1,247.52조원
                </span>
              </div>
            </div>
    </div>
  );
}