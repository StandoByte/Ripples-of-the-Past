package com.github.standobyte.jojo.capability.entity.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public interface PlayerMixinExtension {
    void toNBT(CompoundNBT forgeCapNbt);
    void fromNBT(CompoundNBT forgeCapNbt);
    void syncToClient(ServerPlayerEntity thisAsPlayer);
    void syncToTracking(ServerPlayerEntity tracking);
}
