# Tests Faltantes - MobLevel

## Estado Actual de Tests
Existen 3 archivos de test con lógica básica (main methods, no JUnit):
- ✓ DropsCalculatorTest
- ✓ TagAndNameTest
- ✓ TagApplicationTest

## Tests Que Faltan

### 1. Event Handlers (Critical)
- [ ] **onEntityJoinLevel** - Verificar asignación de nivel a nuevos mobs
  - Validar que mobs reciben tag `lvl:X` al spawnear
  - Validar que no duplica tags si el mob ya existe
  - Validar que calcula nivel aleatorio correctamente
  
- [ ] **onLivingDrops** - Verificar drops multiplicados
  - Validar que multiplica items correctamente por nivel
  - Validar bonus especial para nivel 150+
  - Validar drop de totem necklace (10% chance)
  - Validar que mobs sin nivel siguen dropeando normalmente

- [ ] **onDamageCalculation** - Verificar daño modificado
  - Validar que hostile mobs hacen más daño por nivel
  - Validar multiplicador 0.02 por nivel
  - Validar que no afecta daño de no-mobs

### 2. Mob Stats (Important)
- [ ] **applyLevelStats** - Verificar aplicación de stats
  - Validar que salud aumenta 5% por nivel
  - Validar que Creepers tienen radio de explosión aumentado
  - Validar que solo mobs nivel 150+ reciben totem necklace
  - Validar que equipment check funciona (canHoldItem)

- [ ] **updateMobName** - Verificar nombres con colores
  - Validar colores por rango de nivel (GREEN, AQUA, YELLOW, RED, DARK_PURPLE)
  - Validar que nombre es visible (setCustomNameVisible)
  - Validar que no duplica nombre si ya está correcto

### 3. Death Totem Mechanic (Important)
- [ ] **onLivingDeath** - Verificar totem de rescate
  - Validar que cancela muerte si tiene totem
  - Validar que restaura salud al máximo
  - Validar que aplica efectos (REGENERATION, ABSORPTION, FIRE_RESISTANCE)
  - Validar que remueve totem de inventario
  - Validar que envía network payload correctamente

### 4. Network & Client (Important)
- [ ] **ModMessages.register** - Verificar registro de packets
  - Validar que TotemAnimationPayload se registra correctamente
  - Validar que se envía a cliente correctamente
  
- [ ] **Client Animation** - Verificar animación del totem
  - Validar que displayItemActivation se ejecuta en cliente
  - Validar que sonido TOTEM_USE se reproduce
  - Validar que partículas TOTEM_OF_UNDYING se generan

### 5. Configuration (Medium)
- [ ] **Config Loading** - Verificar valores de config
  - Validar ELITE_CHANCE (0.025 por defecto)
  - Validar LEVEL_RARITY_EXPONENT (6.0 por defecto)
  - Validar MAX_LEVEL (150 por defecto)
  - Validar que config se persiste en archivo

### 6. Level Calculation (Medium)
- [ ] **calculateLevel** - Verificar generación de niveles
  - Validar distribución de niveles normales (1-129)
  - Validar elite mobs (130-150)
  - Validar respeto a ELITE_CHANCE
  - Validar respeto a MAX_LEVEL

### 7. Integration Tests (High Priority)
- [ ] **Full Mob Lifecycle** 
  - Spawn → Nivel Asignado → Stats Aplicados → Nombre Actualizado
  - Verificar que todos los eventos se ejecutan en orden

- [ ] **Passive vs Hostile Mobs**
  - Validar que passive mobs (sheep, cows) NO reciben totem
  - Validar que hostile mobs (zombies, skeletons) reciben totem si nivel 150+
  - Validar que ambos dropean items correctamente

- [ ] **Player Death Prevention**
  - Validar que jugador con totem sobrevive
  - Validar que totem se consume
  - Validar que efectos se aplican

## Prioridad de Implementación
1. **ALTA** - Event Handlers, Integration Tests, Death Totem
2. **MEDIA** - Mob Stats, Level Calculation, Configuration  
3. **BAJA** - Network (ya es más difícil de testear en unit)

## Notas
- Tests actuales son solo lógica de utility (main methods)
- Falta usar JUnit o framework de testing formal
- Muchos tests requieren mocking de objetos de Minecraft
- Tests de eventos son difíciles sin mock de event context
