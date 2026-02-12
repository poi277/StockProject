"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

export default function UnderBarForm() {
  const router = useRouter();
  const [active, setActive] = useState("");

  const menuItems = [
    { label: "관심종목", path: "/watchlist" },
    { label: "자산", path: "/assets" },
    { label: "내 자신", path: "/profile" }
  ];

  return (
    <div style={styles.container}>
      {/* 🔙 돌아가기 */}
      <button
        style={styles.button}
        onClick={() => router.back()}
      >
        돌아가기
      </button>

      {/* 메뉴 */}
      {menuItems.map((item) => (
        <button
          key={item.path}
          style={{
            ...styles.button,
            fontWeight: active === item.path ? "bold" : "normal"
          }}
          onClick={() => {
            setActive(item.path);
            router.push(item.path);
          }}
        >
          {item.label}
        </button>
      ))}
    </div>
  );
}

const styles = {
  container: {
    position: "fixed",
    bottom: 0,
    width: "100%",
    display: "flex",
    justifyContent: "space-around",
    padding: "10px",
    borderTop: "1px solid #ddd",
    backgroundColor: "#fff"
  },
  button: {
    background: "none",
    border: "none",
    cursor: "pointer",
    fontSize: "14px"
  }
};
