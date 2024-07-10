package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonSYOBAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXSYOBLayer extends AnimLayerHandler implements HamonSYOBAnim {
    
    public KosmXSYOBLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    
    private static final ResourceLocation SYO_BARRAGE_START = new ResourceLocation(JojoMod.MOD_ID, "syo_barrage_start");
    private static final ResourceLocation SYO_BARRAGE_FINISHER = new ResourceLocation(JojoMod.MOD_ID, "syo_barrage_finisher");
    
    @Override
    public boolean setStartingAnim(PlayerEntity player) {
        return setAnimFromName(player, SYO_BARRAGE_START);
    }

    @Override
    public boolean setFinisherAnim(PlayerEntity player) {
        return setAnimFromName(player, SYO_BARRAGE_FINISHER);
    }

    @Override
    public void stopAnim(PlayerEntity player) {
        fadeOutAnim(player, AbstractFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }
    
}
