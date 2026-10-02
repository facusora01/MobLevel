# Testing MobLevel

Tests unitarios para XP y drops proporcionales al nivel.

## Ejecutar Tests

**Todos:**
```powershell
.\gradlew test
```

**Varios específicos:**
```powershell
.\gradlew test --tests "com.moblevel.DropsCalculatorTest.testCalculateExperienceDropLevel150*"
```

**Un test:**
```powershell
.\gradlew test --tests "com.moblevel.DropsCalculatorTest.testCalculateExperienceDropLevel150Bonus"
```

## Ver Resultados

Reporte HTML: `build/reports/tests/test/index.html`

Logs completos: `.\src\test\java\run-tests-with-logs.ps1`

## Fórmulas

**XP Multiplier:** `1.0 + (level × 0.01)` (+2.0 si level ≥ 150)
- Nivel 50: 1.5x | Nivel 100: 2.0x | Nivel 150: 4.5x

**Item Drops:** `1.0 + (level × 0.02)` (+3.0 si level ≥ 150)
- Nivel 50: 2.0x | Nivel 100: 3.0x | Nivel 150: 7.0x

## Casos de Test

**DropsCalculatorTest.java** (14 tests)
- Cálculo de drops y XP por nivel
- Casos edge (nivel 0, negativo)
- Extracción de tags

**MobEventsTest.java** (10 tests)
- Validación de escalado proporcional
- Fórmula lineal de XP
- Valores grandes
