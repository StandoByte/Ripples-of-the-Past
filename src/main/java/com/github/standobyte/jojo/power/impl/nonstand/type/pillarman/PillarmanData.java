package com.github.standobyte.jojo.power.impl.nonstand.type.pillarman;

import java.util.UUID;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPillarmanFlagsPacket;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class PillarmanData extends TypeSpecificData {
	public static final int MAX_STAGE_LEVEL = 3;
    private int stage = 1;
    private boolean stoneForm = false;
    private boolean invaded = false;
    private float lastEnergy = -999;
    private Mode mode = Mode.NONE;
    
    public enum Mode {
        NONE,
        WIND,
        HEAT,
        LIGHT
    }

    private static final AttributeModifier ATTACK_DAMAGE = new AttributeModifier(
            UUID.fromString("8312317d-3b9c-4a0e-ac02-01318f3032a7"), "Pillar man attack damage", 1.0D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier ATTACK_SPEED = new AttributeModifier(
            UUID.fromString("45f28d25-681b-44ba-96e5-b99c60582d8b"), "Pillar man attack speed", 0.15D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MOVEMENT_SPEED = new AttributeModifier(
            UUID.fromString("d3ac2755-e2ce-4b0d-b828-532e2c8d65fc"), "Pillar man movement speed", 0.01D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier SWIMMING_SPEED = new AttributeModifier(
            UUID.fromString("1a7dbcb9-fc23-4fe9-8035-1d68e13b2cf4"), "Pillar man swimming speed", 0.15D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MAX_HEALTH = new AttributeModifier(
            UUID.fromString("0b6284b6-48d3-47d0-b612-e1d14f1feed5"), "Pillar man max health", 10D, AttributeModifier.Operation.ADDITION);
    
    
    
    public void setPillarmanBuffs(LivingEntity entity, int rate) {
        World world = entity.level;
        int lvl = (world.getDifficulty().getId() * stage) * rate;
        applyAttributeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE, lvl);
        applyAttributeModifier(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED, lvl);
        applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED, lvl);
        applyAttributeModifier(entity, ForgeMod.SWIM_SPEED.get(), SWIMMING_SPEED, lvl);
        applyAttributeModifier(entity, Attributes.MAX_HEALTH, MAX_HEALTH, lvl);
        if(stage > 0) {
            ServerPlayerEntity user = (ServerPlayerEntity) entity;
            PacketManager.sendToClient(new TrPillarmanFlagsPacket(entity.getId(), this), user);
        }
    }
    
    private static void applyAttributeModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier, int lvl) {
        ModifiableAttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifier);
            attributeInstance.addTransientModifier(new AttributeModifier(modifier.getId(), modifier.getName() + " " + lvl, modifier.getAmount() * lvl, modifier.getOperation()));
        }
    }
    
    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            power.addEnergy(1000);
        }
        setPillarmanBuffs(user, 1);
        user.level.playSound(null, user, ModSounds.PILLAR_MAN_AWAKENING.get(), user.getSoundSource(), 1.0F, 1.0F);
        IStandPower.getStandPowerOptional(user).ifPresent(stand -> {
            if (stand.hasPower() && stand.wasProgressionSkipped()) {
                stand.skipProgression();
            }
        });
    }
    
    // FIXME extra pillar man actions were written for an old version; rewrite this
//    @Override
//    public void updateExtraActions() {
//        addMoreAbilities();
//    }
//    
//    public void addMoreAbilities() {

//    }
    
    public void tick() {
        LivingEntity user = power.getUser();
        if (!user.isAlive()) {
            stoneForm = false;
        }
        if(isStoneFormEnabled()) {
            user.addEffect(new EffectInstance(ModStatusEffects.STUN.get(), 20, 0, false, false, true));
            user.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 40, 3, false, false, true));
            user.addEffect(new EffectInstance(Effects.BLINDNESS, 40, 0, false, false, true));
            user.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 40, 0, false, false, true));
        }
    }
    
    public boolean refreshEnergy(float energy) {
        boolean energyChanged = this.lastEnergy != energy;
        this.lastEnergy = energy;
        return energyChanged;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("PillarmanStage", stage);
        if(mode != null) {
            MCUtil.nbtPutEnum(nbt, "PillarmanMode", mode);
        }
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundNBT nbt) {
        stage = nbt.getInt("PillarmanStage");
        mode = MCUtil.nbtGetEnum(nbt, "PillarmanMode", Mode.class);
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        setPillarmanBuffs(user, 1);
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrPillarmanFlagsPacket(user.getId(), this), entity);
    }
    
    //States
    public int getEvolutionStage() {
        return stage;
    }
    
    public void setEvolutionStage(int stage) {
        this.stage = stage;
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
        	if(user instanceof PlayerEntity) {
        		ServerPlayerEntity player = (ServerPlayerEntity) user;
        		if(stage == 2) {
                	ModCriteriaTriggers.EVOLVE_PILLARMAN.get().trigger(player);
                } else if(stage == 3) {
                	ModCriteriaTriggers.EVOLVE_PILLARMAN.get().trigger(player);
                	ModCriteriaTriggers.EVOLVE_PILLARMAN_AJA.get().trigger(player);
                }
        	}
            PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanFlagsPacket(user.getId(), this), user);
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
                PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanFlagsPacket(user.getId(), this), user);
            }
        }
    }
    
    public boolean isStoneFormEnabled() {
        return stoneForm;
    }
    
    public void setInvaded(boolean isEnabled) {
        if (this.invaded != isEnabled) {
            this.invaded = isEnabled;
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanFlagsPacket(user.getId(), this), user);
            }
        }
    }
    
    public boolean isInvaded() {
        return invaded;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
        	if(user instanceof PlayerEntity) {
        		ServerPlayerEntity player = (ServerPlayerEntity) user;
        		if(mode == mode.WIND) {
                	ModCriteriaTriggers.PILLARMAN_WIND_MODE.get().trigger(player);
                } else if(mode == mode.HEAT) {
                	ModCriteriaTriggers.PILLARMAN_HEAT_MODE.get().trigger(player);
                } else if(mode == mode.LIGHT) {
                	ModCriteriaTriggers.PILLARMAN_LIGHT_MODE.get().trigger(player);
                }
        	}
            PacketManager.sendToClientsTrackingAndSelf(new TrPillarmanFlagsPacket(user.getId(), this), user);
        }
        power.clUpdateHud();
    }
}