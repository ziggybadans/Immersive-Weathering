package com.ordana.immersive_weathering.mixins.fabric;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Moonlight 1.21-2.29.20 ships this anonymous listener with an intermediary-named
 * method (method_25931) but no named reload(...) method in dev, which crashes
 * world/datapack reload with AbstractMethodError on official/parchment mappings.
 */
@Mixin(targets = "net.mehvahdjukaar.moonlight.api.platform.fabric.PlatHelperImpl$1", remap = false)
public abstract class MoonlightReloadListenerBridgeMixin {

    @Shadow(remap = false)
    public abstract CompletableFuture<Void> method_25931(
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            ProfilerFiller prepareProfiler,
            ProfilerFiller applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor
    );

    @Unique
    public CompletableFuture<Void> reload(
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            ProfilerFiller prepareProfiler,
            ProfilerFiller applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        return this.method_25931(
                preparationBarrier,
                resourceManager,
                prepareProfiler,
                applyProfiler,
                prepareExecutor,
                applyExecutor
        );
    }
}
