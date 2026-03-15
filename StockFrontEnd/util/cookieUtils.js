'use server';

import { cookies } from 'next/headers';

const ACCESS_TOKEN  = 'accessToken';
const REFRESH_TOKEN = 'refreshToken';

export async function getAccessToken() {
  const cookieStore = await cookies();
  return cookieStore.get(ACCESS_TOKEN)?.value || null;
}

export async function getRefreshToken() {
  const cookieStore = await cookies();
  return cookieStore.get(REFRESH_TOKEN)?.value || null;
}

export async function setTokenCookies(accessToken, refreshToken) {
  const cookieStore = await cookies();

  // accessToken: 30분
  cookieStore.set(ACCESS_TOKEN, accessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 60 * 30,
    path: '/',
  });

  // refreshToken: 7일
  cookieStore.set(REFRESH_TOKEN, refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 7,
    path: '/',
  });
}

export async function clearTokenCookies() {
  const cookieStore = await cookies();
  cookieStore.delete(ACCESS_TOKEN);
  cookieStore.delete(REFRESH_TOKEN);
}

// 기존 getSessionCookie 인터페이스 유지 (apiFetch 호환용)
export async function getSessionCookie() {
  const accessToken = await getAccessToken();
  return {
    cookieHeader: accessToken ? `Authorization: Bearer ${accessToken}` : '',
    cookieValue: accessToken,
  };
}