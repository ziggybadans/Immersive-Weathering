package com.ordana.immersive_weathering.configs;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;

import java.util.function.Supplier;

public class ClientConfigs {

    public static ModConfigHolder CLIENT_SPEC;


    //client configs

    public static Supplier<Boolean> LEAF_DECAY_PARTICLES;
    public static Supplier<Boolean> FALLING_LEAF_PARTICLES;
    public static Supplier<Boolean> LEAF_DECAY_SOUND;

    public static Supplier<Double> FALLING_LEAF_PARTICLE_RATE;
    public static Supplier<Double> RAINY_FALLING_LEAF_PARTICLE_RATE;
    public static Supplier<Double> STORMY_FALLING_LEAF_PARTICLE_RATE;

    public static void init() {
        ConfigBuilder builder = ConfigBuilder.create(ImmersiveWeathering.res("client"), ConfigType.CLIENT);

        builder.push("general");
        LEAF_DECAY_PARTICLES = builder.define("leaves_decay_particles", true);
        FALLING_LEAF_PARTICLES = builder.define("falling_leaf_particles", true);
        LEAF_DECAY_SOUND = builder.define("decay_sound", true);

        FALLING_LEAF_PARTICLE_RATE = builder.define("falling_leaf_rate", 0.08d, 0d, 1d);
        RAINY_FALLING_LEAF_PARTICLE_RATE = builder.define("rainy_falling_leaf_rate", 0.2d, 0d, 1d);
        STORMY_FALLING_LEAF_PARTICLE_RATE = builder.define("stormy_falling_leaf_rate", 0.4d, 0d, 1d);
        builder.pop();

        CLIENT_SPEC = builder.build();

        //load early
        CLIENT_SPEC.forceLoad();

    }

}
