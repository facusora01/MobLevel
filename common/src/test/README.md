# MobLevel Tests

Suite de tests para validar la lógica de drops y XP multiplicados por nivel.

## Tests Incluidos

### DropsCalculatorTest
Tests unitarios de la lógica core de multiplicación de drops.

- `testNoLevelNoMultiplier` - Sin nivel, drops sin cambios
- `testLevel1` a `testLevel150WithBonus` - Validación de multiplicadores por nivel
- `testStack64Level150` - Multiplicación con stacks de 64 items
- `testShouldIncreaseDrops` - Validar si un drop aumentó

**Casos:** Level 1, 50, 75, 100, 130, 150 (con bonus especial de +3.0x)

### DropsIntegrationTest
Tests de integración que simulan flujos completos.

- `testFullDropMultiplicationFlow_Level50/150` - Extrae nivel del tag, calcula drops
- `testNoLevelTag_NoMultiplication` - Mob sin tag lvl no multiplica
- `testMultipleTagsButOnlyLvlMatters` - Solo tag `lvl:X` se usa, otros ignorados
- `testInvalidLvlTag` - `lvl:abc` devuelve 0 (no se rompe)
- `testStackDropMultiplication` - Stack de 64 items multiplicado
- `testLevel149VsLevel150Bonus` - Valida el bonus especial en level 150
- `testRealisticScenario_MobSpawn` - Simula Zombie nivel 75 dropeando espada + carne

### ColorAndLootTest
Tests de colores por nivel y multiplicadores de loot (convertido de script main() a JUnit).

- `testGreenLevel` - Nivel 1-49 = GREEN
- `testAquaLevel` - Nivel 50-99 = AQUA
- `testYellowLevel` - Nivel 100-129 = YELLOW
- `testRedLevel` - Nivel 130-149 = RED
- `testDarkPurpleLevel` - Nivel 150+ = DARK_PURPLE
- `testLootMultiplierLevel*` - Validar multiplicadores
- `testLevel149vs150Boundary` - Verificar salto de bonus en 150

### MobDeathSimulationTest ⭐
Simula la muerte de un Chicken en varios escenarios.

- `testChickenDeathLevel25` - 1 raw chicken → 2 (1.5x)
- `testChickenDeathLevel50` - 1 raw chicken → 2 (2.0x)
- `testChickenDeathLevel100` - 1 raw chicken → 3 (3.0x)
- `testChickenDeathLevel150` - 1 raw chicken → 7 (7.0x con bonus!)
- `testChickenNoLevel` - Sin nivel, no multiplica
- `testChickenWithExperienceDrop` - Simula XP drop multiplicado
- `testMultipleChickenKills` - 3 chickens nivel 50: 3 → 6 drops

## Cómo Ejecutar

### Todos los tests
```bash
./gradlew test
```

### Solo un test específico
```bash
./gradlew test --tests "com.moblevel.MobDeathSimulationTest"
```

### Un método específico
```bash
./gradlew test --tests "com.moblevel.MobDeathSimulationTest.testChickenDeathLevel150"
```

### Con output detallado
```bash
./gradlew test --info
```

### Ver resultados en HTML
Después de ejecutar tests, abre:
```
build/reports/tests/test/index.html
```

## Fórmula de Multiplicación

```
multiplier = 1.0 + (level * 0.02)
si level >= 150: multiplier += 3.0

resultado = round(drops * multiplier)
```

**Ejemplos:**
- Nivel 50: `1 + (50 * 0.02) = 1 + 1.0 = 2.0x`
- Nivel 100: `1 + (100 * 0.02) = 1 + 2.0 = 3.0x`
- Nivel 150: `1 + (150 * 0.02) + 3.0 = 1 + 3.0 + 3.0 = 7.0x`

## Tags de Mob

Los mobs se marcan con tags en Minecraft:
```
lvl:25  - Mob nivel 25
lvl:100 - Mob nivel 100
lvl:150 - Mob nivel 150 (máximo bonus)
```

Extracción:
```java
Set<String> tags = new HashSet<>();
tags.add("lvl:75");
int level = DropsCalculator.getLevelFromTags(tags); // 75
```

## Ejecución en IDE

### Eclipse / IntelliJ
- Click derecho en la clase test → Run As → JUnit Test
- O en método específico → Run

### VS Code
- Con Extension "Test Runner for Java"
- Click en "Run" encima del test

## Notas

- Todos los tests usan JUnit 4 (`@Test`, `assertEquals`, etc.)
- Sin mocking - tests directos de la lógica
- Los tests validan tanto el **cálculo** como el **comportamiento** (shouldIncreaseDrops)
- Output se muestra en consola para debugging
