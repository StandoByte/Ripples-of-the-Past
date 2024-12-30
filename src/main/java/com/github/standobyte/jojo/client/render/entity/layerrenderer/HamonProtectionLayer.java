package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
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
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

//@OnlyIn(Dist.CLIENT)
public class HamonProtectionLayer<T extends LivingEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> implements IFirstPersonHandLayer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/biped/hamon_protection.png");
    private final PlayerModel<T> model = new PlayerModel<>(0.20F, false);
    
    public HamonProtectionLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
        PlayerAnimationHandler.getPlayerAnimator().onArmorLayerInit(this);
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, 
    		T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
    	if (INonStandPower.getNonStandPowerOptional(pLivingEntity).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()))
                .filter(HamonData::isProtectionEnabled).isPresent()) {
	    	float f = (float)pLivingEntity.tickCount + pPartialTicks;
	        PlayerModel<T> entitymodel = model;
	        entitymodel.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
	        this.getParentModel().copyPropertiesTo(entitymodel);
	        IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.energySwirl(TEXTURE, this.xOffset(f), f * 0.002F));
	        entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
	        entitymodel.renderToBuffer(pMatrixStack, ivertexbuilder, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 0.15F, 0.15F, 0.15F, 0.1F);
    	}
    }
    
    @Override
    public void renderHandFirstPerson(HandSide side, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, 
            PlayerRenderer playerRenderer) {
        if (!player.isSpectator() && INonStandPower.getNonStandPowerOptional(player).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()))
                .filter(HamonData::isProtectionEnabled).isPresent()) {
            float partialTick = ClientUtil.getPartialTick();
            float f = (float)player.tickCount + partialTick;
            PlayerModel<AbstractClientPlayerEntity> model = playerRenderer.getModel();
            ClientUtil.setupForFirstPersonRender(model, player);
            IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.energySwirl(TEXTURE, this.xOffset(f), f * 0.01F));
            ModelRenderer arm = ClientUtil.getArm(model, side);
            ModelRenderer armOuter = ClientUtil.getArmOuter(model, side);
            arm.xRot = 0.0F;
            arm.render(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 0.15F, 0.15F, 0.15F, 0.25F);
            armOuter.xRot = 0.0F;
            armOuter.render(matrixStack, vertexBuilder, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, 0.15F, 0.15F, 0.15F, 0.25F);
        }
    }
    
    protected float xOffset(float p_225634_1_) {
        return p_225634_1_ * 0.01F;
     }
}

