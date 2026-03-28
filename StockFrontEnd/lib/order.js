import { apiFetch } from '../util/apiClient';
import { ORDER_API_URL } from '../util/URLconfig';

export async function getMyCompletedOrder() {
   return await apiFetch(`${ORDER_API_URL}/completed/order`, { auth: true})
}