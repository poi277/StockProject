import { apiFetch } from '../util/apiClient';
import { API_URL,ORDER_API_URL } from '../util/URLconfig';

export async function tradeApi(tradeType,stockCode,quantity,tradePrice) {
  return await apiFetch(`${ORDER_API_URL}/order/trade`,{
    method: 'POST',
    auth: true,
    body: JSON.stringify({
          tradeType,
          stockCode,
          quantity: Number(quantity),
          tradePrice
        }),
    })
}
export async function getOrderbookApi(stockCode) {
   return await apiFetch(`${ORDER_API_URL}/order/orderbook/${stockCode}`)
}

export async function getMyOrder() {
   return await apiFetch(`${ORDER_API_URL}/order/myorder`,{auth:true})
}

export async function cancelOrder(orderId) {
   return await apiFetch(`${ORDER_API_URL}/order/cancel/${orderId}`, { auth: true, method: 'POST' })
}