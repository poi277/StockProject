import { getMyAllOrder } from "../../lib/order";
import MyOrderForm from "../../features/myorder/MyOrderForm";

export default async function myOrder() {
    const res = await getMyAllOrder();
    if (!res.success) {
        throw new Error(res.message); 
    }
    return (
        <MyOrderForm myOrder={res.data} />
    );
}
