import AssetPage from "../../features/asset/AssetPage";
import { getAssetApi } from "../../lib/stock";

export default async function asset()
{
    const res = await getAssetApi()
    if (!res?.success) {
        throw new Error(res.message);
    }
    return(
    <AssetPage myasset={res.data}/>
    )
}