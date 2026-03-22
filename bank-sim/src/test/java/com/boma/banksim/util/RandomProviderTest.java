package com.boma.banksim.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomProviderTest {

    @Test
    void nextDouble_seeded_sameSeedGivesSameSequence() {
        RandomProvider r1 = new RandomProvider(42L);
        RandomProvider r2 = new RandomProvider(42L);
        assertEquals(r1.nextDouble(), r2.nextDouble(), 1e-15);
        assertEquals(r1.nextDouble(), r2.nextDouble(), 1e-15);
    }

    @Test
    void nextDouble_defaultConstructor_returnsInUnitInterval() {
        RandomProvider r = new RandomProvider();
        for (int i = 0; i < 100; i++) {
            double v = r.nextDouble();
            assertTrue(v >= 0.0 && v < 1.0, "Expected [0,1) but got " + v);
        }
    }

    @Test
    void nextDouble_range_allValuesWithinBounds() {
        RandomProvider r = new RandomProvider(7L);
        for (int i = 0; i < 1_000; i++) {
            double v = r.nextDouble(0.1, 0.5);
            assertTrue(v >= 0.1 && v < 0.5, "Out of range: " + v);
        }
    }

    @Test
    void nextInt_allValuesWithinBound() {
        RandomProvider r = new RandomProvider(13L);
        for (int i = 0; i < 1_000; i++) {
            int v = r.nextInt(10);
            assertTrue(v >= 0 && v < 10, "Out of range: " + v);
        }
    }

    @Test
    void nextBoolean_returnsBothValues() {
        RandomProvider r = new RandomProvider(1L);
        boolean sawTrue = false, sawFalse = false;
        for (int i = 0; i < 100; i++) {
            if (r.nextBoolean()) sawTrue = true;
            else sawFalse = true;
        }
        assertTrue(sawTrue && sawFalse);
    }

    @Test
    void nextChance_zero_neverTrue() {
        RandomProvider r = new RandomProvider(99L);
        for (int i = 0; i < 1_000; i++) {
            assertFalse(r.nextChance(0.0));
        }
    }

    @Test
    void nextChance_one_alwaysTrue() {
        RandomProvider r = new RandomProvider(99L);
        for (int i = 0; i < 1_000; i++) {
            assertTrue(r.nextChance(1.0));
        }
    }
}
