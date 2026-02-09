import StockListForm from "../../features/StockList/StockListForm";

export default async function StockListPage() {
      
const stockCodes = ['005930', '000660', '035420'];

    return (
        <StockListForm stockCodes={stockCodes}/>
    );
}
