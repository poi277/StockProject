'use server';

export async function apiFetch(url, options = {}) {
  const { ...fetchOptions } = options;
  try {
    const res = await fetch(url, {
      ...fetchOptions,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
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
