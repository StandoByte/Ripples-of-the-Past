package com.github.standobyte.jojo.action.stand;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.ChooseLifeformScreen;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil.ResLocSet;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.potion.StandVirusEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntityTypeToInstance;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GoldExperienceChooseLifeform extends StandAction {
    
    public GoldExperienceChooseLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public boolean clientOnly() {
        ChooseLifeformScreen.openWindowOnClick();
        return false;
    }
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide()) {
            user.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getMetMobs).ifPresent(
                    metMobs -> metMobs.updateNativeMobs((ServerWorld) world, user, true));
        }
    }
    
    @Override
    public void onProgressionSkipped(IStandPower power) {
        super.onProgressionSkipped(power);
        LivingEntity user = power.getUser();
        if (user instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) user;
            GoldExperienceChooseLifeform.unlockAllEntityTypes(player);
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(data -> {
                PacketManager.sendToClient(data.getGELifeformsUIState().makePacket(), player);
            });
        }
    }
    

    public static void registerExtraEntitySubtypes() {
        EntitySubtype.registerSubtype(
                ModEntityTypes.COCO_JUMBO_TURTLE.get(), 
                "stand", 
                entity -> StandVirusEffect.getRandomStandGiver(entity).ifPresent(standGiver -> standGiver.giveStand(entity)), 
                entity -> entity.getStandPower().getType() == ModStandsInit.MR_PRESIDENT.get());
    }
    
    public static boolean isValidLifeform(EntitySubtype<?> entitySubtype, World world) {
        Entity entity = EntityTypeToInstance.getEntityInstance(entitySubtype, world);
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            EntityType<?> entityType = entitySubtype.vanillaType;
            
            CreatureAttribute mobType = mob.getMobType();
            if (
                    mobType == CreatureAttribute.UNDEAD || 
                    mobType == CreatureAttribute.ILLAGER ||
                    entityType == EntityType.TRADER_LLAMA ||
                    !entityType.canSummon()) {
                return false;
            }
            
//            if (world.getDifficulty() == Difficulty.PEACEFUL && mob.shouldDespawnInPeaceful()) {
//                return false;
//            }
            
            if (entityType == ModEntityTypes.COCO_JUMBO_TURTLE.get() && !IStandPower.getStandPowerOptional(mob).map(IStandPower::hasPower).orElse(false)) {
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
            
            if (GoldExperienceCreateLifeform.getVolume(entity) >= 7.5 || mob.getMaxHealth() > 60) {
                return false;
            }
            
            ResourceLocation typeId = entity.getType().getRegistryName();
            if (DISABLE_SUMMON_MANUALLY_NAMESPACES.contains(typeId.getNamespace()) || DISABLE_SUMMON_MANUALLY.contains(typeId)) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    private static final Set<String> DISABLE_SUMMON_MANUALLY_NAMESPACES = Util.make(new HashSet<>(), set -> {
        set.add("rotp_zbc");
        set.add("rotp_harvest");
    });
    
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
                    )
            .add("rotp_zkq",
                    "sheer_heart");
    
    public static void unlockAllEntityTypes(PlayerEntity player) {
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            EntitySubtype.values()
            .filter(type -> isValidLifeform(type, player.level))
            .forEach(entityType -> {
                cap.addMetEntityType(entityType);
            });
        });
    }
}
