package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMove;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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
        ItemStack itemUsed = CrazyDiamondBlockCheckpointMove.getBlockItemToUse(mc.player);
        ItemStack mainHandItem = mc.player.getMainHandItem();
        ItemStack offHandItem = mc.player.getOffhandItem();
        CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(mc.level, mainHandItem).ifPresent(pos -> 
            list.add(new MarkerInstance(Vector3d.atCenterOf(pos), mainHandItem == itemUsed)));
        CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(mc.level, offHandItem).ifPresent(pos -> {
            list.add(new MarkerInstance(Vector3d.atCenterOf(pos), offHandItem == itemUsed));
        });
    }
}
