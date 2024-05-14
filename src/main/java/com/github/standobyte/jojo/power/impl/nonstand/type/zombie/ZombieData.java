package com.github.standobyte.jojo.power.impl.nonstand.type.zombie;

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
import com.github.standobyte.jojo.init.power.non_stand.zombie.ModZombieActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonFlagsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrVampirismDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrZombieFlagsPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.layout.ActionsLayout;
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

public class ZombieData extends TypeSpecificData {
    private int lastBloodLevel = -999;
    private boolean disguised = false;

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        LivingEntity user = power.getUser();
        
        if (!user.level.isClientSide()) {
            power.addEnergy(1000);
        }
        
        IStandPower.getStandPowerOptional(user).ifPresent(stand -> {
            if (stand.hasPower() && stand.wasProgressionSkipped()) {
                stand.skipProgression();
            }
        });
    }

    @Override
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower power) {
        return  action == ModZombieActions.ZOMBIE_CLAW_LACERATE.get() || 
                action == ModZombieActions.ZOMBIE_DEVOUR.get() ||
                action == ModZombieActions.ZOMBIE_DISGUISE.get();
    }
    
    public void tick() {
    	LivingEntity user = power.getUser();
    	if (!user.isAlive()) {
    		disguised = false;
    	}
    }
    
    public boolean toggleDisguise() {
        setDisguiseEnabled(!disguised);
        return disguised;
    }
    
    public void setDisguiseEnabled(boolean isEnabled) {
        if (this.disguised != isEnabled) {
            this.disguised = isEnabled;
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrZombieFlagsPacket(user.getId(), this), user);
            }
        }
    }
    
    public boolean isDisguiseEnabled() {
        return disguised;
    }

    @Override
    public CompoundNBT writeNBT() {
    	CompoundNBT nbt = new CompoundNBT();
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundNBT nbt) {
    	
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        lastBloodLevel = -999;
    }
    
    public boolean refreshBloodLevel(int bloodLevel) {
        boolean bloodLevelChanged = this.lastBloodLevel != bloodLevel;
        this.lastBloodLevel = bloodLevel;
        return bloodLevelChanged;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
    	PacketManager.sendToClient(new TrZombieFlagsPacket(user.getId(), this), entity);
    }
}
