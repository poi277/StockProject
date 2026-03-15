'use server';

import { API_URL,USER_URL  } from '../util/URLconfig';
import { apiFetch } from '../util/apiClient';
import { setTokenCookies, clearTokenCookies, getAccessToken } from '../util/cookieUtils';

export async function loginHandler(id, password) {
  const response = await fetch(`${USER_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id, password }),
  });

  const data = await response.json();

  if (response.status === 500) {
    throw new Error(data.message || '로그인 오류');
  }

  // 로그인 성공 시 토큰을 httpOnly 쿠키에 저장
  if (data.success && data.data?.accessToken) {
    await setTokenCookies(data.data.accessToken, data.data.refreshToken);
    // AuthContext가 기대하는 형태: res.data = userId
    return { success: true, data: data.data.userId };
  }

  return data;
}

/**
 * 백엔드 호출 없이 쿠키의 JWT를 파싱해서 userId 반환
 * 매 페이지 이동마다 호출되므로 네트워크 요청 없이 처리
 */
export async function checkSession() {
  const accessToken = await getAccessToken();
  if (!accessToken) {
    return { success: false };
  }

  try {
    const payload = JSON.parse(
      Buffer.from(accessToken.split('.')[1], 'base64url').toString()
    );
    // 만료 체크
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      return { success: false };
    }
    return { success: true, data: payload.sub }; // sub = userId
  } catch {
    return { success: false };
  }
}

export async function logoutHandler() {
  const accessToken = await getAccessToken();

  // 백엔드 실패해도 쿠키는 무조건 삭제
  await fetch(`${USER_URL}/auth/logout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
    },
  }).catch(() => {});

  await clearTokenCookies();
  return { success: true };
}

export async function handleSSOLogin(platform) {
  const urlMap = {
    google: '/api/oauth/google/url',
    naver: '/api/oauth/naver/url',
    kakao: '/api/oauth/kakao/url',
    discord: '/api/oauth/discord/url',
  };

  const url = urlMap[platform];
  if (!url) throw new Error('지원하지 않는 플랫폼');

  try {
    const data = await apiFetch(`${API_URL}${url}`);
    return data.data.url;
  } catch (error) {
    console.error(`${platform} 로그인 오류:`, error);
    throw error;
  }
}