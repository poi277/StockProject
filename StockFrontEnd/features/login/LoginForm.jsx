'use client';

import Link from "next/link";
import styles from '../../css/LoginPage.module.css'
import useLogin from "./useLogin";

export default function LoginForm() {
  const {
    id,
    password,
    errorMessage,
    setId,
    setPassword,
    handleSubmit,
    handleFindId,
    handleFindPassword,
    buttonSSOLogin,
  } = useLogin();

   return (
     <>
       <div className={styles.loginPageWrapper}>
         <div className={styles.loginContainer}>
           <h1 className={styles.title}>Stock</h1>
 
           {errorMessage && (
             <div className={styles.errorMessage}>{errorMessage}</div>
           )}

           <form onSubmit={handleSubmit}>
             <div className={styles.formGroup}>
               <label className={styles.label}>아이디</label>
               <input
                 type="text"
                 value={id}
                 onChange={(e) => setId(e.target.value)}
                 placeholder="아이디"
                 className={styles.input}
                 required
               />
             </div>
 
             <div className={styles.formGroup}>
               <label className={styles.label}>비밀번호</label>
               <input
                 type="password"
                 value={password}
                 onChange={(e) => setPassword(e.target.value)}
                 placeholder="비밀번호"
                 className={styles.input}
                 required
               />
             </div>
 
             <button type="submit" className={styles.loginBtn}>
               로그인
             </button>
           </form>
 
           <div className={styles.linkSection}>
             <span className={styles.link} onClick={handleFindId}>
               아이디찾기
             </span>
             <span className={styles.divider}>|</span>
             <span className={styles.link} onClick={handleFindPassword}>
               비밀번호찾기
             </span>
             <span className={styles.divider}>|</span>
             <Link href="/register" className={styles.link}>
               회원가입
             </Link>
           </div>
 
           <div className={styles.socialLoginSection}>
             <button
               className={`${styles.socialBtn} ${styles.googleBtn}`}
               onClick={()=> buttonSSOLogin('google')}
             >
               <span className={styles.socialIcon}>
                 <svg width="18" height="18" viewBox="0 0 24 24">
                   <path fill="#4285f4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                   <path fill="#34a853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                   <path fill="#fbbc05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                   <path fill="#ea4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                 </svg>
               </span>
               구글로 로그인
             </button>
 
             <button
               className={`${styles.socialBtn} ${styles.naverBtn}`}
               onClick={()=>buttonSSOLogin('naver')}
             >
               <span className={styles.socialIcon}>
                 <div className={styles.naverIcon}>N</div>
               </span>
               네이버로 로그인
             </button>
 
             <button
               className={`${styles.socialBtn} ${styles.kakaoBtn}`}
               onClick={()=>buttonSSOLogin('kakao')}
             >
               <span className={styles.socialIcon}>
                 <div className={styles.kakaoIcon}></div>
               </span>
               카카오톡으로 로그인
             </button>
 
             <button
               className={`${styles.socialBtn} ${styles.discordBtn}`}
               onClick={()=>buttonSSOLogin('discord')}
             >
               <span className={styles.socialIcon}>
                 <div className={styles.discordIcon}></div>
               </span>
               디스코드로 로그인
             </button>
           </div>
         </div>
       </div>
     </>
   );
}
