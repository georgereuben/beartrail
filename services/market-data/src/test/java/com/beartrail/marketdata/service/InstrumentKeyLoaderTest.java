package com.beartrail.marketdata.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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

