import StockListForm from "../../features/StockList/StockListForm";
import TossStockList from "../../features/StockList/TossStockListForm";
import { stockListApi } from "../../lib/stock";

export default async function Home() {
    
    return (
        <TossStockList/>
    );
}
