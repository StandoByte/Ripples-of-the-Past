package com.github.standobyte.jojo.client.playeranim.playeranimator.anim;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.playeranim.playeranimator.PlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HeldActionAnimLayer extends AnimLayerHandler {
    private static final Random RANDOM = new Random();

    public HeldActionAnimLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    public boolean setActionAnim(PlayerEntity player, Action<?> action) {
        if (action != null) {
            return setAnimFromName((AbstractClientPlayerEntity) player, getAnimPath(player, action));
        }
        else {
            return fadeOutAnim((AbstractClientPlayerEntity) player, AbstractFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
        }
    }
    
    private ResourceLocation getAnimPath(PlayerEntity player, Action<?> action) {
        if (action == ModHamonActions.HAMON_BREATH.get()) {
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
        
        if (action == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()) {
            return SYO_BARRAGE_START;
        }
        
        return null;
    }
    
    private static final ResourceLocation SYO_BARRAGE_START = new ResourceLocation(JojoMod.MOD_ID, "syo_barrage_start");
    private static final ResourceLocation DEFAULT_POSE = new ResourceLocation(JojoMod.MOD_ID, "breath_pose/default");
    private static final Map<CharacterHamonTechnique, ResourceLocation[]> POSES = Util.make(new HashMap<>(), map -> {
        map.put(ModHamonSkills.CHARACTER_JONATHAN.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_pose/jonathan"),
        });
        map.put(ModHamonSkills.CHARACTER_ZEPPELI.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_pose/zeppeli"),
        });
        map.put(ModHamonSkills.CHARACTER_JOSEPH.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_pose/joseph"),
        });
        map.put(ModHamonSkills.CHARACTER_CAESAR.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_pose/caesar"),
        });
        map.put(ModHamonSkills.CHARACTER_LISA_LISA.get(), new ResourceLocation[] {
                DEFAULT_POSE,
//                new ResourceLocation(JojoMod.MOD_ID, "breath_pose/lisa_lisa"),
        });
    });
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }

    @SubscribeEvent
    public void preRender(RenderPlayerEvent.Pre event) {
        preventCrouch((AbstractClientPlayerEntity) event.getPlayer(), event.getRenderer());
    }
}
