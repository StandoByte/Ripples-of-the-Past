package com.github.standobyte.jojo.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mc.CustomTargetIconMap;
import com.github.standobyte.jojo.util.mc.CustomTargetIconMap.IMapDataMixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

@Mixin(MapData.class)
public abstract class MapDataMixin implements IMapDataMixin {
    @Shadow public int x;
    @Shadow public int z;
    @Shadow public RegistryKey<World> dimension;
    @Shadow public byte scale;
    @Shadow @Final public Map<String, MapDecoration> decorations;
    
    @Inject(method = "tickCarriedBy", at = @At("TAIL"))
    public void jojoAddMapTargetDecoration(PlayerEntity player, ItemStack mapStack, CallbackInfo ci) {
        CustomTargetIconMap.mixinMakeIconDecoration(player, mapStack, this);
    }

    @Override
    public Map<String, MapDecoration> decorations() {
        return decorations;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int z() {
        return z;
    }

    @Override
    public byte scale() {
        return scale;
    }

    @Override
    public RegistryKey<World> dimension() {
        return dimension;
    }
}
