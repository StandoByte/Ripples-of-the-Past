package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class CrazyDiamondAnchorMarker extends MarkerRenderer {
    
    public CrazyDiamondAnchorMarker(Minecraft mc) {
        super(ModStandsInit.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get().getIconTexture(null),
                ModStandsInit.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get(), mc);
    }
    
    @Override
    protected boolean shouldRender() {
        return true;
    }

    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(mc.level, mc.player.getMainHandItem()).ifPresent(pos -> 
            list.add(new MarkerInstance(Vector3d.atCenterOf(pos), false)));
        CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(mc.level, mc.player.getOffhandItem()).ifPresent(pos -> {
            boolean showMarker = ActionsOverlayGui.getInstance().showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get());
            list.add(new MarkerInstance(Vector3d.atCenterOf(pos), showMarker));
        });
    }
}
