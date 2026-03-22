"use client"
import { useState } from "react";
import { addWatchApi, removeWatchApi } from "../../lib/watchlist";

export default function useWatch(stockCode, initialWatched = false) {
    const [isWatched, setIsWatched] = useState(initialWatched);
    const [watchLoading, setWatchLoading] = useState(false);


    const handleWatchToggle = async () => {
        if (watchLoading) return;
        setWatchLoading(true);
        try {
            if (isWatched) {
                await removeWatchApi(stockCode);
                setIsWatched(false);
            } else {
                await addWatchApi(stockCode);
                setIsWatched(true);
            }
        } catch (err) {
            console.error('관심종목 처리 실패:', err);
        } finally {
            setWatchLoading(false);
        }
    };

    return { isWatched, handleWatchToggle, watchLoading };
}