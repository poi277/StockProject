'use client'

import './StockList.css'
import { formatPrice, formatChangeRate, formatValue, getChangeColor, useStockList, getTradeRatio } from './useStockList'
export default function TossStockList() {

    return (
        <main className="ho2myi3 ho2myi2">
            <div className="_1leau170 _1leau171">
                <div className="_2luxl20">
                    <div className="_2luxl23">
                        ...
                    </div>
                    <section className="er9vne0" data-section-name="지수환율">
                        ...
                    </section>
                    <div style={{ flex: '0 0 auto', height: '16px' }}></div>
                    <StockMainForm />
                </div>
            </div>
        </main>
    )
}

function StockMainForm() {

    const { stocklist } = useStockList()

    return (
        <div className="_2luxl21" style={{ display: 'flex', flexDirection: 'column', gap: '0px', justifyContent: 'normal', alignItems: 'normal' }}  >
            <StockMainTab />
            <div style={{ height: '152px', marginTop: '-152px', pointerEvents: 'none' }}></div>
            <div data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-contents-value="사람들이 많이 거래하는 종목, 오늘 많이 오른 종목을 확인할 수 있어요" data-content-tag="message" data-parent-name="TooltipGuideSentinel" style={{ position: 'relative', top: '0px', width: '32px', height: '32px', marginBottom: '-32px' }}></div>
            <div style={{ flex: '0 0 auto', height: '8px' }}></div>
            <div className="_2lux122">
                <div data-nosnippet="true" style={{ position: 'relative' }}>
                    <div style={{ width: '0px', height: '0px', opacity: 0, pointerEvents: 'none' }}></div>
                    <h2 className="sr-only">실시간 차트</h2>
                    <div className="_1vjo0mn4">
                        <h3 className="sr-only">필터</h3>
                        <div data-section-name="실시간차트" className="_1j6t4cj0">
                            <StockRegionFilter />
                            <StockTradeFilter />
                            <StockTimeFilter />
                            <StockDangerListFilter />
                        </div>
                        <div style={{ flex: '0 0 auto', height: '8px' }}></div>
                    </div>
                    <div className="_1vjo0mn0">
                        <LiveChartList stocklist={stocklist} />

                        <LiveChartListSide />
                    </div>
                </div>
            </div>
        </div>
    )
}

const TABLE_COLUMNS = [
    { className: '_5x01f0', width: '268.859px', wrapClass: 'tw6g-kvawo25', align: null, label: <span className="tw6g-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>순위 · 오늘 18:40 기준</span> },
    { className: '_5x01f1', width: '134.422px', wrapClass: 'tw6g-kvawo27', align: 'right', label: '현재가' },
    { className: '_5x01f2', width: '145.625px', wrapClass: 'tw6g-kvawo27', align: 'right', label: '등락률' },
    { className: '_5x01f3', width: '134.422px', wrapClass: 'tw6g-kvawo27', align: 'right', label: <span className="tw6g-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-bold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>거래대금 순</span> },
    { className: '_5x01f5', width: '154.594px', wrapClass: 'tw6g-kvawo27', align: null, label: <>토스증권 거래 비율<span className="t00n7r0" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-contents-value="툴팁" data-content-tag="툴팁" data-parent-name="QuestionmarkTooltip" style={{ width: '0.9em' }} /></> },
    { className: '_5x01f6', width: '179.266px', wrapClass: 'tw6g-kvawo27', align: null, label: <>토스증권 AI 요약<span className="t00n7r0" data-state="closed" data-tossinvest-priority-log="Tooltip.Trigger" data-contents-value="툴팁" data-content-tag="툴팁" data-parent-name="QuestionmarkTooltip" style={{ width: '0.9em' }}><svg xmlns="http://www.w3.org/2000/svg" viewBox="143 -1757 2014 2014" style={{ width: '1em' }}><path shapeRendering="geometricPrecision" d="M645 121.5Q414-14 278.5-245T143-750 278.5-1255 645-1621.5 1150-1757 1655-1621.5 2021.5-1255 2157-750 2021.5-245 1655 121.5 1150 257 645 121.5ZM1590 8.5Q1786-104 1898-303T2010-750 1898-1197 1590-1508.5 1150-1621 710-1508.5 402-1197 290-750 402-303 710 8.5 1150 121 1590 8.5ZM1078-761.5Q1102-801 1169-844 1233-881 1260.5-917.5T1288-1005Q1288-1061 1246-1099.5T1135-1138Q1068-1138 1023.5-1102T973-1008H820Q828-1081 872.5-1140.5T987-1234 1139-1268Q1227-1268 1295-1234T1401-1141 1439-1009Q1439-930 1403.5-870.5T1291-761Q1249-736 1230.5-720.5T1204.5-688 1197-642V-550H1054V-657Q1054-722 1078-761.5ZM1051.5-279.5Q1021-310 1021-354T1051.5-428.5 1125-459Q1169-459 1200-428.5T1231-354 1200-279.5 1125-249Q1082-249 1051.5-279.5Z" fill="currentColor" /></svg></span></> },
]

const HIDDEN_TD_STYLE = {
    height: '37px',
    padding: '0px',
    borderWidth: 'medium',
    borderStyle: 'none',
    borderColor: 'currentColor',
    borderImage: 'initial',
    visibility: 'hidden',
}

function LiveChartList({ stocklist }) {
    return (
        <>
            <div className="_1vjo0mn1" data-section-name="실시간차트">
                <div dir="ltr" style={{ position: 'relative', '--radix-scroll-area-corner-width': '0px', '--radix-scroll-area-corner-height': '0px', overflow: 'hidden', height: 'inherit' }}>
                    <style dangerouslySetInnerHTML={{ __html: "[data-radix-scroll-area-viewport]{scrollbar-width:none;-ms-overflow-style:none;-webkit-overflow-scrolling:touch;}[data-radix-scroll-area-viewport]::-webkit-scrollbar{display:none}" }} />
                    <div data-radix-scroll-area-viewport="" className="_15ndk3s0" style={{ overflow: 'scroll hidden' }}>
                        <div style={{ minWidth: '100%', display: 'table' }}>
                            <table className="tw6g-kvawo28 tw6g-kvawo29 tw6g-kvawo2b tw6g-kvawo2e _15ndk3s1" data-section-name="토스증권 거래대금">
                                <thead className="tw6g-4pu5o90 fpyfvc1" data-tabster='{"mover":{"cyclic":false,"direction":2,"memorizeCurrent":true}}' style={{ position: 'absolute', top: '0px' }}>
                                    <tr className="auto-zebra-pattern">
                                        {TABLE_COLUMNS.map((col) => (
                                            <th key={col.className} className={`tw6g-1apn5az0 fpyfvc0 ${col.className}`} style={{ width: col.width, minWidth: col.width, maxWidth: col.width }}>
                                                <div className={`tw6g-1apn5az2 tw6g-1apn5az1 tw6g-1apn5az4 ${col.wrapClass}`}>
                                                    <div className="tw6g-1apn5azd" style={col.align ? { textAlign: col.align } : undefined}>
                                                        {col.label}
                                                    </div>
                                                </div>
                                            </th>
                                        ))}
                                    </tr>
                                </thead>
                                <tbody data-tabster='{"mover":{"cyclic":false,"direction":3,"memorizeCurrent":true}}' style={{ '--m3gzbu0': '50.75px' }}>
                                    {/* 레이아웃용 숨김 행 */}
                                    <tr aria-hidden="true">
                                        {TABLE_COLUMNS.map((col) => (
                                            <td key={col.className} className={col.className} style={HIDDEN_TD_STYLE} />
                                        ))}
                                    </tr>

                                    {/* 실제 데이터 행 */}
                                    {stocklist.map((stocklist, index) => {
                                        const href = `/stock/${stocklist.stock.stockCode}`

                                        return (
                                            <tr key={stocklist.stock.stockCode} className="tw6g-1s07rpw0 yozeuq0" data-tossinvest-log="AnimatedRankingListRow" data-contents-value={stocklist.stock.stockName} data-content-tag="product_name" data-parent-name="RankingRow">
                                                <td className="tw6g-mq48z20 _1cblj813 _5x01f0" data-tabster='{"groupper":{},"focusable":{}}'>
                                                    <i tabIndex="0" role="none" data-tabster-dummy="" aria-hidden="true" style={{ position: 'fixed', height: '1px', width: '1px', opacity: 0.001, zIndex: -1, contentVisibility: 'hidden', top: '0px', left: '0px' }} />
                                                    <a data-tossinvest-log="Link" data-contents-value={stocklist.stock.stockName} data-content-tag="product_name" data-parent-name="AnimatedRankingListRow" className="tw6g-mq48z22 tw6g-kvawo25 tw6g-mq48z2a" href={href}>
                                                        <div className="tw6g-mq48z23">
                                                            <div className="tw6g-1e8fj1ar">
                                                                <button className="tw6g-emtxt715 tw6g-emtxt7p tw6g-emtxt7u tw6g-emtxt710 tw6g-emtxt716 _1ejaul80 _1cblj811" type="button" aria-disabled="false" aria-label="관심종목설정하기" data-theme="grey" data-variant="clear" data-mode="dark" data-tossinvest-log="ListRow.IconButton" data-contents-label={stocklist.stock.stockName} data-contents-value={stocklist.stock.stockName} data-content-tag="productName">
                                                                    <span className="tw6g-17xiat90 tw6g-17xiat91" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px' }}>
                                                                        <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                                            <path d="m22.223 5.572c-1.107-1.842-2.963-2.94-4.966-2.94-2.969 0-4.549 1.865-5.257 3.062-.708-1.197-2.288-3.062-5.257-3.062-2.003 0-3.858 1.099-4.966 2.94-1.329 2.211-1.317 5.047.031 7.586 1.973 3.714 6.359 6.977 8.798 8.59.424.28.908.421 1.394.421s.97-.141 1.394-.421c2.438-1.613 6.825-4.876 8.798-8.59 1.349-2.539 1.36-5.375.031-7.586z" fill='#b0b8c1' />
                                                                        </svg>
                                                                    </span>
                                                                </button>
                                                            </div>
                                                            <span className="tw6g-7u17ff0 _1cblj812">
                                                                <span className="tw6g-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'inherit', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>{index + 1}</span>
                                                            </span>
                                                            <div data-nosnippet="true" className="favgr63 favgr60">
                                                                <div className="c3f3of0 favgr6c favgr69">
                                                                    <img alt="logo" loading="lazy" width="30" height="30" decoding="async" src={`https://images.tossinvest.com/...`} style={{ color: 'transparent' }} />
                                                                </div>
                                                                <span className="favgr6u favgr6r favgr6q" />
                                                            </div>
                                                        </div>
                                                        <div className="tw6g-mq48z2h">
                                                            <span className="tw6g-1r5dc8g0 _1cblj810 _60z0ev1 _60z0ev2 _60z0ev0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>{stocklist.stock.stockName}</span>
                                                        </div>
                                                    </a>
                                                    <i tabIndex="0" role="none" data-tabster-dummy="" aria-hidden="true" style={{ position: 'fixed', height: '1px', width: '1px', opacity: 0.001, zIndex: -1, contentVisibility: 'hidden', top: '0px', left: '0px' }} />
                                                </td>
                                                <td className="tw6g-mq48z20 _1p5yqoh0 _5x01f1">
                                                    <a data-tossinvest-log="Link" data-contents-value={stocklist.stockName} data-content-tag="product_name" data-parent-name="AnimatedRankingListRow" className="tw6g-mq48z22 tw6g-kvawo27 tw6g-mq48z2a" href={href}>
                                                        <div className="tw6g-mq48z2h">
                                                            <span className="tw6g-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>
                                                                <span className="_1p5yqoh0">{formatPrice(stocklist.stock.closePrice)}</span>
                                                            </span>
                                                        </div>
                                                    </a>
                                                </td>
                                                <td className="tw6g-mq48z20 _1p5yqoh0 _5x01f2">
                                                    <a data-tossinvest-log="Link" data-contents-value={stocklist.stock.stockName} data-content-tag="product_name" data-parent-name="AnimatedRankingListRow" className="tw6g-mq48z22 tw6g-kvawo27 tw6g-mq48z2a" href={href}>
                                                        <div className="tw6g-mq48z2h">
                                                            <div className="_14c0oc30">
                                                                <span className="tw6g-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>
                                                                    <span className="_1p5yqoh0" style={{ color: getChangeColor(stocklist.stock.changeRate) }}>{formatChangeRate(stocklist.stock.changeRate)}</span>
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </a>
                                                </td>
                                                <td className="tw6g-mq48z20 _1p5yqoh0 _5x01f3">
                                                    <a data-tossinvest-log="Link" data-contents-value={stocklist.stockName} data-content-tag="product_name" data-parent-name="AnimatedRankingListRow" className="tw6g-mq48z22 tw6g-kvawo27 tw6g-mq48z2a" href={href}>
                                                        <div className="tw6g-mq48z2h">
                                                            <span className="tw6g-1r5dc8g0" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-grey800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>
                                                                {formatValue(stocklist.tradeStatus.tradeAmount)}
                                                            </span>
                                                        </div>
                                                    </a>
                                                </td>
                                                <td className="tw6g-mq48z20 _5x01f5">
                                                    <a data-tossinvest-log="Link" data-contents-value={stocklist.stockName} data-content-tag="product_name" data-parent-name="AnimatedRankingListRow" className="tw6g-mq48z22 tw6g-kvawo27 tw6g-mq48z2a" href={href}>
                                                        <div className="tw6g-mq48z2h">
                                                            <TradeRatioBar tradeStatus={stocklist.tradeStatus} />
                                                        </div>
                                                    </a>
                                                </td>
                                                <td className="tw6g-mq48z20 _5x01f6">
                                                    <a data-tossinvest-log="Link" data-contents-value={stocklist.stockName} data-content-tag="product_name" data-parent-name="AnimatedRankingListRow" className="tw6g-mq48z22 tw6g-kvawo27 tw6g-mq48z2a" href={href}>
                                                        <div className="tw6g-mq48z2h" />
                                                    </a>
                                                </td>
                                            </tr>
                                        )
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div style={{ flex: '0 0 auto', height: '200px' }} />
            </div>
        </>
    )
}

function TradeRatioBar({ tradeStatus }) {
    const { buyRatio, sellRatio, sellColor,buyColor } = getTradeRatio(tradeStatus);

    return (
        <div className="_6ivj9p0">
            <div className="_6ivj9p1">
                <div className="_6ivj9p2" style={{ backgroundColor: sellColor, width: `${sellRatio}px` }} />
                <div className="_6ivj9p2" style={{ backgroundColor: buyColor, width: `${buyRatio}px` }} />
            </div>
            <div className="_6ivj9p1">
                <span className="tw6g-1r5dc8g0" style={{ textAlign: 'start', '--tds-wts-font-weight': 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-blue600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                    {sellRatio}
                </span>
                <span className="tw6g-1r5dc8g0" style={{ textAlign: 'end', '--tds-wts-font-weight': 'var(--tw-font-weight-regular)', '--tds-wts-foreground-color': 'var(--wts-adaptive-red600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '12px' }}>
                    {buyRatio}
                </span>
            </div>
        </div>
    );
}

function LiveChartListSide() {
    return (
        <div className="_1vjo0mn2">
            <div className="_1vjo0mn3">
                <aside className="hev4ie0" data-section-name="종목요약">
                    <h3 className="sr-only">종목 정보</h3>
                    <a data-tossinvest-log="Link" data-contents-value="디렉시온 마이크론 테크놀로지 2배 ETF" data-content-tag="contentsValue" href="/stocks/NAS0241010007/order">...</a>
                    <div dir="ltr" style={{ position: 'relative', '--radix-scroll-area-corner-width': '0px', '--radix-scroll-area-corner-height': '0px', height: 'calc(-188.047px + 100vh)' }}>...</div>
                </aside>
            </div>
        </div>
    )
}

function StockRegionFilter() {
    const items = [
        { label: '전체', value: 'all' },
        { label: '국내', value: 'kr' },
        { label: '해외', value: 'global' }
    ];
    return (
        <div role="radiogroup" className="tw6g-1sni4y90 tw6g-1sni4y92 tw6g-1sni4y96" tabIndex="0" style={{ outline: 'none' }}>
            <div className="tw6g-1sni4y97 tw6g-1sni4y99" style={{ boxShadow: 'rgba(0, 0, 0, 0.15) 0px 1px 3px 0px', width: '44px', transform: 'none' }}></div>
            {items.map((item, idx) => (
                <FilterButton key={item.value} label={item.label} value={item.value} isActive={idx === 0} />
            ))}
        </div>
    )
}

function StockTradeFilter() {
    const items = [
        { label: '토스증권 거래대금', value: 'toss_amount' },
        { label: '토스증권 거래량', value: 'toss_volume' },
        { label: '거래 대금', value: 'market_amount' },
        { label: '거래량', value: 'market_volume' },
        { label: '급 상승', value: 'surge' },
        { label: '급 하락', value: 'plunge' }
    ];
    return (
        <div style={{ display: 'flex', alignItems: 'center' }}>
            <div role="radiogroup" className="tw6g-1sni4y90 tw6g-1sni4y92 tw6g-1sni4y96" tabIndex="0" style={{ outline: 'none' }}>
                <div className="tw6g-1sni4y97 tw6g-1sni4y99" style={{ boxShadow: 'rgba(0, 0, 0, 0.15) 0px 1px 3px 0px', width: '124px', transform: 'none' }}></div>
                {items.map((item, idx) => (
                    <FilterButton key={item.value} label={item.label} value={item.value} isActive={idx === 0} />
                ))}
            </div>
        </div>
    )
}

function StockTimeFilter() {
    const items = [
        { label: '실시간', value: 'realtime' },
        { label: '1일', value: '1d' },
        { label: '1주일', value: '1w' },
        { label: '1개월', value: '1m' },
        { label: '3개월', value: '3m' },
        { label: '6개월', value: '6m' },
        { label: '1년', value: '1y' }
    ];
    return (
        <div role="radiogroup" className="tw6g-1sni4y90 tw6g-1sni4y92 tw6g-1sni4y95" tabIndex="0" style={{ outline: 'none' }}>
            <div className="tw6g-1sni4y97 tw6g-1sni4y99" style={{ boxShadow: 'rgba(0, 0, 0, 0.15) 0px 1px 3px 0px', width: '57px', transform: 'none' }}></div>
            {items.map((item, idx) => (
                <FilterButton key={item.value} label={item.label} value={item.value} isActive={idx === 0} />
            ))}
        </div>
    )
}
const FilterButton = ({ label, value, isActive }) => (
    <button type="button" role="radio" aria-checked={isActive} data-state={isActive ? "checked" : "unchecked"} value={value} className="tw6g-1cq3gqg0 tw6g-1cq3gqg1" data-seg-state={isActive ? "checked" : "unchecked"} data-tossinvest-log="SegmentedControl.Item" data-contents-label={label} data-contents-label-code={label} data-contents-value={label} data-content-tag={label} data-parent-name="RealtimeRankingMarkets" tabIndex="-1">
        <div className="tw6g-1cq3gqg3 tw6g-1cq3gqg5">
            <div className="tw6g-1cq3gqg8">
                <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqgb" aria-hidden="true" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>{label}</span>
                <span className="tw6g-1r5dc8g0 tw6g-1cq3gqg9 tw6g-1cq3gqgb" style={{ '--tds-wts-font-weight': isActive ? 'var(--tw-font-weight-semibold)' : 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': isActive ? 'var(--wts-adaptive-greyOpacity800)' : 'var(--wts-adaptive-greyOpacity600)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '14px' }}>{label}</span>
            </div>
        </div>
    </button>
);


function StockDangerListFilter() {
    return (
        <button type="button" aria-disabled="false" className="tw6g-1wkoka52h tw6g-1wkoka59 tw6g-1wkoka5e tw6g-1wkoka517 tw6g-1wkoka5x tw6g-1wkoka5r tw6g-1wkoka5l tw6g-1wkoka528 tw6g-1wkoka537" data-tds-wts-button="" data-tossinvest-log="Button" data-contents-label="투자위험 주식 숨기기" data-contents-label-code="투자위험 주식 숨기기" data-contents-value="투자위험 주식 숨기기" data-content-tag="투자위험_주식_숨기기" data-parent-name="RankingDangerousStockFilter" data-checked="true" style={{ color: 'var(--wts-adaptive-blue700)' }}>
            <div className="tw6g-1wkoka532 tw6g-1wkoka52q tw6g-1wkoka51y">
                <span className="tw6g-17xiat90" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px' }}>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" className="">
                        <g fill="none" fillRule="evenodd">
                            <path fill="#3180F2" d="M23 12c0 6.075-4.925 11-11 11S1 18.075 1 12 5.925 1 12 1s11 4.925 11 11"></path>
                            <path stroke="#FFF" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M7.5 11.676l3.416 3.416L16.5 9.508"></path>
                        </g>
                    </svg>
                </span>
            </div>
            <span className="tw6g-1wkoka52g">투자위험 주식 숨기기</span>
        </button>
    )
}

function StockMainTab() {
    const tabs = [
        { id: 'realtime_chart', label: '실시간 차트' },
        { id: 'trending_category', label: '지금 뜨는 카테고리' }
    ];
    const activeTab = 'realtime_chart';

    return (
        <div dir="ltr" data-orientation="horizontal" className="tw6g-336bzic tw6g-336bzie" style={{ padding: '0px 8px' }} data-section-name="메인탭">
            <div className="tw6g-336bzix">
                <div role="tablist" aria-orientation="horizontal" className="tw6g-336bzih" tabIndex="0" data-orientation="horizontal" style={{ outline: 'none' }} data-scrollable="false">
                    <div className="tw6g-336bziw tw6g-336bziu" style={{ width: '68px', transform: 'none' }}></div>
                    {tabs.map((tab) => {
                        const isActive = activeTab === tab.id;
                        return (
                            <button key={tab.id} type="button" role="tab" aria-selected={isActive} aria-controls={`radix-_r7b7_-content-${tab.id}`} data-state={isActive ? "active" : "inactive"} id={`radix-_r7b7_-trigger-${tab.id}`} data-tossinvest-log="Tab.Item" data-contents-value={tab.label} data-content-tag="category_label" className="tw6g-336bzit tw6g-336bzil tw6g-336bzir" data-tds-wts-tab-fit="" tabIndex="-1" data-orientation="horizontal" data-radix-collection-item="">
                                <div style={{ position: 'relative' }}>
                                    <span className="tw6g-1r5dc8g0 tw6g-336bzij tw6g-336bzii tw6g-336bziy" aria-hidden="true" data-tds-wts-tab-item="false" style={{ '--tds-wts-font-weight': 'var(--tw-font-weight-semibold)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>{tab.label}</span>
                                    <span className="tw6g-1r5dc8g0 tw6g-336bzij tw6g-336bzii" data-tds-wts-tab-item={isActive ? "true" : "false"} style={{ '--tds-wts-font-weight': isActive ? 'var(--tw-font-weight-semibold)' : 'var(--tw-font-weight-medium)', '--tds-wts-foreground-color': 'var(--wts-adaptive-greyOpacity800)', '--tds-wts-line-height': '1.45', '--tds-wts-font-size': '15px' }}>{tab.label}</span>
                                </div>
                            </button>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}