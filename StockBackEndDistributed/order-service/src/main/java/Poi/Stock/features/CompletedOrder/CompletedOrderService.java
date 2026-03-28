package Poi.Stock.features.CompletedOrder;

import java.util.List;

import org.springframework.stereotype.Service;

import Poi.Stock.repository.CompletedOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompletedOrderService {
	private final CompletedOrderRepository completedOrderRepository;

	public List<CompletedOrder> getUserCompletedOrders(String userId) {
		return completedOrderRepository.findByUserId(userId);
	}
}
