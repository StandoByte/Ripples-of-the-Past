package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.HamonSunlightYellowOverdriveBarrage.Instance;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.TrSYOBarrageFinisherPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

public class HamonSunlightYellowOverdriveBarrage extends HamonAction implements IPlayerAction<Instance, INonStandPower> {

    public HamonSunlightYellowOverdriveBarrage(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.areHandsFree(user, Hand.MAIN_HAND, Hand.OFF_HAND)) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
//        if (requirementsFulfilled && world.isClientSide()) {
//            ClientTickingSoundsHelper.playStoppableEntitySound(user, ModSounds.HAMON_SYO_CHARGE.get(), 1.0F, 1.0F, false, entity -> power.getHeldAction() != this);
//        }
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide()) {
                power.consumeEnergy(power.getMaxEnergy() / 100);
            }
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user instanceof PlayerEntity) {
            if (!user.level.isClientSide()) {
                JojoModUtil.sayVoiceLine(user, ModSounds.JONATHAN_SYO_BARRAGE.get(), null, true);
                setPlayerAction(user, power);
            }
        }
    }
    
    @Override
    public boolean clHeldStartAnim(PlayerEntity user) {
        return ModPlayerAnimations.syoBarrage.setStartingAnim(user);
    }
    
    @Override
    public void clHeldStopAnim(PlayerEntity user) {
        ModPlayerAnimations.syoBarrage.stopAnim(user);
    }
    
    @Override
    public Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.playerBarrageAnim.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    
    public static class Instance extends ContinuousActionInstance<HamonSunlightYellowOverdriveBarrage, INonStandPower> {
        private boolean finishingPunch = false;

        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, HamonSunlightYellowOverdriveBarrage action) {
            super(user, userCap, playerPower, action);
        }

        private static final int MAX_BARRAGE_DURATION = 70;
        private static final int FINISHING_PUNCH_DURATION = 10;
        
        public void startFinishingPunch() {
            LivingEntity user = getUser();
            INonStandPower power = getPower();
            if (!finishingPunch) {
                finishingPunch = true;
                tick = MAX_BARRAGE_DURATION;
                World world = user.level;

                ActionTarget target = power.getMouseTarget();
                Entity entity = target.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity targetEntity = (LivingEntity) entity;
                    targetEntity.removeEffect(ModStatusEffects.IMMOBILIZE.get());

                    PlayerEntity playerUser = user instanceof PlayerEntity ? ((PlayerEntity) user) : null;
                    if (playerUser != null) {
                        CommonReflection.setAttackStrengthTicker(user, MathHelper.ceil(playerUser.getCurrentItemAttackStrengthDelay()));
                    }

                    if (!world.isClientSide()) {
                        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                        float efficiency = hamon.getActionEfficiency(0, false, ModHamonSkills.SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get());

                        float damage = 15F;
                        damage *= efficiency;

                        if (DamageUtil.dealHamonDamage(targetEntity, damage, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_YELLOW.get()))) {
                            world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), targetEntity.getSoundSource(), 1.0F, 1.0F);
                            DamageUtil.knockback3d(targetEntity, 2F, user.xRot, user.yRot);
                            if (hamon.isSkillLearned(ModHamonSkills.HAMON_SPREAD.get())) {
                                targetEntity.addEffect(new EffectInstance(ModStatusEffects.HAMON_SPREAD.get(), 200, 3));
                            }
                        }
                    }

                    if (playerUser != null) {
                        playerUser.attack(targetEntity);
                    }
                    else if (!world.isClientSide()) {
                        user.doHurtTarget(targetEntity);
                    }

                    if (world.isClientSide()) {
                        user.swing(Hand.MAIN_HAND);
                    }
                }
                
                if (!world.isClientSide()) {
                    PacketManager.sendToClientsTrackingAndSelf(new TrSYOBarrageFinisherPacket(user.getId()), (ServerPlayerEntity) user);
                }
                else if (user instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) user;
                    ModPlayerAnimations.playerBarrageAnim.setAnimEnabled(player, false);
                    ModPlayerAnimations.syoBarrage.setFinisherAnim(player);
                }
            }
        }
        
        @Override
        public void playerTick() {
            LivingEntity user = getUser();
            World world = user.level;
            int tick = getTick();
            if (tick < MAX_BARRAGE_DURATION) {
                // barrage tick
                ActionTarget target = getPower().getMouseTarget();
                LivingEntity targetEntity = null;
                switch (target.getType()) {
                case BLOCK:
                    BlockPos pos = target.getBlockPos();
                    if (!world.isClientSide() && JojoModUtil.canEntityDestroy((ServerWorld) world, pos, world.getBlockState(pos), user)) {
                        if (!world.isEmptyBlock(pos)) {
                            BlockState blockState = world.getBlockState(pos);
                            float digDuration = blockState.getDestroySpeed(world, pos);
                            boolean dropItem = true;
                            if (user instanceof PlayerEntity) {
                                PlayerEntity player = (PlayerEntity) user;
                                digDuration /= player.getDigSpeed(blockState, pos);
                                if (player.abilities.instabuild) {
                                    digDuration = 0;
                                    dropItem = false;
                                }
                                else if (!ForgeHooks.canHarvestBlock(blockState, player, world, pos)) {
                                    digDuration *= 10F / 3F;
//                                    dropItem = false;
                                }
                            }
                            if (digDuration >= 0 && digDuration <= 2.5F * Math.sqrt(user.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                                MCUtil.destroyBlock(world, pos, dropItem, user);
                            }
                            else {
                                SoundType soundType = blockState.getSoundType(world, pos, user);
                                world.playSound(null, pos, soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
                            }
                        }
                    }
                    break;
                case ENTITY:
                    if (target.getEntity() instanceof LivingEntity) {
                        targetEntity = (LivingEntity) target.getEntity();
                        targetEntity.addEffect(new EffectInstance(ModStatusEffects.IMMOBILIZE.get(), 10, 0, false, false, false));
                        if (user instanceof PlayerEntity) {
                            int invulTicks = targetEntity.invulnerableTime;
                            ((PlayerEntity) user).attack(targetEntity);
                            targetEntity.invulnerableTime = invulTicks;
                        }
                        if (!world.isClientSide()) {
                            DamageUtil.dealHamonDamage(targetEntity, 0.1F, user, null, 
                                    attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_YELLOW.get()));
                            if (targetEntity.getHealth() < 2) {
                                startFinishingPunch();
                            }
                        }
                    }
                    break;
                default:
                    break;
                }
                
                if (world.isClientSide() && tick % 2 == 0) {
                    user.swinging = false;
                    user.swing(tick % 4 == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
                }
            }
            else if (!world.isClientSide() && tick == MAX_BARRAGE_DURATION) {
                startFinishingPunch();
            }
        }
        
        @Override
        public int getMaxDuration() {
            return MAX_BARRAGE_DURATION + FINISHING_PUNCH_DURATION;
        }
        
        @Override
        public float getWalkSpeed() {
            return 0;
        }
        
        @Override
        public boolean updateTarget() {
            return true;
        }
    }
}
