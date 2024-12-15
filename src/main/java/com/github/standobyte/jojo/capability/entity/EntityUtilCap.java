package com.github.standobyte.jojo.capability.entity;

import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.Queue;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.IEntityGlowColor;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;

public class EntityUtilCap {
    private final Entity entity;
    private final MobEntity asMob;
    private Boolean prevCanUpdate;
    private Boolean prevNoAi;
    
    private KnockbackCollisionImpact kbImpact;
    
    private boolean stoppedInTime = false;
    private Queue<Runnable> runOnTimeResume = new LinkedList<>();
    
    private OptionalInt glowingColor = OptionalInt.empty();
    private int glowColorTicks = -1;
    
    public EntityUtilCap(Entity entity) {
        this.entity = entity;
        this.asMob = entity instanceof MobEntity ? (MobEntity) entity : null;
        this.kbImpact = new KnockbackCollisionImpact(entity);
    }
    
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!entity.canUpdate() && wasStoppedInTime()) {
            nbt.putBoolean("StoppedInTime", true);
            if (prevCanUpdate != null) nbt.putBoolean("PrevCanUpdate", prevCanUpdate);
            if (prevNoAi != null) nbt.putBoolean("PrevNoAi", prevNoAi);
        }
        nbt.put("KbImpact", kbImpact.serializeNBT());
        return nbt;
    }
    
    public void deserializeNBT(CompoundNBT nbt) {
        stoppedInTime = nbt.getBoolean("StoppedInTime");
        if (stoppedInTime) {
            stoppedInTime = TimeStopHandler.isTimeStopped(entity.level, entity.blockPosition());
            prevCanUpdate = MCUtil.getNbtElement(nbt, "PrevCanUpdate", ByteNBT.class).map(byteNbt -> byteNbt.getAsByte() != 0).orElse(null);
            prevNoAi = MCUtil.getNbtElement(nbt, "PrevNoAi", ByteNBT.class).map(byteNbt -> byteNbt.getAsByte() != 0).orElse(null);
            // updates the Entity#canUpdate field that Forge adds, since it is saved in NBT
            updateEntityTimeStop(stoppedInTime);
        }
        MCUtil.nbtGetCompoundOptional(nbt, "KbImpact").ifPresent(kbImpact::deserializeNBT);
    }
    
    /**
     *  currently is not called on server side, 
     *  uncomment in
     *  {@link GameplayEventHandler.onWorldTick(WorldTickEvent)}
     *  if that's needed
     */
    public void tick() {
        if (entity.level.isClientSide()) {
            tickGlowingColor();
        }
        else {
            kbImpact.tick();
        }
    }
    
    public void updateEntityTimeStop(boolean stopInTime) {
        if (stopInTime) {
            stoppedInTime = true;
            
            prevCanUpdate = entity.canUpdate();
            entity.canUpdate(false);
            
            if (asMob != null) {
                prevNoAi = asMob.isNoAi();
                asMob.setNoAi(true);
            }
        }
        else if (stoppedInTime) {
            if (prevCanUpdate != null && prevCanUpdate) {
                entity.canUpdate(true);
            }
            prevCanUpdate = null;
            
            if (asMob != null) {
                if (prevNoAi != null && !prevNoAi) {
                    asMob.setNoAi(false);
                }
                prevNoAi = null;
            }
            
            runOnTimeResume.forEach(Runnable::run);
            runOnTimeResume.clear();
        }
    }
    
    public boolean wasStoppedInTime() {
        return stoppedInTime;
    }
    
    
    
    public static void queueOnTimeResume(Entity entity, Runnable action) {
        GeneralUtil.ifPresentOrElse(entity.getCapability(EntityUtilCapProvider.CAPABILITY).resolve(), 
                cap -> {
                    if (cap.stoppedInTime) {
                        cap.runOnTimeResume.add(action);
                    }
                    else if (entity.canUpdate()) {
                        action.run();
                    }
                }, 
                action);
    }
    
    
    public final KnockbackCollisionImpact getKbImpact() {
        return kbImpact;
    }
    
    
    public void setClGlowingColor(@Nonnull OptionalInt color, int ticks) {
        if (entity instanceof IEntityGlowColor) {
            this.glowingColor = color;
            this.glowColorTicks = ticks;
            ((IEntityGlowColor) entity).setGlowColor(glowingColor);
        }
    }
    
    public void setClGlowingColor(@Nonnull OptionalInt color) {
        setClGlowingColor(color, -1);
    }
    
    public void resetClGlowingColor() {
        setClGlowingColor(OptionalInt.empty(), -1);
    }
    
    public void refreshClEntityGlowing() {
        if (entity instanceof IEntityGlowColor) {
            IEntityGlowColor colorData = (IEntityGlowColor) entity;
            colorData.setGlowColor(glowingColor);
        }
    }
    
    private void tickGlowingColor() {
        if (glowingColor.isPresent() && glowColorTicks > 0 && --glowColorTicks == 0 && entity instanceof IEntityGlowColor) {
            IEntityGlowColor colorData = (IEntityGlowColor) entity;
            if (colorData.getGlowColor() == this.glowingColor) {
                colorData.setGlowColor(OptionalInt.empty());
            }
        }
    }
}
