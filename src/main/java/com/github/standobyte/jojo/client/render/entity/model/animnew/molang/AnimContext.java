package com.github.standobyte.jojo.client.render.entity.model.animnew.molang;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import team.unnamed.mocha.runtime.binding.Binding;

@Binding("query")
public class AnimContext {
    @Nullable public Entity entity;
    @Binding("head_y_rotation") public double head_y_rotation = 0;
    @Binding("head_x_rotation") public double head_x_rotation = 0;
    
    public static AnimContext fillContext(Entity entity, float yRotOffsetDeg, float xRotDeg) {
        _INSTANCE.entity = entity;
        _INSTANCE.head_y_rotation = MathHelper.wrapDegrees(yRotOffsetDeg);
        _INSTANCE.head_x_rotation = xRotDeg;
        return _INSTANCE;
    }
    
    public static AnimContext clearContext() {
        return fillContext(null, 0, 0);
    }
    
    
    static final AnimContext _INSTANCE = new AnimContext();
    private AnimContext() {}
}
