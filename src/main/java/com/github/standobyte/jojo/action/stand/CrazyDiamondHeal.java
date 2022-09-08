package com.github.standobyte.jojo.action.stand;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.IHasHealth;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class CrazyDiamondHeal extends StandEntityAction {
    private final Supplier<StandEntityMeleeBarrage> barrage;

    public CrazyDiamondHeal(StandEntityAction.Builder builder, Supplier<StandEntityMeleeBarrage> barrage) {
        super(builder);
        this.barrage = barrage;
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity targetEntity = target.getEntity();
        if (targetEntity.is(power.getUser())) {
            return conditionMessage("cd_heal_self");
        }
        if (!(targetEntity instanceof LivingEntity
                || targetEntity instanceof IHasHealth
                || targetEntity instanceof BoatEntity)) {
            return conditionMessage("heal_target");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;    
    }
    
    @Override
    public boolean standCanTick(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        return task.getTarget().getType() == TargetType.ENTITY;
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        Entity targetEntity = task.getTarget().getEntity();
        
        boolean healedThisTick = false;
        
        if (targetEntity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) targetEntity;
            healedThisTick = healLivingEntity(world, targetLiving);
        }
        else if (targetEntity instanceof IHasHealth) {
            healedThisTick = heal(world, targetEntity, (IHasHealth) targetEntity, 
                    (e, clientSide) -> {
                        if (!clientSide) {
                            e.setHealth(e.getHealth() + e.getMaxHealth() / 40);
                        }
                    },
                    e -> e.getHealth() < e.getMaxHealth());
        }
        else if (targetEntity instanceof BoatEntity) {
            healedThisTick = heal(world, targetEntity, (BoatEntity) targetEntity, 
                    (e, clientSide) -> {
                        if (!clientSide) {
                            e.setDamage(Math.max(e.getDamage() - 1, 0));
                        }
                    },
                    e -> e.getDamage() > 0);
        }
        

        if (!world.isClientSide()) {
            barrageTick(standEntity, healedThisTick, targetEntity != null ? targetEntity.getBoundingBox().getCenter() : null);
        }
    }

    public static boolean healLivingEntity(World world, LivingEntity entity) {
        // FIXME disable it if the target is a dead body already
        if (entity.deathTime > 0) {
            if (entity.deathTime > 1) {
                return false;
            }
            return heal(world, entity, 
                    entity, (e, clientSide) -> {
                        LivingEntity toHeal = e;
                        if (!clientSide) {
                            StandUtil.getStandUser(e).setHealth(0.001F);
                        }
                        e.deathTime--;
                        toHeal.deathTime--;
                    }, e -> true);
        }
        
        return heal(world, entity, 
                entity, (e, clientSide) -> {
                    if (!clientSide) {
                        LivingEntity toHeal = StandUtil.getStandUser(e);
                        toHeal.setHealth(toHeal.getHealth() + 0.5F);
                    }
                }, 
                e -> e.getHealth() < e.getMaxHealth());
    }
    
    public static <T> boolean heal(World world, Entity entity, T entityCasted, BiConsumer<T, Boolean> heal, Predicate<T> isHealthMissing) {
        boolean healed = isHealthMissing.test(entityCasted);
        heal.accept(entityCasted, world.isClientSide());
        if (world.isClientSide() && isHealthMissing.test(entityCasted) && StandUtil.shouldStandsRender(ClientUtil.getClientPlayer())) {
            addParticlesAround(entity);
        }
        return healed;
    }
    
    public static void addParticlesAround(Entity entity) {
        if (entity.level.isClientSide() && StandUtil.shouldStandsRender(ClientUtil.getClientPlayer())) {
            int particlesCount = Math.max(MathHelper.ceil(entity.getBbWidth() * (entity.getBbHeight() * 2 * entity.getBbHeight())), 1);
            for (int i = 0; i < particlesCount; i++) {
                entity.level.addParticle(ModParticles.CD_RESTORATION.get(), entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1), 0, 0, 0);
            }
        }
    }
    
    @Override
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return barrageVisuals(task) ? barrage.get().getStandPose(standPower, standEntity, task)
                : super.getStandPose(standPower, standEntity, task);
    }
    
    @Override
    public void onPhaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            @Nullable Phase from, @Nullable Phase to, StandEntityTask task, int nextPhaseTicks) {
        boolean started = to == Phase.PERFORM;
        if (world.isClientSide()) {
            if (barrageVisuals(task)) {
                standEntity.getBarrageHitSoundsHandler().setIsBarraging(started);
            }
        }
        else if (!started) {
            PacketManager.sendToClientsTracking(TrBarrageHitSoundPacket.barrageStopped(standEntity.getId()), standEntity);
        }
        
        if (world.isClientSide()) {
            if (to == Phase.PERFORM) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, 
                        ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), this, Phase.PERFORM, 1.0F, 1.0F, true);
            }
            else if (from == Phase.PERFORM) {
                standEntity.playSound(ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    
    @Nullable
    public SoundEvent getSound(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        return phase == Phase.PERFORM && barrageVisuals(task) ? barrage.get().getSound(standEntity, standPower, phase, task)
                : super.getSound(standEntity, standPower, phase, task);
    }
    
    private void barrageTick(StandEntity stand, boolean healedThisTick, Vector3d soundPos) {
        if (!stand.level.isClientSide()) {
            SoundEvent hitSound = barrage != null && barrage.get() != null ? barrage.get().getHitSound() : null;
            if (hitSound != null) {
                PacketManager.sendToClientsTracking(healedThisTick ? 
                        new TrBarrageHitSoundPacket(stand.getId(), hitSound, soundPos)
                        : TrBarrageHitSoundPacket.noSound(stand.getId()), stand);
            }
        }
    }
    
    private boolean barrageVisuals(StandEntityTask task) {
        if (barrage == null || barrage.get() == null) return false;
        
        ActionTarget target = task.getTarget();
        if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target.getEntity();
            return targetLiving.getHealth() / targetLiving.getMaxHealth() <= 0.5F;
        }
        return false;
    }
}
