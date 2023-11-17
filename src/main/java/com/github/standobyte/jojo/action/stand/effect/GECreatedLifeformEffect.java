package com.github.standobyte.jojo.action.stand.effect;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class GECreatedLifeformEffect extends StandEffectInstance {
    private CompoundNBT originalEntityNbt = null;
    private Entity originalEntity;
    private ItemStack originalAsItem;
    
    public GECreatedLifeformEffect() {
        this(ModStandEffects.GE_CREATED_LIFEFORM.get());
    }
    
    public GECreatedLifeformEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public GECreatedLifeformEffect withOriginalEntity(Entity originalEntity) {
        this.originalEntity = originalEntity;
        if (originalEntity instanceof ItemEntity) {
            originalAsItem = ((ItemEntity) originalEntity).getItem().copy();
        }
        else {
            originalAsItem = null;
        }
        return this;
    }
    
    @Nullable
    public ItemStack getItemView() {
        return originalAsItem;
    }
    
    @Override
    protected void start() {}

    @Override
    protected void tickTarget(LivingEntity target) {}
    
    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            LivingEntity target = getTarget();
            if (target == null) {
                remove();
                return;
            }
            else {
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
                if (originalEntity != null) {
                    GETransformationEntity.turnEntityBack(target, originalEntity, user);
                }
                else {
                    target.remove();
                }
            }
        }
    }
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    public void onTick() {
        if (originalEntityNbt != null) {
            withOriginalEntity(EntityType.create(originalEntityNbt, world).orElse(null));
            originalEntityNbt = null;
        }
        super.onTick();
    }

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        if (originalEntity != null) {
            CompoundNBT entityNbt = originalEntity.serializeNBT();
            nbt.put("OrigEntity", entityNbt);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.contains("OrigEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            originalEntityNbt = nbt.getCompound("OrigEntity");
        }
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
