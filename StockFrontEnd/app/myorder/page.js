import { getMyOrder } from "../../lib/trade";
import MyOrderForm from "../../features/myorder/MyOrderForm"

export default async function myOrder() {
    const res = await getMyOrder();
    if (!res.success) {
        throw new Error(res.message); 
    }
    return (
        <div>
            <MyOrderForm myOrder={res.data} />
        </div>
    );
}