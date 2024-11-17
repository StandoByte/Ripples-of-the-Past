package com.github.standobyte.jojo.util.mc.damage;

import java.util.UUID;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.KnockbackResTickPacket;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;

public class NoKnockbackOnBlocking {
    private static final UUID ONE_TICK_KB_RES_ID = UUID.fromString("94d947b4-5036-4453-a548-d1c213d8281a");
    private static final AttributeModifier ONE_TICK_KB_RES = new AttributeModifier(ONE_TICK_KB_RES_ID, 
            "No stagger when blocking a hit", 1, AttributeModifier.Operation.ADDITION);
    
    public static void setOneTickKbRes(LivingEntity entity) {
        ModifiableAttributeInstance kbRes = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null && kbRes.getModifier(ONE_TICK_KB_RES_ID) == null) {
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
        if (kbRes.hasModifier(ONE_TICK_KB_RES)) {
            kbRes.removeModifier(ONE_TICK_KB_RES);
        }
    }
}
