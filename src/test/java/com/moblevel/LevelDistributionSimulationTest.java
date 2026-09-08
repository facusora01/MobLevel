package com.moblevel;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Monte Carlo simulation of natural spawn levels using the live Config defaults.
 * Run with: gradlew test --tests "com.moblevel.LevelDistributionSimulationTest" -i
 */
public class LevelDistributionSimulationTest {

    // Mirror of Config defaults (Config needs Forge to load, so constants are duplicated here).
    private static final double HIGH_CHANCE = 0.065;
    private static final double EXPONENT = 5.0;
    private static final double COMMON_SKEW = 0.75;
    private static final int MAX = 150;

    @Test
    public void simulate1000Spawns() {
        int[] counts = simulate(1_000);
        printReport("1,000 spawns (a night of play)", counts, 1_000);
    }

    @Test
    public void simulate1MillionSpawns() {
        int[] counts = simulate(1_000_000);
        printReport("1,000,000 spawns (stable percentages)", counts, 1_000_000);

        // Sanity: common band should hold ~93.5% and nothing may exceed MAX.
        int common = 0;
        for (int lvl = 1; lvl <= 20; lvl++) common += counts[lvl];
        double commonPct = 100.0 * common / 1_000_000;
        assertTrue("Common band expected ~93.5%, got " + commonPct, commonPct > 92.0 && commonPct < 95.0);
    }

    private int[] simulate(int n) {
        Random random = new Random(20260705L); // fixed seed: reproducible report
        int[] counts = new int[MAX + 1];
        for (int i = 0; i < n; i++) {
            int level = LevelCalculator.rollSpawnLevel(
                random.nextDouble(), random.nextDouble(), HIGH_CHANCE, EXPONENT, COMMON_SKEW, MAX);
            counts[level]++;
        }
        return counts;
    }

    // ANSI colors matching the in-game name tiers (GREEN <50, AQUA 50+, YELLOW 100+, RED 130+, DARK_PURPLE 150).
    private static final String GREEN = "\u001B[32m";
    private static final String AQUA = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";
    private static final String RESET = "\u001B[0m";

    private void printReport(String title, int[] counts, int total) {
        System.out.println("[SIM] ==== " + title + " ====");
        printBand(counts, total, 1, 20, "1-20    (vanilla-ish)", GREEN);
        printBand(counts, total, 21, 49, "21-49   (strong)", GREEN);
        printBand(counts, total, 50, 99, "50-99   (dangerous)", AQUA);
        printBand(counts, total, 100, 129, "100-129 (elite)", YELLOW);
        printBand(counts, total, 130, 149, "130-149 (nightmare)", RED);
        printBand(counts, total, 150, 150, "150     (apex)", PURPLE);
        System.out.println(String.format("[SIM] highest level rolled: %d", highest(counts)));
    }

    private void printBand(int[] counts, int total, int from, int to, String label, String color) {
        int sum = 0;
        for (int lvl = from; lvl <= to; lvl++) sum += counts[lvl];
        System.out.println(String.format("[SIM] %s%-22s %8d  %7.3f%%%s",
            color, label, sum, 100.0 * sum / total, RESET));
    }

    private int highest(int[] counts) {
        for (int lvl = counts.length - 1; lvl >= 1; lvl--) {
            if (counts[lvl] > 0) return lvl;
        }
        return 0;
    }
}
