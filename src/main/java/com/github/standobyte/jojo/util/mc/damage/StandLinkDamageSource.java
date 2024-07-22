package com.github.standobyte.jojo.util.mc.damage;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class StandLinkDamageSource extends DamageSource {
//    private final Entity standEntity;
    private final DamageSource actualSource;

    public StandLinkDamageSource(Entity standEntity, DamageSource actualSource) {
        super("healthLink");
//        this.standEntity = standEntity;
        this.actualSource = actualSource;
        bypassArmor();
        bypassMagic();
    }

//    @Override
//    public boolean isBypassArmor() {
//        return true;
//    }
//
//    @Override
//    public boolean isBypassMagic() {
//        return true;
//    }
//
//
//    
//    @Override
//    public boolean isProjectile() {
//        return actualSource.isProjectile();
//    }
//
//    @Override
//    public boolean isExplosion() {
//        return actualSource.isExplosion();
//    }
//
//    @Override
//    public float getFoodExhaustion() {
//        return actualSource.getFoodExhaustion();
//    }
//
//    @Override
//    public boolean isBypassInvul() {
//        return actualSource.isBypassInvul();
//    }
//
//    @Override
//    public boolean isFire() {
//        return actualSource.isFire();
//    }
//
//    @Override
//    public boolean scalesWithDifficulty() {
//        return actualSource.scalesWithDifficulty();
//    }
//
//    @Override
//    public boolean isMagic() {
//        return actualSource.isMagic();
//    }
//
//    @Override
//    public boolean isCreativePlayer() {
//        return actualSource.isCreativePlayer();
//    }



    @Override
    public String toString() {
       return "DamageSource (" + msgId + " (" + actualSource.getMsgId() + "))";
    }

    @Nullable
    public Entity getDirectEntity() {
        return actualSource.getDirectEntity();
    }

    @Nullable
    public Entity getEntity() {
        return actualSource.getEntity();
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity dead) {
        return actualSource.getLocalizedDeathMessage(dead);
    }

    @Override
    public String getMsgId() {
        return actualSource.getMsgId();
    }
    
    public DamageSource getOriginalDamageSource() {
        return actualSource;
    }

    @Override
    @Nullable
    public Vector3d getSourcePosition() {
        return null;
    }
}
