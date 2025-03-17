package com.github.standobyte.jojo.client.render.entity.animnew.molang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.client.render.entity.animnew.LivingEntityRenderState;

import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

public class AnimMolangQuery implements ObjectValue {
    public static final String NAMESPACE = "query";
    public static AnimMolangQuery instance = new AnimMolangQuery();
    
    protected AnimMolangQuery() {}
    
    ObjectProperty head_x_rotation;
    ObjectProperty head_y_rotation;
    
    public void fillContext(LivingEntityRenderState renderState) {
        head_x_rotation = ObjectProperty.property(Value.of(renderState.xRot), false);
        head_y_rotation = ObjectProperty.property(Value.of(renderState.yRot), false);
    }
    
    @Override
    public @Nullable ObjectProperty getProperty(@NotNull String name) {
        switch (name) {
            case "head_x_rotation": return head_x_rotation;
            case "head_y_rotation": return head_y_rotation;
        }
        return null;
    }
    
}
