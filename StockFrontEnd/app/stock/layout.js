import UnderBarForm from "../../features/UI/UnderBar";
export default function NormalLayout({ children }) {
  return (
    <>
      {children}
      <UnderBarForm/>
    </>
  );
}
