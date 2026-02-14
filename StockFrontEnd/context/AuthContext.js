// frontend/src/context/AuthContext.js
'use client';
import { createContext, useContext, useEffect, useState } from 'react';
import { usePathname } from 'next/navigation';
import { loginHandler, logoutHandler, checkSession } from '../lib/auth'
import { useRouter } from 'next/navigation';

const AuthContext = createContext();

export function AuthProvider({ children }) {

  const router = useRouter();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const pathname = usePathname();
  useEffect(() => { 
    loadUser();
  }, [pathname]);

    const loadUser = async () => {
    try {
      const res = await checkSession();
      if (res.success) {
        setUser(res.data); // ✅ 백엔드에서 data에 userId가 담겨 있음
        console.log('✅ 세션 확인 완료:', res.data);
      } else {
        console.log('❌ 세션 없음');
        setUser(null);
      }
    } catch (error) {
      console.error('세션 확인 실패:', error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (id, password) => {
    try {
      const res = await loginHandler(id, password);
      
      if (res.success) {
        setUser(res.data); // ✅ 마찬가지로 res.data가 userId
      }
      return res;
    } catch (error) {
      console.error('로그인 에러:', error);
      return { 
        success: false,
        message: error.message || "500에러",
      };
    }
  };

  const logout = async () => {
    try {
      await logoutHandler();
      setUser(null);
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      loading, 
      login, 
      logout,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth는 AuthProvider 내부에서 사용해야 합니다');
  }
  return context;
}