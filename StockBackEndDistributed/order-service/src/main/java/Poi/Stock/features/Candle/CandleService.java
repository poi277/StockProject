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
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Websocket.WebSocketService;
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
	private final CandleDayRepository candleDayRepository;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	public List<CandleDTO> getCandle(CandleType type, String stockCode, String startTime, String endTime) {
		LocalDateTime toStartTime = startTime != null ? LocalDateTime.parse(startTime)
				: LocalDateTime.now().minusDays(3);
		LocalDateTime toEndTime = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();

		// ==================== [1] 분봉(1분봉, 5분봉 등) 조회 파트 ====================
		if (type.isMinuteType()) {
			int minute = type.getMinute();
			List<CandleMinute> candles = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					toStartTime, toEndTime);

			List<CandleDTO> result;
			if (minute == 1) {
				result = new ArrayList<>(candles.stream()
						.map(c -> new CandleDTO(c.getTime().toString(), c.getOpen(), c.getHigh(), c.getLow(),
								c.getClose(), c.getSellQty() != null ? c.getSellQty() : 0L,
								c.getBuyQty() != null ? c.getBuyQty() : 0L))
						.toList());
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
									group.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0).sum(),
									group.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0).sum());
						}).toList());
			}

			// Redis 또는 CandleCache 메모리에 쌓인 아직 마감 안 된 1분봉 실시간 틱 병합
			CandleDTO currentCandle = getCurrentCandleFromRedis(stockCode);
			if (currentCandle != null) {
				result.add(currentCandle);
			}
			return result;
		}
		if (type == CandleType.DAY) {
			LocalDate startDate = toStartTime.toLocalDate();
			LocalDate endDate = toEndTime.toLocalDate();

			List<CandleDTO> result = new ArrayList<>(candleDayRepository
					.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode, startDate, endDate.minusDays(1)).stream()
					.map(d -> new CandleDTO(d.getDate().toString(), d.getOpen(), d.getHigh(), d.getLow(), d.getClose(),
							d.getSellQty() != null ? d.getSellQty() : 0L, d.getBuyQty() != null ? d.getBuyQty() : 0L)) // 매수/매도량
																														// 데이터
																														// 바인딩
																														// 유지
					.toList());

			CandleDTO todayCandle = getTodayCandle(stockCode);
			if (todayCandle != null) {
				result.add(todayCandle);
			}
			return result;
		}

		throw new IllegalArgumentException("지원하지 않는 타입: " + type);
	}

	private CandleDTO getTodayCandle(String stockCode) {
		LocalDate today = LocalDate.now();
		List<CandleMinute> todayMinutes = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
				today.atStartOfDay(), today.plusDays(1).atStartOfDay());

		CandleDTO redisCandle = getCurrentCandleFromRedis(stockCode);

		if (todayMinutes.isEmpty() && redisCandle == null)
			return null;

		int open = !todayMinutes.isEmpty() ? todayMinutes.get(0).getOpen() : redisCandle.getOpen();
		int high = todayMinutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(0);
		int low = todayMinutes.stream().mapToInt(CandleMinute::getLow).min().orElse(Integer.MAX_VALUE);
		int close = !todayMinutes.isEmpty() ? todayMinutes.get(todayMinutes.size() - 1).getClose() : 0;
		long sellQty = todayMinutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0).sum();
		long buyQty = todayMinutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0).sum();

		if (redisCandle != null) {
			high = Math.max(high, redisCandle.getHigh());
			low = Math.min(low, redisCandle.getLow());
			close = redisCandle.getClose();
			sellQty += redisCandle.getSellQty();
			buyQty += redisCandle.getBuyQty();
		}

		if (low == Integer.MAX_VALUE)
			low = 0;

		return new CandleDTO(today.toString(), open, high, low, close, sellQty, buyQty);
	}

	private CandleDTO getCurrentCandleFromRedis(String stockCode) {
		LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
		String key = "candle:1m:" + stockCode + ":" + now.format(FMT);
		Map<Object, Object> current = redisTemplate.opsForHash().entries(key);
		if (current.isEmpty())
			return null;

		try {
			Object open = current.get("open");
			Object high = current.get("high");
			Object low = current.get("low");
			Object close = current.get("close");
			Object buyQty = current.get("buyQty");
			Object sellQty = current.get("sellQty");

			if (open == null || high == null || low == null || close == null)
				return null;

			return new CandleDTO(now.toString(), Integer.parseInt(String.valueOf(open)),
					Integer.parseInt(String.valueOf(high)), Integer.parseInt(String.valueOf(low)),
					Integer.parseInt(String.valueOf(close)),
					sellQty != null ? Long.parseLong(String.valueOf(sellQty)) : 0L,
					buyQty != null ? Long.parseLong(String.valueOf(buyQty)) : 0L);
		} catch (Exception e) {
			log.error("Redis 현재 봉 변환 실패 - {}", e.getMessage());
			return null;
		}
	}

	public void saveCandleOrder(String stockCode, Integer currentPrice, int buyQty, int sellQty, long tradeAmount,
			LocalDateTime lastExecutiontime) {
		CandleDTO candleDTO = candleSchedulerService.saveCurrentCandle(stockCode, currentPrice, buyQty, sellQty,
				tradeAmount, lastExecutiontime);
		webSocketService.sendCurrentCandle(candleDTO, stockCode);
	}

}