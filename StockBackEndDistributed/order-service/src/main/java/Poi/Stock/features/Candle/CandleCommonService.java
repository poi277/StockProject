package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import Poi.Stock.features.Candle.Entity.Candle;
import Poi.Stock.features.Candle.Entity.CandleDay;
import Poi.Stock.features.Candle.Entity.CandleHour;
import Poi.Stock.features.Candle.Entity.CandleMinute;
import Poi.Stock.features.Candle.Entity.CandleMonth;
import Poi.Stock.features.Candle.Entity.CandleWeek;
import Poi.Stock.features.Candle.Entity.CandleYear;
import Poi.Stock.features.Candle.repository.CandleDayRepository;
import Poi.Stock.features.Candle.repository.CandleHourRepository;
import Poi.Stock.features.Candle.repository.CandleMinuteRepository;
import Poi.Stock.features.Candle.repository.CandleMonthRepository;
import Poi.Stock.features.Candle.repository.CandleWeekRepository;
import Poi.Stock.features.Candle.repository.CandleYearRepository;
import Poi.Stock.features.Websocket.WebSocketService;
import Poi.Stock.util.EnumUtil.CandleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleCommonService {
	private final RedisTemplate<String, String> redisTemplate;
	private final CandleMinuteRepository candleMinuteRepository;
	private final CandleHourRepository candleHourRepository;

	private final CandleWeekRepository candleWeekRepository;
	private final CandleMonthRepository candleMonthRepository;
	private final CandleYearRepository candleYearRepository;

	private final CandleDayRepository candleDayRepository;
	private final CandleCacheService candleCacheService;
	private final WebSocketService webSocketService;

	public void upsertUpperPeriodCandle(CandleType type, String stockCode, LocalDate targetDate,
			Optional<? extends Candle> existingOpt, Candle newCandle) {
		if (existingOpt.isPresent()) {
			Candle candle = existingOpt.get();
			candle.setHigh(Math.max(candle.getHigh(), newCandle.getHigh()));
			candle.setLow(Math.min(candle.getLow(), newCandle.getLow()));
			candle.setClose(newCandle.getClose());
			candle.setBuyQty(candle.getBuyQty() + newCandle.getBuyQty());
			candle.setSellQty(candle.getSellQty() + newCandle.getSellQty());
			candle.setTotalVolume(candle.getTotalVolume() + newCandle.getTotalVolume());
			candle.setTradeAmount(candle.getTradeAmount() + newCandle.getTradeAmount());

			saveToRepository(type, candle);
			candleCacheService.upsertCandle(type, stockCode, candle);
		} else {
			saveToRepository(type, newCandle);
			candleCacheService.upsertCandle(type, stockCode, newCandle);
		}
	}

	private void saveToRepository(CandleType type, Candle candle) {
		switch (type) {
		case ONE_MINUTE, THREE_MINUTE, FIVE_MINUTE, TEN_MINUTE -> candleMinuteRepository.save((CandleMinute) candle);
		case HOUR, TWO_HOUR, THREE_HOUR, FOUR_HOUR -> candleHourRepository.save((CandleHour) candle);
		case DAY -> candleDayRepository.save((CandleDay) candle);
		case WEEK -> candleWeekRepository.save((CandleWeek) candle);
		case MONTH -> candleMonthRepository.save((CandleMonth) candle);
		case YEAR -> candleYearRepository.save((CandleYear) candle);
		default -> throw new IllegalArgumentException("지원하지 않는 상위 캔들 타입입니다: " + type);
		}
	}
}
