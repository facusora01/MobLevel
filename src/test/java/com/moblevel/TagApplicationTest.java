package com.moblevel;

import java.util.HashSet;
import java.util.Set;

public class TagApplicationTest {

    public static int parseLevelFromTag(String tag) {
        if (tag.startsWith("lvl:")) {
            try {
                return Integer.parseInt(tag.substring(4));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static int parseLevelFromTags(Set<String> tags) {
        for (String tag : tags) {
            int level = parseLevelFromTag(tag);
            if (level > 0) {
                return level;
            }
        }
        return 0;
    }

    public static String extractBaseName(String rawName) {
        String baseName = rawName;

        if (rawName.startsWith("[Lv")) {
            int endBracket = rawName.indexOf("] ");
            if (endBracket != -1) {
                baseName = rawName.substring(endBracket + 2);
            }
        }

        if (baseName.contains(" ♥")) {
            baseName = baseName.split(" ♥")[0];
        }

        return baseName;
    }

    public static String reconstructName(int level, String baseName) {
        if (level <= 0) {
            return baseName;
        }
        return "[Lv" + level + "] " + baseName;
    }

    public static void main(String[] args) {
        System.out.println("=== Tag Application Simulation ===\n");

        // Scenario 1: Spawn without level, then apply tag
        System.out.println("Scenario 1: Spawn Zombie, then apply /tag @s add lvl:20");
        System.out.println("---");

        String mobTypeName = "Zombie";
        int capabilityLevel = 0;  // No level assigned on spawn
        String displayName = mobTypeName;  // No custom name yet

        System.out.println("1. Mob spawned:");
        System.out.println("   Capability level: " + capabilityLevel);
        System.out.println("   Display name: " + displayName);

        // Simulate tag being applied via command
        Set<String> mobTags = new HashSet<>();
        mobTags.add("lvl:20");

        System.out.println("\n2. Player runs: /tag @s add lvl:20");
        System.out.println("   Mob tags: " + mobTags);

        // Check what current code does (PROBLEM: only reads from capability)
        System.out.println("\n3. Current code behavior (reads from capability only):");
        System.out.println("   Level from capability: " + capabilityLevel);
        System.out.println("   Name update: " + (capabilityLevel > 0 ? "UPDATED" : "NOT UPDATED (level <= 0)"));
        System.out.println("   Result: Display name stays '" + displayName + "' ✗ BUG");

        // Better: Read from tags if capability is empty
        System.out.println("\n4. FIXED code behavior (reads from tags if capability empty):");
        int levelFromTags = parseLevelFromTags(mobTags);
        System.out.println("   Level from tags: " + levelFromTags);
        String baseName = extractBaseName(displayName);
        String newName = reconstructName(levelFromTags, baseName);
        System.out.println("   Result: Display name becomes '" + newName + "' ✓ FIXED");

        // Scenario 2: Mob with level, then change level via tag
        System.out.println("\n\nScenario 2: Mob with [Lv10] Creeper, apply /tag @s add lvl:50");
        System.out.println("---");

        String oldName = "[Lv10] Creeper";
        int oldCapLevel = 10;
        Set<String> tags2 = new HashSet<>();
        tags2.add("lvl:50");

        System.out.println("1. Existing mob:");
        System.out.println("   Capability level: " + oldCapLevel);
        System.out.println("   Display name: " + oldName);

        System.out.println("\n2. Player runs: /tag @s add lvl:50");
        System.out.println("   Mob tags: " + tags2);

        System.out.println("\n3. Current code: reads from capability only");
        System.out.println("   Level: " + oldCapLevel);
        System.out.println("   Name stays: '" + oldName + "' ✗ BUG (not updated to Lv50)");

        System.out.println("\n4. FIXED code: reads from tags (overrides capability)");
        int newLevel = parseLevelFromTags(tags2);
        String newBaseName = extractBaseName(oldName);
        String updatedName = reconstructName(newLevel, newBaseName);
        System.out.println("   Level from tags: " + newLevel);
        System.out.println("   Name becomes: '" + updatedName + "' ✓ FIXED");

        // Scenario 3: With custom tag preserved
        System.out.println("\n\nScenario 3: Mob with [Lv10] Spider #elite, apply /tag @s add lvl:75");
        System.out.println("---");

        String namedMob = "[Lv10] Spider #elite";
        Set<String> tags3 = new HashSet<>();
        tags3.add("lvl:75");

        System.out.println("1. Named mob with custom tag:");
        System.out.println("   Display name: '" + namedMob + "'");
        System.out.println("   Tags: " + tags3);

        System.out.println("\n2. FIXED code preserves custom tags:");
        int newLvl = parseLevelFromTags(tags3);
        String base = extractBaseName(namedMob);
        String final_name = reconstructName(newLvl, base);
        System.out.println("   Base name extracted: '" + base + "'");
        System.out.println("   Final name: '" + final_name + "' ✓ Custom tag preserved");
    }
}
