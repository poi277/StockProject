import { getWatchListApi, removeWatchApi } from "../../lib/watchlist";

export default function useWatchList() {
    const [watchList, setWatchList] = useState([]);

    useEffect(() => {
        fetchWatchList();
    }, []);

    const fetchWatchList = async () => {
        const res = await getWatchListApi();
        setWatchList(res.data);
    };

    const handleRemove = async (stockCode) => {
        await removeWatchApi(stockCode);
        fetchWatchList();
    };

    return { watchList, handleRemove };
}