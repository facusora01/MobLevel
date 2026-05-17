package com.moblevel;

public class DropsCalculatorTest {

    public static int calculateNewCount(int level, int originalCount) {
        int baseBonus = level / 50;
        float percentBonus = 1.0f + (level * 0.05f);
        int newCount = Math.round(originalCount * percentBonus) + baseBonus;
        return newCount;
    }

    public static void main(String[] args) {
        System.out.println("=== Drops Calculator Test ===\n");

        // Test cases
        int[] levels = {5, 10, 20, 30, 50, 75, 100, 130, 150};
        int[] itemCounts = {1, 3};

        for (int count : itemCounts) {
            System.out.println("Original count: " + count);
            System.out.println("Level | Base Bonus | % Bonus | Result | Expected");
            System.out.println("------|------------|---------|--------|----------");

            for (int level : levels) {
                int baseBonus = level / 50;
                float percentBonus = 1.0f + (level * 0.05f);
                int result = calculateNewCount(level, count);

                System.out.printf("%3d   | %2d         | %.2fx    | %3d    |\n",
                    level, baseBonus, percentBonus, result);
            }
            System.out.println();
        }

        // Specific tests
        System.out.println("=== Specific Tests ===\n");

        System.out.println("Level 20, count 1:");
        int r20 = calculateNewCount(20, 1);
        System.out.println("  Result: " + r20 + ", Expected: 2, Pass: " + (r20 == 2 ? "✓" : "✗"));

        System.out.println("Level 50, count 1:");
        int r50 = calculateNewCount(50, 1);
        System.out.println("  Result: " + r50 + ", Expected: 4+, Pass: " + (r50 >= 4 ? "✓" : "✗"));

        System.out.println("Level 100, count 1:");
        int r100 = calculateNewCount(100, 1);
        System.out.println("  Result: " + r100 + ", Expected: 7-8, Pass: " + ((r100 == 7 || r100 == 8) ? "✓" : "✗"));

        System.out.println("Level 150, count 1:");
        int r150 = calculateNewCount(150, 1);
        System.out.println("  Result: " + r150 + ", Expected: 11-12, Pass: " + ((r150 == 11 || r150 == 12) ? "✓" : "✗"));

        System.out.println("\nLevel 50, count 3 (stack):");
        int r50_3 = calculateNewCount(50, 3);
        System.out.println("  Result: " + r50_3 + ", Expected: 11+, Pass: " + (r50_3 >= 11 ? "✓" : "✗"));
    }
}
