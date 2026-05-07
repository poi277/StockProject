import { useEffect, useState } from "react";
import { useWebSocket } from "../../../util/websocket/context/WebSocketContext";
import { useStocksSocket } from "../../../util/websocket/useStocksSocket";
import { getStocksByCodesApi } from "../../../lib/stock";
import { UserHaveAssetContext } from "../../../util/websocket/UserHaveAssetProvider";

export default function useHaveMyStockAsset() {
    const { haveStocks,stocksArray, totalInvestment, totalDiff, totalRate } = UserHaveAssetContext();

    const SEGMENT_ITEMS = [
        { label: "현재가", value: "left", checked: false, state: "unchecked", activeWeight: "medium", activeColor: "var(--wts-adaptive-greyOpacity600)" },
        { label: "평가금", value: "right", checked: true, state: "checked", activeWeight: "semibold", activeColor: "var(--wts-adaptive-greyOpacity800)" },
    ];

    return { SEGMENT_ITEMS, haveStocks, totalInvestment, totalDiff, totalRate, stocks:stocksArray };
}