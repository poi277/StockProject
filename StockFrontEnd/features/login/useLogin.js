'use client';

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from '@/context/AuthContext'
import { handleSSOLogin } from "@/lib/auth";

export default function useLogin() {
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const { login } = useAuth();
  const router = useRouter();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await login(id, password);
      if (res.success) {
        router.push("/");
      } else {
        setErrorMessage(res.message);
      }
    } catch (err) {
      console.error(err);
      setErrorMessage("로그인에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const handleFindId = () => {
    window.open("/find/id", "_blank", "width=600,height=600");
  };

  const handleFindPassword = () => {
    window.open("/find/password", "_blank", "width=600,height=600");
  };

  const buttonSSOLogin = async (platform) => {
    const url = await handleSSOLogin(platform);
    window.location.href = url;
  };

  return {
    // state
    id,
    password,
    errorMessage,
    // setters
    setId,
    setPassword,
    // handlers
    handleSubmit,
    handleFindId,
    handleFindPassword,
    buttonSSOLogin,
  };
}
