import './Header.css';
import '../../tossCss/toss-layout.css'
import { useState } from 'react';
import HeaderProfile from './HeaderProfile';
import useHeader from './useHeader';

export default function Header() {
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const { user, handleLogin } = useHeader();

  return (
    <div className="_1bfoojl0" data-nosnippet="true" id="tossinvest_global_navigation_bar">
      <div className="_1bfoojl1" style={{ opacity: 1 }} />
      <div className="_1leau170 _1leau172">
        <nav className="_1bfoojl3">
          <div className="_1bfoojl4">
            <a className="_1k9p25g2" href="/" style={{ display: 'flex', alignItems: 'center', textDecoration: 'none' }}>
              <span style={{ color: '#fff', fontWeight: 'bold', fontSize: '18px' }}>토스증권</span>
            </a>
          </div>
          <div className='_9x1lao0'>
            <ul className="_9x1lao1">
              <li className="_9x1lao3 _9x1lao2"><a className="_9x1lao5" href="/">홈</a></li>
              <li className="_9x1lao3 _9x1lao2"><a className="_9x1lao5" href="/feed">피드</a></li>
              <li className="_9x1lao3 _9x1lao2"><a className="_9x1lao5" href="/feed">주식 골라보기</a></li>
              <li className="_9x1lao3 _9x1lao2"><a className="_9x1lao5" href="/feed">내 계좌</a></li>
            </ul>
            <div>
              <button type="button" aria-haspopup="dialog" aria-expanded="false" aria-controls="radix-_r_2_" data-state="closed" className="u09klc0" style={{ marginLeft: '16px' }}>
                <span className="tw3v-17xiat90 u09klc1" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px' }}>
                  <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path d="m22.42 20.439-3.767-3.767c1.21-1.62 1.936-3.621 1.936-5.793 0-5.354-4.356-9.709-9.71-9.709s-9.709 4.355-9.709 9.709 4.355 9.71 9.709 9.71c2.172 0 4.174-.726 5.793-1.936l3.767 3.767c.273.273.632.41.99.41s.717-.137.99-.41c.547-.547.547-1.434 0-1.98zm-18.45-9.56c0-3.81 3.1-6.909 6.909-6.909s6.909 3.1 6.909 6.909-3.1 6.909-6.909 6.909-6.909-3.1-6.909-6.909z" fill="#8f959e"></path>
                  </svg>
                </span>
                <div className="u09klc2">
                  <div className="u09klc3">
                    <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey500)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>/</span>
                  </div>
                  <span className="tw3v-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>를 눌러 검색하세요</span>
                </div>
              </button>
            </div>
          </div>
          <div className="_1bfoojl5">
            {user
              ? <LoginProfile isProfileOpen={isProfileOpen} setIsProfileOpen={setIsProfileOpen} />
              : <LogoutProfile onLogin={handleLogin} />
            }
          </div>
        </nav>
      </div>
    </div>
  )
}

function LoginProfile({ isProfileOpen, setIsProfileOpen }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'row', gap: '0px', justifyContent: 'normal', alignItems: 'center' }}>
      <span role="presentation" aria-haspopup="dialog" aria-expanded={isProfileOpen} aria-controls="radix-_r_7_" data-state={isProfileOpen ? "open" : "closed"} className="_1gwjmki0 _1gwjmki2 ae72tz0" onClick={() => setIsProfileOpen(prev => !prev)}>
        <div className="_1gwjmkia _1gwjmkib css-ry5kze">
          <div className="css-1vqadhc" style={{ width: '32px', height: '32px', borderRadius: '8px', backgroundColor: 'transparent' }}>
            <img aria-hidden="true" draggable="false" className="css-zl34r3" src="/default-profile.png" style={{ '--asset-object-fit': 'contain', '--asset-scale': '1', objectFit: 'contain' }} />
          </div>
        </div>
      </span>
      {isProfileOpen && <HeaderProfile onClose={() => setIsProfileOpen(false)} />}
    </div>
  )
}

function LogoutProfile({ onLogin }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'row', gap: '0px', justifyContent: 'normal', alignItems: 'center' }}>
      <button type="button" aria-disabled="false" className="tw3s-1wkoka52h tw3s-1wkoka50 tw3s-1wkoka541 tw3s-1wkoka5e tw3s-1wkoka517 tw3s-1wkoka5x tw3s-1wkoka5r tw3s-1wkoka5l tw3s-1wkoka528" data-tds-wts-button onClick={onLogin}>
        <span className="tw3s-1wkoka52g">로그인</span>
      </button>
    </div>
  )
}