import { apiFetch } from '../util/apiClient';
import { API_URL,USER_URL } from '../util/URLconfig';

export async function getProfileApi() {
  return await apiFetch(`${USER_URL}/profile/`,{auth:true});
}