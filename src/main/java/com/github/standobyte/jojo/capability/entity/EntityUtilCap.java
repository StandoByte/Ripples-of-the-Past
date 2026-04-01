package com.github.standobyte.jojo.capability.entity;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.GoldExperienceLifeDetector;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.IEntityGlowColor;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.subsystems.timestop.EntityTimeStop;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class EntityUtilCap {
    private final Entity entity;
    private final MobEntity asMob;
    
    private KnockbackCollisionImpact kbImpact;
    
    private OptionalInt glowingColor = OptionalInt.empty();
    private int glowColorTicks = -1;
    
    @Nullable private MrPresidentWorldData.ChunkSectionPos mrPresidentRoomPos;
    
    public EntityUtilCap(Entity entity) {
        this.entity = entity;
        this.asMob = entity instanceof MobEntity ? (MobEntity) entity : null;
        this.kbImpact = new KnockbackCollisionImpact(entity);
    }
    
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("KbImpact", kbImpact.serializeNBT());
        return nbt;
    }
    
    public void deserializeNBT(CompoundNBT nbt) {
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
        
        tickMrPresidentOutOfBounds();
    }

    @Deprecated
    public void updateEntityTimeStop(boolean stopInTime) {
        ((EntityTimeStop) entity).jojo_ripples$setStoppedInTime(stopInTime);
    }

    @Deprecated
    public boolean wasStoppedInTime() {
        return ((EntityTimeStop) entity).jojo_ripples$isStoppedInTime();
    }
    
    

    @Deprecated
    public static void queueOnTimeResume(Entity entity, Runnable action) {
        ((EntityTimeStop) entity).jojo_ripples$queueOnTimeResume(action);
    }
    
    
    public final KnockbackCollisionImpact getKbImpact() {
        return kbImpact;
    }
    
    
    public void setClGlowingColor(@Nonnull OptionalInt color, int ticks) {
        setClGlowingColor(color, ticks, null);
    }
    
    public void setClGlowingColor(@Nonnull OptionalInt color, int ticks, @Nullable Object additionalCtx) {
        if (entity instanceof IEntityGlowColor) {
            this.glowingColor = color;
            this.glowColorTicks = ticks;
            ((IEntityGlowColor) entity).setGlowColor(glowingColor);
            setShowHpGEDetector(color.isPresent() && additionalCtx == GoldExperienceLifeDetector.GE_DETECTOR_CTX);
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
                resetClGlowingColor();
            }
        }
    }
    
    private void setShowHpGEDetector(boolean value) {
        if (entity.level.isClientSide()) {
            if (value) {
                ClientEventHandler.getInstance().addGEDetectedEntity(entity);
            }
            else {
                ClientEventHandler.getInstance().removeGEDetectedEntity(entity);
            }
        }
    }
    
    
    private void tickMrPresidentOutOfBounds() {
        if (entity.level.isClientSide()) return;
        if (entity.level.dimension() != ModDimensions.MR_PRESIDENT
                || entity.isSpectator()
                || (entity instanceof PlayerEntity) && ((PlayerEntity) entity).isCreative()) {
            mrPresidentRoomPos = null;
            return;
        }
        if (mrPresidentRoomPos == null) {
            mrPresidentRoomPos = new MrPresidentWorldData.ChunkSectionPos(entity.blockPosition());
        }
        else if (!mrPresidentRoomPos.isPosInsideSection(entity.blockPosition())) {
            BlockPos posMoveTo = mrPresidentRoomPos.blockPosition(8, 6, 8);
            Vector3d pos = Vector3d.atBottomCenterOf(posMoveTo);
            entity.moveTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
            if (entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) entity).connection.send(
                        new SPlayerPositionLookPacket(pos.x, pos.y, pos.z, 
                                0, 0, Util.make(EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), set -> {
                                    set.add(SPlayerPositionLookPacket.Flags.X_ROT);
                                    set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
                                }), -1));
            }
        }
    }
}
