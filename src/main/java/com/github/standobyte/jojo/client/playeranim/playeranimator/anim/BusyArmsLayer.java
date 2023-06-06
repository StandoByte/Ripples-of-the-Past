package com.github.standobyte.jojo.client.playeranim.playeranimator.anim;

import com.github.standobyte.jojo.client.playeranim.playeranimator.PlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class BusyArmsLayer extends AnimLayerHandler {

    public BusyArmsLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    public ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }

}
