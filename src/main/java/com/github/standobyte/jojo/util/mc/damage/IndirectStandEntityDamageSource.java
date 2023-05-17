package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.entity.damaging.DamagingEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IndirectStandEntityDamageSource extends StandEntityDamageSource {
    private final Entity owner;
    private ITextComponent standName = null;
    
    public IndirectStandEntityDamageSource(String msgId, DamagingEntity damagingEntity, LivingEntity owner) {
        super(msgId, damagingEntity, IStandPower.getStandPowerOptional(StandUtil.getStandUser(owner)).orElse(null));
        this.standName = null;
        this.owner = owner;
    }
    
    public IndirectStandEntityDamageSource(String msgId, Entity damagingEntity, Entity owner, IStandPower stand) {
        super(msgId, damagingEntity, stand);
        this.owner = owner;
    }
    
    IndirectStandEntityDamageSource(DamageSource damageSource, IStandPower stand) {
        super(damageSource, stand);
        this.owner = damageSource.getEntity();
    }
    
    public IndirectStandEntityDamageSource setStandName(ITextComponent standName) {
        this.standName = standName;
        return this;
    }
    
    @Override
    public Entity getDirectEntity() {
        return entity;
    }
    
    @Override
    public Entity getEntity() {
        return owner;
    }
    
    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity dead) {
        ITextComponent cause = owner != null ? owner.getDisplayName() : standName != null ? standName : entity.getDisplayName();
        if (showStandUserName && stand != null) {
            LivingEntity standUser = stand.getUser();
            if (standUser != null) {
                return new TranslationTextComponent("death.attack." + msgId + ".stand_user", dead.getDisplayName(), standUser.getDisplayName(), cause);
            }
        }
        return new TranslationTextComponent("death.attack." + msgId, dead.getDisplayName(), cause);
    }
    
}
