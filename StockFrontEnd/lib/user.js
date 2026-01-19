import { API_URL } from '../util/URLconfig';

export async function RegisterSumbitApi(formData) {
  return await fetch(`${API_URL}/user/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(formData)
  });
}
