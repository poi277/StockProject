package Poi.Stock.features.Lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class StockLock {
	private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

	private ReentrantLock getLock(String stockCode) {
		return lockMap.computeIfAbsent(stockCode, k -> new ReentrantLock());
	}

	public void lock(String stockCode) {
		getLock(stockCode).lock();
	}

	public void unlock(String stockCode) {
		getLock(stockCode).unlock();
	}
}