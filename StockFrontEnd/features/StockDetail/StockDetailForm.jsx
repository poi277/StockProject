'use client';

import Header from '../UI/Header';
import StockPriceHeader from './StockHeader/StockPriceHeader';
import './StockDetailForm.css'
import StockTabSelection from './StockHeader/StockTabSelection/StockTabSelection';
import StockRollingBar from './StockRollingBar/StockRollingBar';
import MainContent from './MainContent/MainContent';
import '../../tossCss/toss-layout.css'

export default function StockDetailForm({ stock }) {

  return (
  <div id="_next">
      <div data-nosnippet="true" id="unsupported-device-section" className="f2xx2r0"></div>
    <div id="main-content">
      <div className="ho2myi0 _1kestwgq _1kestwg2">
        <div className="ho2myi1">
          <Header />
        <main className="ho2myi2">
            <div className="_2ozzgc8">
              <div 
                className="_2ozzgcf _2ozzgca _2ozzgch" 
                style={{ '--_2ozzgcg': '42.046875'}}
              >            
                <div 
                  className="_2x64iu3 _2x64iu1" 
                  data-section-name="종목상세" 
                >
                  <div></div>
                  <div></div>
                  {/* 1. 가격 정보 섹션: div class="ia3qp40" */}
                  <div 
                    className="ia3qp40" 
                    style={{ 
                      display: 'flex', 
                      flexDirection: 'column', 
                      gap: '0px', 
                      justifyContent: 'normal', 
                      alignItems: 'normal' 
                    }}
                  >
                    {/* 여기에 종목명, 가격 정보 등이 들어갑니다. */}
                    <StockPriceHeader stock = {stock}/>
                  </div>
                    {/* 여기에 차트/호가/종목정보/뉴스 탭 메뉴가 들어갑니다. */}
                    <StockTabSelection/>

                  {/* 3. 메인 콘텐츠 루트: div class="_2x64iu0" id="stock-contents-root" */}
                  <div 
                    className="_2x64iu0" 
                    id="stock-contents-root" 
                    data-nosnippet="true"
                    
                  >
                    {/* 실제 차트나 호가 내용이 렌더링되는 지점입니다. */}
                    <MainContent stock = {stock}/>
                  </div>

                  {/* 4. 지수 롤링 바: div data-section-name="지수Rolling" */}
                
                    <StockRollingBar/>
                    {/* 하단에 흐르는 지수 정보 영역입니다. */}
                

                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    </div>
  </div>
    );
  }