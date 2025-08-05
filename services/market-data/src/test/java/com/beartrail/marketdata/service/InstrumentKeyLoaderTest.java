package com.beartrail.marketdata.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class InstrumentKeyLoaderTest {

    @Autowired
    private InstrumentKeyLoader instrumentKeyLoader;

    @Test
    void instrumentKeysShouldBeLoaded() {
        assertThat(instrumentKeyLoader.getInstrumentKeys())
            .isNotNull()
            .isNotEmpty();
    }
}
