package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.monster.PatrollerEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.world.World;

public class GoldExperienceChooseLifeform extends StandAction {
    
    public GoldExperienceChooseLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (world.isClientSide()) {
            
        }
    }
    
    
    
    public static boolean isValidLifeform(EntityType<?> entityType) {
        Entity entity = EntityTypeToInstance.getEntityInstance(entityType);
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            
            CreatureAttribute mobType = mob.getMobType();
            if (mobType == CreatureAttribute.UNDEAD || mobType == CreatureAttribute.ILLAGER) {
                return false;
            }
            
            if (!(mob instanceof AmbientEntity || mob instanceof CreatureEntity
                    || mob instanceof FlyingEntity || mob instanceof SlimeEntity)) {
                return false;
            }
            
            if (mob instanceof INPC || mob instanceof IMerchant
                    || mob instanceof GolemEntity || mob instanceof PatrollerEntity) {
                return false;
            }
            
            if (getVolume(entityType) > 4) {
                return false;
            }
            
            return true;
        }
        return false;
    }
    
    public static float getVolume(EntityType<?> mobType) {
        Entity entity = EntityTypeToInstance.getEntityInstance(mobType);
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        return width * width * height;
    }
}
