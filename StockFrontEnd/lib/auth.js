"use client"

import { API_URL } from '../util/URLconfig'
import { apiFetch } from '../util/apiClient';

export async function loginHandler(id, password) {
  const response =await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    credentials: 'include',
    body: JSON.stringify({id, password}),
    headers: { 'Content-Type': 'application/json' },
  });
  const data = await response.json();
  // ✅ 다른 에러는 예외 처리
  if (response.status === 500) {
    throw new Error(data.message || '로그인 오류');
  }
  return data;
}

export async function checkSession() {
  const response = await fetch(`${API_URL}/auth/check`, {
    credentials: 'include', // ✅ 자동으로 쿠키 전송
  });
  return await response.json();
}

export async function logoutHandler() {
  const response = await fetch(`${API_URL}/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });

  return await response.json();
}

export async function handleSSOLogin(platform) {
  const urlMap = {
    google: '/api/oauth/google/url',
    naver: '/api/oauth/naver/url',
    kakao: '/api/oauth/kakao/url',
    discord: '/api/oauth/discord/url',
  };

  const url = urlMap[platform];
  
  if (!url) {
    throw new Error('지원하지 않는 플랫폼');
  }

  try {
    const data = await apiFetch(`${API_URL}${url}`);
    return data.data.url;
  } catch (error) {
    console.error(`${platform} 로그인 오류:`, error);
    throw error;
  }
}

