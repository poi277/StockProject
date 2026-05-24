import StockHeaderGrid from "./StockHeaderGrid";
import './StockHeaderTabs.css'

const SCROLL_BUTTONS = [
  { label: '왼쪽으로 스크롤', tag: '왼쪽으로_스크롤', path: "m4.069 8c0-.23.087-.46.263-.636l4.5-4.5c.226-.235.561-.331.877-.248.316.082.562.328.644.644.082.315-.012.651-.248.877l-3.864 3.864 3.864 3.864c.235.226.33.562.248.877-.082.316-.328.562-.644.644-.316.083-.651-.013-.877-.248l-4.5-4.5c-.168-.169-.263-.398-.263-.636", containerClass: '_8u2t3p4 _8u2t3p2 _8u2t3p3' },
  { label: '오른쪽으로 스크롤', tag: '오른쪽으로_스크롤', path: "M5.75 13.4c-.23 0-.46-.09-.64-.26a.9.9 0 010-1.27L8.98 8 5.11 4.14a.9.9 0 010-1.27.9.9 0 011.27 0l4.5 4.5a.9.9 0 010 1.27l-4.5 4.5c-.17.17-.4.26-.63.26", containerClass: '_8u2t3p5 _8u2t3p2 _8u2t3p3' },
];

export default function StockHeaderTabs({stock}) {
  return (
    <>
      <div className={SCROLL_BUTTONS[0].containerClass}><ScrollButton {...SCROLL_BUTTONS[0]} /></div>
      <div className="_8u2t3p1" data-scroll-root="true"><StockHeaderGrid stock ={stock} /></div>
      <div className={SCROLL_BUTTONS[1].containerClass}><ScrollButton {...SCROLL_BUTTONS[1]} /></div>
    </>
  );
}

function ScrollButton({ label, tag, path }) {
  return (
    <button className="tw3v-emtxt715 tw3v-emtxt7p tw3v-emtxt7t tw3v-emtxt710 tw3v-emtxt716" aria-disabled="false" aria-label={label} type="button" data-theme="grey" data-variant="clear" data-mode="dark" data-tossinvest-log="IconButton" data-contents-value={label} data-content-tag={tag} data-parent-name="HorizontalScrollArea">
      <span className="tw3v-17xiat90 tw3v-17xiat91" aria-hidden="false" role="presentation" style={{ height: '14px', width: '14px', minWidth: '14px' }}>
        <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg"><path d={path} fill="#b0b8c1" fillRule="evenodd" /></svg>
      </span>
    </button>
  );
}