package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StandEntityDamageSource extends EntityDamageSource implements IStandDamageSource, IModdedDamageSource {
    protected final IStandPower stand;
//    @Nullable
//    protected final Entity standUser;
    private float knockbackFactor = 1;
    private boolean stackKnockback = false;
    private boolean bypassInvulTicks = false;
    private boolean preventDamagingArmor = false;
    protected boolean showStandUserName;
    private int barrageHits = 0;
    private int standInvulTicks = 0;
    private boolean standCanHitSelf = false;

    public StandEntityDamageSource(String msgId, Entity damagingEntity, IStandPower stand) {
        super(msgId, damagingEntity);
        this.stand = stand;
    }
    
    StandEntityDamageSource(DamageSource damageSource, IStandPower stand) {
        super(damageSource.getMsgId(), damageSource.getDirectEntity());
        this.stand = stand;
    }
    
    @Override
    public IStandPower getStandPower() {
        return stand;
    }

    @Override
    public boolean scalesWithDifficulty() {
        if (super.scalesWithDifficulty()) {
            if (stand == null) {
               return true;
            } else {
               LivingEntity standUser = stand.getUser();
               return standUser != null && standUser instanceof LivingEntity 
                       && !(standUser instanceof PlayerEntity);
            }
        }
        return false;
    }
    
    public StandEntityDamageSource setBarrageHitsCount(int hits) {
        this.barrageHits = hits;
        return this;
    }
    
    public int getBarrageHitsCount() {
        return barrageHits;
    }

    @Override
    public StandEntityDamageSource setKnockbackReduction(float factor) {
        this.knockbackFactor = MathHelper.clamp(factor, 0, 1);
        return this;
    }

    @Override
    public float getKnockbackFactor() {
        return knockbackFactor;
    }
    
    
    @Override
    public StandEntityDamageSource setStackKnockback() {
        this.stackKnockback = true;
        return this;
    }
    
    @Override
    public boolean doesStackKnockback() {
        return stackKnockback;
    }
    

    @Override
    public StandEntityDamageSource setBypassInvulTicksInEvent() {
        this.bypassInvulTicks = true;
        return this;
    }

    @Override
    public boolean bypassInvulTicks() {
        return bypassInvulTicks;
    }
    
    
    @Override
    public StandEntityDamageSource setPreventDamagingArmor() {
        this.preventDamagingArmor = true;
        return this;
    }
    
    @Override
    public boolean preventsDamagingArmor() {
        return preventDamagingArmor;
    }
    
    
    @Override
    public boolean canHurtStands() {
        return true;
    }
    

    public StandEntityDamageSource setStandInvulTicks(int ticks) {
        this.standInvulTicks = ticks;
        return this;
    }

    @Override
    public int getStandInvulTicks() {
        return standInvulTicks;
    }
    
    @Override
    public StandEntityDamageSource setStandCanHitSelf() {
        standCanHitSelf = true;
        return this;
    }

    @Override
    public boolean standCanHitSelf() {
        return standCanHitSelf;
    }

    public StandEntityDamageSource setShowStandUserName() {
        this.showStandUserName = true;
        return this;
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(LivingEntity dead) {
        if (showStandUserName && stand != null) {
            LivingEntity standUser = stand.getUser();
            if (standUser != null) {
                return new TranslationTextComponent("death.attack." + msgId + ".stand_user", dead.getDisplayName(), standUser.getDisplayName(), entity.getDisplayName());
            }
        }
        return new TranslationTextComponent("death.attack." + msgId, dead.getDisplayName(), entity.getDisplayName());
    }

    @Override
    public String toString() {
       return "StandEntityDamageSource (" + entity + ")";
    }
}
