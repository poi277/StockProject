package Poi.Stock.features.Order;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Poi.Stock.DTO.user.TradeDTO;
import Poi.Stock.features.User.HaveStock;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.HaveStockRepository;
import Poi.Stock.repository.OrderRepository;
import Poi.Stock.repository.StockUserRepository;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final StockUserRepository stockUserRepository;
	private final HaveStockRepository haveStockRepository;

	/**
	 * 주문 생성 및 저장
	 */
	@Transactional
	public Order createOrder(String userId, TradeDTO tradeDTO) {
		// 1. 사용자 확인
		StockUser user = stockUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

		// 2. 매수 시 자산 확인
		if (tradeDTO.getTradeType() == tradeType.BUY) {
			int totalCost = tradeDTO.getTradePrice() * tradeDTO.getQuantity();
			if (user.getAsset() < totalCost) {
				throw new RuntimeException(String.format("자산이 부족합니다. 필요: %d원, 보유: %d원", totalCost, user.getAsset()));
			}

			// 매수 주문 시 자산 예약 (미리 차감)
			user.setAsset(user.getAsset() - totalCost);
			stockUserRepository.save(user);
		}
		// 3. 매도 시 보유 주식 확인
		if (tradeDTO.getTradeType() == tradeType.SELL) {
			HaveStock haveStock = haveStockRepository.findByStockUserAndStockCode(user, tradeDTO.getStockCode())
					.orElseThrow(() -> new RuntimeException("보유한 주식이 없습니다."));
			if (haveStock.getQuantity() < tradeDTO.getQuantity()) {
				throw new RuntimeException(String.format("보유 수량이 부족합니다. 보유: %d주, 매도 요청: %d주", haveStock.getQuantity(),
						tradeDTO.getQuantity()));
			}
			haveStock.setQuantity(haveStock.getQuantity() - tradeDTO.getQuantity());
			haveStockRepository.save(haveStock);
		}

		// 4. 주문 생성
		Order order = new Order();
		order.setUserId(userId);
		order.setStockCode(tradeDTO.getStockCode());
		order.setTradeType(tradeDTO.getTradeType());
		order.setQuantity(tradeDTO.getQuantity());
		order.setRemainingQuantity(tradeDTO.getQuantity());
		order.setTradePrice(tradeDTO.getTradePrice());
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());
		order.setPriority(System.nanoTime());

		// 4. DB 저장
		return orderRepository.save(order);
	}

	/**
	 * 호가창 조회 (매도/매수)
	 */
	public Map<String, Object> getOrderBook(String stockCode) {
		// 매도 호가 (가격 낮은 순)
		List<Order> sellOrders = orderRepository.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(
				stockCode, tradeType.SELL, List.of(OrderStatus.PENDING, OrderStatus.PARTIAL));
		// 매수 호가 (가격 높은 순)
		List<Order> buyOrders = orderRepository.findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceDescPriorityAsc(
				stockCode, tradeType.BUY, List.of(OrderStatus.PENDING, OrderStatus.PARTIAL));
		Map<String, Object> orderBook = new HashMap<>();
		orderBook.put("sellOrders", sellOrders);
		orderBook.put("buyOrders", buyOrders);
		return orderBook;
	}

	/**
	 * 주문 취소
	 */
	@Transactional
	public void cancelOrder(String userId, Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

		// 본인 주문인지 확인
		if (!order.getUserId().equals(userId)) {
			throw new RuntimeException("본인의 주문만 취소할 수 있습니다");
		}

		// 이미 체결된 주문은 취소 불가
		if (order.getStatus() == OrderStatus.COMPLETED) {
			throw new RuntimeException("이미 체결된 주문은 취소할 수 없습니다");
		}

		// 매수 주문이었다면 예약된 자산 반환
		if (order.getTradeType() == tradeType.BUY) {
			StockUser user = stockUserRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

			int refundAmount = order.getTradePrice() * order.getRemainingQuantity();
			user.setAsset(user.getAsset() + refundAmount);
			stockUserRepository.save(user);
		}

		// 주문 상태 변경
		order.setStatus(OrderStatus.CANCELLED);
		orderRepository.save(order);
	}
}