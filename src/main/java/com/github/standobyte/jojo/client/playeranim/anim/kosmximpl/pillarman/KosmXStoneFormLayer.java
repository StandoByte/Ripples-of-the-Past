package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.pillarman;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXArmsRotationModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanPowerType;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class KosmXStoneFormLayer extends AnimLayerHandler implements BasicToggleAnim {

    public KosmXStoneFormLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new KosmXHeadRotationModifier(), new KosmXArmsRotationModifier(player, HandSide.LEFT, HandSide.RIGHT));
    }
    
    
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        enabled &= !player.isPassenger();
        if (enabled) {
            return setAnimFromName((AbstractClientPlayerEntity) player, getAnimPath(player));
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, AbstractFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
        }
    }
    
    private ResourceLocation getAnimPath(PlayerEntity player) {
    	double randomPose = Math.random();
    	if (randomPose < 0.33) {
    		return POSE_1;
    	} else if (randomPose > 0.33 && randomPose < 0.66) {
    		return POSE_2;
    	} else {
    		return POSE_3;
    	}
    }
    
    private static final ResourceLocation POSE_1 = new ResourceLocation(JojoMod.MOD_ID, "stone_form_1");
    private static final ResourceLocation POSE_2 = new ResourceLocation(JojoMod.MOD_ID, "stone_form_2");
    private static final ResourceLocation POSE_3 = new ResourceLocation(JojoMod.MOD_ID, "stone_form_3");

}
