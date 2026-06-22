package org.xeb.xeb.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.buff.EliteBuffRegistry;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import org.xeb.xeb.medallion.MedallionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class XebCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Build Brigadier chain from end (16) to beginning (1)
        ArgumentBuilder<CommandSourceStack, ?> current = null;
        for (int i = 16; i >= 1; i--) {
            final int argIndex = i;
            var nextNode = Commands.argument("buff" + i, StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    for (EliteBuff buff : EliteBuffRegistry.getAll()) {
                        builder.suggest(buff.getId());
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> executeSpawn(ctx, argIndex));
            
            if (current != null) {
                nextNode.then(current);
            }
            current = nextNode;
        }

        var entityNode = Commands.argument("entity", ResourceLocationArgument.id())
            .suggests((ctx, builder) -> {
                for (ResourceLocation key : ForgeRegistries.ENTITY_TYPES.getKeys()) {
                    builder.suggest(key.toString());
                }
                return builder.buildFuture();
            })
            .executes(ctx -> executeSpawn(ctx, 0));
        
        if (current != null) {
            entityNode.then(current);
        }

        dispatcher.register(
            Commands.literal("xeb")
                .requires(source -> source.hasPermission(2)) // Require OP permission level 2 (same as summon)
                .then(entityNode)
        );
    }

    private static int executeSpawn(CommandContext<CommandSourceStack> ctx, int buffCount) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ResourceLocation entityId = ResourceLocationArgument.getId(ctx, "entity");
        
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);
        if (entityType == null) {
            source.sendFailure(Component.literal("Unknown entity type: " + entityId));
            return 0;
        }

        Vec3 pos = source.getPosition();
        ServerLevel level = source.getLevel();

        Entity entity = entityType.create(level);
        if (!(entity instanceof LivingEntity living)) {
            source.sendFailure(Component.literal("Entity is not a LivingEntity."));
            return 0;
        }

        living.moveTo(pos.x, pos.y, pos.z, source.getRotation().y, 0.0F);

        List<String> buffIds = new ArrayList<>();
        for (int i = 1; i <= buffCount; i++) {
            try {
                String buffId = StringArgumentType.getString(ctx, "buff" + i);
                if (buffId != null && !buffId.isEmpty()) {
                    buffIds.add(buffId);
                }
            } catch (IllegalArgumentException e) {
                // Ignore missing node
            }
        }

        List<MedallionData> medallions = new ArrayList<>();
        for (String buffId : buffIds) {
            EliteBuff buff = EliteBuffRegistry.getById(buffId);
            if (buff != null) {
                // Manual spawn uses LEGENDARY (Gold) for customization testing
                medallions.add(new MedallionData(buff, MedallionType.LEGENDARY, UUID.randomUUID()));
            }
        }

        if (!medallions.isEmpty()) {
            MedallionManager.saveMedallions(living, medallions);
            for (MedallionData m : medallions) {
                m.getBuff().onAttach(living);
            }
        } else {
            // Default to random medallions
            MedallionManager.assignRandomMedallions(living, level);
        }

        level.addFreshEntityWithPassengers(living);
        MedallionManager.syncToTracking(living);

        source.sendSuccess(() -> Component.literal("Spawned elite " + entityId.getPath() + " with " + medallions.size() + " custom medallions."), true);
        return 1;
    }
}
