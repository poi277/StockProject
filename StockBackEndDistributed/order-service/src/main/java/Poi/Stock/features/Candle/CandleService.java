package Poi.Stock.features.Candle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Poi.Stock.DTO.user.CandleDTO;
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

	public List<CandleDTO> getCandle(CandleType type, String stockCode, String startTime, String EndTime) {
		// candletype 안에 기본값으로 가져옴
		LocalDateTime tostartTime = startTime != null ? LocalDateTime.parse(startTime)
				: LocalDateTime.now().minusDays(3);
		LocalDateTime toEndTime = EndTime != null ? LocalDateTime.parse(EndTime) : LocalDateTime.now();
		System.out.println(startTime);
		System.out.println(EndTime);
		System.out.println(tostartTime);
		System.out.println(toEndTime);
		if (type.isMinuteType()) {
			int Minute = type.getMinute();
			List<CandleMinute> candles = candleMinuteRepository.findByStockCodeAndTimeBetweenOrderByTimeAsc(stockCode,
					tostartTime, toEndTime);
			if (Minute == 1) {
				return candles.stream().map(c -> new CandleDTO(c.getTime().toString(), c.getOpen(), c.getHigh(),
						c.getLow(), c.getClose(), c.getVolume())).toList();
			}
			return candles.stream()
					.collect(Collectors.groupingBy(c -> c.getTime().withMinute((c.getTime().getMinute() / Minute) * Minute)
							.withSecond(0).withNano(0)))
					.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
						List<CandleMinute> group = entry.getValue();
						group.sort(Comparator.comparing(CandleMinute::getTime));
						return new CandleDTO(group.get(0).getTime().toString(), group.get(0).getOpen(),
								group.stream().mapToInt(CandleMinute::getHigh).max().orElse(0),
								group.stream().mapToInt(CandleMinute::getLow).min().orElse(0),
								group.get(group.size() - 1).getClose(),
								group.stream().mapToLong(CandleMinute::getVolume).sum());
					}).toList();
		}
		if (type == CandleType.DAY) {
			LocalDate StartDate = tostartTime.toLocalDate();
			LocalDate EndDate = toEndTime.toLocalDate();
			return stockRepository.findByStockCodeAndDateBetweenOrderByDateAsc(stockCode, StartDate, EndDate).stream()
					.map(s -> new CandleDTO(s.getDate().toString(), s.getOpenPrice(), s.getHighPrice(), s.getLowPrice(),
							s.getClosePrice(), s.getTotalvolume()))
					.toList();
		}
		throw new IllegalArgumentException("지원하지 않는 타입: " + type);
	}
}