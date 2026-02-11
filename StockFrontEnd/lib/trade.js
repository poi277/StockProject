import { apiFetch } from '../util/apiClient';
import { API_URL } from '../util/URLconfig';

export async function tradeApi(tradeType,userId,stockId,quantity) {
  return await apiFetch(`${API_URL}/stock/trade`,{
    method: 'POST',
    body: JSON.stringify({
          tradeType,
          userId,
          stockId,
          quantity: Number(quantity),
        }),
    })
}
