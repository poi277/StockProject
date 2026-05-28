package Poi.Stock.features.Bot;

import java.util.Random;

import org.springframework.stereotype.Component;

import Poi.Stock.util.EnumUtil.MarketState;

@Component
public class MarketStateHolder {
	private final Random random = new Random();
	private MarketState state = MarketState.BEAR;
	// intensity는 나중에 candle을 이용하여 바꿀예정
	private int intensity = 60; // 추세 강도 (0~100, 높을수록 추세 강함)

	public MarketState getState() {
		return state;
	}

	public void setState(MarketState state) {
		this.state = state;
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public int peoplevix() {
		int roll = random.nextInt(100);
		if (roll < intensity) {
			return random.nextInt(2) + 1;
		} else {
			return random.nextInt(2) + 2;
		}
	}
}