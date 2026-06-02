package Poi.Stock.features.Candle.Entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandleWithMA<T> {

    private T candle;

    private Map<Integer, Double> ma;
}