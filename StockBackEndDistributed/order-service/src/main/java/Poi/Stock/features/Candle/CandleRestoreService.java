package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.CompletedOrder.CompletedOrder;
import Poi.Stock.repository.CompletedOrderRepository;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleRestoreService {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	private static final String DAY_FMT = "yyyyMMdd";

	private final CandleMinuteRepository candleMinuteRepository;
	private final CompletedOrderRepository completedOrderRepository;
	private final RedisTemplate<String, String> redisTemplate;

	private static final String UPDATE_CANDLE_SCRIPT = """
			local candleKey   = KEYS[1]
			local price       = tonumber(ARGV[1])
			local buyQty      = tonumber(ARGV[2])
			local sellQty     = tonumber(ARGV[3])
			local tradeAmount = tonumber(ARGV[4])

			local exists = redis.call('EXISTS', candleKey)
			if exists == 0 then
			    redis.call('HSET', candleKey, 'open', price, 'high', price, 'low', price, 'close', price,
			        'buyQty', buyQty, 'sellQty', sellQty, 'tradeAmount', tradeAmount)
			    redis.call('EXPIRE', candleKey, 120)
			else
			    local high = tonumber(redis.call('HGET', candleKey, 'high'))
			    local low  = tonumber(redis.call('HGET', candleKey, 'low'))
			    if price > high then redis.call('HSET', candleKey, 'high', price) end
			    if price < low  then redis.call('HSET', candleKey, 'low',  price) end
			    redis.call('HSET',          candleKey, 'close',       price)
			    redis.call('HINCRBY',      candleKey, 'buyQty',      buyQty)
			    redis.call('HINCRBY',      candleKey, 'sellQty',     sellQty)
			    redis.call('HINCRBY',      candleKey, 'tradeAmount', tradeAmount)
			end
			return { redis.call('HGET', candleKey, 'open') }
			""";

	/**
	 * 1. [분봉 복구] 과거 유실분은 DB 직행, 현재 진행 중인 분은 1m Redis에만 적재
	 */
	public void restoreMinuteCandle(String stockCode, LocalDateTime now) {
		log.info("1단계: DB 유실 공백 분봉/일봉 복구 및 Redis 웜업 시작... [{}]", stockCode);
		LocalDateTime currentMinuteTime = now.withSecond(0).withNano(0);
		String todayStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		try {
			Optional<CandleMinute> lastSavedMinuteOpt = candleMinuteRepository
					.findTopByStockCodeOrderByTimeDesc(stockCode);

			LocalDateTime criteriaTime = lastSavedMinuteOpt.isPresent()
					? lastSavedMinuteOpt.get().getTime().plusMinutes(1)
					: now.toLocalDate().atStartOfDay();

			if (criteriaTime.isAfter(now)) {
				return; // 단일 종목 메서드이므로 continue 대신 return
			}

			// 🎯 1. 타입 변경: TradeExecution -> CompletedOrder
			List<CompletedOrder> totalMissedExecutions = completedOrderRepository
					.findByStockCodeAndCompletedAtBetweenOrderByCompletedAtAsc(stockCode, criteriaTime, now);

			if (totalMissedExecutions.isEmpty()) {
				return;
			}

			// 🎯 2. 분 단위 정렬 그루핑 (CompletedOrder 매핑)
			TreeMap<LocalDateTime, List<CompletedOrder>> groupedByMinute = new TreeMap<>();
			for (CompletedOrder exe : totalMissedExecutions) {
				// completedAt 필드 사용
				LocalDateTime minute = exe.getCompletedAt().withSecond(0).withNano(0);
				groupedByMinute.computeIfAbsent(minute, k -> new ArrayList<>()).add(exe);
			}

			List<CandleMinute> pastCandlesToDb = new ArrayList<>();

			groupedByMinute.forEach((minuteTime, executions) -> {
				if (minuteTime.isBefore(currentMinuteTime)) {
					// 🅰️ 과거 분봉 -> DB 직행 데이터로 전환 (아래 헬퍼 메서드 타입 수정 필요)
					pastCandlesToDb.add(toGroupedCandleMinute(stockCode, minuteTime, executions));
				} else {
					// 🅱️ 현재 진행 분봉 -> 1분봉 및 일봉 Redis에 웜업 갱신
					String minuteCandleKey = "candle:1m:" + stockCode + ":" + minuteTime.format(FMT);
					String dayCandleKey = "candle:day:" + stockCode + ":" + todayStr;

					// 동기화 전 기존 키가 있다면 초기화
					redisTemplate.delete(minuteCandleKey);
					// 일봉 키는 오늘 전체 누적이므로 함부로 지우지 않고 스크립트로 누적하거나,
					// 복구 시점이 완전 초기화 단계라면 delete 후 재적재 결정 (여기서는 덮어쓰기 정합성을 위해 유지 또는 초기화 선택)
					// 전체 동기화이므로 안전하게 지우고 새로 쌓는 것을 권장합니다.
					redisTemplate.delete(dayCandleKey);

					for (CompletedOrder exe : executions) {
						// 🎯 엔티티 필드명에 맞게 호출 (tradePrice, filledQuantity)
						int price = exe.getTradePrice();
						int qty = exe.getFilledQuantity();
						long amount = (long) price * qty;
						int buyQty = tradeType.BUY == exe.getTradeType() ? qty : 0;
						int sellQty = tradeType.SELL == exe.getTradeType() ? qty : 0;

						// 1분봉 루아 스크립트 실행
						redisTemplate.execute(new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, List.class),
								List.of(minuteCandleKey), String.valueOf(price), String.valueOf(buyQty),
								String.valueOf(sellQty), String.valueOf(amount));

						// 🎯 일봉 루아 스크립트도 동시 실행 (실시간 엔진 구조와 매킹 일치)
						redisTemplate.execute(new DefaultRedisScript<>(UPDATE_CANDLE_SCRIPT, List.class),
								List.of(dayCandleKey), String.valueOf(price), String.valueOf(buyQty),
								String.valueOf(sellQty), String.valueOf(amount));
					}

					// TTL 설정 보장
					redisTemplate.expire(minuteCandleKey, 120, TimeUnit.SECONDS);
					redisTemplate.expire(dayCandleKey, 24, TimeUnit.HOURS);
				}
			});

			if (!pastCandlesToDb.isEmpty()) {
				candleMinuteRepository.saveAll(pastCandlesToDb);
			}
		} catch (Exception e) {
			log.error("분봉/일봉 복구 실패 - 종목: {}, 에러: {}", stockCode, e.getMessage());
		}
	}

	/**
	 * 2. [일봉 복구] 완성된 오늘 자 DB 분봉 데이터 + 방금 복구한 Redis 현재 분봉을 결합하여 일봉 Redis 생성
	 */
	public void restoreDayCandle(String stockCode, LocalDateTime now) {
		LocalDate today = now.toLocalDate();
		LocalDateTime startOfDay = today.atStartOfDay();
		LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
		String todayStr = today.format(DateTimeFormatter.ofPattern(DAY_FMT));
		String currentMinStr = now.withSecond(0).withNano(0).format(FMT);

		log.info("당일 실시간 일봉 Redis 캐시(DB 분봉 + Redis 현재분 결합) 복구 시작... [{}]", stockCode);

		try {
			// ① DB에서 오늘 누적 확정된 분봉들 가져오기
			List<CandleMinute> dayMinutes = candleMinuteRepository
					.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode, startOfDay, endOfDay);

			String currentMinKey = "candle:1m:" + stockCode + ":" + currentMinStr;
			Map<Object, Object> redisMinFields = redisTemplate.opsForHash().entries(currentMinKey);

			// 두 군데 모두 데이터가 없다면 복구 패스 (단일 메서드이므로 continue -> return 변경)
			if (dayMinutes.isEmpty() && (redisMinFields == null || redisMinFields.isEmpty())) {
				log.warn("종목 [{}] 의 금일 데이터가 DB와 Redis에 전혀 존재하지 않아 일봉 복구를 패스합니다.", stockCode);
				return;
			}

			// 베이스 변수 초기화
			int open = 0, high = 0, low = 0, close = 0;
			long buyQty = 0, sellQty = 0, tradeAmount = 0;
			boolean hasData = false;

			// ② DB 분봉 데이터가 있다면 먼저 베이스로 지정
			if (!dayMinutes.isEmpty()) {
				open = dayMinutes.get(0).getOpen();
				close = dayMinutes.get(dayMinutes.size() - 1).getClose();
				high = dayMinutes.stream().mapToInt(CandleMinute::getHigh).max().orElse(open);
				low = dayMinutes.stream().mapToInt(CandleMinute::getLow).min().orElse(open);
				buyQty = dayMinutes.stream().mapToLong(c -> c.getBuyQty() != null ? c.getBuyQty() : 0L).sum();
				sellQty = dayMinutes.stream().mapToLong(c -> c.getSellQty() != null ? c.getSellQty() : 0L).sum();
				tradeAmount = dayMinutes.stream().mapToLong(c -> c.getTradeAmount() != null ? c.getTradeAmount() : 0L)
						.sum();
				hasData = true;
			}

			// ③ [핵심] 현재 진행 중인 분봉의 실시간 Redis 값 병합
			if (redisMinFields != null && !redisMinFields.isEmpty()) {
				int rOpen = Integer.parseInt((String) redisMinFields.get("open"));
				int rHigh = Integer.parseInt((String) redisMinFields.get("high"));
				int rLow = Integer.parseInt((String) redisMinFields.get("low"));
				int rClose = Integer.parseInt((String) redisMinFields.get("close"));
				long rBuyQty = Long.parseLong((String) redisMinFields.get("buyQty"));
				long rSellQty = Long.parseLong((String) redisMinFields.get("sellQty"));
				long rAmount = Long.parseLong((String) redisMinFields.get("tradeAmount"));

				if (!hasData) {
					// 오늘 첫 거래인데 아직 DB에 안 가고 Redis 현재분봉에만 있는 경우
					open = rOpen;
					high = rHigh;
					low = rLow;
				} else {
					// DB 과거 데이터와 Redis 최신 진행분 최고/최저가 비교 갱신
					high = Math.max(high, rHigh);
					low = Math.min(low, rLow);
				}
				close = rClose; // 종가는 항상 가장 최신 진행 분봉의 종가로 무조건 갱신
				buyQty += rBuyQty;
				sellQty += rSellQty;
				tradeAmount += rAmount;
			}

			// ④ 최종 취합된 데이터를 일봉 Redis 해시맵에 덮어쓰기
			String dayKey = "candle:day:" + stockCode + ":" + todayStr;

			Map<String, String> dayCandleMap = new HashMap<>();
			dayCandleMap.put("open", String.valueOf(open));
			dayCandleMap.put("high", String.valueOf(high));
			dayCandleMap.put("low", String.valueOf(low));
			dayCandleMap.put("close", String.valueOf(close));
			dayCandleMap.put("buyQty", String.valueOf(buyQty));
			dayCandleMap.put("sellQty", String.valueOf(sellQty));
			dayCandleMap.put("tradeAmount", String.valueOf(tradeAmount));

			redisTemplate.opsForHash().putAll(dayKey, dayCandleMap);
			redisTemplate.expire(dayKey, 24, TimeUnit.HOURS);

			log.info("종목 [{}] 의 당일 완벽한 실시간 일봉 레디스 복구 완료 (시가: {}, 종가: {})", stockCode, open, close);
		} catch (Exception e) {
			log.error("당일 일봉 복구 실패 - 종목: {}, 에러: {}", stockCode, e.getMessage());
		}
	}

	private CandleMinute toGroupedCandleMinute(String stockCode, LocalDateTime candleTime, List<CompletedOrder> group) {
		CompletedOrder first = group.get(0);
		CompletedOrder last = group.get(group.size() - 1);

		int open = first.getTradePrice();
		int close = last.getTradePrice();

		int high = group.stream().mapToInt(CompletedOrder::getTradePrice).max().orElse(open);
		int low = group.stream().mapToInt(CompletedOrder::getTradePrice).min().orElse(open);

		// 🎯 [보안] 엔티티 필드 명세에 맞춰 getQuantity() 대신 getFilledQuantity()로 바인딩 일치
		long buyQty = group.stream()
				.mapToLong(exe -> tradeType.BUY == exe.getTradeType() ? exe.getFilledQuantity() : 0L)
				.sum();
		long sellQty = group.stream()
				.mapToLong(exe -> tradeType.SELL == exe.getTradeType() ? exe.getFilledQuantity() : 0L)
				.sum();
		long tradeAmount = group.stream().mapToLong(exe -> (long) exe.getTradePrice() * exe.getFilledQuantity()).sum();

		return new CandleMinute(null, stockCode, candleTime, open, high, low, close, buyQty, sellQty, buyQty + sellQty,
				tradeAmount);
	}
}