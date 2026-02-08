package Poi.Stock.features.Stock;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Poi.Stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
	private final StockRepository stockRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final Random random = new Random();

	// 1초마다 DB에서 주식 정보를 읽어서 WebSocket으로 전송
	@Scheduled(fixedRate = 5000)
	public void broadcastStockPrices() {
		List<Stock> stocks = stockRepository.findAll();

		for (Stock stock : stocks) {
			// 랜덤 변동
			updateStockPrice(stock);

			// 각 종목별로 전송
			messagingTemplate.convertAndSend("/topic/stock/" + stock.getId(), stock);
		}
	}

	// 테스트용: 랜덤 가격 변동
	private void updateStockPrice(Stock stock) {
		int change = random.nextInt(2001) - 1000; // -1000 ~ +1000
		int newPrice = stock.getPrice() + change;

		stock.setPrice(newPrice);
		stock.setChangeAmount(change);
		stock.setChangeRate((double) change / stock.getPrice() * 100);
		stock.setVolume(random.nextLong(1000000) + 100000);

		// DB에 저장 (선택사항)
		// stockRepository.save(stock);
	}

	public List<Stock> getStockList() {
		return stockRepository.findAll();
	}

	public Optional<Stock> getStock(String stockId) {
		return stockRepository.findById(stockId);
	}
}

