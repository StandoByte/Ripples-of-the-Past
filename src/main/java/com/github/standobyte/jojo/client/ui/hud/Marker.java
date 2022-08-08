package com.github.standobyte.jojo.client.ui.hud;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class Marker {
    public final int color;
    public final ResourceLocation iconTexture;
    public final Vector3d position;
    
    public Marker(int color, ResourceLocation iconTexture, Vector3d position) {
        this.color = color;
        this.iconTexture = iconTexture;
        this.position = position;
    }
}
