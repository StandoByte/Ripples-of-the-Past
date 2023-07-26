package com.github.standobyte.jojo.client.sound.loopplayer;

import java.util.function.Predicate;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

public class HamonSparksLoopPlayer<T extends Entity> extends EntitySoundLoopPlayer<T> {
    private SparksPosition positionType = SparksPosition.CENTER;
    
    public HamonSparksLoopPlayer(T entity, Predicate<T> playWhile, float volume, float pitch) {
        super(entity, playWhile, ModSounds.HAMON_SPARK_SHORT.get(), SoundCategory.AMBIENT, volume, pitch);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (playedSoundThisTick) {
            Vector3d pos = particlePos();
            ClientUtil.createHamonSparkParticles(pos.x, pos.y, pos.z, 2);
        }
    }
    
    @Override
    protected int soundDelayTicks() {
        return 2 + RANDOM.nextInt(3);
    }
    
    public void setPosition(SparksPosition positionType) {
        if (positionType != null) {
            this.positionType = positionType;
        }
    }
    
    @Override
    protected Vector3d soundPos() {
        return positionType.soundPos(entity);
    }
    
    protected Vector3d particlePos() {
        return positionType.particlePos(entity);
    }
    
    
    public static enum SparksPosition {
        CENTER {
            @Override Vector3d soundPos(Entity entity) { return entity.getBoundingBox().getCenter(); }
            @Override Vector3d particlePos(Entity entity) { return entity.getBoundingBox().getCenter(); }
        },
        BOTTOM {
            @Override Vector3d soundPos(Entity entity) { return entity.position(); }
            @Override Vector3d particlePos(Entity entity) { return entity.position(); }
        },
        HITBOX {
            @Override Vector3d soundPos(Entity entity) { return entity.getBoundingBox().getCenter(); }
            @Override Vector3d particlePos(Entity entity) {
                return new Vector3d(entity.getRandomX(1), entity.getRandomY(), entity.getRandomZ(1));
            }
        };
        
        abstract Vector3d soundPos(Entity entity);
        abstract Vector3d particlePos(Entity entity);
    }
}
