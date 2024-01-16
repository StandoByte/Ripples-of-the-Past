package com.github.standobyte.jojo.client.render.entity.renderer.mob;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.StandUserDummyEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StandUserDummyRenderer extends BipedRenderer<StandUserDummyEntity, PlayerModel<StandUserDummyEntity>> {

    public StandUserDummyRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PlayerModel<>(0, false), 0.5F);
    }
    
    @Override
    public void render(StandUserDummyEntity pEntity, float pEntityYaw, float pPartialTicks, 
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        if (Minecraft.renderNames() && pEntity == this.entityRenderDispatcher.crosshairPickEntity) {
            IStandPower stand = pEntity.getStandPower();
            if (stand.hasPower()) {
                pMatrixStack.pushPose();
                pMatrixStack.translate(0, 0.25, 0);
                float staminaRatio = stand.getStamina() / stand.getMaxStamina();
                float staminaCondition = 0.25F + Math.min(staminaRatio * 1.5F, 0.75F);
                int color = ClientUtil.fromRgb(1 - staminaCondition, staminaCondition, 0f);
                this.renderNameTag(pEntity, 
                        new TranslationTextComponent("Stamina: %s", 
                                new StringTextComponent(String.format("%.2f%%", staminaRatio * 100)).withStyle(ClientUtil.textColor(color))), 
                        pMatrixStack, pBuffer, pPackedLight);
                
//                // doesn't sync
//                pMatrixStack.translate(0, 0.25, 0);
//                float resolveRatio = stand.getResolve() / stand.getMaxResolve();
//                this.renderNameTag(pEntity, 
//                        new StringTextComponent(String.format("Resolve: %.2f%% (level %d)", resolveRatio * 100, stand.getResolveLevel())), 
//                        pMatrixStack, pBuffer, pPackedLight);
                
                pMatrixStack.translate(0, 0.25, 0);
                this.renderNameTag(pEntity, 
                        stand.getName(), 
                        pMatrixStack, pBuffer, pPackedLight);
                pMatrixStack.popPose();
            }
        }
    }
}
