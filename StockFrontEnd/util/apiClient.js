'use server';

import { getSessionCookie } from './cookieUtils';

export async function apiFetch(url, options = {}) {
  const { auth = false, ...fetchOptions } = options;  
  try {
    let cookieHeader = null;
    if (auth) {
      const session = await getSessionCookie();
      cookieHeader = session?.cookieHeader;

      if (!cookieHeader) {
        return {
          success: false,
          status: 401,
          message: '로그인이 필요합니다.',
        };
      }
    }
    
    const isFormData = fetchOptions.body instanceof FormData;

    const res = await fetch(url, {
      ...fetchOptions,
      credentials: 'include',
      headers: {
        ...(!isFormData && { 'Content-Type': 'application/json' }),
        ...(cookieHeader && { Cookie: cookieHeader }),
        ...fetchOptions.headers,
      },
    });

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
