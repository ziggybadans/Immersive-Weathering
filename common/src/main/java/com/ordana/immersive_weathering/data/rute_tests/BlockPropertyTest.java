package com.ordana.immersive_weathering.data.rute_tests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ordana.immersive_weathering.data.block_growths.Operator;
import com.ordana.immersive_weathering.reg.ModRuleTests;
import com.ordana.immersive_weathering.util.StrOpt;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class BlockPropertyTest extends RuleTest {

    public static final MapCodec<BlockPropertyTest> CODEC = PropPredicate.CODEC.codec().listOf().fieldOf("properties")
            .xmap(BlockPropertyTest::new, (t) -> t.propPredicates);

    private final List<PropPredicate> propPredicates;

    private BlockPropertyTest(List<PropPredicate> propPredicates) {
        this.propPredicates = propPredicates;
    }

    @Override
    public boolean test(BlockState state, RandomSource random) {
        for (var p : propPredicates) {
            if (!p.test(state)) return false;
        }
        return true;
    }

    @Override
    protected RuleTestType<BlockPropertyTest> getType() {
        return ModRuleTests.BLOCK_PROPERTY_TEST.get();
    }

    private static final class PropPredicate implements Predicate<BlockState> {

        public static final MapCodec<PropPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("from_block").forGetter(PropPredicate::getFromBlock),
                Codec.STRING.fieldOf("property").forGetter(PropPredicate::getPropertyName),
                StrOpt.of(Codec.STRING, "value").forGetter(PropPredicate::getTargetValueString),
                StrOpt.of(Operator.CODEC, "operator", Operator.EQUAL).forGetter(PropPredicate::getOperator)
        ).apply(instance, PropPredicate::new));

        private final Block fromBlock;
        private final String propertyName;
        private final Operator operator;
        @Nullable
        private final Property<?> property;
        @Nullable
        private final Comparable<?> targetValue;
        private final Integer intValue;
        @Nullable
        private final String targetValueString;

        public PropPredicate(Block fromBlock, String propertyName, Optional<String> value, Operator operator) {
            this.fromBlock = fromBlock;
            this.propertyName = propertyName;
            this.operator = operator;
            this.property = findProperty(fromBlock.defaultBlockState(), propertyName);
            this.targetValueString = value.orElse(null);
            if (this.property != null && this.targetValueString != null) {
                this.targetValue = this.property.getValue(this.targetValueString).orElse(null);
            } else {
                this.targetValue = null;
            }
            if (this.property instanceof IntegerProperty && this.targetValue instanceof Integer i) {
                this.intValue = i;
            } else {
                this.intValue = null;
            }
        }

        public Block getFromBlock() {
            return fromBlock;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Optional<String> getTargetValueString() {
            return Optional.ofNullable(targetValueString);
        }

        public Operator getOperator() {
            return operator;
        }

        @Override
        public boolean test(BlockState state) {
            if (property == null || !state.is(fromBlock)) return false;
            var val = state.getOptionalValue(property);
            if (val.isEmpty()) return false;
            if (intValue != null && val.get() instanceof Integer actual) {
                return operator.apply(actual, intValue);
            }
            return targetValue == null || Objects.equals(val.get(), targetValue);
        }

        @Nullable
        private static Property<?> findProperty(BlockState state, String name) {
            for (var p : state.getProperties()) {
                if (p.getName().equals(name)) {
                    return p;
                }
            }
            return null;
        }
    }
}
