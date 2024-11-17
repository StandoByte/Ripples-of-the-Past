package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonSYOBAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXSYOBHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements HamonSYOBAnim {
    
    public KosmXSYOBHandler(ResourceLocation id) {
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
        fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }
    
}
