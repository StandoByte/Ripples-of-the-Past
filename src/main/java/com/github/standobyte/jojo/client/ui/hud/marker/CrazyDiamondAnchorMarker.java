package com.github.standobyte.jojo.client.ui.hud.marker;

import java.util.List;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.client.resources.ActionSpriteUploader;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.power.stand.ModStandActions;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.IPower.ActionType;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class CrazyDiamondAnchorMarker extends MarkerRenderer {
    
    public CrazyDiamondAnchorMarker(Minecraft mc) {
        super(ModStands.CRAZY_DIAMOND.getStandType().getColor(), 
                ActionSpriteUploader.getIcon(ModStandActions.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get()), mc);
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
            Action<?> selectedAbility = ActionsOverlayGui.getInstance().getSelectedActionIfEnabled(ActionType.ABILITY);
            boolean abilitySelected = selectedAbility != null && selectedAbility.getShiftVariationIfPresent()
                    == ModStandActions.CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get().getShiftVariationIfPresent();
            list.add(new MarkerInstance(Vector3d.atCenterOf(pos), abilitySelected));
        });
    }
}
