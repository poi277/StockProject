package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.repository.CandleMinuteRepository;
import Poi.Stock.repository.StockRepository;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {

	private final CandleMinuteRepository candleMinuteRepository;
	private final StockRepository stockRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final CandleSchedulerService candleSchedulerService;
	private final WebSocketService webSocketService;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	public List<CandleDTO> getCandle(CandleType type, String stockCode, String startTime, String endTime) {
		LocalDateTime toStartTime = startTime != null ? LocalDateTime.parse(startTime)
				: LocalDateTime.now().minusDays(3);
		LocalDateTime toEndTime = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();

		if (type.isMinuteType()) {
			int minute = type.getMinute();
			List<CandleMinute> candles = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					toStartTime, toEndTime);

			List<CandleDTO> result;
			if (minute == 1) {
				result = new ArrayList<>(candles.stream().map(c -> new CandleDTO(c.getTime().toString(), c.getOpen(),
						c.getHigh(), c.getLow(), c.getClose(), c.getVolume())).toList());
			} else {
				result = new ArrayList<>(candles.stream()
						.collect(Collectors.groupingBy(c -> c.getTime()
								.withMinute((c.getTime().getMinute() / minute) * minute).withSecond(0).withNano(0)))
						.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
							List<CandleMinute> group = entry.getValue();
							group.sort(Comparator.comparing(CandleMinute::getTime));
							return new CandleDTO(group.get(0).getTime().toString(), group.get(0).getOpen(),
									group.stream().mapToInt(CandleMinute::getHigh).max().orElse(0),
									group.stream().mapToInt(CandleMinute::getLow).min().orElse(0),
									group.get(group.size() - 1).getClose(),
									group.stream().mapToLong(CandleMinute::getVolume).sum());
						}).toList());
			}

			// 현재 진행중인 분봉 Redis에서 add
			CandleDTO currentCandle = getCurrentCandleFromRedis(stockCode);
			if (currentCandle != null) {
				result.add(currentCandle);
			}
			return result;
		}

		if (type == CandleType.DAY) {
			LocalDate startDate = toStartTime.toLocalDate();
			LocalDate endDate = toEndTime.toLocalDate();

			// 오늘 제외한 과거 일봉
			List<CandleDTO> result = new ArrayList<>(stockRepository
					.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode, startDate, endDate.minusDays(1)).stream()
					.map(s -> new CandleDTO(s.getDate().toString(), s.getOpenPrice(), s.getHighPrice(), s.getLowPrice(),
							s.getClosePrice(), s.getTotalvolume()))
					.toList());

			// 오늘 일봉 = 오늘 분봉(DB) + Redis 현재 봉 합산
			CandleDTO todayCandle = getTodayCandle(stockCode);
			if (todayCandle != null) {
				result.add(todayCandle);
			}
			return result;
		}

		throw new IllegalArgumentException("지원하지 않는 타입: " + type);
	}

	// 오늘 분봉 합산 + Redis 현재 봉 합산
	private CandleDTO getTodayCandle(String stockCode) {
		LocalDate today = LocalDate.now();
		List<CandleMinute> todayMinutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
				today.atStartOfDay(), today.plusDays(1).atStartOfDay());

		CandleDTO redisCandle = getCurrentCandleFromRedis(stockCode);

		if (todayMinutes.isEmpty() && redisCandle == null)
			return null;

		// 분봉 기준 OHLCV
		int open = !todayMinutes.isEmpty() ? todayMinutes.get(0).getOpen() : redisCandle.getOpen();
		int high = todayMinutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(0);
		int low = todayMinutes.stream().mapToInt(CandleMinute::getLow).min().orElse(Integer.MAX_VALUE);
		int close = !todayMinutes.isEmpty() ? todayMinutes.get(todayMinutes.size() - 1).getClose() : 0;
		long volume = todayMinutes.stream().mapToLong(CandleMinute::getVolume).sum();

		// Redis 현재 봉 합산
		if (redisCandle != null) {
			high = Math.max(high, redisCandle.getHigh());
			low = Math.min(low, redisCandle.getLow());
			close = redisCandle.getClose();
			volume += redisCandle.getVolume();
		}

		if (low == Integer.MAX_VALUE)
			low = 0;

		return new CandleDTO(today.toString(), open, high, low, close, volume);
	}

	// Redis에서 현재 진행중인 분봉 가져오기
	private CandleDTO getCurrentCandleFromRedis(String stockCode) {
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		String key = "candle:1m:" + stockCode + ":" + now.format(FMT);
		Map<Object, Object> current = redisTemplate.opsForHash().entries(key);
		if (current.isEmpty())
			return null;

		try {
			return new CandleDTO(now.toString(), Integer.parseInt(String.valueOf(current.get("open"))),
					Integer.parseInt(String.valueOf(current.get("high"))),
					Integer.parseInt(String.valueOf(current.get("low"))),
					Integer.parseInt(String.valueOf(current.get("close"))),
					Long.parseLong(String.valueOf(current.get("volume"))));
		} catch (Exception e) {
			log.error("Redis 현재 봉 변환 실패 - {}", e.getMessage());
			return null;
		}
	}

	public void updateCandle(String stockCode, Integer currentPrice, int filledQty, LocalDateTime lastExecutiontime) {
		CandleDTO candleDTO = candleSchedulerService.saveCurrentCandle(stockCode, currentPrice, filledQty,
				lastExecutiontime);
		webSocketService.sendCurrentCandle(candleDTO, stockCode);
	}

}