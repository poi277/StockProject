import { UserHaveAssetContext } from "../../../../util/websocket/UserHaveAssetProvider";

export default function useHaveStock({ stockCode }) {
    const { stocksArray, haveStocks } = UserHaveAssetContext();

    const stock = stocksArray?.find(s => s.stockCode === stockCode);
    const matched = haveStocks?.find(h => h.stockCode === stockCode);

    const STOCK_INFO_ROWS = [
        { label: "총 금액", value: stock ? stock.evaluatedAmount.toLocaleString() + "원" : "-" },
        { label: "수량", value: matched ? matched.quantity + "주" : "-" },
        { label: "1주 평균 금액", value: matched ? matched.averagePrice.toLocaleString() + "원" : "-" },
    ];

    const totalDiff = stock?.diff ?? 0;
    const totalRate = stock?.rate ?? 0;

    return { STOCK_INFO_ROWS, totalDiff, totalRate, stock };
}