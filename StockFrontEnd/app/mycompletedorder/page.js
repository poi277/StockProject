import MyCompletedForm from "../../features/myCompletedOrder/MyCompletedOrder";
import { getMyCompletedOrder } from "../../lib/order";

export default async function myCompletedOrder() {
    const res = await getMyCompletedOrder();
    console.log(res)
    if (!res.success) {
        throw new Error(res.message); 
    }
    return (
        <div>
            <MyCompletedForm myCompletedOrder={res.data} />
        </div>
    );
}