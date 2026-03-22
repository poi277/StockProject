package Poi.Stock.features.WatchList;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Poi.Stock.DTO.user.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/watch")
public class WatchListController {

    private final WatchListService watchListService;

    @PostMapping("/{stockCode}")
    public ResponseEntity<?> addWatch(@PathVariable("stockCode") String stockCode, Authentication authentication) {
        String userId = authentication.getName();
        watchListService.addWatch(userId, stockCode);
        return ResponseEntity.ok(new ApiResponse(true, "관심종목 즐겨찾기 완료"));
    }

    @DeleteMapping("/{stockCode}")
    public ResponseEntity<?> removeWatch(@PathVariable("stockCode") String stockCode, Authentication authentication) {
        String userId = authentication.getName();
        watchListService.removeWatch(userId, stockCode);
        return ResponseEntity.ok(new ApiResponse(true, "관심종목 삭제 완료"));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getWatchList(Authentication authentication) {
		// stock-service에서 주식 상세 정보까지 포함해서 반환
		System.out.println("ddd");
		List<Object> watchList = watchListService.getWatchListWithStockInfo(authentication.getName());
		System.out.println(watchList);
		return ResponseEntity.ok(new ApiResponse(true, "관심종목 조회 완료", watchList));
    }

	@GetMapping("/{stockCode}")
	public ResponseEntity<ApiResponse> isWatched(@PathVariable("stockCode") String stockCode,
			Authentication authentication) {
		boolean watched = watchListService.isWatched(authentication.getName(), stockCode);
		return ResponseEntity.ok(new ApiResponse(true, "조회 완료", watched));
	}
}
