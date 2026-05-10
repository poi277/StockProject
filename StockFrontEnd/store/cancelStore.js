import { create } from 'zustand';
import { cancelOrder } from '../lib/order';


const useCancelStore = create((set, get) => ({
    cancelOpen: false,
    cancelType: null,  // 'order' | 'sidebar'
    cancelTarget: null,
    openCancel: (order, type) => set({ cancelOpen: true, cancelTarget: order, cancelType: type }),
    closeCancel: () => set({ cancelOpen: false, cancelTarget: null, cancelType: null }),
    executeCancel: async () => {
        const { cancelTarget, closeCancel } = get();
        try {
            await cancelOrder(cancelTarget.orderId);
            closeCancel();
        } catch (e) {
            console.error(e);
        }
    }
}));

export default useCancelStore;