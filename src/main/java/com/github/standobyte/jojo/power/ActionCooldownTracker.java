package com.github.standobyte.jojo.power;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ActionCooldownPacket;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ActionCooldownTracker {
    private final Map<Action<?>, ActionCooldownTracker.Cooldown> cooldowns = new HashMap<>();
    private int tickCount;

    public ActionCooldownTracker() {}
    
    public ActionCooldownTracker(CompoundNBT nbt) {
        tickCount = 0;
        for (String key : nbt.getAllKeys()) {
            int[] array = nbt.getIntArray(key);
            if (array.length == 2) {
                Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(new ResourceLocation(key));
                if (action != null) {
                    cooldowns.put(action, new ActionCooldownTracker.Cooldown(array[0], array[1]));
                }
            }
        }
    }
    
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        for (Entry<Action<?>, ActionCooldownTracker.Cooldown> entry : cooldowns.entrySet()) {
            nbt.putIntArray(entry.getKey().getRegistryName().toString(), new int[]{ entry.getValue().startTime - tickCount, entry.getValue().endTime - tickCount });
        }
        return nbt;
    }

    public boolean isOnCooldown(Action<?> action) {
        return getCooldownTimer(action) > 0;
    }
    
    public int getCooldownTimer(Action<?> action) {
        ActionCooldownTracker.Cooldown cooldown = cooldowns.get(action);
        if (cooldown != null) {
            int cooldownValue = cooldown.endTime - tickCount;
            return cooldownValue;
        }
        return 0;
    }

    public float getCooldownPercent(Action<?> action, float partialTick) {
        ActionCooldownTracker.Cooldown cooldown = cooldowns.get(action);
        if (cooldown != null) {
            float cooldownTotal = (float) (cooldown.endTime - cooldown.startTime);
            float cooldownValue = (float) cooldown.endTime - ((float) tickCount + partialTick);
            return MathHelper.clamp(cooldownValue / cooldownTotal, 0.0F, 1.0F);
        }
        return 0.0F;
    }

    public void tick() {
        ++tickCount;
        if (!cooldowns.isEmpty()) {
            Iterator<Entry<Action<?>, ActionCooldownTracker.Cooldown>> iterator = cooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Action<?>, ActionCooldownTracker.Cooldown> entry = iterator.next();
                if (entry.getValue().endTime <= tickCount) {
                    iterator.remove();
                }
            }
        }
    }

    public void addCooldown(Action<?> action, int ticks) {
        addCooldown(action, ticks, ticks);
    }
    
    public void addCooldown(Action<?> action, int ticks, int totalTicks) {
        ticks = Math.max(ticks, 0);
        totalTicks = Math.max(totalTicks, 0);
        cooldowns.put(action, new ActionCooldownTracker.Cooldown(tickCount + ticks - totalTicks, tickCount + ticks));
    }

    public boolean removeCooldown(Action<?> action) {
        return cooldowns.remove(action) != null;
    }
    
    public void resetCooldowns() {
        cooldowns.clear();
    }
    
    public void syncWithUser(int userId, PowerClassification classification, ServerPlayerEntity user) {
        for (Entry<Action<?>, ActionCooldownTracker.Cooldown> entry : cooldowns.entrySet()) {
            Cooldown cooldown = entry.getValue();
            PacketManager.sendToClient(new ActionCooldownPacket(userId, classification, entry.getKey(), 
                    cooldown.endTime - tickCount, cooldown.endTime - cooldown.startTime), user);
        }
    }

    class Cooldown {
        private final int startTime;
        private final int endTime;

        private Cooldown(int startTime, int endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
