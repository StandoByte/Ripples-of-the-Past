package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GoldExperienceCreateLifeform extends StandAction {

    public GoldExperienceCreateLifeform(StandAction.Builder builder) {
        super(builder);
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            EntityType<?> type = GoldExperienceChooseLifeform.chosenTypeTmp;
            if (type != null) {
                Entity lifeFormCreated = type.create(world);
                if (lifeFormCreated instanceof LivingEntity) {
                    if (lifeFormCreated instanceof MobEntity) {
                        ((MobEntity) lifeFormCreated).finalizeSpawn((ServerWorld) world, 
                                world.getCurrentDifficultyAt(user.blockPosition()), 
                                SpawnReason.COMMAND, null, null);
                    }
                    int ticks = getTicksToCreate(user, power, lifeFormCreated);
                    Entity tf = new GETransformationEntity(world)
                            .withTransformationTarget((LivingEntity) lifeFormCreated) // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! make it work with any entity?
                            .withDuration(ticks);
                    tf.moveTo(user.getX(), user.getY(), user.getZ(), user.yRot, 0);
                    lifeFormCreated.copyPosition(tf);
                    lifeFormCreated.setYHeadRot(user.yRot);
                    world.addFreshEntity(tf);
                }
            }
        }
    }
    
    public static int getTicksToCreate(LivingEntity user, IStandPower power, Entity lifeForm) {
        double speed = 0;
        if (power.hasPower()) {
            StandStats stats = power.getType().getStats();
            speed = stats.getBaseAttackSpeed() + stats.getDevAttackSpeed(power.getStatsDevelopment());
        }
        float modifier = MathHelper.clamp(100 - (float) speed * 2F, 0, 100);
        float volume = getVolume(lifeForm);
        return 20 + MathHelper.ceil(volume * modifier);
    }
    
    public static float getVolume(Entity entity) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        return width * width * height;
    }
    
    
    
    public static void onTransformationFinish(Entity entity) {
        if (entity instanceof MobEntity) {
            ((MobEntity) entity).playAmbientSound();
        }
    }
}
