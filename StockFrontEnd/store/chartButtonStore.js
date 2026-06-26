import { create } from 'zustand';
import { ChartMinuteEnum, ChartTimeEnum } from '../util/function/ChartTimeEnum';

const useChartButtonStore = create((set, get) => ({
    // 🎯 차트 분봉(해상도) 및 일/주/월/년 관리
    isResolutionOpen: false,
    selectedMinute: '1',             // 프론트에 표시할 분 (예: '1', '3')
    selectedChartTime: 'ONE_MINUTE', // 백엔드에 전달할 enum 타입 key (예: 'ONE_MINUTE', 'DAY')

    openResolution: () => set({ isResolutionOpen: true }),
    closeResolution: () => set({ isResolutionOpen: false }),
    toggleResolution: () => set((state) => ({ isResolutionOpen: !state.isResolutionOpen })),

    // 🎯 분봉 선택 시 (selectedMinute 변경 O, selectedChartTime 변경 O)
    setResolution: (num) => {
        const targetKey = Object.keys(ChartMinuteEnum).find(
            (key) => ChartMinuteEnum[key] === String(num)
        );
        set({ 
            selectedMinute: String(num), 
            selectedChartTime: targetKey || 'ONE_MINUTE',
            isResolutionOpen: false 
        });
    },
    
    setChartTime: (label) => {
        const targetKey = Object.keys(ChartTimeEnum).find(
            (key) => ChartTimeEnum[key] === label
        );

        set({
            selectedChartTime: targetKey || 'DAY',
            isResolutionOpen: false
        });
    },

    // 🎯 [추가] 사용자가 차트를 확대/축소하거나 스크롤할 때 실시간으로 상태를 동기화할 함수
    setChartViewport: (visibleBarsCount, rightOffset) => set({ 
        visibleBarsCount, 
        rightOffset 
    }),
}));

export default useChartButtonStore;