package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockBullet;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class CrazyDiamondBloodHomingMarker extends MarkerRenderer {
    
    public CrazyDiamondBloodHomingMarker(Minecraft mc) {
        super(new ResourceLocation(JojoMod.MOD_ID, "textures/icons/blood_drops.png"), mc);
        renderThroughBlocks = false;
    }
    
    @Override
    protected boolean shouldRender() {
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        return hud.showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get())
               && !mc.player.isShiftKeyDown();
    }

    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            Optional<StandEffectInstance> outlined = CrazyDiamondBlockBullet.getTarget(CrazyDiamondBlockBullet.targets(stand), mc.player);
            CrazyDiamondBlockBullet.targets(stand).forEach(effect -> {
                LivingEntity target = effect.getTarget();
                list.add(new MarkerInstance(target.getPosition(partialTick).add(0, target.getBbHeight() * 1.1, 0), 
                        outlined.map(outlinedEffect -> effect == outlinedEffect).orElse(false)));
            });
        });
    }
}
