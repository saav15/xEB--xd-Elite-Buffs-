package org.xeb.xeb.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.xeb.xeb.effect.ModEffects;

import java.util.List;

public class MobEnergyItem extends Item {
    public MobEnergyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            // Madness for 2m 10s (130 seconds = 2600 ticks)
            entity.addEffect(new MobEffectInstance(ModEffects.MADNESS.get(), 2600, 0));
            // Strength II (amplifier 1) for 2m 10s (2600 ticks)
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2600, 1));
            // Speed II (amplifier 1) for 2m 10s (2600 ticks)
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2600, 1));
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return stack;
        }

        // Damage the item by 1
        stack.setDamageValue(stack.getDamageValue() + 1);

        // If it has been used twice, replace it with a glass bottle
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.xeb.mob_energy.desc1"));
        tooltip.add(Component.translatable("item.xeb.mob_energy.desc2"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
