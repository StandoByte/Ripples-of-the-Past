package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.item.TommyGunItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class TommyGunLoopSound extends TickableSound {
    protected final LivingEntity entity;
    protected final ItemStack tommyGunItem;
    
    public TommyGunLoopSound(SoundEvent sound, SoundCategory category, float volume, LivingEntity entity, ItemStack tommyGunItem) {
        super(sound, category);
        this.entity = entity;
        this.volume = volume;
        this.pitch = 1;
        this.looping = true;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.tommyGunItem = tommyGunItem;
    }

    @Override
    public boolean canPlaySound() {
        return !getEntity().isSilent();
    }
    
    public LivingEntity getEntity() {
        return entity;
    }
    
    @Override
    public void tick() {
        LivingEntity entity = getEntity();
        ItemStack usedItem = entity.getUseItem();
        if (!(Minecraft.getInstance().level == entity.level && entity.isAlive()
                && !usedItem.isEmpty() && usedItem.getItem() == tommyGunItem.getItem() && TommyGunItem.getAmmo(usedItem) > 0)) {
            stop();
        }
        else {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        }
    }
}
