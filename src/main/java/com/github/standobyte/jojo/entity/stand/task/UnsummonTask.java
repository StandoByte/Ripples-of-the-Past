package com.github.standobyte.jojo.entity.stand.task;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;

public class UnsummonTask extends StandEntityTask {
    private LivingEntity user;
    private boolean playedSound = false;

    public UnsummonTask(StandEntity standEntity, LivingEntity user) {
        super(standEntity.isArmsOnlyMode() ? 10 : 20, false, standEntity);
        this.user = user;
    }
    
    @Override
    protected void tick() {
        if (standEntity.isCloseToEntity(user) && !standEntity.hasEffect(ModEffects.STUN.get())) {
            if (!playedSound && ticksLeft == ticks) {
                standEntity.playStandSound(StandSoundType.UNSUMMON);
                playedSound = true;
            }
            standEntity.setAlpha((float) (ticksLeft - 1) / (float) ticks);
            if (ticksLeft == 1) {
                IStandPower.getStandPowerOptional(user).ifPresent(power -> {
                    power.getType().forceUnsummon(user, power);
                });
            }
            standEntity.setPos(user.getX(), user.getY(), user.getZ());
        }
        else {
            ticksLeft++;
        }
    }
    
    @Override
    public boolean canClearMidway() {
        return !standEntity.isArmsOnlyMode();
    }
    
    @Override
    protected void onClear() {
        standEntity.setAlpha(1.0F);
        if (ticksLeft > 0) {
            standEntity.stopStandSound(StandSoundType.UNSUMMON);
        }
    }

}
