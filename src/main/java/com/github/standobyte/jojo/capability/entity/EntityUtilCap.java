package com.github.standobyte.jojo.capability.entity;

import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.Queue;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.IEntityGlowColor;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.Entity;

public class EntityUtilCap {
    private final Entity entity;
    
    private boolean stoppedInTime = false;
    private Queue<Runnable> runOnTimeResume = new LinkedList<>();
    
    private OptionalInt glowingColor = OptionalInt.empty();
    private int glowColorTicks = -1;
    
    public EntityUtilCap(Entity entity) {
        this.entity = entity;
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
    }
    
    public void updateEntityTimeStop(boolean stopInTime) {
        if (stopInTime) {
            stoppedInTime = true;
            entity.canUpdate(false);
        }
        else if (stoppedInTime) {
            entity.canUpdate(true);
            runOnTimeResume.forEach(Runnable::run);
            runOnTimeResume.clear();
        }
    }
    
    public boolean wasStoppedInTime() {
        return stoppedInTime;
    }
    
    // updates the Entity#canUpdate field that Forge adds, since it is saved in NBT
    void nbtSetWasStoppedInTime(boolean wasStoppedInTime) {
        if (wasStoppedInTime) {
            stoppedInTime = true;
            wasStoppedInTime = TimeStopHandler.isTimeStopped(entity.level, entity.blockPosition());
            updateEntityTimeStop(wasStoppedInTime);
        }
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
    
    
    
    public void setClGlowingColor(@Nonnull OptionalInt color, int ticks) {
        if (entity instanceof IEntityGlowColor) {
            this.glowingColor = color;
            this.glowColorTicks = ticks;
            ((IEntityGlowColor) entity).setGlowColor(glowingColor);
            if (showHp && !color.isPresent()) {
                showHp = false;
            }
        }
    }
    
    public void setClGlowingColor(@Nonnull OptionalInt color) {
        setClGlowingColor(color, -1);
    }
    
    public void resetClGlowingColor() {
        setClGlowingColor(OptionalInt.empty(), -1);
    }
    
    private void tickGlowingColor() {
        if (glowingColor.isPresent() && glowColorTicks > 0 && --glowColorTicks == 0 && entity instanceof IEntityGlowColor) {
            IEntityGlowColor colorData = (IEntityGlowColor) entity;
            if (colorData.getGlowColor() == this.glowingColor) {
                resetClGlowingColor();
            }
        }
    }
    
    private boolean showHp = false;
    public boolean showsHp() {
        return showHp;
    }
    
    public void setShowHp() {
        showHp = true;
    }
}
