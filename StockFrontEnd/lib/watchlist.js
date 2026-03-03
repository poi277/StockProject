import { apiFetch } from '../util/apiClient';
import { API_URL } from '../util/URLconfig';
export async function getWatchListApi() {
    return await apiFetch(`${API_URL}/watch/list`);
}
export async function addWatchApi(stockCode) {
    return await apiFetch(`${API_URL}/watch/${stockCode}`, { method: "POST" });
}
export async function removeWatchApi(stockCode) {
    return await apiFetch(`${API_URL}/watch/${stockCode}`, { method: "DELETE" });
}