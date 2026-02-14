'use server';

import { cookies } from 'next/headers';

export async function getSessionCookie() {
  const cookieStore = await cookies();
  const sessionCookie = cookieStore.get('JSESSIONID');  // connect.sid → JSESSIONID

  const cookieHeader = sessionCookie ? `JSESSIONID=${sessionCookie.value}` : '';
  const cookieValue = sessionCookie?.value || null;

  return { cookieHeader, cookieValue };
}