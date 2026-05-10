import { UserHaveAssetContext } from "../../../util/websocket/UserHaveAssetProvider";

export default function useOrderSideBar()
{
    const { orders,setOrders } = UserHaveAssetContext();

    const TAB_ITEMS = [
        { label: "대기", controls: "pending", selected: true, state: "active", activeWeight: "semibold" },
        { label: "완료", controls: "completed", selected: false, state: "inactive", activeWeight: "medium" },
        { label: "조건주문", controls: "conditionalOrder", selected: false, state: "inactive", activeWeight: "medium" },
    ];

    return {TAB_ITEMS,orders,setOrders}
}