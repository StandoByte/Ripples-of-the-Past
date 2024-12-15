package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXArmsRotationModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class KosmXPillarmanEvasionLayer extends AnimLayerHandler<ModifierLayer<IAnimation>> implements BasicToggleAnim {
	private static final float SPEED = 2F;

    public KosmXPillarmanEvasionLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new SpeedModifier(SPEED));
    }
    
    
    private static final ResourceLocation ANIM = new ResourceLocation(JojoMod.MOD_ID, "evasion");
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        enabled &= !player.isPassenger();
        if (enabled) {
            return setAnimFromName((AbstractClientPlayerEntity) player, ANIM);
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
        }
    }

}
