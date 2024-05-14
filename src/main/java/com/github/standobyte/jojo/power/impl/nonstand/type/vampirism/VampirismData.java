package com.github.standobyte.jojo.power.impl.nonstand.type.vampirism;

import java.util.Optional;
import java.util.Random;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrVampirismDataPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistry;

public class VampirismData extends TypeSpecificData {
    private boolean vampireFullPower = false;
    private int lastBloodLevel = -999;
    
    private int curingTicks = 0;
    private boolean curingStageChanged = false;

    private boolean vampireHamonUser = false;
    private float hamonStrengthLevel;
    private Optional<CharacterHamonTechnique> hamonTechnique = Optional.empty();

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        LivingEntity user = power.getUser();
        
        if (!user.level.isClientSide()) {
            if (oldType == ModPowers.HAMON.get() && oldData instanceof HamonData) {
                setVampireHamonUser(true, Optional.of((HamonData) oldData));
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

    public void setVampireHamonUser(boolean vampireHamonUser, Optional<HamonData> prevHamon) {
        if (this.vampireHamonUser == vampireHamonUser) {
            return;
        }
        this.vampireHamonUser = vampireHamonUser;
        
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(TrVampirismDataPacket.wasHamonUser(user.getId(), vampireHamonUser), user);
        }
        
        
        if (vampireHamonUser && prevHamon.isPresent()) {
            HamonData hamon = prevHamon.get();
            hamonStrengthLevel = hamon.getHamonStrengthLevel();
            hamonTechnique = Optional.ofNullable(hamon.getCharacterTechnique());
        }
        else {
            hamonStrengthLevel = 0;
        }
        
        if (user.level.isClientSide()) {
            power.clUpdateHud();
        }
    }
    
    public float getPrevHamonStrengthLevel() {
        return hamonStrengthLevel;
    }
    
    public Optional<CharacterHamonTechnique> getPrevHamonCharacter() {
        return hamonTechnique;
    }

    public boolean isVampireAtFullPower() {
        return vampireFullPower;
    }
    
    public void setVampireFullPower(boolean vampireFullPower) {
        if (this.vampireFullPower == vampireFullPower) {
            return;
        }
        
        LivingEntity user = power.getUser();
        this.vampireFullPower = vampireFullPower;
        
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(TrVampirismDataPacket.atFullPower(user.getId(), vampireFullPower), user);
        }
        else {
            power.clUpdateHud();
        }
    }
    
    public int getCuringTicks() {
        return curingTicks;
    }
    
    public float getCuringProgress() {
        int curingMaxTicks = getMaxCuringTicks(power.getUser());
        return (float) curingTicks / (float) curingMaxTicks;
    }
    
    public int getCuringStage() {
        if (isBeingCured()) {
            float curingProgress = getCuringProgress();
            return (int) MathHelper.clamp(curingProgress * 4, 0, 3) + 1;
        }
        else {
            return 0;
        }
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
            
            int curingMaxTicks = getMaxCuringTicks(user);
            if (!user.level.isClientSide() && curingTicks >= curingMaxTicks && user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).displayClientMessage(new TranslationTextComponent("jojo.vampire.ready_to_cure"), true);
            }
            
            if (curingTicks < curingMaxTicks) {
                if (power.getEnergy() == 0) {
                    curingTicks = Math.min(curingTicks + getCuringTickProgress(), curingMaxTicks);
                }
                curingStageChanged = curingStage != getCuringStage();
            }
        }
    }
    
    public void setCuringTicks(int ticks) {
        if (this.curingTicks != ticks) {
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(TrVampirismDataPacket.curingTicks(user.getId(), ticks), user);
            }
            this.curingTicks = ticks;
        }
    }
    
    private int getCuringTickProgress() {
        int i = 1;
        LivingEntity user = power.getUser();
        Random random = user.getRandom();
        if (random.nextFloat() < 0.01F) {
            int accelBlocks = 0;
            BlockPos pos = user.blockPosition();
            BlockPos.Mutable blockPos = new BlockPos.Mutable();
            for (int x = pos.getX() - 4; x < pos.getX() + 4; ++x) {
                for (int y = pos.getY() - 4; y < pos.getY() + 4; ++y) {
                    for (int z = pos.getZ() - 4; z < pos.getZ() + 4; ++z) {
                        Block block = user.level.getBlockState(blockPos.set(x, y, z)).getBlock();
                        if (block == Blocks.IRON_BARS || block instanceof BedBlock) {
                            if (random.nextFloat() < 0.3F) {
                                ++i;
                            }
                            if (++accelBlocks >= 14) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return i;
    }
    
    public static void finishCuringOnWakingUp(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                    if (vampirism.curingTicks >= getMaxCuringTicks(entity)) {
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
                        entity.removeEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get());
                        entity.removeEffect(Effects.WEAKNESS);
                        entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 600, 1));
                        entity.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 600, 1));
                        entity.addEffect(new EffectInstance(Effects.WEAKNESS, 600, 1));
                    }
                });
            });
        }
    }
    
    private static int getMaxCuringTicks(LivingEntity entity) {
        return JojoModConfig.getCommonConfigInstance(entity.level.isClientSide()).vampirismCuringDuration.get();
    }
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("VampireFullPower", vampireFullPower);
        nbt.putInt("CuringTicks", curingTicks);
        
        nbt.putBoolean("VampireHamonUser", vampireHamonUser);
        if (vampireHamonUser) {
            nbt.putFloat("HamonStrength", hamonStrengthLevel);
            if (nbt.contains("CharacterTechnique", MCUtil.getNbtId(StringNBT.class))) {
                ResourceLocation techniqueId = new ResourceLocation(nbt.getString("CharacterTechnique"));
                IForgeRegistry<CharacterHamonTechnique> registry = JojoCustomRegistries.HAMON_CHARACTER_TECHNIQUES.getRegistry();
                if (registry.containsKey(techniqueId)) {
                    this.hamonTechnique = Optional.ofNullable(registry.getValue(techniqueId));
                }
            }
        }
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundNBT nbt) {
        this.vampireFullPower = nbt.getBoolean("VampireFullPower");
        this.curingTicks = nbt.getInt("CuringTicks");

        this.vampireHamonUser = nbt.getBoolean("VampireHamonUser");
        this.hamonStrengthLevel = nbt.getFloat("HamonStrength");
        this.hamonTechnique.ifPresent(character -> nbt.putString("HamonTechnique", character.getRegistryName().toString()));
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
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
