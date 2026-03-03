import useWatchList from "./Watchlist";

export default function WatchListForm() {
    const { watchList, handleRemove } = useWatchList();

    return (
        <div>
            {watchList.map((item) => (
                <div key={item.id}>
                    <span>{item.stockCode}</span>
                    <button onClick={() => handleRemove(item.stockCode)}>삭제</button>
                </div>
            ))}
        </div>
    );
}