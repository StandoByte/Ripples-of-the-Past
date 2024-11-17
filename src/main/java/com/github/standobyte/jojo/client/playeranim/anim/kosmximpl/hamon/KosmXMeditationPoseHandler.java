package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.IEntityAnimApplier;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.HamonMeditationPoseAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.KosmXKeyframeAnimPlayer;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.mob.KosmXHamonMasterAnimApplier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.render.entity.model.mob.HamonMasterModel;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXMeditationPoseHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements HamonMeditationPoseAnim {

    public KosmXMeditationPoseHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    
    private static final ResourceLocation SIT_DOWN_PATH = new ResourceLocation(JojoMod.MOD_ID, "meditation");
//    private static final ResourceLocation STAND_UP_PATH = new ResourceLocation(JojoMod.MOD_ID, "meditation_stand_up");
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        if (enabled) {
            return setAnimFromName(player, SIT_DOWN_PATH);
        }
        else {
//            setAnimFromName(player, STAND_UP_PATH);
            return fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(4, Ease.OUTCUBIC), null);
        }
    }
    
    @Override
    public IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> initHamonMasterPose(HamonMasterModel model) {
        if (model instanceof IMutableModel) {
            IMutableModel modelWithMixin = ((IMutableModel) model);
            IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> animApplier;
            animApplier = new KosmXHamonMasterAnimApplier(model, modelWithMixin, getHamonMasterAnim());
            animApplier.onInit();
            return animApplier;
        }
        return HamonMeditationPoseAnim.super.initHamonMasterPose(model);
    }
    
    protected IAnimation getHamonMasterAnim() {
        KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(SIT_DOWN_PATH);
        if (keyframes != null) {
            ModifierLayer<IAnimation> anim = new ModifierLayer<>(new KosmXKeyframeAnimPlayer(keyframes, keyframes.returnToTick));
            anim.addModifierLast(new SpeedModifier(0.5F));
            return anim;
        }
        return null;
    }
    
}
