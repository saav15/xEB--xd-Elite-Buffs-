package org.xeb.xeb.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class HolyMantleItem extends Item {
    public HolyMantleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.xeb.holy_mantle.desc1"));
        tooltip.add(Component.translatable("item.xeb.holy_mantle.desc2"));
        tooltip.add(Component.translatable("item.xeb.holy_mantle.desc3"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
