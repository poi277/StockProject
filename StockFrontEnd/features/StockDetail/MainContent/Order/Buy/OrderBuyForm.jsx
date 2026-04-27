import { OrderLoc, OrderResult, PriceForm, QuantityForm, SubmitButton } from "../Commonutil/OrderCommon";

export default function OrderBuyForm({tradeTypeTab}) {
    return (
        <div id="trade-order-section">
            <OrderLoc/>
            <form id="new-order-form" onSubmit={(e) => { e.preventDefault(); executeOrder({ tradeType: tradeTypeTab }); }} className="xl0v5q1" method="post" data-gtm-form-interact-id="3">
                <input type="hidden" value="A005930" name="stockCode" />
                <input type="hidden" value="BUY" name="tradeType" />
                <input type="hidden" value="KSP" name="market" />
                <input type="hidden" value="KRW" name="currencyMode" />
                <input type="hidden" value="1481.4" name="exchangeRate" />
                <input type="hidden" value="false" name="marginTrading" />
                <input type="hidden" value="false" name="noAutoExchange" />
                <input type="hidden" value="false" name="buyMaxQuantity" />
                <input type="hidden" value={priceType ? "00" : "03"} name="orderPriceType" />
                <div className="xl0v5q2" id="trade-order-section">
                    <PriceForm priceLabel={priceLabel} priceType={priceType} price={price} setPrice={setPrice} setPriceType={setPriceType} />
                    <QuantityForm quantity={quantity} setQuantity={setQuantity} isPending={false} />
                    <OrderResult />
                    <SubmitButton tradeTypeTab={tradeTypeTab} tradeType={tradeTypeTab} />
                </div>
            </form>
        </div>
    )
}