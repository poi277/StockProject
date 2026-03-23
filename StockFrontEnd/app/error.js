'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function Error({ error, reset }) {
  const router = useRouter();

  useEffect(() => {
    if (error.message === '로그인이 필요합니다.') {
      router.replace('/login');
    }
  }, [error]);

  return (
    <div style={{ textAlign: "center", padding: "40px" }}>
      <h1> 오류가 발생했습니다.</h1>
      <p>{error.message}</p>

      <button
        onClick={() => reset()}
        style={{
          marginTop: 20,
          padding: "10px 20px",
          borderRadius: "8px",
          cursor: "pointer"
        }}
      >
        다시 시도
      </button>
    </div>
  );
}