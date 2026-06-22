# xEB — xd Elite Buffs | AI IDE Project Specification

> **Target**: Minecraft Forge 1.20.1 Mod | **Mod ID**: `xeb` | **AI IDE Context File**
>
> This file is the complete implementation specification for an AI coding agent (Cursor, Windsurf, Antigravity, Copilot, etc.).
> It contains the full architecture, all 28 buff specifications, and 8 incremental build prompts.

---

## 1. PROJECT OVERVIEW

**Mod Name**: xEB (xd Elite Buffs)
**Platform**: Minecraft Forge 1.20.1 (Forge version 47.2.0+)
**Core Dependency**: GeckoLib 1.20.1 (for medallion rendering and animations)
**Inspiration**: Mewgenics Elite Medallion system

### Core Concept
Every mob in Minecraft can spawn with **visual medallions** floating above their heads. Each medallion represents an **Elite Buff** that grants special abilities to the mob. The number and type of medallions depend on:
1. **Mob type**: Common enemies vs Bosses (different buff pools and weight limits)
2. **Game difficulty**: Easy/Normal/Hard/Hardcore (higher difficulty = more medallions)
3. **Medallion tier**: Common (bronze), Rare (silver), Legendary (gold)

### Visual Effects
- **Medallion**: Octagonal coin rendered above mob head via GeckoLib, rotating and bobbing
- **Mob Color Tinting**: Mob body tinted with buff color(s). Multiple buffs blend additively
- **Glowing Eyes**: Eyes emit light matching primary buff color via GeckoLib bone glow
- **Particles**: Each buff type has unique particle effects (fire trail, lightning arcs, etc.)

---

## 2. TECHNICAL STACK

```
Language:       Java 17
Build System:   Gradle (Forge MDK)
Mod Loader:     Minecraft Forge 47.2.0 (1.20.1)
MC Version:     1.20.1
Rendering:      GeckoLib 4.x for 1.20.1
Mappings:       Mojang Official (or SRG)
```

### build.gradle Dependencies
```groovy
dependencies {
    minecraft "net.minecraftforge:forge:1.20.1-47.2.0"
    implementation fg.deobf("software.bernie.geckolib:geckolib-forge-1.20.1:4.2.4")
}
```

---

## 3. COMPLETE PACKAGE STRUCTURE

```
src/main/java/com/xeb/
├── XEBMain.java                         // @Mod("xeb") entry point, registers all deferred registers
├── config/
│   ├── XEBConfig.java                   // Forge config (CommonConfig)
│   └── BuffConfig.java                  // Per-buff toggle, weight, intensity
├── buff/
│   ├── EliteBuff.java                   // Abstract base class for all buffs
│   ├── EliteBuffRegistry.java           // Registry (Map<String, EliteBuff>), lookup by ID
│   ├── BuffType.java                    // Enum: ENEMY_ONLY, BOSS_ONLY, UNIVERSAL
│   └── impl/
│       ├── SpikyBuff.java
│       ├── ReactiveBuff.java
│       ├── DamagingBuff.java
│       ├── ToughBuff.java
│       ├── ShieldedBuff.java
│       ├── ProtectedBuff.java
│       ├── SpeedyBuff.java
│       ├── FlamingBuff.java
│       ├── CreepyBuff.java
│       ├── LuckyBuff.java
│       ├── StaticBuff.java
│       ├── BouncyBuff.java
│       ├── MirrorBuff.java
│       ├── ResonantBuff.java
│       ├── UndyingBuff.java
│       ├── HealthyBuff.java
│       ├── SandyBuff.java
│       ├── InfestedBuff.java
│       ├── AbsorbentBuff.java
│       ├── DepressingBuff.java
│       ├── SlightlyDepressingBuff.java
│       ├── EvolvingBuff.java
│       ├── PlowBuff.java
│       ├── MegaBuff.java
│       ├── MadBuff.java
│       ├── TwinBuff.java
│       ├── HardyBuff.java
│       └── StickyBuff.java
├── medallion/
│   ├── MedallionManager.java            // Core: attach/detach/query medallions on entities
│   ├── MedallionData.java               // Data class: buffType, tier, intensity, color
│   └── MedallionType.java               // Enum: COMMON(Bronze), RARE(Silver), LEGENDARY(Gold)
├── event/
│   ├── MedallionSpawnHandler.java       // EntityJoinLevelEvent -> attach medallions
│   ├── BuffTickHandler.java             // ServerTickEvent -> buff per-tick logic
│   ├── BuffDamageHandler.java           // LivingDamageEvent -> thorns, shield, etc.
│   └── BuffDeathHandler.java            // LivingDeathEvent -> undying, infested, sandy
├── render/
│   ├── MedallionRenderLayer.java        // GeckoLib RenderLayer<LivingEntity> for medallion model
│   ├── MobColorOverlay.java             // Color tint overlay per active buffs
│   ├── GlowEyeOverlay.java              // GeckoLib bone-based emissive eye rendering
│   └── BuffParticleHandler.java         // Spawn buff-specific particles on server tick
├── compat/
│   ├── ModCompatManager.java            // Singleton, scans loaded mods, registers hooks
│   └── hooks/
│       ├── VanillaMobHook.java          // Default: vanilla entity tag registration
│       ├── TwilightForestHook.java
│       ├── MowziesMobsHook.java
│       ├── AlexMobsHook.java
│       ├── MutantMonstersHook.java
│       ├── CataclysmHook.java
│       └── // ... extensible per mod
├── effect/
│   ├── ModEffects.java                  // DeferredRegister<MobEffect> for all custom effects
│   ├── HolyShieldEffect.java            // Persistent shield that regenerates 1pt/tick
│   ├── PetrifyEffect.java               // Immobilize entity (no movement, no attack)
│   ├── TarredEffect.java                // Movement slow, stackable (20% per stack, max 5)
│   ├── SandstormEffect.java             // AoE weather: damage aura + particles
│   └── ManaDrainEffect.java              // Drains custom "mana" attribute from nearby entities
├── entity/
│   ├── ModEntities.java                 // DeferredRegister<EntityType>
│   ├── EliteFlyEntity.java              // Small hostile flying mob (spawned by Infested buff)
│   └── EliteFlyModel.java              // GeckoLib model for the fly
├── network/
│   ├── XEBNetwork.java                  // SimpleChannel instance, registers all packets
│   ├── MedallionSyncPacket.java          // C->S: client reports medallion visual state
│   └── BuffParticlePacket.java          // S->C: server tells client to spawn particles
└── data/
    ├── loot_tables/                     // Modified loot tables for elite mobs
    └── tags/entity_types/
        ├── bosses.json                  // Tag for boss entities
        ├── common_enemies.json          // Tag for common enemies
        └── elite eligible.json         // Tag for entities that can receive medallions
```

### Resource Pack Structure
```
src/main/resources/
├── META-INF/mods.toml                   // Forge mod descriptor
├── assets/xeb/
│   ├── geo/                             // GeckoLib .geo.json models
│   │   ├── medallion_common.geo.json
│   │   ├── medallion_rare.geo.json
│   │   └── medallion_legendary.geo.json
│   ├── textures/
│   │   ├── entity/
│   │   │   ├── medallion_bronze.png
│   │   │   ├── medallion_silver.png
│   │   │   ├── medallion_gold.png
│   │   │   └── elite_fly.png
│   │   └── effect/                      // Custom effect icons
│   │       ├── holy_shield.png
│   │       ├── petrify.png
│   │       ├── tarred.png
│   │       ├── sandstorm.png
│   │       └── mana_drain.png
│   └── sounds.json
└── data/xeb/
    ├── loot_tables/                     // Bonus loot for elite mobs
    └── tags/entity_types/
        ├── bosses.json
        ├── common_enemies.json
        └── elite_eligible.json
```

---

## 4. ALL 28 ELITE BUFFS — FULL SPECIFICATION

Each buff entry follows this structure:
```
BUFF_ID | Display Name | BuffType | Color (hex) | Mewgenics Source | Minecraft Adaptation | Custom Systems Required | Forge Events Hooked
```

### 4.1 Spiky
- **ID**: `spiky`
- **BuffType**: UNIVERSAL
- **Color**: `#8B4513` (saddlebrown)
- **Enemy Effect**: +2 Thorns (reflects 2 damage to attackers)
- **Boss Effect**: +1 Thorns
- **MC Adaptation**: Listen to `LivingDamageEvent`. If attacker != null and source is from entity attack, deal `thornsAmount` damage back to attacker. Use `DamageSources.mobEvent()` to avoid infinite loops.
- **Custom Systems**: None (uses vanilla damage reflection pattern)
- **Events**: `LivingDamageEvent`

### 4.2 Reactive
- **ID**: `reactive`
- **BuffType**: UNIVERSAL
- **Color**: `#FF6347` (tomato)
- **Effect**: When entity takes damage, emits shockwave dealing 2 damage in 3-block radius
- **MC Adaptation**: On `LivingDamageEvent`, get all `LivingEntity` within 3 blocks via `level.getEntitiesOfClass(LivingEntity.class, AABB)`. Deal 2 damage to each. Spawn `sonic_boom` or custom shockwave particles.
- **Custom Systems**: None
- **Events**: `LivingDamageEvent`

### 4.3 Damaging
- **ID**: `damaging`
- **BuffType**: UNIVERSAL
- **Color**: `#DC143C` (crimson)
- **Enemy Effect**: +2 base attack damage
- **Boss Effect**: +1 base attack damage
- **MC Adaptation**: Apply `AttributeModifier` to `Attributes.ATTACK_DAMAGE` on medallion attach. Remove on detach.
- **Custom Systems**: None
- **Events**: None (attribute modifier is persistent)

### 4.4 Tough
- **ID**: `tough`
- **BuffType**: UNIVERSAL
- **Color**: `#696969` (dimgray)
- **Enemy Effect**: +2 Armor
- **Boss Effect**: +1 Armor
- **MC Adaptation**: Apply `AttributeModifier` to `Attributes.ARMOR` on medallion attach.
- **Custom Systems**: None
- **Events**: None (attribute modifier)

### 4.5 Shielded
- **ID**: `shielded`
- **Color**: `#4169E1` (royalblue)
- **BuffType**: UNIVERSAL
- **Enemy Shield**: 3/5/7 (scales with difficulty: Easy=3, Normal=5, Hard=7, Hardcore=7)
- **Boss Shield**: 4/8/12 (scales with difficulty)
- **Health Penalty**: -20% max health (apply `AttributeModifier` to `Attributes.MAX_HEALTH`)
- **MC Adaptation**: Implement custom shield via `LivingDamageEvent`. Before health is reduced, absorb damage from shield pool. Store shield value in entity's persistent data (`PackedInteger` in NBT). Shield does NOT regenerate during combat.
- **Custom Systems**: Custom shield absorption (NBT-stored shield points)
- **Events**: `LivingDamageEvent` (intercept and absorb)

### 4.6 Protected
- **ID**: `protected`
- **BuffType**: UNIVERSAL
- **Color**: `#FFD700` (gold)
- **Health Penalty**: -20% max health
- **Effect**: At end of each server tick (every 20 ticks = 1 second), if entity's holy shield is 0, set it to 1. Holy shield fully absorbs one hit, then resets to 0.
- **MC Adaptation**: Use `ServerTickEvent` to check if `holyShield` NBT tag is 0, set to 1. In `LivingDamageEvent`, if `holyShield > 0`, cancel damage, set holyShield to 0, spawn golden particle burst.
- **Custom Systems**: Holy Shield mechanic (tick-based regeneration, single-hit absorption)
- **Events**: `ServerTickEvent`, `LivingDamageEvent`

### 4.7 Speedy
- **ID**: `speedy`
- **BuffType**: UNIVERSAL
- **Color**: `#00CED1` (darkturquoise)
- **Effect**: +0.2 movement speed
- **MC Adaptation**: Apply `AttributeModifier` to `Attributes.MOVEMENT_SPEED`.
- **Custom Systems**: None
- **Events**: None

### 4.8 Flaming
- **ID**: `flaming`
- **BuffType**: UNIVERSAL
- **Color**: `#FF4500` (orangered)
- **Effect**: Fire immune. Leaves fire trail. Bosses apply Burn to attackers.
- **MC Adaptation**: Apply `FireResistance` mob effect permanently (refresh on tick). In `ServerTickEvent`, set block below mob to `Blocks.FIRE` if passable. On `LivingDamageEvent` (when mob deals damage), if boss, apply custom `BurnEffect` (fire damage over time) to target.
- **Custom Systems**: BurnEffect (custom DoT similar to vanilla fire but as mob effect)
- **Events**: `ServerTickEvent`, `LivingDamageEvent`

### 4.9 Creepy
- **ID**: `creepy`
- **BuffType**: UNIVERSAL
- **Color**: `#32CD32` (limegreen)
- **Effect**: Melee attacks inflict Poison II for 3 seconds. Leaves creep trail (poisonous ground).
- **MC Adaptation**: On `LivingDamageEvent` (when this mob attacks), apply `MobEffects.POISON` amplifier 1 (Poison II) duration 60 ticks to target. In `ServerTickEvent`, place custom creep block or apply poison area effect at mob position.
- **Custom Systems**: Creep block (custom block that deals poison damage to entities standing on it, similar to Wither Rose)
- **Events**: `LivingDamageEvent`, `ServerTickEvent`

### 4.10 Lucky
- **ID**: `lucky`
- **BuffType**: UNIVERSAL
- **Color**: `#FFD700` (gold)
- **Effect**: +3 Luck attribute. 10% crit chance (deal 2x damage). 10% dodge chance (cancel incoming hit).
- **MC Adaptation**: Apply `AttributeModifier` to `Attributes.LUCK`. For crit: in `LivingDamageEvent` (when attacking), `Random.nextFloat() < 0.10` -> multiply damage by 2. For dodge: in `LivingDamageEvent` (when taking damage), `Random.nextFloat() < 0.10` -> cancel event, spawn dodge particles.
- **Custom Systems**: None (uses random chance + vanilla luck attribute)
- **Events**: `LivingDamageEvent` (both as attacker and as target)

### 4.11 Static
- **ID**: `static`
- **BuffType**: UNIVERSAL
- **Color**: `#FFFF00` (yellow)
- **Effect**: After movement completes, arcs 1 electric damage to adjacent tiles (2-block radius).
- **MC Adaptation**: Track previous position each tick in NBT. If position changed, at end of tick, deal 1 lightning damage to all entities within 2 blocks. Spawn chain lightning particle effect.
- **Custom Systems**: None (uses position tracking + AoE damage)
- **Events**: `ServerTickEvent`

### 4.12 Bouncy
- **ID**: `bouncy`
- **BuffType**: UNIVERSAL
- **Color**: `#FF69B4` (hotpink)
- **Effect**: When hit, both the mob and attacker are knocked back 3 blocks.
- **MC Adaptation**: In `LivingDamageEvent`, get attacker direction vector. Apply knockback of 1.5 to both entities in opposite directions using `entity.push()` or `entity.hurtMarked = true` with deltaMovement.
- **Custom Systems**: None
- **Events**: `LivingDamageEvent`

### 4.13 Mirror
- **ID**: `mirror`
- **BuffType**: ENEMY_ONLY (does NOT appear on bosses)
- **Color**: `#C0C0C0` (silver)
- **Effect**: Reflects ALL projectiles. Takes 2 damage per reflection.
- **MC Adaptation**: Use `ProjectileImpactEvent` or `ProjectileHitEvent`. Check if impacted entity has Mirror buff. If projectile is `AbstractArrow`, `Fireball`, `ThrowableProjectile`, `Snowball`, etc.: reverse projectile direction toward shooter, set new owner. Deal 2 damage to the reflecting mob.
- **Custom Systems**: None (uses projectile event + reflection)
- **Events**: `ProjectileImpactEvent` / `ProjectileHitEvent`

### 4.14 Resonant
- **ID**: `resonant`
- **BuffType**: ENEMY_ONLY
- **Color**: `#9370DB` (mediumpurple)
- **Effect**: When any entity within 8 blocks uses an item with cooldown (or "casts"), this mob gains +1 to all attributes and heals 1 HP.
- **MC Adaptation**: Use `LivingEntityUseItemEvent` or track `ItemCooldowns`. When any entity within 8 blocks finishes using an item: apply +1 `AttributeModifier` to ATTACK_DAMAGE, ARMOR, MAX_HEALTH, MOVEMENT_SPEED (stackable), and heal 1 HP.
- **Custom Systems**: None (uses item use event + attribute stacking)
- **Events**: `LivingEntityUseItemEvent.Finish`

### 4.15 Undying
- **ID**: `undying`
- **BuffType**: ENEMY_ONLY
- **Color**: `#8B0000` (darkred)
- **Effect**: Revives to 50% health 2 seconds after death. One-time only. Minions from undying mobs get no extra corpse health.
- **MC Adaptation**: In `LivingDeathEvent`, if entity has undying buff and `hasRevived` NBT is false: cancel death, set health to maxHealth * 0.5, set `hasRevived = true`, remove undying medallion, spawn dramatic revival particles, play sound. Use scheduled tick delay (40 ticks) for the 2-second revival animation.
- **Custom Systems**: Death cancellation + delayed revival + one-time flag
- **Events**: `LivingDeathEvent`

### 4.16 Healthy
- **ID**: `healthy`
- **BuffType**: ENEMY_ONLY
- **Color**: `#228B22` (forestgreen)
- **Effect**: +3 Health Regen. +4 bonus max health.
- **MC Adaptation**: Apply `MobEffects.REGENERATION` amplifier 2 (Regen III equivalent, ~1 HP/1.6s) permanently. Apply `AttributeModifier` to `Attributes.MAX_HEALTH` for +4 bonus.
- **Custom Systems**: None
- **Events**: None (persistent effect + attribute)

### 4.17 Sandy
- **ID**: `sandy`
- **BuffType**: ENEMY_ONLY
- **Color**: `#F4A460` (sandybrown)
- **Effect**: 10% dodge chance. On death, creates sandstorm (AoE damage aura). If sandstorm exists, increases its damage by 1.
- **MC Adaptation**: Dodge: same as Lucky (10% cancel `LivingDamageEvent`). On death (`LivingDeathEvent`): spawn invisible `AreaEffectCloud` entity at death position with sandstorm particle effect, custom `SandstormEffect` on nearby entities (1 damage/second, 5-block radius). Track if sandstorm already exists via level-level capability; if so, increment damage.
- **Custom Systems**: Sandstorm area effect (custom AoE cloud with stacking damage)
- **Events**: `LivingDamageEvent`, `LivingDeathEvent`

### 4.18 Infested
- **ID**: `infested`
- **BuffType**: ENEMY_ONLY
- **Color**: `#556B2F` (darkolivegreen)
- **Effect**: Spawns 3-5 Elite Fly mobs on death.
- **MC Adaptation**: In `LivingDeathEvent`, spawn 3-5 `EliteFlyEntity` at death position with random offsets. Elite Flies are small (0.3x0.3 hitbox), flying, 2 HP each, deal 1 damage, despawn after 30 seconds.
- **Custom Systems**: Elite Fly entity (custom `Mob` subclass with GeckoLib model)
- **Events**: `LivingDeathEvent`

### 4.19 Absorbent
- **ID**: `absorbent`
- **BuffType**: UNIVERSAL
- **Color**: `#483D8B` (darkslateblue)
- **Effect**: At end of each entity's turn within 6 blocks, drains 1-2 mana and deals equal damage.
- **MC Adaptation**: **NEW SYSTEM**: Add custom `mana` attribute to all `LivingEntity` via Forge attribute system. Mana = saturaton-like resource, max 20, regenerates 1/second. In `ServerTickEvent`, for each entity within 6 blocks that is not moving/acting (end of "turn"), drain 1-2 mana, deal that much magic damage.
- **Custom Systems**: Mana attribute (custom Forge attribute, attached to all LivingEntity)
- **Events**: `ServerTickEvent`

### 4.20 Depressing
- **ID**: `depressing`
- **BuffType**: UNIVERSAL
- **Color**: `#2F4F4F` (darkslategray)
- **Effect**: Nearby enemies (within 8 blocks) have -1 to ALL attributes. Minions from depressing mobs get "Slightly Depressing" instead. Max 2 Depressing per fight.
- **MC Adaptation**: In `ServerTickEvent`, apply -1 `AttributeModifier` to ATTACK_DAMAGE, ARMOR, MAX_HEALTH, MOVEMENT_SPEED, LUCK to all hostile entities within 8 blocks. Track active Depressing count per player (capability). When spawning minions for a depressing mob, replace Depressing with SlightlyDepressing in medallion assignment.
- **Custom Systems**: Per-player fight tracking for max 2 Depressing cap
- **Events**: `ServerTickEvent`

### 4.21 Slightly Depressing
- **ID**: `slightly_depressing`
- **BuffType**: UNIVERSAL
- **Color**: `#708090` (slategray)
- **Effect**: Adjacent entities (within 2 blocks) have -1 to all attributes.
- **MC Adaptation**: Same as Depressing but radius is 2 blocks instead of 8. No cap on count.
- **Custom Systems**: None (uses same attribute modifier system as Depressing)
- **Events**: `ServerTickEvent`

### 4.22 Evolving
- **ID**: `evolving`
- **BuffType**: ENEMY_ONLY
- **Color**: `#7B68EE` (mediumslateblue)
- **Effect**: Every 30 seconds (600 ticks), gains a random Elite Buff. Can stack up to 5 buffs total.
- **MC Adaptation**: Track tick counter in entity NBT. In `ServerTickEvent`, increment counter. When counter >= 600, pick random buff from `EliteBuffRegistry` (excluding ENEMY_ONLY buffs that the entity already has, excluding Evolving itself). Assign via `MedallionManager`. Max 5 medallions total on entity.
- **Custom Systems**: Tick-based buff acquisition + stacking limit
- **Events**: `ServerTickEvent`

### 4.23 Plow
- **ID**: `plow`
- **BuffType**: ENEMY_ONLY
- **Color**: `#D2691E` (chocolate)
- **Effect**: Trample (deals damage to entities it walks through). Moves 1 block toward attacker when damaged.
- **MC Adaptation**: On damage (`LivingDamageEvent`): get direction from attacker to self, move 1 block in that direction. For trample: in `ServerTickEvent`, if mob is moving, deal 1 damage to any entity it collides with (check bounding box overlap with non-allied entities).
- **Custom Systems**: Movement-based collision damage
- **Events**: `LivingDamageEvent`, `ServerTickEvent`

### 4.24 Mega
- **ID**: `mega`
- **BuffType**: ENEMY_ONLY
- **Color**: `#FF1493` (deeppink)
- **Effect**: Takes one less turn per round (attacks faster). +50% health and damage.
- **MC Adaptation**: +50% max health: `AttributeModifier` to MAX_HEALTH (multiply by 1.5). +50% damage: `AttributeModifier` to ATTACK_DAMAGE (multiply by 1.5). Faster attack: `AttributeModifier` to ATTACK_SPEED (multiply by 1.5, simulating "one less turn").
- **Custom Systems**: None (all attribute-based)
- **Events**: None

### 4.25 Mad
- **ID**: `mad`
- **BuffType**: ENEMY_ONLY
- **Color**: `#B22222` (firebrick)
- **Effect**: Gains +1 to all attributes and heals 4 HP on kill. Extra attack per round.
- **MC Adaptation**: On `LivingDeathEvent` (when this mob kills): apply +1 `AttributeModifier` to ATTACK_DAMAGE, ARMOR, MAX_HEALTH, MOVEMENT_SPEED (stackable). Heal 4 HP. Extra attack: `AttributeModifier` to ATTACK_SPEED (multiply by 2.0).
- **Custom Systems**: Kill-tracking + stacking attribute modifiers
- **Events**: `LivingDeathEvent`

### 4.26 Twin
- **ID**: `twin`
- **BuffType**: UNIVERSAL
- **Color**: `#9400D3` (darkviolet)
- **Effect**: Half health. Spawns with an identical twin (same entity type, same buff set).
- **MC Adaptation**: On medallion attach: set entity max health to 50%. Clone entity: spawn same EntityType at position + random offset (2 blocks). Copy all medallions from original to twin via `MedallionManager.copyMedallions()`. Both entities persist independently.
- **Custom Systems**: Entity cloning + medallion copying
- **Events**: `EntityJoinLevelEvent` (during spawn handling)

### 4.27 Hardy
- **ID**: `hardy`
- **BuffType**: UNIVERSAL
- **Color**: `#B8860B` (darkgoldenrod)
- **Effect**: Immune to Stun, Petrify, Freeze (powdered snow effect), and Sleep (custom).
- **MC Adaptation**: In any event that applies these effects, check if entity has Hardy buff. If yes, cancel effect. For vanilla Freeze (powder snow): cancel `LivingEntity.tick()` freeze check. For custom effects: cancel effect application if Hardy is present.
- **Custom Systems**: CC immunity check (intercepts all crowd control effects)
- **Events**: Multiple (effect application events)

### 4.28 Sticky
- **ID**: `sticky`
- **BuffType**: UNIVERSAL
- **Color**: `#2E8B57` (seagreen)
- **Effect**: Any entity that contacts or attacks gets +1 Tarred (20% speed reduction per stack, max 5 stacks).
- **MC Adaptation**: Custom `TarredEffect` (MobEffect). On contact (`ServerTickEvent`, check bounding box overlap) or on attack (`LivingDamageEvent`), apply TarredEffect to the other entity. TarredEffect: each level reduces movement speed by 20%. Max amplifier 4 (5 stacks).
- **Custom Systems**: TarredEffect (custom stackable MobEffect with speed reduction)
- **Events**: `ServerTickEvent`, `LivingDamageEvent`

---

## 5. DIFFICULTY SCALING TABLE

```
Difficulty  | Spawn Chance (Common) | Max Medallions (Common) | Spawn Chance (Boss) | Max Medallions (Boss) | Shield Amount (Enemy) | Shield Amount (Boss)
Easy        | 15%                   | 1                       | 40%                | 1                     | 3                     | 4
Normal      | 25%                   | 2                       | 60%                | 2                     | 5                     | 8
Hard        | 40%                   | 2                       | 80%                | 2                     | 7                     | 12
Hardcore    | 60%                   | 3                       | 95%                | 3                     | 7                     | 16
```

### Buff Weight System (lower weight = rarer)
```
Common buffs   (weight 10): Spiky, Damaging, Speedy, Tough, Hardy, Sticky
Uncommon buffs (weight 5):  Reactive, Flaming, Creepy, Lucky, Static, Bouncy, Protected, Healthy, Twin, Slightly Depressing
Rare buffs     (weight 2):  Shielded, Depressing, Absorbent, Plow
Ultra-rare    (weight 1):  Mirror, Resonant, Undying, Sandy, Infested, Evolving, Mega, Mad
```

---

## 6. CUSTOM SYSTEMS SUMMARY

These are NEW systems that do NOT exist in vanilla Minecraft and must be created:

| System | Purpose | Implementation |
|--------|---------|---------------|
| **Mana Attribute** | Custom resource for Absorbent buff | Forge `RangedAttribute` registered via `RegisterAttributesEvent`, attached to all `LivingEntity`, max 20, regens 1/s |
| **Holy Shield** | Single-hit absorption that regenerates per tick | NBT tag `holyShield` (int), checked in `LivingDamageEvent` to cancel, regenerated in `ServerTickEvent` |
| **Custom Shield Points** | Damage absorption pool (Shielded buff) | NBT tag `xebShield` (int), absorbs damage before health in `LivingDamageEvent` |
| **Tarred Effect** | Stackable movement slow (20%/stack, max 5) | Custom `MobEffect` that modifies `MOVEMENT_SPEED` attribute per amplifier level |
| **Petrify Effect** | Complete immobilization (no move, no attack) | Custom `MobEffect` that sets movement to 0 and prevents goal-oriented AI |
| **Sandstorm Aura** | AoE damage cloud left on death | `AreaEffectCloud` entity with custom particle, damage tick handler |
| **Burn Effect** | Fire DoT applied by Flaming boss on attack | Custom `MobEffect` similar to vanilla fire but as a removable mob effect |
| **Elite Fly Entity** | Small mob spawned by Infested | Custom `Mob` entity, 0.3x0.3 box, 2 HP, flies, 30s lifetime, GeckoLib model |
| **CC Immunity Framework** | Hardy buff intercepts all CC | Event handlers that check for Hardy before applying Petrify/Tarred/Freeze/Sleep |

---

## 7. RENDERING SPECIFICATION

### Medallion (GeckoLib)
- **RenderLayer**: Custom `RenderLayer<LivingEntity>` registered in `RenderLevelStageEvent`
- **Model**: Octagonal medallion .geo.json, 3 tiers (bronze/silver/gold textures)
- **Animation**: `controller_bob.idle` - gentle float up/down 0.1 blocks, slow rotation on Y axis
- **Position**: Above entity head, offset +0.5 blocks Y from eye height
- **Size**: 0.25 blocks wide, scales slightly with entity size

### Mob Color Tinting
- In `RenderLevelStageEvent.AFTER_TRANSLATEABLES`, apply color overlay to entity renderer
- Get all active buff colors from medallion data
- Blend colors additively (RGB addition, then normalize to 0-1 range)
- Apply as overlay with ~30% opacity so base texture shows through

### Glowing Eyes
- GeckoLib `IAnimatable` bone glow rendering
- Target bone: `head` or `eye_L` / `eye_R` in entity model
- Emissive texture layer with buff's primary color at full brightness
- Pulses gently (alpha oscillation 0.7-1.0 over 2 seconds)

### Particles per Buff
```
Buff      | Particle Type              | Spawn Rate
Spiky     | DamageIndicator (outward)   | On damage received
Reactive  | sonic_boom (expanding ring)  | On damage received
Flaming   | flame (at feet)             | Every 3 ticks
Creepy    | cloud (green, at feet)       | Every 5 ticks
Static    | electric_spark (chain)       | After movement
Lucky     | enchantment_table (sparkle)  | Every 10 ticks
Undying   | explosion (large)            | On revival
Sandy     | sand (swirl)                 | On death, continues 10s
Evolving  | end_rod (spiral)             | On new buff gain
Mega      | dragon_breath (purple)       | Every 8 ticks
Mad       | anger (red particles)        | On kill
```

---

## 8. CROSS-MOD COMPATIBILITY HOOKS

`ModCompatManager` checks `ModList.get().isLoaded()` and registers entity type hooks:

```java
// Pseudocode
if (isLoaded("twilightforest"))  registerHook(new TwilightForestHook());
if (isLoaded("mowziesmobs"))      registerHook(new MowziesMobsHook());
if (isLoaded("alexsmobs"))        registerHook(new AlexMobsHook());
if (isLoaded("mutantmonsters"))   registerHook(new MutantMonstersHook());
if (isLoaded("cataclysm"))        registerHook(new CataclysmHook());
if (isLoaded("epicfight"))        registerHook(new EpicFightHook());
```

Each hook registers mod-specific entity types into the `#xeb:elite_eligible` tag at runtime via `TagManager`.

**Config options**:
- `compat.enableTwilightForest` (default: true)
- `compat.enableAlexMobs` (default: true)
- `compat.enableMowziesMobs` (default: true)
- `compat.enableMutantMonsters` (default: true)
- `compat.enableCataclysm` (default: true)
- `compat.blacklistEntities` (default: []) - entity registry names to exclude
- `compat.whitelistOnly` (default: false) - if true, only whitelisted entities get medallions

---

## 9. NETWORK PACKETS

```java
// SimpleChannel registration
public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
    ResourceLocation.fromNamespaceAndPath("xeb", "main"),
    () -> PROTOCOL_VERSION,
    PROTOCOL_VERSION::equals,
    PROTOCOL_VERSION::equals
);

// Packets
CHANNEL.registerMessage(0, MedallionSyncPacket.class, MedallionSyncPacket::encode, MedallionSyncPacket::decode, MedallionSyncPacket::handle);
CHANNEL.registerMessage(1, BuffParticlePacket.class, BuffParticlePacket::encode, BuffParticlePacket::decode, BuffParticlePacket::handle);
```

---

## 10. FORGE CONFIG STRUCTURE

```java
// CommonConfig - via ForgeConfigSpec
public class XEBConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue COMMON_SPAWN_CHANCE_EASY;
    public static final ForgeConfigSpec.DoubleValue COMMON_SPAWN_CHANCE_NORMAL;
    public static final ForgeConfigSpec.DoubleValue COMMON_SPAWN_CHANCE_HARD;
    public static final ForgeConfigSpec.DoubleValue COMMON_SPAWN_CHANCE_HARDCORE;
    public static final ForgeConfigSpec.IntValue MAX_MEDALLIONS_COMMON;
    public static final ForgeConfigSpec.IntValue MAX_MEDALLIONS_BOSS;
    public static final ForgeConfigSpec.BooleanValue MEDALLION_RENDER_ENABLED;
    public static final ForgeConfigSpec.DoubleValue MEDALLION_SIZE_SCALE;
    public static final ForgeConfigSpec.BooleanValue COLOR_OVERLAY_ENABLED;
    public static final ForgeConfigSpec.BooleanValue GLOW_EYES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("xEB (xd Elite Buffs) Configuration");

        ENABLED = builder.define("enabled", true);
        COMMON_SPAWN_CHANCE_EASY = builder.comment("Spawn chance for common mobs on Easy").defineInRange("commonSpawnChance.easy", 0.15, 0.0, 1.0);
        COMMON_SPAWN_CHANCE_NORMAL = builder.defineInRange("commonSpawnChance.normal", 0.25, 0.0, 1.0);
        COMMON_SPAWN_CHANCE_HARD = builder.defineInRange("commonSpawnChance.hard", 0.40, 0.0, 1.0);
        COMMON_SPAWN_CHANCE_HARDCORE = builder.defineInRange("commonSpawnChance.hardcore", 0.60, 0.0, 1.0);
        MAX_MEDALLIONS_COMMON = builder.defineInRange("maxMedallions.common", 3, 1, 5);
        MAX_MEDALLIONS_BOSS = builder.defineInRange("maxMedallions.boss", 3, 1, 5);
        MEDALLION_RENDER_ENABLED = builder.define("render.medallionEnabled", true);
        MEDALLION_SIZE_SCALE = builder.defineInRange("render.medallionSizeScale", 1.0, 0.5, 2.0);
        COLOR_OVERLAY_ENABLED = builder.define("render.colorOverlayEnabled", true);
        GLOW_EYES_ENABLED = builder.define("render.glowEyesEnabled", true);

        SPEC = builder.build();
    }
}
```

---

# 11. INCREMENTAL ANTIGRAVITY PROMPTS

> Copy each prompt into Antigravity sequentially. Each prompt is self-contained and assumes previous prompts have been completed.

---

## PROMPT 1: Project Setup and Skeleton

```
You are building a Minecraft Forge 1.20.1 mod called "xEB" (xd Elite Buffs), mod ID: "xeb".

TASK: Create the complete project skeleton with the following files. Use Forge MDK 1.20.1-47.2.0 with GeckoLib 4.2.4 as a dependency.

REQUIRED FILES:
1. build.gradle - Include Forge, GeckoLib dependency (software.bernie.geckolib:geckolib-forge-1.20.1:4.2.4), mod ID "xeb", version "1.0.0"
2. src/main/resources/META-INF/mods.toml - Mod descriptor with dependencies on forge and geckolib
3. src/main/java/com/xeb/XEBMain.java - @Mod("xeb") entry point that:
   - Registers DeferredRegisters for Items, Blocks, EntityTypes, MobEffects
   - Has @SubscribeEvent for RegisterAttributesEvent (registers custom "mana" attribute, RangedAttribute, 0-20 range)
   - Has @SubscribeEvent for RegisterCommandsEvent (registers /xeb_admin command)
   - Initializes XEBConfig
   - Logs "xEB (xd Elite Buffs) loaded!" on mod construction
4. src/main/java/com/xeb/config/XEBConfig.java - Forge ForgeConfigSpec with CommonConfig
5. src/main/java/com/xeb/network/XEBNetwork.java - SimpleChannel setup with protocol version "1"

BASE PACKAGE: com.xeb
MC VERSION: 1.20.1
FORGE VERSION: 47.2.0
JAVA VERSION: 17

Generate ALL files with complete, compilable Java code. Do not use placeholders.
```

---

## PROMPT 2: Core Buff System

```
Continuing the xEB (xd Elite Buffs) Minecraft Forge 1.20.1 mod, mod ID: "xeb", base package: com.xeb.

TASK: Create the core buff system infrastructure.

REQUIRED FILES:
1. src/main/java/com/xeb/buff/BuffType.java - Enum with values: ENEMY_ONLY, BOSS_ONLY, UNIVERSAL
2. src/main/java/com/xeb/buff/EliteBuff.java - Abstract base class with:
   - Fields: String id, String displayName, BuffType buffType, int color (hex int), double weight
   - Abstract methods: void onAttach(LivingEntity entity), void onDetach(LivingEntity entity), void onServerTick(LivingEntity entity, ServerLevel level)
   - Default implementations for event handlers (empty, override in subclasses)
3. src/main/java/com/xeb/buff/EliteBuffRegistry.java - Singleton registry:
   - Map<String, EliteBuff> registry
   - void register(EliteBuff buff)
   - EliteBuff getById(String id)
   - List<EliteBuff> getByType(BuffType type)
   - EliteBuff getRandomByWeight(RandomSource random, boolean isBoss)
4. src/main/java/com/xeb/medallion/MedallionType.java - Enum: COMMON, RARE, LEGENDARY with color and weight fields
5. src/main/java/com/xeb/medallion/MedallionData.java - Data class storing: EliteBuff buff, MedallionType tier, UUID uniqueId
6. src/main/java/com/xeb/medallion/MedallionManager.java - Core manager class:
   - Static methods to attach/detach/query medallions on entities
   - Uses entity's persistent data (PackedInteger in capabilities or NBT)
   - getMaxMedallions(boolean isBoss, Difficulty difficulty) - returns max based on config
   - getSpawnChance(boolean isBoss, Difficulty difficulty) - returns probability
   - assignRandomMedallions(LivingEntity entity, ServerLevel level) - main spawn logic:
     a) Check spawn chance via random
     b) If passes, determine count (1 to max)
     c) For each, roll buff from EliteBuffRegistry.getRandomByWeight()
     d) Filter by BuffType (ENEMY_ONLY only on non-bosses, BOSS_ONLY only on bosses)
     e) Apply attribute modifiers from buff's onAttach()
     f) Store MedallionData in entity NBT
   - removeAllMedallions(LivingEntity entity) - cleanup on death/respawn
   - getMedallions(LivingEntity entity) - returns List<MedallionData>
   - hasBuff(LivingEntity entity, String buffId) - check if entity has specific buff
   - isBoss(LivingEntity entity) - checks entity type tags #xeb:bosses or health > 100

Generate ALL files with complete, compilable Java code. Use Forge 1.20.1 APIs (DeferredRegister, capabilities, NBT). No placeholders.
```

---

## PROMPT 3: Spawn Event Handling and Difficulty

```
Continuing the xEB mod (com.xeb, Forge 1.20.1, mod ID "xeb").

TASK: Create spawn event handlers and the remaining config/buff infrastructure.

REQUIRED FILES:
1. src/main/java/com/xeb/event/MedallionSpawnHandler.java:
   - @SubscribeEvent on EntityJoinLevelEvent (ServerSide only)
   - Check if entity is LivingEntity
   - Check if entity is in #xeb:elite_eligible tag
   - Check if entity is not a player
   - Check XEBConfig.ENABLED
   - Call MedallionManager.assignRandomMedallions()
   - Sync medallion data to tracking capability

2. src/main/java/com/xeb/config/BuffConfig.java:
   - ForgeConfigSpec for per-buff configuration
   - Boolean toggle per buff (28 entries)
   - Double weight multiplier per buff (default 1.0, range 0.0-10.0)
   - register() method called from XEBMain

3. src/main/java/com/xeb/buff/impl/SpikyBuff.java - First buff implementation:
   - Extends EliteBuff
   - ID: "spiky", displayName: "Spiky", BuffType: UNIVERSAL, color: 0x8B4513, weight: 10
   - onAttach(): Apply AttributeModifier(ATTACK_KNOCKBACK) if desired, store thornsAmount in NBT (2 for enemy, 1 for boss)
   - Override event hook for LivingDamageEvent: when this entity TAKES damage, deal thornsAmount back to attacker

4. src/main/java/com/xeb/buff/impl/DamagingBuff.java:
   - ID: "damaging", UNIVERSAL, color: 0xDC143C, weight: 10
   - onAttach(): Apply AttributeModifier to ATTACK_DAMAGE (+2 enemy, +1 boss via MedallionData)

5. src/main/java/com/xeb/buff/impl/ToughBuff.java:
   - ID: "tough", UNIVERSAL, color: 0x696969, weight: 10
   - onAttach(): Apply AttributeModifier to ARMOR (+2 enemy, +1 boss)

6. src/main/java/com/xeb/buff/impl/SpeedyBuff.java:
   - ID: "speedy", UNIVERSAL, color: 0x00CED1, weight: 10
   - onAttach(): Apply AttributeModifier to MOVEMENT_SPEED (+0.2)

7. src/main/java/com/xeb/buff/impl/HardyBuff.java:
   - ID: "hardy", UNIVERSAL, color: 0xB8860B, weight: 10
   - onAttach(): Store flag in NBT. CC immunity is checked in effect application events.

8. src/main/java/com/xeb/buff/impl/StickyBuff.java:
   - ID: "sticky", UNIVERSAL, color: 0x2E8B57, weight: 10
   - onServerTick(): Check for nearby entities within contact range, apply TarredEffect

Generate ALL files with complete, compilable Java code. Use proper Forge event bus registration.
```

---

## PROMPT 4: Custom Effects System

```
Continuing the xEB mod (com.xeb, Forge 1.20.1, mod ID "xeb").

TASK: Create all custom MobEffects and their implementations.

REQUIRED FILES:
1. src/main/java/com/xeb/effect/ModEffects.java:
   - DeferredRegister<MobEffect> for all custom effects
   - Register: HOLY_SHIELD, PETRIFY, TARRED, SANDSTORM_AURA, MANA_DRAIN, BURN

2. src/main/java/com/xeb/effect/HolyShieldEffect.java:
   - Extends MobEffect, category BENEFICIAL, color 0xFFD700
   - No effect per tick (regeneration handled in ProtectedBuff's tick logic)
   - Is removable: true
   - This is applied visually; actual absorption is handled in LivingDamageEvent

3. src/main/java/com/xeb/effect/PetrifyEffect.java:
   - Extends MobEffect, category HARMFUL, color 0x808080
   - applyEffectTick(): Set entity movement to zero (deltaMovement = Vec3.ZERO), set noAI flag
   - isDurationEffectTick(): true (tick every 10 ticks)
   - HardyImmunity check: if entity has Hardy medallion, remove effect immediately

4. src/main/java/com/xeb/effect/TarredEffect.java:
   - Extends MobEffect, category HARMFUL, color 0x2E8B57
   - Stackable (amplifier 0-4 = 1-5 stacks)
   - addAttributeModifier(): Add MOVEMENT_SPEED modifier of -0.2 per amplifier level
   - Each level adds additional -20% movement speed

5. src/main/java/com/xeb/effect/SandstormEffect.java:
   - Extends MobEffect, category HARMFUL, color 0xF4A460
   - applyEffectTick(): Deal 1 magic damage per tick to the affected entity, spawn sand particles

6. src/main/java/com/xeb/effect/ManaDrainEffect.java:
   - Extends MobEffect, category HARMFUL, color 0x483D8B
   - applyEffectTick(): Reduce entity's custom "mana" attribute by 1 per tick

7. src/main/java/com/xeb/effect/BurnEffect.java:
   - Extends MobEffect, category HARMFUL, color 0xFF4500
   - Similar to vanilla Wither effect but fire-themed
   - applyEffectTick(): Deal 1 fire damage per tick (every 20 ticks)

8. Update XEBMain.java to register ModEffects deferred register in the constructor.

Generate ALL files with complete, compilable Java code. Use Forge 1.20.1 MobEffect system.
```

---

## PROMPT 5: Buff Implementations Part 1 (Buffs 1-14)

```
Continuing the xEB mod (com.xeb, Forge 1.20.1, mod ID "xeb").

TASK: Implement 14 more elite buffs. Each buff is a class in com.xeb.buff.impl extending EliteBuff.

Buff specifications (create ONE file per buff):

1. ReactiveBuff.java - ID: "reactive", UNIVERSAL, color: 0xFF6347, weight: 5
   On LivingDamageEvent (when this entity takes damage):
   Get all LivingEntity within 3 blocks, deal 2 magic damage to each, spawn sonic_boom particle at entity position.

2. ShieldedBuff.java - ID: "shielded", UNIVERSAL, color: 0x4169E1, weight: 2
   onAttach(): Set NBT "xebShield" = difficulty-scaled amount (3/5/7 for enemies, 4/8/12 for bosses). Apply -20% MAX_HEALTH modifier.
   On LivingDamageEvent: Before reducing health, absorb damage from xebShield. If shield reaches 0, remaining damage hits health.

3. ProtectedBuff.java - ID: "protected", UNIVERSAL, color: 0xFFD700, weight: 5
   onAttach(): Apply -20% MAX_HEALTH. Initialize NBT "xebHolyShield" = 1.
   onServerTick(): If xebHolyShield == 0, set to 1 (regenerates every second).
   On LivingDamageEvent: If xebHolyShield > 0, cancel damage, set xebHolyShield = 0, spawn totem_of_undying particle burst.

4. FlamingBuff.java - ID: "flaming", UNIVERSAL, color: 0xFF4500, weight: 5
   onAttach(): Apply FireResistance permanently (reapply every tick).
   onServerTick(): Set block below entity to Fire (if block is air/grass).
   On LivingDamageEvent (as attacker): If boss, apply BurnEffect to target for 3 seconds.

5. CreepyBuff.java - ID: "creepy", UNIVERSAL, color: 0x32CD32, weight: 5
   On LivingDamageEvent (as attacker): Apply MobEffects.POISON (amplifier 1, duration 60 ticks) to target.
   onServerTick(): Create poison area at mob's feet (similar to wither rose effect).

6. LuckyBuff.java - ID: "lucky", UNIVERSAL, color: 0xFFD700, weight: 5
   onAttach(): Apply +3 LUCK attribute modifier.
   On LivingDamageEvent (attacking): 10% chance -> multiply damage by 2, spawn critical hit particles.
   On LivingDamageEvent (defending): 10% chance -> cancel damage, spawn dodge particles.

7. StaticBuff.java - ID: "static", UNIVERSAL, color: 0xFFFF00, weight: 5
   Track previous position in NBT. onServerTick(): If position changed, deal 1 lightning damage to all entities within 2 blocks. Spawn chain lightning particle.

8. BouncyBuff.java - ID: "bouncy", UNIVERSAL, color: 0xFF69B4, weight: 5
   On LivingDamageEvent: Apply 1.5 knockback to both this entity and attacker in opposite directions.

9. MirrorBuff.java - ID: "mirror", ENEMY_ONLY, color: 0xC0C0C0, weight: 1
   On ProjectileHitEvent/ProjectileImpactEvent: If projectile (arrow, fireball, snowball, throwable) hits this entity, reverse projectile direction toward source, deal 2 damage to self.

10. ResonantBuff.java - ID: "resonant", ENEMY_ONLY, color: 0x9370DB, weight: 1
    Listen to LivingEntityUseItemEvent.Finish within 8 blocks. On trigger: add +1 AttributeModifier to ATTACK_DAMAGE, ARMOR, MAX_HEALTH, MOVEMENT_SPEED (stacking). Heal 1 HP.

11. UndyingBuff.java - ID: "undying", ENEMY_ONLY, color: 0x8B0000, weight: 1
    On LivingDeathEvent: If NBT "xebRevived" is false, cancel death, set health to maxHealth*0.5, set xebRevived=true, remove undying medallion, spawn huge particle explosion, schedule removal of this buff after 40 ticks.

12. HealthyBuff.java - ID: "healthy", ENEMY_ONLY, color: 0x228B22, weight: 5
    onAttach(): Apply MobEffects.REGENERATION (amplifier 2, infinite duration, refresh each tick). Apply +4 MAX_HEALTH modifier.

13. SandyBuff.java - ID: "sandy", ENEMY_ONLY, color: 0xF4A460, weight: 1
    10% dodge in LivingDamageEvent (cancel + particles).
    On LivingDeathEvent: Spawn AreaEffectCloud with SandstormEffect, 5 block radius, 20 second duration. If sandstorm already tracked in level capability, increase damage by 1 instead.

14. InfestedBuff.java - ID: "infested", ENEMY_ONLY, color: 0x556B2F, weight: 1
    On LivingDeathEvent: Spawn 3-5 EliteFlyEntity at death position with random offsets. EliteFlies are registered separately.

Generate ALL 14 files with complete, compilable Java code. Each class must register itself in a static block or via a helper.
```

---

## PROMPT 6: Buff Implementations Part 2 (Buffs 15-28)

```
Continuing the xEB mod (com.xeb, Forge 1.20.1, mod ID "xeb").

TASK: Implement the remaining 14 elite buffs.

1. AbsorbentBuff.java - ID: "absorbent", UNIVERSAL, color: 0x483D8B, weight: 2
   onServerTick(): For each LivingEntity within 6 blocks, drain 1-2 mana attribute, deal equal magic damage. Use entity.getAttribute(xeb:mana) for the custom attribute.

2. DepressingBuff.java - ID: "depressing", UNIVERSAL, color: 0x2F4F4F, weight: 2
   onServerTick(): For all non-allied LivingEntity within 8 blocks, apply -1 AttributeModifiers to ATTACK_DAMAGE, ARMOR, MAX_HEALTH, MOVEMENT_SPEED, LUCK. Track count per player via capability, max 2 Depressing entities per player's active fight.

3. SlightlyDepressingBuff.java - ID: "slightly_depressing", UNIVERSAL, color: 0x708090, weight: 5
   Same as Depressing but radius is 2 blocks. No count limit.

4. EvolvingBuff.java - ID: "evolving", ENEMY_ONLY, color: 0x7B68EE, weight: 1
   onServerTick(): Increment tick counter NBT. When counter >= 600 ticks, pick random buff from EliteBuffRegistry (exclude Evolving, exclude ENEMY_ONLY that entity already has), call MedallionManager to add new medallion. Cap at 5 total medallions.

5. PlowBuff.java - ID: "plow", ENEMY_ONLY, color: 0xD2691E, weight: 2
   On LivingDamageEvent: Calculate direction from attacker to self, move 1 block in that direction.
   onServerTick(): If moving, deal 1 damage to non-allied entities with overlapping bounding boxes.

6. MegaBuff.java - ID: "mega", ENEMY_ONLY, color: 0xFF1493, weight: 1
   onAttach(): Apply 1.5x multiplier AttributeModifier to MAX_HEALTH and ATTACK_DAMAGE. Apply 1.5x ATTACK_SPEED.

7. MadBuff.java - ID: "mad", ENEMY_ONLY, color: 0xB22222, weight: 1
   On LivingDeathEvent (when THIS mob gets a kill): Add +1 stacking AttributeModifier to all combat attributes. Heal 4 HP.
   onAttach(): Apply 2x ATTACK_SPEED modifier.

8. TwinBuff.java - ID: "twin", UNIVERSAL, color: 0x9400D3, weight: 1
   onAttach(): Set entity health to 50% of max. Spawn an identical entity (same EntityType) 2 blocks away. Copy all medallions from this entity to twin via MedallionManager.copyMedallions().

9. Also create src/main/java/com/xeb/event/BuffTickHandler.java:
   - @SubscribeEvent on ServerTickEvent
   - Iterate all loaded LivingEntities with medallions
   - Call each buff's onServerTick()

10. Also create src/main/java/com/xeb/event/BuffDamageHandler.java:
    - @SubscribeEvent on LivingDamageEvent
    - Route to all buffs on the entity that handle damage events
    - Handle shield absorption, holy shield, thorns, lucky dodge, etc.

11. Also create src/main/java/com/xeb/event/BuffDeathHandler.java:
    - @SubscribeEvent on LivingDeathEvent
    - Route to all buffs that handle death (Undying, Infested, Sandy)

12. Register all 28 buffs in EliteBuffRegistry in XEBMain init.

Generate ALL files with complete, compilable Java code.
```

---

## PROMPT 7: GeckoLib Rendering System

```
Continuing the xEB mod (com.xeb, Forge 1.20.1, mod ID "xeb").

TASK: Create the complete GeckoLib rendering system for medallions and visual effects.

REQUIRED FILES:
1. src/main/java/com/xeb/render/MedallionRenderLayer.java:
   - Implements RenderLayer<LivingEntity> or uses GeckoLib's GeoLayerRenderer
   - Registered in FMLClientSetupEvent via RenderLevelStageEvent.AFTER_TRANSLATEABLES
   - For each entity with medallions:
     a) Get entity's MedallionData list
     b) Position above head: entity eyeHeight + 0.5 blocks
     c) Render medallion model based on highest tier (COMMON < RARE < LEGENDARY)
     d) Apply rotation animation (Y-axis spin, 1 rotation per 4 seconds)
     e) Apply bob animation (sine wave, 0.1 block amplitude, 1 cycle per 2 seconds)
     f) Tint medallion with primary buff's color

2. src/main/java/com/xeb/render/MobColorOverlay.java:
   - Registered in RenderLevelStageEvent.AFTER_TRANSLATEABLES
   - For entities with medallions:
     a) Collect all buff colors
     b) Blend additively: R = min(1, sum of Rs), G = min(1, sum of Gs), B = min(1, sum of Bs)
     c) Render a semi-transparent colored overlay on the entity model (30% alpha)
   - Config: XEBConfig.COLOR_OVERLAY_ENABLED

3. src/main/java/com/xeb/render/GlowEyeOverlay.java:
   - Uses GeckoLib's emissive rendering for entity eyes
   - Target the "head" bone or eye bones in entity models
   - Apply primary buff color as emissive texture at full brightness
   - Pulse alpha: 0.7 to 1.0 over 2 seconds (sine wave)
   - Config: XEBConfig.GLOW_EYES_ENABLED

4. src/main/java/com/xeb/render/BuffParticleHandler.java:
   - Server-side particle spawning logic
   - Map of buff IDs to particle types and spawn conditions
   - Called from buff tick handlers
   - Uses ServerLevel.sendParticles() for vanilla particles
   - Custom particles registered via ParticleProvider

5. GeckoLib model files (create placeholder .geo.json):
   - src/main/resources/assets/xeb/geo/medallion.geo.json
   - Simple octagonal coin geometry (8-sided disc)
   - Single bone: "medallion"
   - UV mapped for tier texture

6. Texture files (create as placeholder paths):
   - assets/xeb/textures/entity/medallion_bronze.png
   - assets/xeb/textures/entity/medallion_silver.png
   - assets/xeb/textures/entity/medallion_gold.png

7. Register client setup in XEBMain or separate XEBClient.java:
   - Register render layers
   - Register entity renderers for EliteFlyEntity
   - Register particle factories

Generate ALL files. For .geo.json, create a minimal valid GeckoLib model file.
```

---

## PROMPT 8: Cross-Mod Compatibility + Elite Fly Entity + Final Integration

```
Continuing the xEB mod (com.xeb, Forge 1.20.1, mod ID "xeb").

TASK: Complete cross-mod compatibility, Elite Fly entity, and final integration.

REQUIRED FILES:
1. src/main/java/com/xeb/compat/ModCompatManager.java:
   - Singleton class, initialized in FMLCommonSetupEvent
   - void scanMods(): Check ModList.get().isLoaded() for each supported mod
   - Register entity type hooks into #xeb:elite_eligible runtime tag
   - Supported mods: twilightforest, mowziesmobs, alexsmobs, mutantmonsters, cataclysm
   - Fallback: vanilla mobs always registered via data tag

2. src/main/java/com/xeb/compat/hooks/VanillaMobHook.java:
   - Registers vanilla entity types: Zombie, Skeleton, Creeper, Spider, Enderman, Witch, Blaze, Guardian, etc.
   - Adds to #xeb:elite_eligible tag
   - Registers #xeb:bosses tag: Wither, Elder Guardian, Ender Dragon, Evoker, Warden

3. src/main/java/com/xeb/compat/hooks/TwilightForestHook.java:
   - Registers Twilight Forest entity types if mod is loaded
   - Hydras, Naga, Minoshroom, Ur-Ghast, Alpha Yeti, etc.

4. src/main/java/com/xeb/compat/hooks/AlexMobsHook.java:
   - Registers Alex's Mobs entity types
   - Great White Shark, Tarantula Hawk, Cachalot Whale, etc.

5. src/main/java/com/xeb/compat/hooks/MowziesMobsHook.java:
   - Registers Mowzie's Mobs entity types
   - Ferrous Wroughtnaut, Frostmaw, Barakoa, etc.

6. src/main/java/com/xeb/entity/ModEntities.java:
   - DeferredRegister<EntityType> for EliteFlyEntity
   - Register with EntityType.Builder of Mob.class, 0.3x0.3 spawn bounding box

7. src/main/java/com/xeb/entity/EliteFlyEntity.java:
   - Extends Mob, implements IAnimatable (GeckoLib)
   - AI Goals: MeleeAttackGoal (1 damage), FlyGoal (random flying), WaterAvoidingRandomFlyingGoal
   - Health: 2 hearts (4 HP)
   - Lifetime: 600 ticks (30 seconds), then despawns
   - Spawns when InfestedBuff triggers on death
   - Drops nothing, no XP

8. src/main/java/com/xeb/entity/EliteFlyModel.java:
   - GeckoLib GeoModel for EliteFlyEntity
   - Simple flying insect model with wings animation

9. Data tags (create JSON files):
   - data/xeb/tags/entity_types/bosses.json - Vanilla boss entity types
   - data/xeb/tags/entity_types/common_enemies.json - Vanilla common enemy types
   - data/xeb/tags/entity_types/elite_eligible.json - All eligible entity types

10. Final integration:
    - Ensure all event handlers are registered in XEBMain
    - Ensure all DeferredRegisters are added to the mod constructor
    - Ensure network packets are registered
    - Add @Mod.EventBusSubscriber for all event handler classes
    - Verify all 28 buffs are registered in EliteBuffRegistry

Generate ALL files with complete, compilable Java/JSON code. This is the final prompt - the mod should be fully compilable after this.
```

---

## 12. DEVELOPMENT ROADMAP

```
Phase | Tasks                                    | Estimated Time
------|------------------------------------------|---------------
  1   | Project skeleton, Forge setup, GeckoLib  | 3-4 days
  2   | Core buff system, MedallionManager       | 4-5 days
  3   | Custom effects (HolyShield, Tarred, etc.) | 2-3 days
  4   | Buff implementations 1-14                | 5-6 days
  5   | Buff implementations 15-28 + events      | 5-6 days
  6   | GeckoLib rendering system                 | 4-5 days
  7   | Elite Fly entity + cross-mod compat       | 3-4 days
  8   | Config, balance, testing, polish          | 4-5 days
------|------------------------------------------|---------------
TOTAL |                                          | ~5-6 weeks
```

---

## 13. TESTING CHECKLIST

- [ ] Mod loads without errors in dev environment
- [ ] Common mobs spawn with medallions on all difficulties
- [ ] Boss mobs spawn with medallions on all difficulties
- [ ] Each of 28 buffs applies correct effect
- [ ] Shielded buff absorbs damage before health
- [ ] Protected buff regenerates holy shield
- [ ] Undying buff revives exactly once
- [ ] Evolving buff gains new buffs over time
- [ ] Twin buff spawns a clone with matching medallions
- [ ] Mirror buff reflects projectiles
- [ ] Medallion renders above mob head with correct tier texture
- [ ] Mob color tinting works with multiple buffs
- [ ] Glowing eyes render correctly
- [ ] Particles spawn for each buff type
- [ ] Config options work (disable individual buffs, adjust spawn rates)
- [ ] Cross-mod entities receive medallions (test each supported mod)
- [ ] No crash when mod is removed and world is loaded
- [ ] Performance is acceptable (no lag from tick handlers)
