export default function NotFound() {
  return (
    <div style={{
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      height: "60vh",
      textAlign: "center"
    }}>
      <h1>404 - 페이지를 찾을 수 없습니다</h1>
      <p>주소가 잘못되었거나 존재하지 않는 페이지입니다.</p>
      <a href="/" style={{ marginTop: 20, color: "blue" }}>홈으로 이동</a>
    </div>
  );
}
