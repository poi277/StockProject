import { Fragment } from 'react';
import './SideBar.css';
import useSideBar from './useSideBar';
import SideBarMenu from './SideBarMenu';
import AccountBenner from './AccountBenner';
import OrderSideBar from './OrderSideBar';
import AccountMoney from './AccountMoney';
import HaveMyStockAsset from './HaveMyStockAssect';
import AccountInfomation from './AccountInfomation';

export default function SideBar() {
    const { } = useSideBar();

    return (
        <div className="_1kestwg0">
            <div data-nosnippet="true" className="_17lx8ak0 _1kestwgy _1kestwga" data-section-name="리모콘" style={{ width: "370px" }}>
                <SideBarMenu/>
                <div className="_1oe23q51 _1kestwgw _1kestwg8 _1kestwgd _17lx8ak1 _1oe23q5b _1oe23q5c" id="ts-1nb" style={{ transform: "none" }}>
                    <div className="_1oe23q5d">
                        <AccountInfomation  />
                        <AccountBenner />
                        <AccountMoney  />
                        <HaveMyStockAsset />
                        <div className="_1oe23q5e">
                            <div className="_1oe23q5f"></div>
                            <div data-tossinvest-log="div" data-parent-name="ResizeHandle" data-skip="true" role="button" tabIndex={-1} className="_1oe23q58">
                                <div className="_1oe23q59"></div>
                            </div>
                        </div>
                        <div style={{ flex: "0 0 auto", height: "12px" }}></div>
                        <OrderSideBar/>
                        <div style={{ flex: "0 0 auto", height: "12px" }}></div>
                    </div>
                </div>
            </div>
        </div>
    )
}
