// "use client";

// import { useRouter } from "next/navigation";
// import { useState } from "react";
// import { logoutHandler } from "../../lib/auth";
// import { useAuth } from "../../context/AuthContext";

// export default function UnderBarForm() {
//   const router = useRouter();
//   const {user,logout} = useAuth();
//   const [active, setActive] = useState("");

//   const menuItems = [
//     { label: "관심종목", path: "/watchlist" },
//     { label: "자산", path: "/asset" },
//     { label: "내 주문", path: "/myorder" },
//     { label: "내 완료된 주문", path: "/mycompletedorder" },
//     { label: "내 자신", path: "/profile" },
//   ];

//   const handleLogout = async () => {
//     await logout();
//     router.push('/');
//   };

//   return (
//     <div style={styles.container}>
//       {/* 🔙 돌아가기 */}
//       <button
//         style={styles.button}
//         onClick={() => router.push('/')}
//       >
//         돌아가기
//       </button>

//       {/* 메뉴 */}
//       {menuItems.map((item) => (
//         <button
//           key={item.path}
//           style={{
//             ...styles.button,
//             fontWeight: active === item.path ? "bold" : "normal"
//           }}
//           onClick={() => {
//             setActive(item.path);
//             router.push(item.path);
//           }}
//         >
//           {item.label}
//         </button>
//       ))}
//      {user ? (
//       <button onClick={handleLogout}> 로그아웃 </button>
//      ):(
//          <button onClick={() => {router.push(`/login`)}}>로그인</button>
//       )}
//     </div>
//   );
// }

// const styles = {
//   container: {
//     position: "fixed",
//     bottom: 0,
//     width: "100%",
//     display: "flex",
//     justifyContent: "space-around",
//     padding: "10px",
//     borderTop: "1px solid #ddd",
//     backgroundColor: "#fff"
//   },
//   button: {
//     background: "none",
//     border: "none",
//     cursor: "pointer",
//     fontSize: "14px"
//   }
// };
