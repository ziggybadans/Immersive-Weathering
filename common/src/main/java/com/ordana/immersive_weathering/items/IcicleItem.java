package com.ordana.immersive_weathering.items;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class IcicleItem extends BlockItem {

    public IcicleItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 80));
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!CommonConfigs.ICICLE_FOOD.get()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }
}
