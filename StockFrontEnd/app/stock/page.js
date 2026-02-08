import StockListForm from "../../features/StockList/StockListForm";
import { stockListApi } from "../../lib/stock";

export default async function StockListPage() {

    const stock = await stockListApi();

    console.log(stock.data)
    return (
        <StockListForm data={stock.data} />
    );
}
