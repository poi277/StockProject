package Poi.Stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Poi.Stock.features.Order.Order;
import Poi.Stock.util.EnumUtil.OrderStatus;
import Poi.Stock.util.EnumUtil.tradeType;

public interface OrderRepository extends JpaRepository<Order, Long> {
	/**
	 * 특정 종목의 매도 호가 조회 메서드 이름만으로 자동 쿼리 생성
	 */
	List<Order> findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceAscPriorityAsc(String stockCode,
			tradeType tradeType, List<OrderStatus> statuses);

	/**
	 * 특정 종목의 매수 호가 조회
	 */
	List<Order> findByStockCodeAndTradeTypeAndStatusInOrderByTradePriceDescPriorityAsc(String stockCode,
			tradeType tradeType, List<OrderStatus> statuses);

	/**
	 * 사용자의 특정 종목 주문 내역
	 */
	List<Order> findByUserIdAndStockCodeOrderByCreatedAtDesc(String userId, String stockCode);

	List<Order> findByUserId(String userId);

	List<Order> findByUserIdAndStockCode(String userId, String stockCode);

	List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

	// 매도 호가 GROUP BY
//	@Query("SELECT o.tradePrice as tradePrice, SUM(o.remainingQuantity) as remainingQuantity " + "FROM Order o "
//			+ "WHERE o.stockCode = :stockCode " + "AND o.tradeType = :tradeType " + "AND o.status IN :statuses "
//			+ "GROUP BY o.tradePrice " + "ORDER BY o.tradePrice ASC")
//	List<OrderSummary> findGroupedOrdersAsc(@Param("stockCode") String stockCode,
//			@Param("tradeType") tradeType tradeType, @Param("statuses") List<OrderStatus> statuses);
//
//	@Query("SELECT o.tradePrice as tradePrice, SUM(o.remainingQuantity) as remainingQuantity " + "FROM Order o "
//			+ "WHERE o.stockCode = :stockCode " + "AND o.tradeType = :tradeType " + "AND o.status IN :statuses "
//			+ "GROUP BY o.tradePrice " + "ORDER BY o.tradePrice DESC")
//	List<OrderSummary> findGroupedOrdersDesc(@Param("stockCode") String stockCode,
//			@Param("tradeType") tradeType tradeType, @Param("statuses") List<OrderStatus> statuses);
}