package com.github.standobyte.jojo.client.particle.custom;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public interface IFirstPersonParticle {
    void renderSprite(Matrix4f matrixEntry, IVertexBuilder buffer, int light, float partialTick, Vector3f[] avector3f);
}
