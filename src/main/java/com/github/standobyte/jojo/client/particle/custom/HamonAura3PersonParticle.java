package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.particle.HamonAuraParticle;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

public class HamonAura3PersonParticle extends HamonAuraParticle {
    private final LivingEntity user;
    private final AbstractClientPlayerEntity userAsPlayer;
    private Vector3d userPositionPrev;
    
    protected HamonAura3PersonParticle(ClientWorld world, LivingEntity entity, 
            double x, double y, double z, double xda, double yda, double zda,
            IAnimatedSprite sprites) {
        super(world, x, y, z, xda, yda, zda, sprites);
        this.user = entity;
        this.userPositionPrev = entity.position();
        this.userAsPlayer = user instanceof AbstractClientPlayerEntity ? (AbstractClientPlayerEntity) user : null;
    }
    
    @Override
    public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float partialTick) {
        if (!ClientModSettings.getSettingsReadOnly().thirdPersonHamonAura) return;
        
        if (user != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.cameraEntity == user && mc.options.getCameraType() == PointOfView.FIRST_PERSON) {
                return;
            }
        }
        
        Vector3d playerAnimPos = userAsPlayer != null ? PlayerAnimationHandler.getPlayerAnimator().getBodyPos(userAsPlayer, partialTick) : Vector3d.ZERO;
        x += playerAnimPos.x;
        y += playerAnimPos.y;
        z += playerAnimPos.z;
        xo += playerAnimPos.x;
        yo += playerAnimPos.y;
        zo += playerAnimPos.z;
        
        super.render(vertexBuilder, camera, partialTick);
        
        x -= playerAnimPos.x;
        y -= playerAnimPos.y;
        z -= playerAnimPos.z;
        xo -= playerAnimPos.x;
        yo -= playerAnimPos.y;
        zo -= playerAnimPos.z;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (user != null) {
            Vector3d offset = user.position().subtract(userPositionPrev);
            move(offset.x, offset.y, offset.z);
            this.userPositionPrev = user.position();
        }
    }
    
    
    public static HamonAuraParticle createCustomParticle(IAnimatedSprite sprites, ClientWorld world, 
            LivingEntity entity, double x, double y, double z) {
        HamonAuraParticle particle = new HamonAura3PersonParticle(world, entity, x, y, z, 0, 0, 0, sprites);
        return particle;
    }
}
