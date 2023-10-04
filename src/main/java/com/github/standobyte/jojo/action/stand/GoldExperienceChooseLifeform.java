package com.github.standobyte.jojo.action.stand;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.GhastEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class GoldExperienceChooseLifeform extends StandAction {
    public static EntityType<?> chosenTypeTmp = null;
    public static Set<EntityType<?>> hiddenEntriesTmp = new HashSet<>();
    
    public GoldExperienceChooseLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    
    public static boolean isValidLifeform(EntityType<?> entityType, World world) {
        Entity entity = EntityTypeToInstance.getEntityInstance(entityType, world);
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            
            CreatureAttribute mobType = mob.getMobType();
            if (mobType == CreatureAttribute.UNDEAD || mobType == CreatureAttribute.ILLAGER) {
                return false;
            }
            
            if (entityType == EntityType.TRADER_LLAMA) {
                return false;
            }
            
            if (!(mob instanceof AmbientEntity || mob instanceof CreatureEntity
                    || mob instanceof FlyingEntity || mob instanceof SlimeEntity)) {
                return false;
            }
            
            if (mob instanceof INPC || mob instanceof IMerchant
                    || mob instanceof GolemEntity || mob instanceof PatrollerEntity
                    || mob instanceof GhastEntity || mob instanceof BlazeEntity || mob instanceof VexEntity
                    || mob instanceof CreeperEntity || mob instanceof EndermanEntity
                    || mob instanceof AbstractPiglinEntity || mob instanceof GuardianEntity) {
                return false;
            }
            
            if (GoldExperienceCreateLifeform.getVolume(entity) >= 7.5) { // no too large mobs
                return false;
            }
            
            return true;
        }
        return false;
    }
    
    public static void unlockAllEntityTypes(PlayerEntity player) {
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            ForgeRegistries.ENTITIES.getValues()
            .stream().filter(type -> isValidLifeform(type, player.level))
            .forEach(entityType -> {
                cap.addMetEntityType(entityType);
            });
        });
    }
}
