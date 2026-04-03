import { apiFetch } from '../util/apiClient';
import { API_URL,ORDER_API_URL } from '../util/URLconfig';

function toKSTDateTimeStr(isoStr) {
  if (!isoStr) return undefined;
  const d = new Date(isoStr);
  const kst = new Date(d.getTime() + 9 * 60 * 60 * 1000);
  return kst.toISOString().replace("Z", "").slice(0, 23); // "2026-04-03T17:19:37.887"
}

export async function getCandleApi(stockCode, type, startTime, endTime) {
  const params = new URLSearchParams({ type });
  if (startTime) params.append("startTime", toKSTDateTimeStr(startTime));
  if (endTime)   params.append("endTime",   toKSTDateTimeStr(endTime));
  return await apiFetch(`${ORDER_API_URL}/candle/${stockCode}?${params.toString()}`);
}