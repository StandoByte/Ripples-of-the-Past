package com.github.standobyte.jojo.codesnippets;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.ModdedDamageSourceWrapper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// Uncomment/edit all the stuff below to make players be able to hit Stands on some condition

//@EventBusSubscriber(modid = AddonMain.MOD_ID)
@SuppressWarnings("unused")
public class LetEntitiesHitStands {
    
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public static void onAttackStart(LivingAttackEvent event) {
//        DamageSource damageSource = event.getSource();
//        LivingEntity target = event.getEntityLiving();
//        if (target instanceof StandEntity && !((StandEntity) target).canTakeDamageFrom(damageSource)) {
//
//            if (damageSource.getDirectEntity() instanceof LivingEntity) {
//                LivingEntity attacker = (LivingEntity) damageSource.getDirectEntity();
//                
//                boolean canHitStands = IStandPower.getStandPowerOptional(attacker).map(stand -> {
//                    return stand.getType() == StandsInit. /*... your Stand*/
//                }).orElse(false);
//
//                if (canHitStands) {
//                    event.setCanceled(true);
//                    target.hurt(new ModdedDamageSourceWrapper(damageSource).setCanHurtStands(), event.getAmount());
//                }
//            }
//        }
//    }
}
