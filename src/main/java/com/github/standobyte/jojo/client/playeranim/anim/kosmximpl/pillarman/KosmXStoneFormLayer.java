package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXStoneFormLayer extends AnimLayerHandler<ModifierLayer<IAnimation>> implements BasicToggleAnim {

    public KosmXStoneFormLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null/*, new KosmXHeadRotationModifier(), new KosmXArmsRotationModifier(player, HandSide.LEFT, HandSide.RIGHT)*/);
    }
    
    
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        enabled &= !player.isPassenger();
        if (enabled) {
            return setAnimFromName((AbstractClientPlayerEntity) player, getAnimPath(player));
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
        }
    }
    
    private ResourceLocation getAnimPath(PlayerEntity player) { // TODO sync pillar man pose variant with all clients
        return POSES[player.getRandom().nextInt(POSES.length)];
    }
    
    private static final ResourceLocation[] POSES = {
            new ResourceLocation(JojoMod.MOD_ID, "stone_form_1"),
            new ResourceLocation(JojoMod.MOD_ID, "stone_form_2"),
            new ResourceLocation(JojoMod.MOD_ID, "stone_form_3")
    };

}
