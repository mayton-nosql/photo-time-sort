package mayton.phototimesort;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategySelectorTest {

    @Test
    void beanUtilsTest() {
        assertEquals("DummyStrategyCopyStrategy", StrategySelector.strategyToClassName("DUMMY_STRATEGY"));
    }

}