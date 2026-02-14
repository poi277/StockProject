import { apiFetch } from '../util/apiClient';
import { API_URL } from '../util/URLconfig';

export async function tradeApi(tradeType,stockId,quantity) {
  return await apiFetch(`${API_URL}/stock/trade`,{
    method: 'POST',
    auth: true,
    body: JSON.stringify({
          tradeType,
          stockId,
          quantity: Number(quantity),
        }),
    })
}