import StockDetailForm from "../../../features/StockDetail/StockDetailForm";
import { stockApi } from "../../../lib/stock";

export default async function StockDetail({ params }) {
    const { stockId } = await params;

    const stock = await stockApi(stockId);
    return (
        <StockDetailForm data={stock.data} />
    );
}
    