import StockDetailForm from "../../../features/StockDetail/StockDetailForm";
import { StockDetailApi, } from "../../../lib/stock";
import { isWatchedApi } from "../../../lib/watchlist";

export default async function StockDetail({ params }) {
    const { stockCode } = await params;
    const [res, watchRes] = await Promise.all([
        StockDetailApi(stockCode),
    ]);
    return (
        <StockDetailForm stock={res.data} />
    );
}