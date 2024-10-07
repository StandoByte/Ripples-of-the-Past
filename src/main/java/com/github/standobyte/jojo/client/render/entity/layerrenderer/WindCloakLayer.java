package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

//@OnlyIn(Dist.CLIENT)
public class WindCloakLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> implements IFirstPersonHandLayer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/wind_cloak.png");
    private final PlayerModel<T> model = new PlayerModel<>(0.50F, false);
    
    public WindCloakLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, 
    		T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
    	if (INonStandPower.getNonStandPowerOptional(pLivingEntity)
                .map(power -> power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_WIND_CLOAK.get())
                .orElse(false)) {
	    	float f = (float)pLivingEntity.tickCount + pPartialTicks;
	        PlayerModel<T> entitymodel = model;
	        entitymodel.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
	        this.getParentModel().copyPropertiesTo(entitymodel);
	        IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.energySwirl(getTexture(model, pLivingEntity), this.xOffset(f), f * 0.01F));
	        entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
	        entitymodel.renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 0.2F, 0.2F, 0.2F, 0.25F);
    	}
    }
    
    @Nullable
    private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
    	if (INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getHeldAction(true) == ModPillarmanActions.PILLARMAN_WIND_CLOAK.get())
                .orElse(false)) {
            return TEXTURE;
        }
        return null;
    }
    
    @Override
    public void renderHandFirstPerson(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer) {
        PlayerModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();
        IFirstPersonHandLayer.defaultRender(side, matrixStack, buffer, light, player, playerRenderer, 
                model, getTexture(model, player));
    }
    
    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.01F;
     }
}

