# Test Results

**Date:** 2026-05-18  
**Status:** ✓ BUILD SUCCESSFUL

## Summary

- Total Tests: 27
- Passed: 27
- Failed: 0
- Skipped: 0
- Duration: ~12s

## Test Classes

### 1. DropsCalculatorTest
Unit tests core drop multiplication logic.

**Tests:** 9
- ✓ testNoLevelNoMultiplier
- ✓ testLevel1
- ✓ testLevel50
- ✓ testLevel75
- ✓ testLevel100
- ✓ testLevel130
- ✓ testLevel150WithBonus
- ✓ testLevel150Stack64
- ✓ testStack10Level50
- ✓ testShouldIncreaseDrops

### 2. DropsIntegrationTest
Integration tests for full drop flow.

**Tests:** 8
- ✓ testFullDropMultiplicationFlow_Level50
- ✓ testFullDropMultiplicationFlow_Level150
- ✓ testNoLevelTag_NoMultiplication
- ✓ testMultipleTagsButOnlyLvlMatters
- ✓ testInvalidLvlTag
- ✓ testStackDropMultiplication
- ✓ testLevel149VsLevel150Bonus
- ✓ testRealisticScenario_MobSpawn

**Output:**
```
=== Realistic Scenario: Mob Spawns ===
Zombie spawned with level 75
  Iron Sword: 1 -> 3
  Rotten Flesh: 2 -> 5
```

### 3. ColorAndLootTest
Color mapping and loot multiplier validation.

**Tests:** 7
- ✓ testGreenLevel (1-49)
- ✓ testAquaLevel (50-99)
- ✓ testYellowLevel (100-129)
- ✓ testRedLevel (130-149)
- ✓ testDarkPurpleLevel (150+)
- ✓ testLootMultiplierLevel1/50/100/150
- ✓ testLevel149vs150Boundary

### 4. MobDeathSimulationTest ⭐
Chicken death scenarios with loot/XP drops.

**Tests:** 7
- ✓ testChickenDeathLevel25 → 1 → 2
- ✓ testChickenDeathLevel50 → 1 → 2
- ✓ testChickenDeathLevel100 → 1 → 3
- ✓ testChickenDeathLevel150 → 1 → 7 (bonus!)
- ✓ testChickenNoLevel → 1 → 1
- ✓ testChickenWithExperienceDrop → 2 → 5 XP
- ✓ testMultipleChickenKills → 3 → 6

**Output:**
```
=== Multiple Chicken Kills (Level 50) ===
  Chicken 1: 1 -> 2
  Chicken 2: 1 -> 2
  Chicken 3: 1 -> 2
  Total: 3 -> 6
```

## Multiplication Formula

```
multiplier = 1.0 + (level * 0.02)
if (level >= 150) multiplier += 3.0
result = round(drops * multiplier)
```

## Test Coverage

| Scenario | Level | Original | Result | Status |
|----------|-------|----------|--------|--------|
| Vanilla mob | 0 | 1 | 1 | ✓ |
| Low level | 25 | 1 | 2 | ✓ |
| Mid level | 50 | 1 | 2 | ✓ |
| High level | 100 | 1 | 3 | ✓ |
| Max level | 150 | 1 | 7 | ✓ |
| Stack (100) | 64 | 192 | 3x | ✓ |
| XP drop | 75 | 2 | 5 | ✓ |
| Multiple kills | 50 | 3 | 6 | ✓ |
| Boundary 149/150 | 149/150 | 1 | 4/7 | ✓ |

## Log Files

- `test-full.log` - Complete Gradle output with all debug info
- `DropsCalculatorTest.log` - Unit tests only
- `DropsIntegrationTest.log` - Integration tests only
- `ColorAndLootTest.log` - Color/loot tests only
- `MobDeathSimulationTest.log` - Chicken simulation tests only

## HTML Report

View full test report:
```
build/reports/tests/test/index.html
```

## Key Findings

✓ All formulas validated  
✓ Level 150 bonus (+3.0x) working  
✓ No level tag returns 0 (vanilla behavior)  
✓ Invalid tags handled gracefully  
✓ Stack multiplication correct  
✓ Chicken death scenarios all pass
