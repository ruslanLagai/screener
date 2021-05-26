package com.home.project.stocks.model.processing;

import com.home.project.stocks.model.candles.Candle;
import com.home.project.stocks.processor.DodgeProcessor;
import com.home.project.stocks.processor.HammerProcessor;
import com.home.project.stocks.processor.StocksProcessor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Getter
@Setter
public class ProcessingResult {
    private static Map<Class<? extends StocksProcessor>, String> stocksProcessorMap = Map.of(
            DodgeProcessor.class, "isDodge",
            HammerProcessor.class, "isHammer"
    );

    private String figi;
    private String ticker;
    private Boolean isDodge;
    private Boolean isHammer;
    private MultiValueMap<StocksProcessor.Processors, Candle> processedCandles = new LinkedMultiValueMap<>();

    @SneakyThrows
    public void initField(boolean value, StocksProcessor stocksProcessor) {
        var field = stocksProcessorMap.get(stocksProcessor.getClass());
        this.getClass().getDeclaredField(field).set(this, value);
    }

    public boolean shouldBeSent() {
        return isDodge || isHammer;
    }
}
