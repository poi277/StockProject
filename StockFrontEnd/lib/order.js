import { apiFetch } from '../util/apiClient';
import { ORDER_API_URL } from '../util/URLconfig';

export async function orderApi(tradeType,stockCode,stockName,quantity,tradePrice) {
  console.log(tradeType,stockCode,quantity,tradePrice)
  return await apiFetch(`${ORDER_API_URL}/order/trade`,{
    method: 'POST',
    auth: true,
    body: JSON.stringify({
          tradeType,
          stockCode,
          stockName,
          quantity: Number(quantity),
          tradePrice
        }),
    })
}

export async function getMyCompletedOrder() {
   return await apiFetch(`${ORDER_API_URL}/completed/order`, { auth: true})
}

export async function editOrderApi(orderId,tradeType,stockName,stockCode,quantity,tradePrice) {
  console.log(tradeType,stockCode,quantity,tradePrice)
  return await apiFetch(`${ORDER_API_URL}/order/edit`,{
    method: 'POST',
    auth: true,
    body: JSON.stringify({
          orderId,
          tradeType,
          stockCode,
          stockName,
          quantity: Number(quantity),
          tradePrice
        }),
    })
}

export async function getMyStockOrder(stockCode) {
   return await apiFetch(`${ORDER_API_URL}/order/myorder/${stockCode}`,{auth:true})
}

export async function getMyAllOrder() {
   return await apiFetch(`${ORDER_API_URL}/order/myallorder`,{auth:true})
}

export async function cancelOrder(orderId) {
   return await apiFetch(`${ORDER_API_URL}/order/cancel/${orderId}`, { auth: true, method: 'POST' })
}