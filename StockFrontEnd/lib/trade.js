import { apiFetch } from '../util/apiClient';
import { API_URL,ORDER_API_URL } from '../util/URLconfig';


export async function getOrderbookApi(stockCode) {
   return await apiFetch(`${ORDER_API_URL}/order/orderbook/${stockCode}`)
}

