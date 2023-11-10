package com.github.standobyte.jojo.util.mc.damage;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class ModdedDamageSourceWrapper extends DamageSource implements IModdedDamageSource {
    private final DamageSource dmgSource;
    private float knockbackFactor = 1;
    private boolean stackKnockback = false;
    private boolean bypassInvulTicks = false;
    private boolean preventDamagingArmor = false;
    protected boolean showStandUserName;
    protected boolean canHurtStands;

    public ModdedDamageSourceWrapper(DamageSource dmgSource) {
        super(dmgSource.msgId);
        this.dmgSource = dmgSource;
    }
    
    
    
    @Override
    public ModdedDamageSourceWrapper setKnockbackReduction(float factor) {
        this.knockbackFactor = MathHelper.clamp(factor, 0, 1);
        return this;
    }
    
    @Override
    public float getKnockbackFactor() {
        return knockbackFactor;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setStackKnockback() {
        this.stackKnockback = true;
        return this;
    }
    
    @Override
    public boolean doesStackKnockback() {
        return stackKnockback;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setBypassInvulTicksInEvent() {
        this.bypassInvulTicks = true;
        return this;
    }
    
    @Override
    public boolean bypassInvulTicks() {
        return bypassInvulTicks;
    }
    
    
    @Override
    public ModdedDamageSourceWrapper setPreventDamagingArmor() {
        this.preventDamagingArmor = true;
        return this;
    }
    
    @Override
    public boolean preventsDamagingArmor() {
        return preventDamagingArmor;
    }
    
    
    public ModdedDamageSourceWrapper setCanHurtStands() {
        this.canHurtStands = true;
        return this;
    }
    
    @Override
    public boolean canHurtStands() {
        return canHurtStands;
    }
    
    
    
    // redirect all the vanilla methods (except Thorns armor damage from EntityDamageSource) to the wrapped DamageSoruce instance
    public String toString() {
        return "RotP wrapper of " + dmgSource.toString();
    }
    
    public boolean isProjectile() {
        return dmgSource.isProjectile();
    }
    
    public DamageSource setProjectile() {
        dmgSource.setProjectile();
        return this;
    }
    
    public boolean isExplosion() {
        return dmgSource.isExplosion();
    }
    
    public DamageSource setExplosion() {
        dmgSource.setExplosion();
        return this;
    }
    
    public boolean isBypassArmor() {
        return dmgSource.isBypassArmor();
    }
    
    public float getFoodExhaustion() {
        return dmgSource.getFoodExhaustion();
    }
    
    public boolean isBypassInvul() {
        return dmgSource.isBypassInvul();
    }
    
    public boolean isBypassMagic() {
        return dmgSource.isBypassMagic();
    }
    
    @Nullable
    public Entity getDirectEntity() {
        return dmgSource.getDirectEntity();
    }
    
    @Nullable
    public Entity getEntity() {
        return dmgSource.getEntity();
    }
    
    public DamageSource bypassArmor() {
        dmgSource.bypassArmor();
        return this;
    }
    
    public DamageSource bypassInvul() {
        dmgSource.bypassInvul();
        return this;
    }
    
    public DamageSource bypassMagic() {
        dmgSource.bypassMagic();
        return this;
    }
    
    public DamageSource setIsFire() {
        dmgSource.setIsFire();
        return this;
    }
    
    public ITextComponent getLocalizedDeathMessage(LivingEntity pLivingEntity) {
        return dmgSource.getLocalizedDeathMessage(pLivingEntity);
    }
    
    public boolean isFire() {
        return dmgSource.isFire();
    }
    
    public String getMsgId() {
        return dmgSource.getMsgId();
    }
    
    public DamageSource setScalesWithDifficulty() {
        dmgSource.setScalesWithDifficulty();
        return this;
    }
    
    public boolean scalesWithDifficulty() {
        return dmgSource.scalesWithDifficulty();
    }
    
    public boolean isMagic() {
        return dmgSource.isMagic();
    }
    
    public DamageSource setMagic() {
        dmgSource.setMagic();
        return this;
    }
    
    public boolean isCreativePlayer() {
        return dmgSource.isCreativePlayer();
    }
    
    @Nullable
    public Vector3d getSourcePosition() {
        return dmgSource.getSourcePosition();
    }
}
