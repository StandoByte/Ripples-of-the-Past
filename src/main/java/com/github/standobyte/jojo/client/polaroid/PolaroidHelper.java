package com.github.standobyte.jojo.client.polaroid;

import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import org.lwjgl.system.MemoryUtil;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PhotosCache.PhotoInstance;
import com.github.standobyte.jojo.item.PhotoItem;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class PolaroidHelper {
    private static final int PHOTO_WIDTH = 272;
    private static final int PHOTO_HEIGHT = 236;
    
    public static void takePicture(@Nullable Vector3d cameraPos, @Nullable UnaryOperator<Vector3f> cameraAngle, 
            boolean canCaptureStands, int giveToPlayerId) {
        Minecraft mc = Minecraft.getInstance();
        
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        setupRemoteBuffer(mc, width, height);
        
        Framebuffer mainBuffer = mc.getMainRenderTarget();
        boolean guiWasHidded = mc.options.hideGui;
        Boolean forcedCanSeeStands = ClientUtil.forcedCanSeeStands;
        PointOfView pov = mc.options.getCameraType();
        mc.setScreen(null);
        mc.options.hideGui = true;
        if (!canCaptureStands) {
            ClientUtil.forcedCanSeeStands = false;
        }
        mc.options.setCameraType(PointOfView.FIRST_PERSON);
        PolaroidHelper.cameraAngle = cameraAngle;
        PolaroidHelper.cameraPos = cameraPos;
        ClientReflection.setMainRenderTarget(mc, remoteRenderTarget);
        
        renderOnRemoteBuffer(Minecraft.getInstance());
        NativeImage image = ScreenShotHelper.takeScreenshot(width, height, remoteRenderTarget);
        ClientReflection.setMainRenderTarget(mc, mainBuffer);
        PolaroidHelper.cameraAngle = null;
        PolaroidHelper.cameraPos = null;
        mc.options.setCameraType(pov);
        mc.options.hideGui = guiWasHidded;
        ClientUtil.forcedCanSeeStands = forcedCanSeeStands;
        
        
        int ssWidth = image.getWidth();
        int ssHeight = image.getHeight();
        double widthRatio = (double) ssWidth / PHOTO_WIDTH;
        double heightRatio = (double) ssHeight / PHOTO_HEIGHT;
        double photoRatio = Math.min(widthRatio, heightRatio);
        int cropWidth = (int) (photoRatio * PHOTO_WIDTH);
        int cropHeight = (int) (photoRatio * PHOTO_HEIGHT);
        NativeImage cropped = cropImage(image, 
                (ssWidth - cropWidth) / 2, (ssHeight - cropHeight) / 2,
                (ssWidth + cropWidth) / 2, (ssHeight + cropHeight) / 2);
        image.close();
        
        NativeImage resized = new NativeImage(Math.min(cropWidth, PHOTO_WIDTH), Math.min(cropHeight, PHOTO_HEIGHT), false);
        cropped.resizeSubRectTo(0, 0, cropWidth, cropHeight, resized);
        PhotosCache.queueToSendToServer(resized, cropped, giveToPlayerId);
    }
    
    
    
    public static Framebuffer remoteRenderTarget;
    private static UnaryOperator<Vector3f> cameraAngle;
    private static Vector3d cameraPos;
    
    private static void setupRemoteBuffer(Minecraft mc, int width, int height) {
        if (remoteRenderTarget == null) {
            remoteRenderTarget = new Framebuffer(width, height, true, Minecraft.ON_OSX) {
                @Override
                public void blitToScreen(int width, int height, boolean flag) {}
            };
            remoteRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        else {
            remoteRenderTarget.resize(width, height, Minecraft.ON_OSX);
        }
    }
    
    public static boolean isTakingPhoto() {
        return Minecraft.getInstance().getMainRenderTarget() == remoteRenderTarget;
    }
    
    @SuppressWarnings("deprecation")
    private static void renderOnRemoteBuffer(Minecraft mc) {
        RenderSystem.pushMatrix();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        remoteRenderTarget.bindWrite(true);
        FogRenderer.setupNoFog();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        if (!mc.noRender) {
            float partialTick = mc.isPaused() ? ClientReflection.getPausePartialTick(mc) : mc.getFrameTime();
            BasicEventHooks.onRenderTickStart(partialTick);
            
            RenderSystem.viewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());
            if (mc.level != null) {
                mc.getProfiler().push("level");
                MatrixStack matrixStack = new MatrixStack();
                mc.gameRenderer.renderLevel(partialTick, Util.getNanos(), matrixStack);
                mc.levelRenderer.doEntityOutline();

                remoteRenderTarget.bindWrite(true);
            }
            
            BasicEventHooks.onRenderTickEnd(partialTick);
        }
        
        remoteRenderTarget.unbindWrite();
        RenderSystem.popMatrix();
    }
    
    public static boolean pictureCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (isTakingPhoto()) {
            ActiveRenderInfo camera = event.getInfo();
            if (cameraPos != null) {
                ClientReflection.setPosition(camera, cameraPos);
                ClientReflection.setIsDetached(camera, true);
            }
            if (cameraAngle != null) {
                Vector3f angles = new Vector3f(event.getPitch(), event.getYaw(), event.getRoll());
                angles = cameraAngle.apply(angles);
                event.setPitch(angles.x());
                event.setYaw(angles.y());
                event.setRoll(angles.z());
            }
            ClientReflection.setMirror(camera, false);
            return true;
        }
        return false;
    }
    
    
    
    public static void renderPhotoInHand(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, 
            float pEquippedProgress, HandSide pHand, float pSwingProgress, ItemStack pStack, float partialTick) {
        pMatrixStack.pushPose();
        Minecraft mc = Minecraft.getInstance();
        float f = pHand == HandSide.RIGHT ? 1.0F : -1.0F;
        pMatrixStack.translate((double)(f * 0.125F), -0.125D, 0.0D);
        if (!mc.player.isInvisible()) {
            pMatrixStack.pushPose();
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 10.0F));
            renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand);
            pMatrixStack.popPose();
        }

        pMatrixStack.pushPose();
        pMatrixStack.translate((double)(f * 0.51F), (double)(-0.08F + pEquippedProgress * -1.2F), -0.75D);
        float f1 = MathHelper.sqrt(pSwingProgress);
        float f2 = MathHelper.sin(f1 * (float)Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
        float f5 = -0.3F * MathHelper.sin(pSwingProgress * (float)Math.PI);
        pMatrixStack.translate((double)(f * f3), (double)(f4 - 0.3F * f2), (double)f5);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f2 * -30.0F));
        renderPhoto(pMatrixStack, pBuffer, pCombinedLight, pStack, partialTick);
        pMatrixStack.popPose();
        pMatrixStack.popPose();
    }

    private static void renderPlayerArm(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HandSide pSide) {
        Minecraft mc = Minecraft.getInstance();
        boolean flag = pSide != HandSide.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(pSwingProgress);
        float f2 = -0.3F * MathHelper.sin(f1 * (float)Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(pSwingProgress * (float)Math.PI);
        pMatrixStack.translate((double)(f * (f2 + 0.64000005F)), (double)(f3 + -0.6F + pEquippedProgress * -0.6F), (double)(f4 + -0.71999997F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
        float f5 = MathHelper.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
        float f6 = MathHelper.sin(f1 * (float)Math.PI);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayerEntity abstractclientplayerentity = mc.player;
        mc.getTextureManager().bind(abstractclientplayerentity.getSkinTextureLocation());
        pMatrixStack.translate((double)(f * -1.0F), (double)3.6F, 3.5D);
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
        pMatrixStack.translate((double)(f * 5.6F), 0.0D, 0.0D);
        PlayerRenderer playerrenderer = (PlayerRenderer)mc.getEntityRenderDispatcher().<AbstractClientPlayerEntity>getRenderer(abstractclientplayerentity);
        if (flag) {
            playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayerentity);
        } else {
            playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayerentity);
        }

    }
    
    public static final ResourceLocation PHOTO_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/photo_background.png");
    public static final RenderType PHOTO_BACKGROUND = RenderType.text(PHOTO_TEXTURE);
    private static void renderPhoto(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, ItemStack pStack, float partialTick) {
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        pMatrixStack.scale(0.38F, 0.38F, 0.38F);
        pMatrixStack.translate(-0.5D, -0.5D, 0.0D);
        pMatrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        IVertexBuilder ivertexbuilder = pBuffer.getBuffer(PHOTO_BACKGROUND);
        Matrix4f matrix4f = pMatrixStack.last().pose();
        ivertexbuilder.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pCombinedLight).endVertex();
        ivertexbuilder.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pCombinedLight).endVertex();
        ivertexbuilder.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pCombinedLight).endVertex();
        ivertexbuilder.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pCombinedLight).endVertex();
        
        PhotoInstance photo = PhotosCache.getOrTryLoadPhoto(ClientUtil.getServerUUID(), PhotoItem.getPhotoId(pStack));
        if (photo != null) {
            float alpha = PhotoItem.getPhotoAlpha(pStack, partialTick);
            drawPhoto(pMatrixStack, pBuffer, pCombinedLight, alpha, photo.renderType);
        }
        
        if (pStack.hasCustomHoverName()) {
            ITextComponent name = pStack.getHoverName();
            Minecraft mc = Minecraft.getInstance();
            int x = 64;
            int y = 117;
            pMatrixStack.pushPose();
            pMatrixStack.translate(0, 0, -0.1);
            ClientUtil.drawCenteredStringNoShadow(pMatrixStack, mc.font, name, x, y, 0x606060);
            pMatrixStack.popPose();
        }
    }

    private static void drawPhoto(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, float alpha, RenderType photo) {
        if (photo != null && alpha > 0) {
            float x0 = 3.65f - 0.01f;
            float x1 = x0 + 120.7f + 0.02f;
            float y0 = 3.65f - 0.01f;
            float y1 = y0 + 104.725f + 0.02f;
            Matrix4f matrix4f = pMatrixStack.last().pose();
            IVertexBuilder ivertexbuilder = pBuffer.getBuffer(photo);
            int alphaInt = (int) (alpha * 255);
            ivertexbuilder.vertex(matrix4f, x0, y1, -0.01F).color(255, 255, 255, alphaInt).uv(0.0F, 1.0F).uv2(pPackedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, x1, y1, -0.01F).color(255, 255, 255, alphaInt).uv(1.0F, 1.0F).uv2(pPackedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, x1, y0, -0.01F).color(255, 255, 255, alphaInt).uv(1.0F, 0.0F).uv2(pPackedLight).endVertex();
            ivertexbuilder.vertex(matrix4f, x0, y0, -0.01F).color(255, 255, 255, alphaInt).uv(0.0F, 0.0F).uv2(pPackedLight).endVertex();
        }
    }
    
    
    
    private static NativeImage cropImage(NativeImage image, int x0, int y0, int x1, int y1) {
        x0 = Math.max(x0, 0);
        y0 = Math.max(y0, 0);
        x1 = Math.min(x1, image.getWidth());
        y1 = Math.min(y1, image.getHeight());
        int width = x1 - x0;
        int height = y1 - y0;
        NativeImage cropped = new NativeImage(image.format(), width, height, true);

        int pixelLen = cropped.format().components();

        long destPixels = ClientReflection.getPixelsAddress(cropped);
        if (destPixels == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
        long sourcePixels = ClientReflection.getPixelsAddress(image);
        if (sourcePixels == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }

        for (int row = y0; row < y1; row++) {
            int srcIndex = (row * image.getWidth() + x0) * pixelLen;
            int destIndex = ((row - y0) * cropped.getWidth()) * pixelLen;
            MemoryUtil.memCopy(sourcePixels + srcIndex, destPixels + destIndex, width * pixelLen);
        }

        return cropped;
    }
}
