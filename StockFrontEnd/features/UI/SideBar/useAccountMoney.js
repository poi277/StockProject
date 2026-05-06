import { UserHaveAssetContext } from "../../../util/websocket/UserHaveAssetProvider";

export default function useAccountMoney() {
    const { asset, availableAsset } = UserHaveAssetContext();
    

    const MONEY_ITEMS = [
        {
            label: "원화",
            value: asset != null ? `${asset.toLocaleString()}원` : "로딩중...",
            href: "/account/transactions/kr",
            borderRadius: "12px 0px 0px 12px",
            contentsValue: `총 자산 ${asset?.toLocaleString()}원`
        },
        {
            label: "달러(지금은 사용금액)",
            value: availableAsset != null ? `${availableAsset.toLocaleString()}원` : "로딩중...",
            href: "/account/transactions/us",
            borderRadius: "0px 12px 12px 0px",
            contentsValue: `사용 가능 ${availableAsset?.toLocaleString()}원`
        },
    ];

    return { MONEY_ITEMS };
}