import './Header.css';

export default function Header() {
    return(
         <div className="_1bfoojl0" data-nosnippet="true" id="tossinvest_global_navigation_bar">
          <div className="_1bfoojl1" style={{ opacity: 1 }} />
          
          <div className="_1leau170 _1leau172">
            <nav className="_1bfoojl3">
                {/* 1. 로고 영역 */}
                <div className="_1bfoojl4">
                  <a className="_1k9p25g2" href="/" style={{ display: 'flex', alignItems: 'center', textDecoration: 'none' }}>
                    {/* 임시 로고 텍스트 - 스타일 확인용 */}
                    <span style={{ color: '#fff', fontWeight: 'bold', fontSize: '18px' }}>토스증권</span>
                  </a>
                </div>

                {/* 2. 메뉴 영역 (이제 로고 바로 옆에 붙습니다) */}
                <ul className="_9x1lao1">
                  <li className="_9x1lao2"><a className="_9x1lao5" href="/">홈</a></li>
                  <li className="_9x1lao2"><a className="_9x1lao5" href="/feed">피드</a></li>
                  <li className="_9x1lao2"><a className="_9x1lao5" href="/screener">주식 골라보기</a></li>
                  <li className="_9x1lao2"><a className="_9x1lao5" href="/account">내 계좌</a></li>
                </ul>

                {/* 3. 오른쪽 검색창 영역 (필요하다면 추가) */}
                <div style={{ marginLeft: 'auto' }}>
                  {/* 이 marginLeft: 'auto'가 검색창을 오른쪽 끝으로 밀어냅니다 */}
                </div>
              </nav>
          </div>
        </div>
    )
}