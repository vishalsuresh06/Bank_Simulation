package com.boma.banksim.util;

import java.util.Random;

public class RandomProvider {

    private final Random random;

    public RandomProvider(long seed) {
        this.random = new Random(seed);
    }

    public RandomProvider() {
        this.random = new Random();
    }

    /** Returns a value in [0.0, 1.0). */
    public double nextDouble() {
        return random.nextDouble();
    }

    /** Returns a value in [min, max). */
    public double nextDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    /** Returns a non-negative random int less than bound. */
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    /** Returns true with the given probability (0.0 – 1.0). */
    public boolean nextChance(double probability) {
        return random.nextDouble() < probability;
    }
}
