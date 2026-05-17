package com.moblevel;

public class TagAndNameTest {

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
        String prefix = "[Lv" + level + "] ";
        return prefix + baseName;
    }

    public static void main(String[] args) {
        System.out.println("=== Tag and Name Logic Test ===\n");

        // Test tag parsing
        System.out.println("=== Tag Parsing ===");
        String[] tags = {"lvl:20", "lvl:50", "lvl:150", "lvl:5", "other_tag", ""};

        for (String tag : tags) {
            int level = parseLevelFromTag(tag);
            System.out.println("Tag '" + tag + "' -> Level " + level + (level > 0 ? " ✓" : " (not lvl:X)"));
        }

        // Test name extraction
        System.out.println("\n=== Name Extraction ===");
        String[] names = {
            "[Lv20] Zombie",
            "[Lv50] Skeleton #boss",
            "[Lv150] Creeper",
            "Zombie ♥",
            "[Lv100] Spider #elite ♥",
            "Plain Zombie"
        };

        for (String name : names) {
            String baseName = extractBaseName(name);
            System.out.println("'" + name + "' -> base: '" + baseName + "'");
        }

        // Test name reconstruction
        System.out.println("\n=== Name Reconstruction ===");
        String[] baseNames = {
            "Zombie",
            "Skeleton #boss",
            "Creeper",
            "Spider #elite"
        };

        for (String baseName : baseNames) {
            String reconstructed = reconstructName(50, baseName);
            System.out.println("Level 50 + '" + baseName + "' -> '" + reconstructed + "'");
        }

        // Integration test: combine level from tag + name reconstruction
        System.out.println("\n=== Integration: Tag -> Name Update ===");
        String mobName = "Zombie";
        String tagWithLevel = "lvl:20";
        int levelFromTag = parseLevelFromTag(tagWithLevel);
        String newName = reconstructName(levelFromTag, mobName);
        System.out.println("Original name: '" + mobName + "'");
        System.out.println("Tag applied: '" + tagWithLevel + "'");
        System.out.println("New name: '" + newName + "'");
        System.out.println("Should be '[Lv20] Zombie': " + (newName.equals("[Lv20] Zombie") ? "✓" : "✗"));

        // Test case: name with existing level, new level applied via tag
        System.out.println("\n=== Update Existing Name with New Level ===");
        String oldName = "[Lv10] Skeleton #boss";
        String baseName2 = extractBaseName(oldName);
        String newTagLevel = "lvl:50";
        int newLevel = parseLevelFromTag(newTagLevel);
        String updatedName = reconstructName(newLevel, baseName2);
        System.out.println("Old name: '" + oldName + "'");
        System.out.println("New tag: '" + newTagLevel + "'");
        System.out.println("Updated name: '" + updatedName + "'");
        System.out.println("Should be '[Lv50] Skeleton #boss': " + (updatedName.equals("[Lv50] Skeleton #boss") ? "✓" : "✗"));
    }
}
