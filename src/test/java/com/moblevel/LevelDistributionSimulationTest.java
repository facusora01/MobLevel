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
    private static final double HIGH_CHANCE = 0.020;
    private static final double EXPONENT = 8.0;
    private static final double HOSTILE_HIGH_CHANCE = 0.045;
    private static final double HOSTILE_EXPONENT = 6.0;
    private static final double COMMON_SKEW = 0.75;
    private static final int MAX = 150;

    @Test
    public void simulate1000Spawns() {
        int[] counts = simulate(1_000, HIGH_CHANCE, EXPONENT);
        printReport("1,000 passive spawns (a night of play)", counts, 1_000);
    }

    @Test
    public void simulateHostileSpawns() {
        int[] counts = simulate(1_000_000, HOSTILE_HIGH_CHANCE, HOSTILE_EXPONENT);
        printReport("1,000,000 HOSTILE spawns", counts, 1_000_000);

        // Hostiles must stay rarer than one in a hundred at level 100+, but clearly
        // more common than passives, which sit near one in 800.
        int elite = 0;
        for (int lvl = 100; lvl <= MAX; lvl++) elite += counts[lvl];
        double elitePct = 100.0 * elite / 1_000_000;
        assertTrue("Hostile 100+ expected ~0.36%, got " + elitePct,
            elitePct > 0.25 && elitePct < 0.55);
    }

    @Test
    public void simulate1MillionSpawns() {
        int[] counts = simulate(1_000_000, HIGH_CHANCE, EXPONENT);
        printReport("1,000,000 PASSIVE spawns (stable percentages)", counts, 1_000_000);

        // Sanity: common band should hold ~98% and nothing may exceed MAX.
        int common = 0;
        for (int lvl = 1; lvl <= 20; lvl++) common += counts[lvl];
        double commonPct = 100.0 * common / 1_000_000;
        assertTrue("Common band expected ~98%, got " + commonPct, commonPct > 97.0 && commonPct < 99.0);

        // Passive 100+ is the whole point of the split: it must stay near one in 800.
        int elite = 0;
        for (int lvl = 100; lvl <= MAX; lvl++) elite += counts[lvl];
        double elitePct = 100.0 * elite / 1_000_000;
        assertTrue("Passive 100+ expected ~0.12%, got " + elitePct,
            elitePct > 0.07 && elitePct < 0.20);
    }

    private int[] simulate(int n, double highChance, double exponent) {
        Random random = new Random(20260705L); // fixed seed: reproducible report
        int[] counts = new int[MAX + 1];
        for (int i = 0; i < n; i++) {
            int level = LevelCalculator.rollSpawnLevel(
                random.nextDouble(), random.nextDouble(), highChance, exponent, COMMON_SKEW, MAX);
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
