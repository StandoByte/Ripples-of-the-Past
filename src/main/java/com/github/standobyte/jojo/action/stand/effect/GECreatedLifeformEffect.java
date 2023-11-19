package com.github.standobyte.jojo.action.stand.effect;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.GETransformationEntity.GETransformationData;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class GECreatedLifeformEffect extends StandEffectInstance {
    private GETransformationData source = new GETransformationData();
    private ItemStack originalAsItem = ItemStack.EMPTY; // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! this resets on savefile reload
    
    public GECreatedLifeformEffect() {
        this(ModStandEffects.GE_CREATED_LIFEFORM.get());
    }
    
    public GECreatedLifeformEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public GETransformationData getSource() {
        return source;
    }
    
    public void setSource(GETransformationData source) {
        this.source.copyFrom(source, world);
    }
    
    @Nullable
    public ItemStack getItemView() {
        return originalAsItem;
    }
    
    @Override
    protected void start() {
        if (!world.isClientSide()) {
            originalAsItem = source.makeSourceItemView();
        }
    }
    
    @Override
    protected void updateTarget(World world) {
        if (!world.isClientSide()) {
            Entity target = getTarget();
            if (target != null && !target.isAlive() && target instanceof GETransformationEntity) {
                GETransformationEntity tfEntity = (GETransformationEntity) target;
                Entity tfTarget = tfEntity.getTransformationTarget();
                if (tfTarget != null) {
                    setTargetEntity(tfTarget);
                }
                else {
                    clearTarget();
                    return;
                }
            }
        }
        
        super.updateTarget(world);
    }
    
    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            LivingEntity target = getTargetLiving();
            if (target != null) {
                float staminaCost = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().getStaminaCostTicking(userPower, target);
                if (!userPower.consumeStamina(staminaCost)) {
                    remove();
                }
            }
        }
    }

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            Entity target = getTarget();
            if (target != null) {
                GETransformationEntity.turnEntityBack(target, source, user);
            }
        }
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    public void onTick() {
        if (!world.isClientSide()) {
            source.resolveNbtRead(world);
        }
        super.onTick();
    }

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        source.writeNbt(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        source.readNbt(nbt);
    }

    @Override
    public void writeAdditionalPacketData(PacketBuffer buf, boolean sendingToUser) {
        if (sendingToUser) {
            NetworkUtil.writeOptionally(buf, originalAsItem, item -> buf.writeItemStack(originalAsItem, true));
        }
    }

    @Override
    public void readAdditionalPacketData(PacketBuffer buf, boolean clientIsUser) {
        if (clientIsUser) {
            originalAsItem = NetworkUtil.readOptional(buf, () -> buf.readItem()).orElse(null);
        }
    }
}
