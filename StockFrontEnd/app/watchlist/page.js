import WatchListForm from "../../features/watchList/WatchlistForm";
import { getWatchListApi } from "../../lib/watchlist";

export default async function WatchListPage() {
    const res = await getWatchListApi();
    
    if (!res.success) {
        throw new Error(res.message); 
    }

    return <WatchListForm initialWatchList={res.data ?? []} />;
}