import { apiFetch } from '../util/apiClient';
import { API_URL } from '../util/URLconfig';

export async function stockApi(stockId) {
  return await apiFetch(`${API_URL}/stock/${stockId}`);
}

export async function stockListApi() {
  return await apiFetch(`${API_URL}/stock/`);
}
