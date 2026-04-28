import { OrderLoc, OrderResult, PriceForm, QuantityForm, SubmitButton } from "../Commonutil/OrderCommon";
import useOrderSell from "./useOrderSell";

export default function OrderSellForm({executeOrder,tradeTypeTab,priceType,setPriceType,sellQuantity,setSellQuantity,sellPrice,setSellPrice}) {
 
    return (
        <div id="trade-order-section">
            <OrderLoc/>
            <form id="new-order-form" onSubmit={(e) => { e.preventDefault(); executeOrder({ tradeTypeTab: tradeTypeTab }); }} className="xl0v5q1" method="post" data-gtm-form-interact-id="3">
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
                    <PriceForm priceLabel={"판매"} priceType={priceType} price={sellPrice} setPrice={setSellPrice} setPriceType={setPriceType} />
                    <QuantityForm quantity={sellQuantity} setQuantity={setSellQuantity} isPending={false} />
                    <OrderResult />
                    <SubmitButton tradeTypeTab={tradeTypeTab} tradeType={tradeTypeTab} />
                </div>
            </form>
        </div>
    )
}