package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class HierophantGreenBarrierDetectionMarker extends MarkerRenderer {

    public HierophantGreenBarrierDetectionMarker(Minecraft mc) {
        super(new ResourceLocation(JojoMod.MOD_ID, "textures/action/hierophant_green_barrier.png"), mc);
    }
    
    @Override
    protected boolean shouldRender() {
        return true;
    }

    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            if (stand.getStandManifestation() instanceof HierophantGreenEntity) {
                // FIXME the net isn't synced to client
                ((HierophantGreenEntity) stand.getStandManifestation()).getBarriersNet()
                .wasRippedAt().forEach(point -> {
                    list.add(new MarkerInstance(point, false));
                });
            }
        });
    }
}
