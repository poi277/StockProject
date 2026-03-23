'use client';
import { createContext, useContext, useEffect, useState } from 'react';
import { loginHandler, logoutHandler, checkSession } from '../lib/auth';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadUser();
  }, []);

  const loadUser = async () => {
    try {
      const res = await checkSession();
      if (res.success) {
        setUser(res.data);
      } else {
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
        setUser(res.data);
      }
      return res;
    } catch (error) {
      console.error('로그인 에러:', error);
      return { success: false, message: error.message || '500에러' };
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
    <AuthContext.Provider value={{ user, loading, login, logout }}>
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