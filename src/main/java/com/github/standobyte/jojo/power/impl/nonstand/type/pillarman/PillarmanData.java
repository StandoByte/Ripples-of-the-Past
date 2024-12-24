package com.github.standobyte.jojo.power.impl.nonstand.type.pillarman;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPillarmanDataPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class PillarmanData extends TypeSpecificData {
	public static final int MAX_STAGE_LEVEL = 3;
    private int stage = 1;
    private boolean stoneForm = false;
    private float lastEnergy = -999;
    private int lastStage = -1;
    private Mode mode = Mode.NONE;
    private List<MutableInt> eatenTntFuse = new ArrayList<>();
    private boolean bladesVisible = false;
    
    public enum Mode {
        NONE,
        WIND,
        HEAT,
        LIGHT
    }

    private static final AttributeModifier ATTACK_DAMAGE = new AttributeModifier(
            UUID.fromString("8312317d-3b9c-4a0e-ac02-01318f3032a7"), "Pillar man attack damage", 1.0, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier ATTACK_SPEED = new AttributeModifier(
            UUID.fromString("45f28d25-681b-44ba-96e5-b99c60582d8b"), "Pillar man attack speed", 0.15, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MOVEMENT_SPEED = new AttributeModifier(
            UUID.fromString("d3ac2755-e2ce-4b0d-b828-532e2c8d65fc"), "Pillar man movement speed", 0.01, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier SWIMMING_SPEED = new AttributeModifier(
            UUID.fromString("1a7dbcb9-fc23-4fe9-8035-1d68e13b2cf4"), "Pillar man swimming speed", 0.15, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MAX_HEALTH = new AttributeModifier(
            UUID.fromString("0b6284b6-48d3-47d0-b612-e1d14f1feed5"), "Pillar man max health", 10, AttributeModifier.Operation.ADDITION);
    
    

    private void updatePillarmanBuffs(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            World world = entity.level;
            int lvl = (world.getDifficulty().getId() * stage);
            MCUtil.applyAttributeModifierMultiplied(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE, lvl);
            MCUtil.applyAttributeModifierMultiplied(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED, lvl);
            MCUtil.applyAttributeModifierMultiplied(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED, lvl);
            MCUtil.applyAttributeModifierMultiplied(entity, ForgeMod.SWIM_SPEED.get(), SWIMMING_SPEED, lvl);
            MCUtil.applyAttributeModifierMultiplied(entity, Attributes.MAX_HEALTH, MAX_HEALTH, lvl);
        }
    }
    
    void onClear() {
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            MCUtil.removeAttributeModifier(user, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
            MCUtil.removeAttributeModifier(user, Attributes.ATTACK_SPEED, ATTACK_SPEED);
            MCUtil.removeAttributeModifier(user, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
            MCUtil.removeAttributeModifier(user, ForgeMod.SWIM_SPEED.get(), SWIMMING_SPEED);
            MCUtil.removeAttributeModifier(user, Attributes.MAX_HEALTH, MAX_HEALTH);
        }
    }
    
    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            power.addEnergy(1000);
        }
        updatePillarmanBuffs(user);
        user.level.playSound(null, user, ModSounds.PILLAR_MAN_AWAKENING.get(), user.getSoundSource(), 1.0F, 1.0F);
        
        super.onPowerGiven(oldType, oldData);
    }
    
    public void tick() {
        LivingEntity user = power.getUser();
        if (!user.isAlive()) {
            stoneForm = false;
        }
        if (!user.level.isClientSide()) {
            if (isStoneFormEnabled()) {
                user.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), 20, 0, false, false, true));
                user.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 40, 3, false, false, true));
                user.addEffect(new EffectInstance(Effects.BLINDNESS, 40, 0, false, false, true));
                user.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 40, 0, false, false, true));
            }
            if (!eatenTntFuse.isEmpty()) {
                Iterator<MutableInt> iter = eatenTntFuse.iterator();
                while (iter.hasNext()) {
                    MutableInt fuseTimer = iter.next();
                    if (fuseTimer.decrementAndGet() <= 0) {
                        user.level.playSound(null, user.getX(), user.getY(), user.getZ(), 
                                SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 
                                0.1f, (1.0F + (user.level.random.nextFloat() - user.level.random.nextFloat()) * 0.2F) * 0.7F);
                        iter.remove();
                    }
                }
            }
        }
    }
    
    public boolean needsEffectsRefresh(INonStandPower power) {
        float energy = power.getEnergy();
        boolean energyChanged = this.lastEnergy != energy;
        boolean stageChanged = this.lastStage != stage;
        this.lastEnergy = energy;
        this.lastStage = stage;
        return energyChanged || stageChanged;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("PillarmanStage", stage);
        if (mode != null) {
            MCUtil.nbtPutEnum(nbt, "PillarmanMode", mode);
        }
        nbt.putBoolean("StoneForm", stoneForm);
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundNBT nbt) {
        stage = nbt.getInt("PillarmanStage");
        mode = MCUtil.nbtGetEnum(nbt, "PillarmanMode", Mode.class);
        stoneForm = nbt.getBoolean("StoneForm");
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        updatePillarmanBuffs(user);
    }

    // TODO check the packet
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrPillarmanDataPacket(user.getId(), this), entity);
    }
    
    //States
    public int getEvolutionStage() {
        return stage;
    }

    // TODO
    public void setEvolutionStage(int stage) {
        this.stage = stage;
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            serverPlayer.ifPresent(player -> {
                if (stage == 2) {
                    ModCriteriaTriggers.EVOLVE_PILLARMAN.get().trigger(player);
                } else if (stage == 3) {
                    ModCriteriaTriggers.EVOLVE_PILLARMAN.get().trigger(player);
                    ModCriteriaTriggers.EVOLVE_PILLARMAN_AJA.get().trigger(player);
                }
            });
            
        	updatePillarmanBuffs(user);
            PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanDataPacket(user.getId(), this), user);
        }
        power.clUpdateHud();
    }
    
    public boolean toggleStoneForm() {
        setStoneFormEnabled(!stoneForm);
        return stoneForm;
    }
    
    public void setStoneFormEnabled(boolean isEnabled) {
        if (this.stoneForm != isEnabled) {
            this.stoneForm = isEnabled;
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanDataPacket(user.getId(), this), user);
            }
        }
    }
    
    public boolean isStoneFormEnabled() {
        return stoneForm;
    }
    
    public void setBladesVisible(boolean visible) {
        this.bladesVisible = visible;
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanDataPacket(user.getId(), this), user);
        }
    }
    
    public boolean getBladesVisible() {
        return bladesVisible;
    }
    
    public Mode getMode() {
        return mode;
    }

    // TODO
    public void setMode(Mode mode) {
        this.mode = mode;
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
        	if (user instanceof ServerPlayerEntity) {
        	    ServerPlayerEntity player = (ServerPlayerEntity) user;
        	    switch (mode) {
        	    case WIND:
        	        // TODO make a single trigger with a mod predicate for that
        	        ModCriteriaTriggers.PILLARMAN_WIND_MODE.get().trigger(player);
        	        break;
        	    case HEAT:
        	        ModCriteriaTriggers.PILLARMAN_HEAT_MODE.get().trigger(player);
        	        break;
        	    case LIGHT:
        	        ModCriteriaTriggers.PILLARMAN_LIGHT_MODE.get().trigger(player);
        	        break;
    	        default:
    	            break;
        	    }
        	}
            PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanDataPacket(user.getId(), this), user);
        }
        power.clUpdateHud();
    }
    
    public void addEatenTntFuse(int fuse) {
        if (fuse > 0) {
            eatenTntFuse.add(new MutableInt(fuse));
        }
    }
}