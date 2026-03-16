import { apiFetch } from '../util/apiClient';
import { API_URL,USER_URL } from '../util/URLconfig';

export async function RegisterSumbitApi(formData) {
  return await fetch(`${USER_URL}/user/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(formData)
  });
}
