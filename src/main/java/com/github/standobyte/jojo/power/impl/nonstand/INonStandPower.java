package com.github.standobyte.jojo.power.impl.nonstand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.power.NonStandCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobPowerUser;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

public interface INonStandPower extends IPower<INonStandPower, NonStandPowerType<?>> {
    float getEnergy();
    float getMaxEnergy();
    boolean hasEnergy(float amount);
    void addEnergy(float amount);
    boolean consumeEnergy(float amount);
    void setEnergy(float amount);
    
    boolean hadPowerBefore(NonStandPowerType<?> type);
    void addHadPowerBefore(NonStandPowerType<?> type);
    
    <T extends NonStandPowerType<D>, D extends TypeSpecificData> Optional<D> getTypeSpecificData(@Nullable T requiredType);
    
    public static LazyOptional<INonStandPower> getNonStandPowerOptional(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return entity.getCapability(NonStandCapProvider.NON_STAND_CAP);
        }
        if (entity instanceof IMobPowerUser) {
            return LazyOptional.of(() -> ((IMobPowerUser) entity).getPower());
        }
        return LazyOptional.empty();
    }
    
    public static INonStandPower getPlayerNonStandPower(PlayerEntity player) {
        return getNonStandPowerOptional(player).orElseThrow(() -> new IllegalStateException("Player's non-stand power capability is empty."));
    }
}
