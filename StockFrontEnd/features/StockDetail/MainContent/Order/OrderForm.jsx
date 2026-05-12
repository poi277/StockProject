import { useState } from 'react'
import './OrderForm.css'
import useOrder, { DUMMY_PENDING_ORDERS } from './useOrder';
import { OrderLoc } from './Commonutil/OrderCommon';
import OrderBuyForm from './Buy/OrderBuyForm';
import OrderSellForm from './Sell/OrderSellForm';
import OrderPendingForm from './Edit/OrderPendingForm';
import useOrderBuy from './Buy/useOrderBuy';    
import useOrderSell from './Sell/useOrderSell';
import useOrderEdit from './Edit/useOrderEdit';

export default function OrderForm({ selectedPrice, stockCode }) {
    const [tradeTypeTab, setTradeTypeTab] = useState("BUY")
    const [priceType, setPriceType] = useState("limit")
    const { sellExcuteOrder, sellPrice, setSellPrice, sellQuantity, setSellQuantity } = useOrderSell(selectedPrice, stockCode, tradeTypeTab, priceType);
    const { buyExecuteOrder, buyPrice, setBuyPrice, buyQuantity, setBuyQuantity } = useOrderBuy(selectedPrice, stockCode, tradeTypeTab, priceType);
    const { editExecuteOrder,
        edit, setEdit,
        editTarget, setEditTarget,
        editPrice, setEditPrice,
        editQuantity, setEditQuantity,
        editPriceType, setEditPriceType,
        handleEditOpen,
        handleEditClose, stockOrders } = useOrderEdit(selectedPrice, stockCode, tradeTypeTab);

    return (
        <div className="sa1m6r0">
            <div className="sa1m6r1">
                <div style={{ display: "flex", flexDirection: "column", height: "100%", overflow: "auto" }}>
                    <div style={{ flex: "1 1 0", minHeight: "0px" }}>
                        <OrderTypeTab tradeType={tradeTypeTab} setTradeType={setTradeTypeTab} />
                        {tradeTypeTab === 'BUY' && (
                            <OrderBuyForm
                                tradeTypeTab={tradeTypeTab}
                                priceType={priceType}
                                setPriceType={setPriceType}
                                stockCode={stockCode}
                                selectedPrice={selectedPrice}
                                buyPrice={buyPrice}
                                setBuyPrice={setBuyPrice}
                                buyQuantity={buyQuantity}
                                setBuyQuantity={setBuyQuantity}
                                executeOrder={buyExecuteOrder}
                            />
                        )}
                        {tradeTypeTab === 'SELL' && (
                            <OrderSellForm
                                tradeTypeTab={tradeTypeTab}
                                priceType={priceType}
                                setPriceType={setPriceType}
                                stockCode={stockCode}
                                selectedPrice={selectedPrice}
                                sellPrice={sellPrice}
                                setSellPrice={setSellPrice}
                                sellQuantity={sellQuantity}
                                setSellQuantity={setSellQuantity}
                                executeOrder={sellExcuteOrder}
                            />
                        )}
                        {tradeTypeTab === 'PENDING' && (
                            <OrderPendingForm
                                tradeTypeTab={tradeTypeTab}
                                stockCode={stockCode}
                                selectedPrice={selectedPrice}
                                executeOrder={editExecuteOrder}
                                edit={edit}
                                setEdit={setEdit}
                                editTarget={editTarget}
                                setEditTarget={setEditTarget}
                                editPrice={editPrice}
                                setEditPrice={setEditPrice}
                                editQuantity={editQuantity}
                                setEditQuantity={setEditQuantity}
                                editPriceType={editPriceType}
                                setEditPriceType={setEditPriceType}
                                handleEditOpen={handleEditOpen}
                                handleEditClose={handleEditClose}
                                orders={stockOrders}
                            />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

function OrderTypeTab({ tradeType, setTradeType }) {
    const TABS = [
        { value: 'BUY', label: '구매', activeClass: 'xl0v5qo', containerClass: 'xl0v5qg' },
        { value: 'SELL', label: '판매', activeClass: 'xl0v5qp', containerClass: 'xl0v5qh' },
        { value: 'PENDING', label: '대기', activeClass: 'xl0v5qq', containerClass: 'xl0v5qi' },
    ];
    const currentIndex = TABS.findIndex(t => t.value === tradeType);
    const colorClass = TABS[currentIndex]?.containerClass ?? 'xl0v5qg';
    
    return (
        <div className="xl0v5qc">
            <div
                role="radiogroup"
                aria-required="false"
                dir="ltr"
                className={`tw3v-1sni4y90 tw3v-1sni4y92 tw3v-1sni4y95 xl0v5qf xl0v5qk xl0v5ql ${colorClass}`}
                tabIndex="0"
                style={{ outline: "none", position: "relative" }}
                data-scrollable="false"
            >
                <div
                    className="tw3v-1sni4y97 tw3v-1sni4y99"
                    style={{
                        boxShadow: "rgba(0, 0, 0, 0.15) 0px 1px 3px 0px",
                        width: "33.333%",
                        transform: currentIndex === 0 ? "none" : `translateX(${currentIndex * 100}%)`,
                    }}
                ></div>
                {TABS.map(({ value, label, activeClass }) => (
                    <button
                        key={value}
                        type="button"
                        role="radio"
                        aria-checked={tradeType === value}
                        className={`tw3v-1cq3gqg0 tw3v-1cq3gqg2 xl0v5qn ${tradeType === value ? activeClass : 'xl0v5qr'}`}
                        style={{ flex: 1, zIndex: 1, position: "relative", background: "transparent" }}
                        onClick={() => setTradeType(value)}
                    >
                        <div className="tw3v-1cq3gqg3 tw3v-1cq3gqg5">
                            <div className="tw3v-1cq3gqg8">
                                <span className="tw3v-1r5dc8g0" style={{ "--tds-wts-font-weight": "var(--tw-font-weight-semibold)", "--tds-wts-foreground-color": "var(--wts-adaptive-greyOpacity800)", "--tds-wts-line-height": "1.45", "--tds-wts-font-size": "14px" }}>{label}</span>
                            </div>
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
}