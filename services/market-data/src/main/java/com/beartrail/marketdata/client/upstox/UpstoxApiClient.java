package com.beartrail.marketdata.client.upstox;

import com.beartrail.marketdata.config.UpstoxConfig;
import com.beartrail.marketdata.model.dto.DataDto;
import com.beartrail.marketdata.model.dto.LatestMarketDataResponseDto;
import com.beartrail.marketdata.model.entity.MarketData;
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

    public UpstoxApiClient(UpstoxConfig config, RestTemplate restTemplate) {
        this.baseUrl = config.getBaseUrl();
        this.authToken = config.getAuthToken();
        this.restTemplate = restTemplate;
    }

    public List<MarketData> getMarketData(List<String> symbolList, String interval) {
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

        try {
            return response.getData().entrySet().stream()
                    .map(entry -> {
                        String symbol = entry.getKey();
                        DataDto dataDto = entry.getValue();
                        dataDto.setSymbol(symbol);
                        return new MarketData(
                                dataDto.getSymbol(),
                                dataDto.getLastPrice(),
                                dataDto.getInstrumentToken(),
                                dataDto.getPrevOhlc(),
                                dataDto.getLiveOhlc(),
                                dataDto.getTimeInterval() // Assuming that interval is part of DataDto for now
                        );
                    }).toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse market data response", e);
        }
    }
}