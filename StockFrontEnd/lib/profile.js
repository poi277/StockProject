import { apiFetch } from '../util/apiClient';
import { API_URL } from '../util/URLconfig';

export async function getProfileApi() {
  return await apiFetch(`${API_URL}/profile/`,{auth:true,cookie:true});
}