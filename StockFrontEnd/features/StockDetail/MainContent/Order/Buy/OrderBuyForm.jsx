import { OrderLoc, OrderResult, PriceForm, QuantityForm, SubmitButton } from "../Commonutil/OrderCommon";
import useOrderBuy from "./useOrderBuy";

export default function OrderBuyForm({executeOrder,tradeTypeTab,priceType,setPriceType,buyQuantity,setBuyQuantity,buyPrice,setBuyPrice}) {

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
                    <PriceForm priceLabel={"구매"} priceType={priceType} price={buyPrice} setPrice={setBuyPrice} setPriceType={setPriceType} />
                    <QuantityForm quantity={buyQuantity} setQuantity={setBuyQuantity} isPending={false} />
                    <OrderResult />
                    <SubmitButton tradeTypeTab={tradeTypeTab} tradeType={tradeTypeTab} />
                </div>
            </form>
        </div>
    )
}