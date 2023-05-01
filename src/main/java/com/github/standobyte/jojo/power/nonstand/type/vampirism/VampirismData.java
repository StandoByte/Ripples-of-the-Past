package com.github.standobyte.jojo.power.nonstand.type.vampirism;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrVampirismDataPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.TranslationTextComponent;

public class VampirismData extends TypeSpecificData {
    private static final int CURING_MAX_TICKS = 72000;
    private boolean vampireHamonUser = false;
    private boolean vampireFullPower = false;
    private int lastBloodLevel = -999;
    private int curingTicks = 0;
    private boolean curingStageChanged = false;

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType) {
        LivingEntity user = power.getUser();
        
        if (!user.level.isClientSide()) {
            if (oldType == ModPowers.HAMON.get()) {
                setVampireHamonUser(true);
            }
            power.addEnergy(300);
        }
        
        IStandPower.getStandPowerOptional(user).ifPresent(stand -> {
            if (stand.hasPower() && stand.wasProgressionSkipped()) {
                stand.skipProgression();
            }
        });
    }

    @Override
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower power) {
        return vampireFullPower || 
                action == ModVampirismActions.VAMPIRISM_BLOOD_DRAIN.get() || 
                action == ModVampirismActions.VAMPIRISM_BLOOD_GIFT.get() || 
                vampireHamonUser && action == ModVampirismActions.VAMPIRISM_HAMON_SUICIDE.get();
    }

    public boolean isVampireHamonUser() {
        return vampireHamonUser;
    }

    public void setVampireHamonUser(boolean vampireHamonUser) {
        if (!this.vampireHamonUser == vampireHamonUser) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(TrVampirismDataPacket.wasHamonUser(
                        player.getId(), vampireHamonUser), player);
            });
        }
        this.vampireHamonUser = vampireHamonUser;
        if (vampireHamonUser) {
            addHamonSuicideAbility();
        }
    }
    
    // FIXME (layout editing) !! hamon suicide addition
    private void addHamonSuicideAbility() {
//        VampirismAction hamonAbility = ModActions.VAMPIRISM_HAMON_SUICIDE.get();
//        List<Action<INonStandPower>> abilities = power.getAbilities();
//        if (vampireHamonUser && !abilities.contains(hamonAbility)) {
//            abilities.add(hamonAbility);
//        }
    }

    public boolean isVampireAtFullPower() {
        return vampireFullPower;
    }
    
    public void setVampireFullPower(boolean vampireFullPower) {
        if (this.vampireFullPower != vampireFullPower) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(TrVampirismDataPacket.atFullPower(
                        player.getId(), vampireFullPower), player);
            });
        }
        this.vampireFullPower = vampireFullPower;
    }
    
    public int getCuringTicks() {
        return curingTicks;
    }
    
    public float getCuringProgress() {
        return (float) curingTicks / (float) CURING_MAX_TICKS;
    }
    
    public int getCuringStage() {
        return isBeingCured() ? Math.min(curingTicks * 4 / CURING_MAX_TICKS + 1, 4) : 0;
    }
    
    public boolean isBeingCured() {
        return curingTicks > 0;
    }
    
    // FIXME (vampire\curing) curing side effects
    // FIXME (vampire\curing) also lower buffs over time
    private static final double[] NAUSEA_CHANCE = {0, 0, 1/2400, 1/1200, 1/600};
    void tickCuring(LivingEntity user, INonStandPower power) {
        if (isBeingCured()) {
            user.yRot += (float) (Math.cos((double) user.tickCount * 3.25) * Math.PI * 0.4);
            int curingStage = getCuringStage();
            if (curingStage >= 2 && user.getRandom().nextDouble() <= NAUSEA_CHANCE[Math.min(curingStage, NAUSEA_CHANCE.length + 1)]) {
                user.addEffect(new EffectInstance(Effects.CONFUSION, 200));
            }
            if (!user.level.isClientSide() && curingTicks >= CURING_MAX_TICKS && user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).displayClientMessage(new TranslationTextComponent("jojo.vampire.ready_to_cure"), true);
            }
            
            if (curingTicks < CURING_MAX_TICKS) {
                if (power.getEnergy() == 0) {
                    curingTicks++;
                }
                curingStageChanged = curingStage != getCuringStage();
            }
        }
    }
    
    public void setCuringTicks(int ticks) {
        if (this.curingTicks != ticks) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(TrVampirismDataPacket.curingTicks(
                        player.getId(), ticks), player);
            });
            this.curingTicks = ticks;
        }
    }
    
    public static void onEnchantedGoldenAppleEaten(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            EffectInstance weakness = entity.getEffect(Effects.WEAKNESS);
            if (!(weakness != null && weakness.getAmplifier() >= 4)) {
                return;
            }
            
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                    if (!entity.isSilent()) {
                        entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                                ModSounds.VAMPIRE_CURE_START.get(), entity.getSoundSource(), 1.0F, 1.0F);
                    }
                    vampirism.setCuringTicks(1);
                });
            });
        }
    }
    
    public static void finishCuringOnWakingUp(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                    if (vampirism.curingTicks >= CURING_MAX_TICKS) {
                        if (!entity.isSilent()) {
                            entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                                    ModSounds.VAMPIRE_CURE_END.get(), entity.getSoundSource(), 1.0F, 1.0F);
                        }
                        power.clear();
                        if (entity instanceof ServerPlayerEntity) {
                            ServerPlayerEntity player = (ServerPlayerEntity) entity;
                            player.getFoodData().setFoodLevel(1);
                            ModCriteriaTriggers.VAMPIRISM_CURED.get().trigger(player);
                        }
                        entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 600, 1));
                        entity.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 600, 1));
                        entity.addEffect(new EffectInstance(Effects.WEAKNESS, 600, 1));
                    }
                });
            });
        }
    }
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("VampireHamonUser", vampireHamonUser);
        nbt.putBoolean("VampireFullPower", vampireFullPower);
        nbt.putInt("CuringTicks", curingTicks);
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundNBT nbt) {
        this.vampireHamonUser = nbt.getBoolean("VampireHamonUser");
        this.vampireFullPower = nbt.getBoolean("VampireFullPower");
        this.curingTicks = nbt.getInt("CuringTicks");
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        if (vampireHamonUser) {
            addHamonSuicideAbility();
        }
        lastBloodLevel = -999;
    }
    
    public boolean refreshBloodLevel(int bloodLevel) {
        boolean bloodLevelChanged = this.lastBloodLevel != bloodLevel;
        this.lastBloodLevel = bloodLevel;
        return bloodLevelChanged || curingStageChanged;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(TrVampirismDataPacket.wasHamonUser(
                user.getId(), vampireHamonUser), entity);
        PacketManager.sendToClient(TrVampirismDataPacket.atFullPower(
                user.getId(), vampireFullPower), entity);
        PacketManager.sendToClient(TrVampirismDataPacket.curingTicks(
                user.getId(), curingTicks), entity);
    }
}
