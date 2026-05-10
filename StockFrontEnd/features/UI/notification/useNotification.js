import { UserHaveAssetContext } from '../../../util/websocket/UserHaveAssetProvider';

export default function useNotification() {
    const { notifications } = UserHaveAssetContext();
    return { notifications };
}