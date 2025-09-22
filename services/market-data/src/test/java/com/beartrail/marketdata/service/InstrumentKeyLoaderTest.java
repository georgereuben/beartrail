package com.beartrail.marketdata.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InstrumentKeyLoaderTest {

  @Autowired private InstrumentKeyLoader instrumentKeyLoader;

  @Test
  void instrumentKeysShouldBeLoaded() {
    assertThat(instrumentKeyLoader.getInstrumentKeysToSymbolMap()).isNotNull().isNotEmpty();
  }

  @Test
  void shouldLoadEquityInstrumentsOnly() {
    var instrumentMap = instrumentKeyLoader.getInstrumentKeysToSymbolMap();

    // verify that all loaded instruments are from equity segments
    instrumentMap
        .keySet()
        .forEach(
            key -> {
              assertThat(key).matches("(NSE_EQ|BSE_EQ)\\|.*");
            });
  }

  @Test
  void shouldMapInstrumentKeyToTradingName() {
    var instrumentMap = instrumentKeyLoader.getInstrumentKeysToSymbolMap();

    // verify that the map contains instrument keys mapped to trading names
    assertThat(instrumentMap).isNotEmpty();

    // check that all values (trading names) are not empty
    instrumentMap
        .values()
        .forEach(
            name -> {
              assertThat(name).isNotBlank();
            });
  }
}
