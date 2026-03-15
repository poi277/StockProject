import { apiFetch } from '../util/apiClient';
import { USER_URL } from '../util/URLconfig';  // ← USER_URL로 변경

export async function getWatchListApi() {
    return await apiFetch(`${USER_URL}/watch/list`, { auth: true });  // cookie 제거
}
export async function addWatchApi(stockCode) {
    return await apiFetch(`${USER_URL}/watch/${stockCode}`, { auth: true, method: "POST" });
}
export async function removeWatchApi(stockCode) {
    return await apiFetch(`${USER_URL}/watch/${stockCode}`, { auth: true, method: "DELETE" });
}