import ResolutionDropdown from "../../../features/StockDetail/Chart/ChartSelectMenu";
import CancelForm from "../../../features/StockDetail/MainContent/Order/Cancel/CancelForm";

export default function NormalLayout({ children }) {
  return (
    <>
      {children}
        <CancelForm/>
        <ResolutionDropdown/>
    </>
  );
}
