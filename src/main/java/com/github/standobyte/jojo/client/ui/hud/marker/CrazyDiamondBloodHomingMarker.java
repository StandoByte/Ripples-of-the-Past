package com.github.standobyte.jojo.client.ui.hud.marker;

import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModStandEffects;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class CrazyDiamondBloodHomingMarker extends MarkerRenderer {
    
    public CrazyDiamondBloodHomingMarker(Minecraft mc) {
        super(ModStandTypes.CRAZY_DIAMOND.get().getColor(), new ResourceLocation(JojoMod.MOD_ID, "textures/icons/blood_drops.png"), mc);
    }
    
    @Override
    protected boolean shouldRender() {
        return ActionsOverlayGui.getInstance().getSelectedAction(ActionType.ATTACK) == ModActions.CRAZY_DIAMOND_ITEM_PROJECTILE.get();
    }

    @Override
    protected void updatePositions(List<Vector3d> list, float partialTick) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            stand.getContinuousEffects().getEffects(effect -> effect.effectType == ModStandEffects.DRIED_BLOOD_DROPS.get())
            .forEach(effect -> {
                LivingEntity target = effect.getTarget();
                if (target != null) {
                    list.add(target.getPosition(partialTick).add(0, target.getBbHeight() * 1.1, 0));
                }
            });
        });
    }
}
