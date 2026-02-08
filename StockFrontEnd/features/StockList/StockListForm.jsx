export default function StockListForm({ data }) {
    return (
        <div>
            <h2>Stock List</h2>

            {data?.map((stock) => (
                <div key={stock.id}>
                    <span> - {stock.price}</span>
                </div>
            ))}
        </div>
    );
}
