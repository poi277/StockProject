import { API_URL } from '../util/URLconfig';

export async function getHello() {
  return await fetch(`${API_URL}/hello`);
}
