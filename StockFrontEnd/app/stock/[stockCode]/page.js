import StockDetailForm from "../../../features/StockDetail/StockDetailForm";
import { stockApi } from "../../../lib/stock";

export default async function StockDetail({ params }) {
    const {stockCode} = await params;
    return (
        <div>
            <StockDetailForm stockCode={stockCode} />
        </div>
    );
}
    