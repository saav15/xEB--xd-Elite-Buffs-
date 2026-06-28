package org.xeb.xeb.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassKnucklesItem extends Item {
    public BrassKnucklesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.xeb.brass_knuckles.desc2"));
        tooltip.add(Component.translatable("item.xeb.brass_knuckles.desc3"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
