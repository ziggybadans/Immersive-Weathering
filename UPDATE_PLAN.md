# Immersive Weathering: 1.20.1 → 1.21.1 Update Plan

## Context
Updating this multiloader Minecraft mod from 1.20.1 to 1.21.1. Forge is being replaced with NeoForge (Forge doesn't support 1.21+). The mod has 243 Java files, ~2000 JSON resources, and depends heavily on the Moonlight library. User is on Windows/IDEA, assistant is on WSL2.

---

## Phase 1: Build Infrastructure

**Goal**: Get Gradle to resolve all dependencies (code won't compile yet).

1. **Create branch** `1.21.1-multiloader` from current `1.20.0-multiloader`
2. **Rename `forge/` → `neoforge/`** (directory + all references)
3. **Update `settings.gradle`**: `include("neoforge")`, add NeoForge maven repo
4. **Update `gradle.properties`**:
   - `minecraft_version = 1.21.1`, `mod_version = 1.21.1-2.1.0`
   - All dependency versions (Fabric API, NeoForge, Moonlight, EMI, Cloth Config, ModMenu, Parchment)
   - `enabled_platforms = fabric,neoforge`
   - Remove `forge_version`, add `neoforge_version`
5. **Update root `build.gradle`**:
   - Architectury Loom → `1.7-SNAPSHOT` (needed for NeoForge 1.21)
   - Java target: `17` → `21`
6. **Update `neoforge/gradle.properties`**: `loom.platform=neoforge`
7. **Rewrite `neoforge/build.gradle`**: `forge()` → `neoForge()`, NeoForge dependency syntax, `developmentNeoForge`, etc.
8. **Update `common/build.gradle`** and **`fabric/build.gradle`**: Moonlight/EMI/Cloth Config dependency IDs for 1.21.1

**User action**: Run `./gradlew.bat --refresh-dependencies` in IDEA to verify dependency resolution.

---

## Phase 2: NeoForge Module Conversion

**Goal**: Convert all Forge-specific code to NeoForge APIs.

1. **Rename Java packages**: `forge` → `neoforge` in all neoforge module source files
2. **Rewrite `ImmersiveWeatheringForge.java`** → `ImmersiveWeatheringNeoForge.java`:
   - `net.minecraftforge.*` → `net.neoforged.*`
   - NeoForge 1.21 uses constructor-injected `IEventBus` (no more `FMLJavaModLoadingContext.get()`)
3. **Update NeoForge mixins**:
   - `SelfRustableMixin`: `ToolAction`/`ToolActions` → `ItemAbility`/`ItemAbilities`; `IForgeBlock` → `IBlockExtension`
   - `FluidInteractionRegistryMixin`: update imports
   - Color accessors: verify targets
4. **Rename metadata**: `META-INF/mods.toml` → `META-INF/neoforge.mods.toml`, update loader version ranges, `forge` dep → `neoforge` dep, MC version → `[1.21.1]`
5. **Update mixin config**: `compatibilityLevel` → `JAVA_21`, update package references
6. **Move biome modifiers**: `data/.../forge/biome_modifier/` → `data/.../neoforge/biome_modifier/`, change `"type": "forge:add_features"` → `"neoforge:add_features"`
7. **Convert Forge tags**: `data/forge/tags/` → `data/c/tags/` (common namespace)

---

## Phase 3: Common Java Code Updates

**Goal**: Fix all compilation errors in `common/src`. Largest phase.

1. **ResourceLocation migration** (~93 occurrences across many files):
   - `new ResourceLocation(ns, path)` → `ResourceLocation.fromNamespaceAndPath(ns, path)`
   - `new ResourceLocation(string)` → `ResourceLocation.parse(string)`
2. **Mixin updates** (26 files — highest risk):
   - `ServerLevelMixin.tickChunk`: Local capture variables almost certainly reordered. Must re-map against 1.21.1 decompiled source. **HIGH RISK**.
   - `BlocksMixin`: ~20 `@Redirect` targets in `Blocks.<clinit>`. Constructor signatures likely changed. **HIGH RISK**.
   - All other mixins: verify target method signatures still exist
3. **`BonemealableBlock` interface** (11 files): `isValidBonemealTarget` lost its `boolean isClient` parameter
4. **Access widener updates** (`immersive_weathering.accesswidener`): Verify all 4 entries still exist with same signatures
5. **Mixin configs**: `compatibilityLevel` → `JAVA_21` in all 3 mixin JSON files
6. **Other API changes**: `BlockBehaviour.Properties` methods, `ItemStack` NBT → components (if any item NBT is used), Feature/RuleTest codecs, etc.

---

## Phase 4: Fabric Module Updates

1. **Update `fabric.mod.json`**: MC dep → `1.21.1`, Moonlight version range
2. **Update Fabric mixins**: Verify `ArmorLayerMixin` (rendering changes), other Fabric-specific mixins
3. **Update `IWPlatformStuffImpl.java`**: Verify `BiomeModifications` API

---

## Phase 5: Resource File Updates

**Goal**: Fix ~2000 JSON files to match 1.21.1 formats.

1. **Recipe format changes** (~350 files, scriptable):
   - Crafting: `"result": {"item": "..."}` → `"result": {"id": "..."}`
   - Smelting/blasting: `"result": "string"` → `"result": {"id": "string"}`
   - Stonecutting: `"result": "...", "count": N` → `"result": {"id": "...", "count": N}`
2. **Advancement updates** (~22 files):
   - Icon: `"item"` → `"id"`, remove `"nbt"` fields
   - Predicates: `"tag": "minecraft:axes"` → `"items": "#minecraft:axes"`
3. **Pack format**: Update all `pack.mcmeta` files to format 34
4. **Tag files**: Replace `#forge:is_hot/wet/cold` → `#c:is_hot/wet/cold`
5. **Loot tables**: Review for any format changes (likely minimal)
6. **Worldgen**: Review configured/placed features (likely minimal)

---

## Phase 6: Testing

1. `./gradlew.bat build` — fix all compilation errors
2. Launch Fabric client — check block registration, creative tab, recipes, weathering mechanics
3. Launch NeoForge client — same checks
4. Watch logs for mixin failures, missing textures, JSON parse errors

---

## Risk Assessment

| Risk | Area | Why |
|------|------|-----|
| **CRITICAL** | `ServerLevelMixin` local capture | Local variable order changes between MC versions; `CAPTURE_FAILHARD` will crash |
| **CRITICAL** | `BlocksMixin` redirects | 20+ redirects into `Blocks.<clinit>`; constructor signatures change |
| **HIGH** | Moonlight API compatibility | Mod uses Moonlight for nearly everything; its 1.21 API may differ |
| **HIGH** | NeoForge event system | Significantly restructured vs Forge 1.20.1 |
| **MEDIUM** | Recipe/advancement JSON | ~370 files need mechanical updates (scriptable) |
| **LOW** | Models/blockstates/textures | Format is stable; should work as-is |

---

## Approach

Work phase by phase. After Phase 1, the user does a Gradle sync to verify deps resolve. After Phase 3, attempt `./gradlew :common:build`. After each subsequent phase, build that module. This lets us catch issues incrementally rather than debugging everything at once.

For the high-risk mixins (ServerLevelMixin, BlocksMixin), we'll need to inspect the 1.21.1 decompiled vanilla source after Gradle sync to know the correct method signatures and local variable order.
