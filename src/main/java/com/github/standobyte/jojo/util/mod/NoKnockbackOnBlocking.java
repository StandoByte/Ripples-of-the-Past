package com.github.standobyte.jojo.util.mod;

import java.util.UUID;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.KnockbackResTickPacket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

public class NoKnockbackOnBlocking {
    public static boolean clCancelHurtBob = false;
    public static boolean clDidHurtWithNoBob = false;

    
    public static final AttributeModifier ONE_TICK_KB_RES = new AttributeModifier(UUID.fromString("94d947b4-5036-4453-a548-d1c213d8281a"), 
            "No stagger when blocking a hit", 1, AttributeModifier.Operation.ADDITION);
    
    public static void setOneTickKbRes(LivingEntity entity) {
        ModifiableAttributeInstance kbRes = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null && !kbRes.hasModifier(ONE_TICK_KB_RES)) {
            kbRes.addTransientModifier(ONE_TICK_KB_RES);
        }
        if (!entity.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new KnockbackResTickPacket(entity.getId()), entity);
            if (entity instanceof StandEntity) {
                LivingEntity user = ((StandEntity) entity).getUser();
                if (user != null && user != entity) {
                    setOneTickKbRes(user);
                }
            }
        }
    }
    
    public static boolean hasOneTickKbRes(LivingEntity entity) {
        return entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).hasModifier(ONE_TICK_KB_RES);
    }
    
    public static boolean cancelHurtSound(LivingEntity entity) {
        return !(entity instanceof StandEntity) && hasOneTickKbRes(entity);
    }
    
    public static void tickAttribute(LivingEntity entity) {
        ModifiableAttributeInstance kbRes = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes.hasModifier(NoKnockbackOnBlocking.ONE_TICK_KB_RES)) {
            kbRes.removeModifier(NoKnockbackOnBlocking.ONE_TICK_KB_RES);
        }
    }
    
    
    public static void onClientPlayerDamage(LivingEntity player) {
        if (hasOneTickKbRes(player)) {
            if (clCancelHurtBob) {
                if (!clDidHurtWithNoBob) {
                    clDidHurtWithNoBob = true;
                }
                else {
                    clDidHurtWithNoBob = false;
                    clCancelHurtBob = false;
                }
            }
        }
        else {
            clCancelHurtBob = false;
        }
    }
}
