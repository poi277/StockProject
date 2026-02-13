import StockListForm from "../../features/StockList/StockListForm";
import { stockListApi } from "../../lib/stock";

export default async function Home() {
      
const stocklist = await stockListApi()
const stockArray = Array.isArray(stocklist.data) ? stocklist.data : [];

    return (
        <StockListForm stocklist={stockArray}/>
    );
}
