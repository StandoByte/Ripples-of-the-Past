package com.github.standobyte.jojo.action.stand.effect;

import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance;

import net.minecraft.entity.LivingEntity;

public class BoyIIManStandPartTakenEffect extends StandEffectInstance {
    private StandInstance partsTaken;
    
    public static BoyIIManStandPartTakenEffect serverSide(StandEffectType<?> effectType, StandInstance partsTaken) {
        BoyIIManStandPartTakenEffect effect = new BoyIIManStandPartTakenEffect(effectType);
        effect.partsTaken = partsTaken;
        return effect;
    }
    
    public BoyIIManStandPartTakenEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    @Override
    protected void start() {}

    @Override
    protected void tickTarget(LivingEntity target) {}
    
    @Override
    protected void tick() {}

    @Override
    protected void stop() {
        targets.stream().findAny().ifPresent(target -> {
            IStandPower.getStandPowerOptional(target).ifPresent(power -> {
                if (!power.hasPower()) {
                    power.giveStand(partsTaken, false);
                }
                else {
                    power.getStandInstance().ifPresent(stand -> {
                        if (stand.getType() == partsTaken.getType()) {
                            partsTaken.getAllParts().forEach(part -> {
                                if (!stand.hasPart(part)) {
                                    stand.addPart(part);
                                }
                            });
                        }
                    });
                }
            });
        });
    }

}
