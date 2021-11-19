package com.github.standobyte.jojo.client.renderer.player;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.renderer.player.layer.TornadoOverdriveEffectLayer;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;

public class ModdedPlayerRenderer extends PlayerRenderer {
    private ModdedPlayerModel moddedModel;
    private TornadoOverdriveEffectLayer<AbstractClientPlayerEntity> tornadoEffectLayer = new TornadoOverdriveEffectLayer<>(this);

    public ModdedPlayerRenderer(EntityRendererManager renderManager, boolean slim) {
        super(renderManager, slim);
        this.moddedModel = new ModdedPlayerModel(0, slim);
        this.model = moddedModel;
        addLayer(tornadoEffectLayer);
    }
    
    public boolean shouldChangeRender(AbstractClientPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
            return true;
        }
        
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            Action heldAction = power.getHeldAction(true);
            if (heldAction == ModActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                tornadoEffectLayer.setShouldRender(true);
                return true;
            }
            tornadoEffectLayer.setShouldRender(false);
            
            if (power.isActionOnCooldown(ModActions.HAMON_ZOOM_PUNCH.get())) {
                return true;
            }
            if (heldAction == ModActions.JONATHAN_OVERDRIVE_BARRAGE.get()
                    || heldAction == ModActions.VAMPIRISM_BLOOD_DRAIN.get()
                    || heldAction == ModActions.VAMPIRISM_FREEZE.get()
                    || heldAction == ModActions.VAMPIRISM_BLOOD_GIFT.get()) {
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public void render(AbstractClientPlayerEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YP.rotation(moddedModel.yRotation));
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        matrixStack.popPose();
    }

}
