import React from 'react';
import './ChartForm.css';
import ChartComponent from './ChartComponent';

const TIME_BUTTONS = ['일', '주', '월', '년'];

export default function ChartForm({stockCode}) {
    return (
        <div className="sa1m6r0">
            <div className="sa1m6r1">
                <div className="mnc8st0">
                    <ChartConfig />
                    <div style={{ display: "flex", flexDirection: "column", gap: "4px", paddingTop: "8px" }}></div>
                    <div style={{ flex: "0 0 auto", height: "16px", backgroundColor: "var(--wts-adaptive-background)" }}></div>
                   <div className="mnc8st3" >
                        <ChartComponent stockCode={stockCode}/> 
                    </div>
                </div>
            </div>
        </div>
    );
}

function ChartConfig()
{
    return(
    <div style={{ position: "relative" }}>
        <div style={{ position: "absolute", inset: "-8px -8px 0px", cursor: "move", touchAction: "none", userSelect: "none" }}>&nbsp;</div>
        <div className="_1owxq230">
            <div style={{ position: "absolute", inset: "0px", cursor: "move", touchAction: "none", userSelect: "none" }}>&nbsp;</div>        
            <ChartTimeConfig/>
            <div className="_1owxq233">
                <div className="_1owxq235 _1owxq234">
                <div className="_1owxq232"></div>
                 <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false" aria-label="차트모양" data-theme="grey" data-variant="clear" data-mode="dark" data-state="closed">
                    <span className="tw3v-17xiat90" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="">
                            <path fill="none" stroke="#fc2d4c" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" d="M4 10.98v5.84"></path>
                            <path fill="none" stroke="#007ff3" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" d="M8 6.98v11.84"></path>
                            <path fill="none" stroke="#fc2d4c" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" d="M20 3.98v9.93M12 3.18v18.44">
                            </path>
                            <path fill="none" stroke="#007ff3" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" strokeMiterlimit="10" d="M16 9.18v7.44"></path>
                        </svg>
                    </span>
                </button>
                <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false" aria-label="지표 및 그리기 제거" data-theme="grey" data-variant="clear" data-mode="dark" data-state="closed">
                    <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <g fill="#b0b8c1">
                                <path d="m 20.5 5.121 h -3.428 v -1.281 c 0 -1.654 -1.346 -3 -3 -3 h -4.144 c -1.654 0 -3 1.346 -3 3 v 1.281 h -3.428 c -0.635 0 -1.15 0.515 -1.15 1.15 s 0.515 1.15 1.15 1.15 h 0.206 l 0.529 12.517 c 0.052 1.231 1.065 2.203 2.298 2.203 h 10.944 c 1.233 0 2.247 -0.972 2.298 -2.204 l 0.52 -12.516 h 0.205 c 0.635 0 1.15 -0.515 1.15 -1.15 s -0.515 -1.15 -1.15 -1.15 Z m -11.572 -1.281 c 0 -0.551 0.449 -1 1 -1 h 4.144 c 0.552 0 1 0.449 1 1 v 1.281 h -6.144 Z m 8.577 15.33 c -0.016 0.375 -0.324 0.671 -0.699 0.671 h -9.602 c -0.375 0 -0.684 -0.296 -0.699 -0.67 l -0.496 -11.75 h 11.984 Z"></path>
                                <path d="m 9.934 17.636 c 0.635 0 1.15 -0.515 1.15 -1.15 v -4.506 c 0 -0.635 -0.515 -1.15 -1.15 -1.15 s -1.15 0.515 -1.15 1.15 v 4.506 c 0 0.635 0.515 1.15 1.15 1.15 Z"></path>
                                <path d="m 14.066 17.636 c 0.635 0 1.15 -0.515 1.15 -1.15 v -4.506 c 0 -0.635 -0.515 -1.15 -1.15 -1.15 s -1.15 0.515 -1.15 1.15 v 4.506 c 0 0.635 0.515 1.15 1.15 1.15 Z"></path>
                            </g>
                        </svg>
                    </span>
                </button>
                <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false" aria-label="설정" data-theme="grey" data-variant="clear" data-mode="dark" data-state="closed">
                    <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path d="m 22 14.85 l -1 -0.8 c -0.5 -0.5 -0.8 -1.2 -0.8 -1.9 s 0.3 -1.4 0.8 -1.9 l 0.9 -1 c 0.4 -0.4 0.5 -1 0.2 -1.5 l -1.3 -2.2 c -0.3 -0.5 -0.8 -0.7 -1.4 -0.6 l -1.3 0.3 c -0.7 0.2 -1.5 0.1 -2.1 -0.3 s -1.1 -0.9 -1.3 -1.6 l -0.4 -1.3 c 0 -0.6 -0.5 -1 -1 -1 h -2.5 c -0.6 0 -1 0.4 -1.2 0.9 l -0.4 1.4 c -0.2 0.6 -0.6 1.2 -1.2 1.5 c -0.1 0 -0.1 0.1 -0.2 0.1 c -0.6 0.3 -1.3 0.4 -1.9 0.3 l -1.4 -0.4 c -0.5 -0.1 -1.1 0.1 -1.4 0.6 l -1.3 2.2 c -0.3 0.5 -0.2 1.1 0.2 1.5 l 0.9 1 c 0.5 0.5 0.8 1.2 0.8 1.9 s -0.3 1.4 -0.8 1.9 l -0.5 0.5 c -0.6 0.7 -0.8 1.7 -0.3 2.5 l 0.9 1.6 c 0.3 0.5 0.8 0.7 1.4 0.6 l 1.4 -0.4 c 0.6 -0.2 1.3 -0.1 1.9 0.3 c 0.1 0 0.1 0.1 0.2 0.1 c 0.6 0.3 1 0.9 1.2 1.5 l 0.4 1.4 c 0.2 0.5 0.6 0.9 1.2 0.9 h 2.5 c 0.6 0 1 -0.4 1.2 -0.9 l 0.4 -1.4 c 0.2 -0.6 0.6 -1.2 1.2 -1.5 c 0.1 0 0.1 -0.1 0.2 -0.1 c 0.6 -0.3 1.3 -0.4 1.9 -0.3 l 1.4 0.4 c 0.5 0.1 1.1 -0.1 1.4 -0.6 l 1.3 -2.2 c 0.3 -0.5 0.2 -1.1 -0.2 -1.5 Z m -10 0.9 c -2 0 -3.7 -1.6 -3.7 -3.7 s 1.6 -3.7 3.7 -3.7 s 3.7 1.6 3.7 3.7 s -1.7 3.7 -3.7 3.7 Z" fill="#b0b8c1" fillRule="evenodd"></path>
                        </svg>
                    </span>
                </button>
                </div>
                <div className="_1owxq234">
                    <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false" aria-label="보조지표" data-theme="grey" data-variant="clear" data-mode="dark" data-state="closed">
                        <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="line-icon">
                                <path fill="#B0B8B1" d="M 20.318 10.8 h -7 v -7 a 1.2 1.2 0 1 0 -2.4 0 v 7 h -7 a 1.2 1.2 0 1 0 0 2.4 h 7 v 7 a 1.2 1.2 0 1 0 2.4 0 v -7 h 7 a 1.2 1.2 0 1 0 0 -2.4" fillRule="evenodd"></path>
                            </svg>
                        </span>
                    </button>
                    <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false" aria-label="그리기" data-theme="grey" data-variant="clear" data-mode="dark" data-state="closed">
                        <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <g fill="none" stroke="#b1b8c1">
                                    <path d="m 6.844 16.993 l 10.336 -9.941" strokeWidth="2.28"></path>
                                    <g strokeWidth="1.982">
                                        <ellipse cx="5.814" cy="17.979" rx="1.95" ry="1.916"></ellipse>
                                        <ellipse cx="18.186" cy="6.021" rx="1.95" ry="1.916"></ellipse>
                                    </g>
                                </g>
                            </svg>
                        </span>
                    </button>
                    <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false" aria-label="종목비교" data-theme="grey" data-variant="clear" data-mode="dark" data-state="closed">
                        <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                            <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <g fill="#b0b8c1">
                                    <path d="m 20.669 15.813 l -0.103 -0.092 l -0.022 -0.02 l -0.028 -0.013 l -0.089 -0.041 c -0.092 -0.043 -0.247 -0.116 -0.458 -0.116 l -13.602 -0.005 l 2.006 -2.006 c 0.297 -0.297 0.277 -0.791 0.277 -0.796 c -0.021 -0.401 -0.245 -0.746 -0.6 -0.924 c -0.156 -0.078 -0.33 -0.12 -0.506 -0.12 c -0.225 0 -0.442 0.066 -0.63 0.19 l -4.1 4.082 l -0.019 0.019 l -0.013 0.023 l -0.029 0.051 c -0.251 0.438 -0.204 0.935 0.127 1.331 l 3.951 3.948 c 0.218 0.174 0.467 0.261 0.72 0.261 c 0.148 0 0.293 -0.029 0.429 -0.087 c 0.376 -0.159 0.626 -0.501 0.669 -0.914 c 0.014 -0.143 -0.005 -0.568 -0.264 -0.827 l -2.017 -2.017 l 13.657 -0.005 c 0.406 -0.035 0.75 -0.286 0.914 -0.656 c 0.164 -0.374 0.118 -0.8 -0.121 -1.112 c -0.038 -0.049 -0.086 -0.099 -0.149 -0.154 Z"></path>
                                    <path d="m 21.12 6.613 l -3.951 -3.948 c -0.218 -0.174 -0.467 -0.261 -0.72 -0.261 c -0.148 0 -0.293 0.029 -0.429 0.087 c -0.376 0.159 -0.626 0.501 -0.669 0.914 c -0.014 0.142 0.005 0.568 0.264 0.827 l 2.017 2.017 l -13.09 0.005 c -0.406 0.035 -0.75 0.286 -0.914 0.656 c -0.164 0.374 -0.118 0.8 0.121 1.112 c 0.038 0.049 0.086 0.099 0.149 0.154 l 0.103 0.092 l 0.023 0.02 l 0.028 0.013 l 0.089 0.041 c 0.092 0.043 0.247 0.116 0.458 0.116 l 13.035 0.005 l -2.006 2.006 c -0.297 0.297 -0.277 0.791 -0.277 0.795 c 0.021 0.401 0.245 0.746 0.6 0.924 c 0.155 0.078 0.33 0.12 0.506 0.12 c 0.225 0 0.442 -0.066 0.63 -0.19 l 4.1 -4.082 l 0.019 -0.019 l 0.013 -0.023 l 0.029 -0.051 c 0.251 -0.438 0.204 -0.935 -0.127 -1.331 Z"></path>
                                </g>
                            </svg>
                        </span>
                    </button>
                    <div>
                        <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710" aria-disabled="false"aria-label="차트 크게보기"data-theme="grey"data-variant="clear"data-mode="dark"data-tossinvest-log="IconButton"data-contents-value="차트 크게보기"data-content-tag="label"data-parent-name="ControllerButton"data-state="closed"data-tossinvest-priority-log="Tooltip.Trigger">
                            <span className="tw3v-17xiat90 tw3v-17xiat91"aria-hidden="false"role="presentation"style={{ height: "14px", width: "14px", minWidth: "14px", color: "var(--wts-adaptive-grey700)" }}>
                            <svg height="24" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
                                <g fill="#b0b8c1" stroke="#b0b8c1" strokeWidth=".5" transform="translate(.075379)">
                               <path d="M4.803 9.506c0.109 0.106 0.286 0.106 0.394 0l4.603-4.489c0.13-0.126 0.2-0.296 0.2-0.471 0-0.175-0.07-0.346-0.201-0.471l-0.064-0.055c-0.272-0.205-0.658-0.182-0.901 0.056l-3.151 3.074 0.001-6.483c0-0.368-0.306-0.666-0.683-0.666l-0.079 0.005c-0.344 0.039-0.604 0.324-0.604 0.662l-0.001 6.482-3.15-3.073c-0.267-0.26-0.699-0.26-0.966 0s-0.267 0.682 0 0.942Z"
                                    style={{ transform: "matrix(-0.707107, -0.707107, 0.707107, -0.707107, 16.9957, 13.8492)" }}/>
                                <path d="M4.803 9.506c0.109 0.106 0.286 0.106 0.394 0l4.603-4.489c0.13-0.126 0.2-0.296 0.2-0.471 0-0.175-0.07-0.346-0.201-0.471l-0.064-0.055c-0.272-0.205-0.658-0.182-0.901 0.056l-3.151 3.074 0.001-6.483c0-0.368-0.306-0.666-0.683-0.666l-0.079 0.005c-0.344 0.039-0.604 0.324-0.604 0.662l-0.001 6.482-3.15-3.073c-0.267-0.26-0.699-0.26-0.966 0s-0.267 0.682 0 0.942Z"
                                    style={{ transform: "matrix(0.707107, 0.707107, -0.707107, 0.707107, 6.77817, 9.86827)" }}/>
                                </g>
                            </svg>
                            </span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    )
}

function ChartTimeConfig() {
  return (
    <div className="_1owxq237">
      <button type="button" tabIndex="0" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka58 tw3v-1wkoka541 tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528" data-tds-wts-button id="radix-_r_ign_" aria-haspopup="menu" aria-expanded="false" data-state="closed">
        <span className="tw3v-1wkoka52g">1분∨</span>
      </button>
      {TIME_BUTTONS.map((label) => (
        <button key={label} type="button" aria-disabled="false" className="tw3v-1wkoka52h tw3v-1wkoka5a tw3v-1wkoka5e tw3v-1wkoka517 tw3v-1wkoka5x tw3v-1wkoka5r tw3v-1wkoka5l tw3v-1wkoka528" data-tds-wts-button>
          <span className="tw3v-1wkoka52g">{label}</span>
        </button>
      ))}
    </div>
  );
}
