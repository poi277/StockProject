import StockDetailForm from "../../../features/StockDetail/StockDetailForm";
import { StockDetailApi } from "../../../lib/stock";

export default async function StockDetail({ params }) {
    const {stockCode} = await params;
    const res = await StockDetailApi(stockCode)
    console.log(res)
    if (!res?.success) {
        throw new Error(res.message);
    }
    return (
        <div>
            <StockDetailForm stock={res.data.stock} watched={res.data.watched} />
        </div>
    );
}