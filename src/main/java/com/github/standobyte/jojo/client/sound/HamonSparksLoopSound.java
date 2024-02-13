package com.github.standobyte.jojo.client.sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.client.ClientTicking;
import com.github.standobyte.jojo.client.ClientTicking.ITicking;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

public class HamonSparksLoopSound implements ITicking {
    private static final Random RANDOM = new Random();
    private static final HamonSparksLoopSound instance = new HamonSparksLoopSound();
    private final Map<Entity, MutableInt> soundDelays = new HashMap<>();
    private final Map<EntityType<?>, MutableInt> soundLimitPerType = new HashMap<>();
    
    private HamonSparksLoopSound() {
        ClientTicking.addTicking(this);
    }
    
    public static boolean playSparkSound(Entity entity, Vector3d soundPos, float volume, float reducedFrequency) {
        if (RANDOM.nextFloat() < 1F / getDelay() * reducedFrequency) {
            return playSparkSound(entity, soundPos, volume);
        }
        
        return false;
    }
    
    public static boolean playSparkSound(Entity entity, Vector3d soundPos, float volume) {
        return playSparkSound(entity, soundPos, volume, false);
    }
    
    public static boolean playSparkSound(Entity entity, Vector3d soundPos, float volume, boolean limitForEntityType) {
        double distSqr = soundPos.distanceToSqr(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        if (distSqr > 256) {
            return false;
        }
        limitForEntityType = limitForEntityType && distSqr > 8;
        
        MutableInt typeDelay = null;
        if (limitForEntityType) {
            typeDelay = instance.soundLimitPerType.computeIfAbsent(entity.getType(), type -> new MutableInt(0));
            if (typeDelay.getValue() > 0) {
                return false;
            }
        }
        
        MutableInt curDelay = instance.soundDelays.computeIfAbsent(entity, e -> new MutableInt(0));
        if (curDelay.getValue() > 0) {
            return false;
        }
        
        entity.level.playLocalSound(soundPos.x, soundPos.y, soundPos.z, 
                ModSounds.HAMON_SPARK_SHORT.get(), SoundCategory.AMBIENT, volume, 1.0F, false);
        
        curDelay.setValue(getDelay());
        if (limitForEntityType) {
            typeDelay.setValue(1);
        }
        
        return true;
    }
    
    private static int getDelay() {
        return 3 + RANDOM.nextInt(3);
    }

    @Override
    public void tick() {
        if (!soundDelays.isEmpty()) {
            Iterator<Map.Entry<Entity, MutableInt>> iter = soundDelays.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Entity, MutableInt> entry = iter.next();
                if (!entry.getKey().isAlive()) {
                    iter.remove();
                }
                else {
                    MutableInt delay = entry.getValue();
                    if (delay.getValue() > 0) {
                        delay.decrement();
                    }
                }
            }
        }

        if (!soundLimitPerType.isEmpty()) {
            Iterator<Map.Entry<EntityType<?>, MutableInt>> iter = soundLimitPerType.entrySet().iterator();
            while (iter.hasNext()) {
                MutableInt delay = iter.next().getValue();
                if (delay.getValue() > 0) {
                    delay.decrement();
                }
            }
        }
    }

}
