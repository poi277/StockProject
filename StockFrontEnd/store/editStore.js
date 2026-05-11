import { create } from 'zustand';

const useEditStore = create((set, get) => ({
    editOpen: false,
    editTarget: null,
    openEdit: (order) => set({ editOpen: true, editTarget: order }),
    closeEdit: () => set({ editOpen: false, editTarget: null }),
}));

export default useEditStore;