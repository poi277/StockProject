import CancelForm from "../../../features/StockDetail/MainContent/Order/Cancel/CancelForm";

export default function NormalLayout({ children }) {
  return (
    <>
      {children}
        <CancelForm/>
    </>
  );
}
