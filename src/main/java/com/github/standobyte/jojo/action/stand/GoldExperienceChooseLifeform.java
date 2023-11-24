package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil.ResLocSet;

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

            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! tmp, will be reserved for specific items/blocks
            if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
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
            
            if (mob.getMaxHealth() > 60) {
                return false;
            }
            
            if (DISABLE_SUMMON_MANUALLY.contains(entity.getType().getRegistryName())) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    private static final ResLocSet DISABLE_SUMMON_MANUALLY = new ResLocSet()
            .add("twilightforest", 
                    "quest_ram",
                    "wraith",
                    "redcap",
                    "redcap_sapper",
                    "death_tome",
                    "minoshroom",
                    "minotaur",
                    "maze_slime",
                    "mist_wolf",
                    "tower_golem",
                    "blockchain_goblin",
                    "goblin_knight_upper",
                    "goblin_knight_lower",
                    "knight_phantom",
                    "yeti",
                    "snow_guardian",
                    "stable_ice_core",
                    "unstable_ice_core",
                    "snow_queen",
                    "ice_crystal",
                    "troll",
                    "adherent",
                    "roving_cube",
                    "plateau_boss"
                    )
            .add("alexsmobs",
                    "centipede_head",
                    "guster",
                    "enderiophage",
                    "mimicube"
                    );
    
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
