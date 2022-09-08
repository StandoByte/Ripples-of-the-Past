package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.EntityTimeResumeSoundPacket;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;

public class TheWorldHeavyPunch extends StandEntityHeavyAttack {

    public TheWorldHeavyPunch(Builder builder) {
        super(builder);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return new TheWorldHeavyPunchInstance(stand, target, dmgSource)
                .copyProperties(super.punchEntity(stand, target, dmgSource))
                .armorPiercing((float) stand.getAttackDamage() * 0.01F)
                .addKnockback(6)
                .impactSound(ModSounds.THE_WORLD_PUNCH_HEAVY_ENTITY);
    }

    
    
    public static class TheWorldHeavyPunchInstance extends HeavyPunchInstance {

        public TheWorldHeavyPunchInstance(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
        }

        @Override
        protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
            if (!stand.level.isClientSide() && hurt && !target.canUpdate()) {
                PacketManager.sendToClientsTrackingAndSelf(new EntityTimeResumeSoundPacket(
                        target.getBoundingBox().getCenter(), ModSounds.THE_WORLD_PUNCH_HEAVY_TS_IMPACT.get()), target);
            }
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
        }
    }
}
