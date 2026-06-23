package org.xeb.xeb.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xeb.xeb.Config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BossTargetingSystemTest {

    @BeforeAll
    public static void initRegistry() {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            java.lang.reflect.Field field = net.minecraft.server.Bootstrap.class.getDeclaredField("isBootstrapped");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LivingEntity mockEntity;
    private EntityType<?> mockType;
    private CompoundTag nbt;

    @BeforeEach
    public void setup() {
        mockEntity = mock(LivingEntity.class);
        mockType = mock(EntityType.class);
        nbt = new CompoundTag();
        when(mockEntity.getType()).thenReturn((EntityType) mockType);
        when(mockEntity.getPersistentData()).thenReturn(nbt);
        when(mockEntity.getMaxHealth()).thenReturn(20.0F);
        when(mockEntity.getDeltaMovement()).thenReturn(Vec3.ZERO);
    }

    @Test
    public void testIsBossLowHealthEntityReturnsFalse() {
        assertFalse(UniversalBossDetector.isBoss(mockEntity));
    }

    @Test
    public void testIsBossHighHealthEntityReturnsTrue() {
        // High health is greater than default 300.0 health threshold
        when(mockEntity.getMaxHealth()).thenReturn(350.0F);
        // BossEvent check (we return true via high health + class check or class simple name containing boss)
        // Wait, UniversalBossDetector: evaluateBoss returns highHealth && (hasBossBar || isMisc || isBossModNamespace || classNameMatch)
        // Let's mock a class name match: e.g. class simple name containing boss
        LivingEntity bossMock = mock(LivingBossEntity.class);
        when(bossMock.getType()).thenReturn((EntityType) mockType);
        when(bossMock.getMaxHealth()).thenReturn(350.0F);
        when(bossMock.getPersistentData()).thenReturn(nbt);

        assertTrue(UniversalBossDetector.isBoss(bossMock));
    }

    @Test
    public void testRejectionBufferPreventsRepeatedTargets() {
        Mob boss = mock(Mob.class);
        when(boss.getPersistentData()).thenReturn(nbt);

        assertFalse(TargetRejectionBuffer.isRejected(boss, 42));
        TargetRejectionBuffer.addRejectedTarget(boss, 42);
        assertTrue(TargetRejectionBuffer.isRejected(boss, 42));

        // Ticking the buffer should clear it after 200 ticks
        for (int i = 0; i < 201; i++) {
            TargetRejectionBuffer.tick(boss);
        }
        assertFalse(TargetRejectionBuffer.isRejected(boss, 42));
    }

    // Helper class for class name mocking
    static abstract class LivingBossEntity extends LivingEntity {
        protected LivingBossEntity(EntityType<? extends LivingEntity> type, net.minecraft.world.level.Level level) {
            super(type, level);
        }
    }
}
