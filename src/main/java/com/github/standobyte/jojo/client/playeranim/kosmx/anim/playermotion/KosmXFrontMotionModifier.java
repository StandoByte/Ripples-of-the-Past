package com.github.standobyte.jojo.client.playeranim.kosmx.anim.playermotion;

import com.github.standobyte.jojo.util.general.MathUtil;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@Deprecated
public class KosmXFrontMotionModifier extends AbstractModifier {
    private PlayerEntity player;
    private float maxFrontOffset = 0;
    
    public KosmXFrontMotionModifier(PlayerEntity player) {
        this.player = player;
    }

    public void setAnimStart(PlayerEntity player) {
        maxFrontOffset = 0;
    }
    
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        if (type == TransformType.POSITION && "body".equals(modelName)) {
            return animBodyPosMovePlayer(tickDelta);
        }
        return super.get3DTransform(modelName, type, tickDelta, value0);
    }
    
    public boolean animMovesEntity() {
//        return this.player == Minecraft.getInstance().player;
        return true;
    }
    
    public Vec3f animBodyPosMovePlayer(float partialTick) {
        Vec3f value = super.get3DTransform("body", TransformType.POSITION, partialTick, Vec3f.ZERO);
        
        float frontOffset = -value.getZ();
        if (frontOffset > maxFrontOffset) {
            double diff = frontOffset - maxFrontOffset;
            maxFrontOffset = frontOffset;
            if (animMovesEntity()) {
                float yRot = (-MathHelper.clamp(partialTick, player.yRotO, player.yRot)) * MathUtil.DEG_TO_RAD;
                Vector3d moveVec = new Vector3d(0, 0, diff).yRot(yRot);
                player.move(MoverType.SELF, moveVec);
            }
        }
        value = new Vec3f(value.getX(), value.getY(), 0);
        
        return value;
    }
    
    
    // TODO move the player in 1st person
    public static void onRenderFrameStart() {
        AbstractClientPlayerEntity player = Minecraft.getInstance().player;
    }
    
    public static void onRenderFrameEnd(float partialTick) {
        AbstractClientPlayerEntity player = Minecraft.getInstance().player;
    }
    
}
