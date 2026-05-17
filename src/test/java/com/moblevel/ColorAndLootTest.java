package com.moblevel;

public class ColorAndLootTest {

    // Color mapping based on level (returns color name as string)
    public static String getColorForLevel(int level) {
        if (level >= 150) return "DARK_PURPLE";
        if (level >= 130) return "RED";
        if (level >= 100) return "YELLOW";
        if (level >= 50) return "AQUA";
        return "GREEN";
    }

    // Loot calculation
    public static int calculateLootMultiplier(int level, int originalCount) {
        double lootMultiplierPerLevel = 0.02;
        float multiplier = 1.0f + (level * (float) lootMultiplierPerLevel);

        if (level >= 150) {
            multiplier += 3.0f;
        }

        return Math.round(originalCount * multiplier);
    }

    public static void main(String[] args) {
        System.out.println("=== Color and Loot Test ===\n");

        // Test 1: Color by Level
        System.out.println("=== Test 1: Color Mapping by Level ===");
        System.out.println("Level | Expected Color        | Actual Color");
        System.out.println("------|------------------------|-------------------------");

        int[] levels = {1, 5, 10, 20, 50, 75, 100, 125, 130, 150};
        String[] expectedColors = {
            "GREEN", "GREEN", "GREEN", "GREEN",
            "AQUA", "AQUA", "YELLOW", "YELLOW",
            "RED", "DARK_PURPLE"
        };

        boolean allColorsCorrect = true;
        for (int i = 0; i < levels.length; i++) {
            int level = levels[i];
            String color = getColorForLevel(level);
            String expected = expectedColors[i];
            boolean match = color.equals(expected);

            System.out.printf("%3d   | %-24s | %-24s | %s\n",
                level, expected, color, match ? "✓" : "✗");

            if (!match) allColorsCorrect = false;
        }

        System.out.println("\n" + (allColorsCorrect ? "✓ All colors correct" : "✗ Some colors incorrect"));

        // Test 2: Loot Multiplier by Level
        System.out.println("\n=== Test 2: Loot Multiplier ===");
        System.out.println("Level | Original | Expected Range | Actual | Pass");
        System.out.println("------|----------|-----------------|--------|------");

        int[] testLevels = {1, 5, 10, 20, 50, 75, 100, 130, 150};
        int originalCount = 1;

        for (int level : testLevels) {
            int result = calculateLootMultiplier(level, originalCount);

            // Expected ranges based on formula
            double minMultiplier = 1.0 + (level * 0.02);
            double maxMultiplier = minMultiplier;
            if (level >= 150) maxMultiplier += 3.0;

            int minResult = (int) Math.round(originalCount * minMultiplier);
            int maxResult = (int) Math.round(originalCount * maxMultiplier);

            boolean pass = result >= minResult && result <= maxResult;

            System.out.printf("%3d   | %8d | %4d - %4d     | %6d | %s\n",
                level, originalCount, minResult, maxResult, result, pass ? "✓" : "✗");
        }

        // Test 3: Stack Loot (count > 1)
        System.out.println("\n=== Test 3: Loot Multiplier with Stacks ===");
        System.out.println("Level | Count | Original | Expected Range | Actual | Pass");
        System.out.println("------|-------|----------|-----------------|--------|------");

        int[] stackCounts = {3, 10, 64};
        int[] stackLevels = {10, 50, 100, 150};

        for (int count : stackCounts) {
            for (int level : stackLevels) {
                int result = calculateLootMultiplier(level, count);
                double multiplier = 1.0 + (level * 0.02);
                if (level >= 150) multiplier += 3.0;

                int minResult = (int) Math.round(count * multiplier) - 1;
                int maxResult = (int) Math.round(count * multiplier) + 1;

                boolean pass = result >= minResult && result <= maxResult;

                System.out.printf("%3d   | %5d | %8d | %4d - %4d     | %6d | %s\n",
                    level, count, count, minResult, maxResult, result, pass ? "✓" : "✗");
            }
        }

        // Test 4: Level 150+ Special Bonus
        System.out.println("\n=== Test 4: Level 150+ Special Bonus ===");
        System.out.println("Level 150+ mobs get +3.0x multiplier bonus");
        System.out.println("Level | Base Mult | Bonus | Total | Result");
        System.out.println("------|-----------|-------|-------|-------");

        int[] bonusLevels = {130, 140, 150, 160, 200};
        for (int level : bonusLevels) {
            double baseMult = 1.0 + (level * 0.02);
            double bonus = (level >= 150) ? 3.0 : 0.0;
            double totalMult = baseMult + bonus;
            int result = calculateLootMultiplier(level, 1);

            System.out.printf("%3d   | %.2fx      | %.1fx  | %.2fx  | %d\n",
                level, baseMult, bonus, totalMult, result);
        }

        // Integration: Color + Loot
        System.out.println("\n=== Integration Test: Color + Loot ===");
        System.out.println("Zombie Level 50: Green color, 2.5x loot");
        System.out.println("Zombie Level 100: Yellow color, 5x loot");
        System.out.println("Zombie Level 150: Dark Purple color, 8.5x loot");

        int[] integrationLevels = {50, 100, 150};
        String[] integrationColors = {"GREEN", "YELLOW", "DARK_PURPLE"};
        double[] integrationMultipliers = {2.5, 5.0, 8.5};

        boolean allIntegrationPass = true;
        for (int i = 0; i < integrationLevels.length; i++) {
            int level = integrationLevels[i];
            String color = getColorForLevel(level);
            int loot = calculateLootMultiplier(level, 1);

            boolean colorMatch = color.equals(integrationColors[i]);
            boolean lootMatch = loot >= (integrationMultipliers[i] - 0.5) && loot <= (integrationMultipliers[i] + 0.5);
            boolean pass = colorMatch && lootMatch;

            System.out.printf("Level %3d: Color=%s (%s) Loot=%d (%s) %s\n",
                level,
                color,
                colorMatch ? "✓" : "✗",
                loot,
                lootMatch ? "✓" : "✗",
                pass ? "✓ PASS" : "✗ FAIL");

            if (!pass) allIntegrationPass = false;
        }

        System.out.println("\n" + (allIntegrationPass ? "✓ All integration tests PASSED" : "✗ Some tests FAILED"));
    }
}
