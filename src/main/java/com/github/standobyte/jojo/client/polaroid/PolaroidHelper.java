package com.github.standobyte.jojo.client.polaroid;

import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.EntityViewRenderEvent;

public class PolaroidHelper {

    
    public static void takePicture(@Nullable Vector3d cameraPos, @Nullable UnaryOperator<Vector3f> cameraAngle, boolean canCaptureStands) {
        
    }
    
    public static void pictureCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        
    }
    
    
    
    public static void renderPhotoInHand(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, 
            float pEquippedProgress, HandSide pHand, float pSwingProgress, ItemStack pStack) {
        
    }
}
