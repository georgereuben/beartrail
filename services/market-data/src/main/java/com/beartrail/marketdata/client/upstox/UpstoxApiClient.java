package com.beartrail.marketdata.client.upstox;

import com.beartrail.marketdata.config.UpstoxConfig;
import com.beartrail.marketdata.model.dto.DataDto;
import com.beartrail.marketdata.model.dto.LatestMarketDataResponseDto;
import com.beartrail.marketdata.model.entity.Candle;
import com.beartrail.marketdata.model.entity.Stock;
import com.beartrail.marketdata.model.entity.TimeFrame;
import com.beartrail.marketdata.model.entity.TimeFrameValue;
import com.beartrail.marketdata.repository.StockRepository;
import com.beartrail.marketdata.repository.TimeFrameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UpstoxApiClient {

    private final String baseUrl;
    private final String authToken;
    private final RestTemplate restTemplate;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private TimeFrameRepository timeFrameRepository;

    public UpstoxApiClient(UpstoxConfig config, RestTemplate restTemplate) {
        this.baseUrl = config.getBaseUrl();
        this.authToken = config.getAuthToken();
        this.restTemplate = restTemplate;
    }

    public List<Candle> getCandles(List<String> symbolList, String interval) {
        String url = String.format("%s/market-quote/ohlc?instrument_key=%s&interval=%s", baseUrl, String.join(",", symbolList), interval);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<LatestMarketDataResponseDto> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                LatestMarketDataResponseDto.class
        );

        LatestMarketDataResponseDto response = responseEntity.getBody();
        if(response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new RuntimeException("No market data found for the given symbol and interval");
        }

        TimeFrame timeFrame = timeFrameRepository.findByValue(interval);
        if (timeFrame == null) {
            throw new IllegalArgumentException("Unsupported time interval: " + interval + ". Supported intervals: I1, I30, 1d");
        }

        try {
            return response.getData().entrySet().stream()
                    .map(entry -> {
                        String symbol = entry.getKey();
                        DataDto dataDto = entry.getValue();
                        Stock stock = stockRepository.findBySymbol(symbol);
                        if (stock == null) {
                            stock = Stock.builder()
                                    .symbol(symbol)
                                    .instrumentToken(dataDto.getInstrumentToken())
                                    .lastPrice(dataDto.getLastPrice())
                                    .build();
                            stock = stockRepository.save(stock);
                        } else {
                            stock.setInstrumentToken(dataDto.getInstrumentToken());
                            stock.setLastPrice(dataDto.getLastPrice());
                            stock = stockRepository.save(stock);
                        }
                        Candle candle = new Candle(
                                symbol,
                                dataDto.getLastPrice(),
                                dataDto.getInstrumentToken(),
                                dataDto.getPrevOhlc(),
                                dataDto.getLiveOhlc()
                        );
                        candle.setStock(stock);
                        candle.setTimeFrame(timeFrame);
                        return candle;
                    }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse market data response", e);
        }
    }
}