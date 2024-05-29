package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.stand.GoldExperienceMarkItem;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;

public class GoldExperienceMarkedItemMarker extends MarkerRenderer {
    
    public GoldExperienceMarkedItemMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
        return true;
    }
    
    @Override
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {
        ItemStack item = ((ItemMarkerInstance) marker).item;
        if (item != null && !item.isEmpty()) {
            renderItem(matrixStack, item, partialTick);
        }
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            List<Pair<GEItemMarkEffect, Vector3d>> targets = GoldExperienceMarkItem.getTargets(stand, mc.player);
            Optional<GEItemMarkEffect> outlined = GoldExperienceMarkItem.getTargetedEffect(targets, mc.player);
            
            for (Pair<GEItemMarkEffect, Vector3d> pair : targets) {
                GEItemMarkEffect effect = pair.getLeft();
                Vector3d pos = pair.getRight();
                TrackerItemStack item = effect.getItemTracker(false);
                list.add(new ItemMarkerInstance(pos, 
                        outlined.map(outlinedEffect -> pair.getLeft() == outlinedEffect).orElse(false),
                        item.getItem()));
            }
        });
    }
    
    
    private static class ItemMarkerInstance extends MarkerInstance {
        final ItemStack item;

        public ItemMarkerInstance(Vector3d pos, boolean outlined, ItemStack itemStack) {
            super(pos, outlined);
            this.item = itemStack;
        }
    }

}
