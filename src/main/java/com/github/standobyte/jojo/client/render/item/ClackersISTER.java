package com.github.standobyte.jojo.client.render.item;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.render.item.generic.CustomModelItemISTER;
import com.github.standobyte.jojo.client.render.item.generic.ISTERWithEntity;
import com.github.standobyte.jojo.item.ClackersItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ClackersISTER extends ItemStackTileEntityRenderer implements ISTERWithEntity {
    private final ClackersItemModel clackersModel = new ClackersItemModel();
    private final ResourceLocation clackersTexture = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/clackers.png");
    @Nullable protected LivingEntity entity;

    public ClackersISTER() {
        super();
    }
    
    @Override
    public void setEntity(@Nullable LivingEntity entity) {
        this.entity = entity;
    }

    private int holdTick = 0;
    @Override
    public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, int overlay) {
        switch (transformType) {
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND:
            renderFirstPerson(itemStack, transformType, matrixStack, buffer, light, overlay);
            break;
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
            renderThirdPerson(itemStack, transformType, matrixStack, buffer, light, overlay);
            break;
        default:
            IBakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null);
            CustomModelItemISTER.renderItemNormally(matrixStack, itemStack, transformType, buffer, light, overlay, model);
            break;
        }
    }
    
    private void renderThirdPerson(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, int overlay) {
        float partialTick = ClientUtil.getPartialTick();
        boolean leftHand = transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transformType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        HandSide side = leftHand ? HandSide.LEFT : HandSide.RIGHT;
        
        ModelRenderer clackers = clackersModel.getMainPart();
        ClientUtil.setRotationAngle(clackers, 0, 0, 0);
        clackersModel.setStringAngles(0, 0, 0, 0, 0, 0);
        
        IVertexBuilder vertexBuilder = buffer.getBuffer(clackersModel.renderType(clackersTexture));
        
        
        
        if (entity.getUseItem() == itemStack) {
            int useTicks = entity.getTicksUsingItem();
            float angle;
            boolean clack;
            boolean up = false;
            
            if (useTicks > ClackersItem.TICKS_MAX_POWER) {
                float LOOP_LEN = 2f;
                float loopPos = (useTicks % LOOP_LEN + partialTick) / LOOP_LEN;
                loopPos = 1 - loopPos;
                loopPos *= loopPos;
                loopPos = 1 - loopPos;
                if (useTicks % (LOOP_LEN * 2) < LOOP_LEN) {
                    loopPos = 1 - loopPos;
                }
                
                angle = (float) Math.PI * ((1 - loopPos) * 7F/8 + 1F/16);
                clack = holdTick != useTicks && useTicks % LOOP_LEN == 0;
                if (clack) {
                    up = useTicks % (LOOP_LEN * 2) == LOOP_LEN;
                }
            }
            else {
                float LOOP_LEN = 5;
                float loopPos = (useTicks % LOOP_LEN + partialTick) / LOOP_LEN;
                loopPos = loopPos < 0.5F ? loopPos * 2 : 2 - loopPos * 2;
                loopPos = 1 - loopPos;
                loopPos *= loopPos;
                loopPos = 1 - loopPos;
                
                float amplitude = (float) Math.PI * (1F/6 + 1F/12 * (useTicks / LOOP_LEN) - 1F/16);
                angle = amplitude * loopPos + (float) Math.PI / 16;
                
                clack = holdTick != useTicks && useTicks % LOOP_LEN == 0;
            }
            holdTick = useTicks;
            clackersModel.setStringAngles(
                    0, 0, angle, 
                    0, 0, (float) Math.PI - angle);

            if (clack) {
                ClackersItem.playClackSound(entity.level, entity);
//                float clackYOffset = -0.75f;
//                Vector3d particlesPos = entity.getPosition(partialTick)
//                        .add(new Vector3d(bipedHand.x / 16F, 1.5F + clackYOffset -bipedHand.y / 16F, 1.5f + bipedHand.z / 16F)
//                                .yRot(-MathHelper.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * MathUtil.DEG_TO_RAD))
//                        .add(new Vector3d(0, -1.5, 0).zRot(-bipedHand.zRot).yRot(-bipedHand.yRot).xRot(-bipedHand.xRot))
//                        ;
//
//                for(int i = 0; i < 4; ++i) {
//                    entity.level.addParticle(ParticleTypes.CRIT, false, particlesPos.x, particlesPos.y, particlesPos.z, 0, 0, 0);
//                }
            }
        }
        
        
        
        else {
            ModelRenderer bipedHand = null;
            float limbSwing = 0;
            float limbSwingAmount = 0;
            if (entity != null) {
                boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
                EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                if (entityRenderer instanceof LivingRenderer) {
                    EntityModel<?> entityModel = ((LivingRenderer<?, ?>) entityRenderer).getModel();
                    if (entityModel instanceof BipedModel<?>) {
                        BipedModel<?> biped = (BipedModel<?>) entityModel;
                        bipedHand = leftHand ? biped.leftArm : biped.rightArm;
                    }
                }
                
                if (entity.isAlive() && !shouldSit) {
                    limbSwingAmount = MathHelper.lerp(partialTick, entity.animationSpeedOld, entity.animationSpeed);
                    limbSwing = entity.animationPosition - entity.animationSpeed * (1.0F - partialTick);
                    if (entity.isBaby()) {
                        limbSwing *= 3.0F;
                    }

                    if (limbSwingAmount > 1.0F) {
                        limbSwingAmount = 1.0F;
                    }
                }
            }
            
            float xRotAdd = MathHelper.cos(limbSwing * 0.6664F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
            if (leftHand) {
                xRotAdd *= -1;
            }
            float xRot1 = (float) Math.PI / 2 + (float) Math.PI / 32;
            float xRot2 = -xRot1;
            xRot1 += xRotAdd;
            xRot2 += xRotAdd;
            float yRot1 = (float) Math.PI / 16;
            float yRot2 = -yRot1;
            float zRot1 = 0;
            float zRot2 = 0;
            clackersModel.setStringAngles(xRot1, yRot1, zRot1, xRot2, yRot2, zRot2);
            
            if (bipedHand != null) {
                clackers.xRot = bipedHand.xRot;
            }
        }
        
        PlayerAnimationHandler.getPlayerAnimator().heldItemLayerChangeItemLocation(entity, matrixStack, side);
        clackersModel.renderToBuffer(matrixStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    private void renderFirstPerson(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer buffer, int light, int overlay) {
        
    }

}
