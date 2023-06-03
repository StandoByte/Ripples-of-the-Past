package com.github.standobyte.jojo.client.render.entity.layerrenderer;

import java.util.Random;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.render.item.ClackersItemModel;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.ClackersItem;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class SpecialHeldItemLayer<T extends LivingEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
    private final ClackersItemModel clackersModel = new ClackersItemModel();
    private final ResourceLocation clackersTexture = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/clackers.png");
    private boolean playerAnimHandled = false;
    
    public SpecialHeldItemLayer(IEntityRenderer<T, M> renderer) {
        super(renderer);
    }
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        boolean rightHanded = entity.getMainArm() == HandSide.RIGHT;
        ItemStack leftHandItem = rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();
        ItemStack rightHandItem = rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
        
        // clackers
        if (specialRender(leftHandItem) || specialRender(rightHandItem)) {
            matrixStack.pushPose();
            if (getParentModel().young) {
                matrixStack.translate(0.0D, 0.75D, 0.0D);
                matrixStack.scale(0.5F, 0.5F, 0.5F);
            }

            renderItemSpecial(entity, rightHandItem, HandSide.RIGHT, matrixStack, buffer, packedLight, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot);
            renderItemSpecial(entity, leftHandItem, HandSide.LEFT, matrixStack, buffer, packedLight, 
                    limbSwing, limbSwingAmount, partialTick, ticks, yRot, xRot);
            matrixStack.popPose();
        }
    }
    
    /* not using the ISTER system for clackers because
     *  1) i forgot about it
     *  2) its model needs to know stuff about the player holding them & their model
     */
    private boolean specialRender(ItemStack item) {
        return !item.isEmpty() && 
                item.getItem() == ModItems.CLACKERS.get();
    }
    
    private int holdTick = 0;
    private void renderItemSpecial(LivingEntity entity, ItemStack item, HandSide side, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, 
            float limbSwing, float limbSwingAmount, float partialTick, float ticks, float yRot, float xRot) {
        M entityModel = getParentModel();
        if (specialRender(item)) {
            matrixStack.pushPose();
            entityModel.translateToHand(side, matrixStack);
            PlayerAnimationHandler.getPlayerAnimator().heldItemLayerRender(entity, matrixStack, side);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            boolean leftHand = side == HandSide.LEFT;
            matrixStack.translate((double)((float)(leftHand ? -1 : 1) / 16.0F), 0.125D, -0.625D);
            if (item.getItem() == ModItems.CLACKERS.get()) {
                ModelRenderer clackers = clackersModel.getMainPart();
                ModelRenderer arm = leftHand ? entityModel.leftArm : entityModel.rightArm;
                
                ClientUtil.setRotationAngle(clackers, 0, 0, 0);
                clackersModel.setStringAngles(0, 0, 0, 0, 0, 0);
                
                IVertexBuilder vertexBuilder = buffer.getBuffer(clackersModel.renderType(clackersTexture));
                if (entity.getUseItem() == item) {
                    int useTicks = entity.getTicksUsingItem();
                    float angle;
                    boolean clack;
                    boolean up = false;
                    if (useTicks > 40) {
                        int LOOP_LEN = 2;
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
                        int LOOP_LEN = 8;
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
                        Vector3d particlesPos = entity.getPosition(partialTick)
                                .add(new Vector3d(arm.x / 16F, 1.5F -arm.y / 16F, arm.z / 16F)
                                        .yRot(-MathHelper.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * MathUtil.DEG_TO_RAD))
//                                .add(new Vector3d(0, -1.5, 0).zRot(-arm.zRot).yRot(-arm.yRot).xRot(-arm.xRot))
                                ;

                        Random random = new Random();
                        for(int i = 0; i < 4; ++i) {
                            double d0 = (double)(random.nextFloat() * 2.0F - 1.0F) * 0.25;
                            double d1 = (double)(random.nextFloat() * 2.0F - 1.0F) * 0.25;
                            double d2 = (double)(random.nextFloat() * 2.0F - 1.0F) * 0.25;
                            if (!(d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)) {
                                double d3 = particlesPos.x + d0 * 0.25;
                                double d4 = particlesPos.y + d1 * 0.25;
                                double d5 = particlesPos.z + d2 * 0.25;
//                                entity.level.addParticle(ParticleTypes.CRIT, false, d3, d4, d5, d0, d1, d2);
                                entity.level.addParticle(ParticleTypes.CRIT, false, particlesPos.x, particlesPos.y, particlesPos.z, 0, 0, 0);
                            }
                        }
                    }
                }
                else {
                    boolean swinging = false;
                    if (entity.swinging) {
                        HandSide swingingHand = entity.swingingArm == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
                        swinging = swingingHand == side;
                    }

                    
                    if (swinging) {
                    }
                    else {
                        MatrixStack hand = new MatrixStack();
                        entityModel.translateToHand(side, hand);
                        
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
                        
                        clackers.xRot = arm.xRot;
                    }
                }
                PlayerAnimationHandler.getPlayerAnimator().heldItemLayerChangeItemLocation(entity, matrixStack, side);
                clackersModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }
            matrixStack.popPose();
        }
    }

}
