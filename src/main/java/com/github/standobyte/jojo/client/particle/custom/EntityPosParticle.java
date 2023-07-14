package com.github.standobyte.jojo.client.particle.custom;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public abstract class EntityPosParticle extends SpriteTexturedParticle {
    protected final Entity entity;
    
    protected EntityPosParticle(ClientWorld world, Entity entity) {
        super(world, 0, 0, 0);
        this.entity = entity;
        this.hasPhysics = false;
    }
    
    protected final void initPos() {
        Vector3d pos = getNextPos(entity);
        this.setPos(pos.x, pos.y, pos.z);
        this.xo = pos.x;
        this.yo = pos.y;
        this.zo = pos.z;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (age++ >= lifetime || entity == null || !entity.isAlive()) {
            remove();
            return;
        }
        Vector3d nextPos = getNextPos(entity);
        if (nextPos == null) {
            remove();
            return;
        }
        xo = x;
        yo = y;
        zo = z;
        x = nextPos.x;
        y = nextPos.y;
        z = nextPos.z;
    }
    
    @Override
    public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float partialTick) {
        if (entity != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.cameraEntity == entity && mc.options.getCameraType() == PointOfView.FIRST_PERSON) {
                renderFirstPerson(vertexBuilder, camera, partialTick);
                return;
            }
        }
        super.render(vertexBuilder, camera, partialTick);
    }
    
    private void renderFirstPerson(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float partialTick) {
        
    }
    
    public Vector3d getPos() {
        return new Vector3d(x, y, z);
    }
    
    protected abstract Vector3d getNextPos(Entity entity);
}
