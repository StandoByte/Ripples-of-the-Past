package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.BasicToggleAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class KosmXHamonBreathHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements BasicToggleAnim {
    private static final Random RANDOM = new Random();

    public KosmXHamonBreathHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        enabled &= PlayerAnimationHandler.canAnimate(player);
        if (enabled) {
            return setAnimFromName((AbstractClientPlayerEntity) player, getAnimPath(player));
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, KosmXFixedFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
        }
    }
    
    private ResourceLocation getAnimPath(PlayerEntity player) {
        ResourceLocation[] poses = INonStandPower.getNonStandPowerOptional(player).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()))
                .map(hamon -> {
                    return POSES.getOrDefault(hamon.getCharacterTechnique(), new ResourceLocation[] {});
                }).orElse(new ResourceLocation[] {});
        if (poses.length == 0) {
            return DEFAULT_POSE;
        }
        return poses[RANDOM.nextInt(poses.length)];
    }
    
    private static final ResourceLocation DEFAULT_POSE = new ResourceLocation(JojoMod.MOD_ID, "breath_default");
    private static final Map<CharacterHamonTechnique, ResourceLocation[]> POSES = Util.make(new HashMap<>(), map -> {
        map.put(ModHamonSkills.CHARACTER_JONATHAN.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_jonathan"),
        });
        map.put(ModHamonSkills.CHARACTER_ZEPPELI.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_zeppeli"),
        });
        map.put(ModHamonSkills.CHARACTER_JOSEPH.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_joseph"),
        });
        map.put(ModHamonSkills.CHARACTER_CAESAR.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_caesar"),
        });
        map.put(ModHamonSkills.CHARACTER_LISA_LISA.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_lisa_lisa"),
        });
    });
    
}
