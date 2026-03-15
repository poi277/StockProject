'use server';

import { getAccessToken, setTokenCookies, clearTokenCookies, getRefreshToken } from './cookieUtils';
import { API_URL,USER_URL } from './URLconfig';

export async function apiFetch(url, options = {}) {
  const { auth = false, ...fetchOptions } = options;

  try {
    const accessToken = await getAccessToken();

    if (!accessToken && auth) {
      return {
        success: false,
        status: 401,
        message: '로그인이 필요합니다.',
      };
    }

    const isFormData = fetchOptions.body instanceof FormData;

    const res = await fetch(url, {
      ...fetchOptions,
      headers: {
        ...(!isFormData && { 'Content-Type': 'application/json' }),
        ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        ...fetchOptions.headers,
      },
    });

    // accessToken 만료 시 → refreshToken으로 재발급 후 재시도
    if (res.status === 401) {
      const refreshed = await tryRefresh();
      if (refreshed) {
        const newToken = await getAccessToken();
        const retryRes = await fetch(url, {
          ...fetchOptions,
          headers: {
            ...(!isFormData && { 'Content-Type': 'application/json' }),
            Authorization: `Bearer ${newToken}`,
            ...fetchOptions.headers,
          },
        });
        const retryData = await retryRes.json().catch(() => null);
        if (!retryRes.ok || retryData?.success === false) {
          return {
            success: false,
            status: retryRes.status,
            message: retryData?.message || `요청 실패 (${retryRes.status})`,
          };
        }
        return {
          success: true,
          status: retryRes.status,
          data: retryData?.data ?? retryData,
          message: retryData?.message,
        };
      }
      // refresh도 실패 → 로그아웃
      await clearTokenCookies();
      return { success: false, status: 401, message: '로그인이 필요합니다.' };
    }

    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        success: false,
        status: res.status,
        message: data?.message || `요청 실패 (${res.status})`,
      };
    }

    return {
      success: true,
      status: res.status,
      data: data?.data ?? data,
      message: data?.message,
    };
  } catch (error) {
    console.error('apiFetch error:', error);
    return {
      success: false,
      status: 500,
      message: error.message || '네트워크 오류',
    };
  }
}

// accessToken 만료 시 refreshToken으로 재발급
async function tryRefresh() {
  try {
    const refreshToken = await getRefreshToken();
    if (!refreshToken) return false;

    const res = await fetch(`${USER_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!res.ok) return false;

    const data = await res.json();
    if (data?.data?.accessToken) {
      await setTokenCookies(data.data.accessToken, data.data.refreshToken);
      return true;
    }
    return false;
  } catch {
    return false;
  }
}