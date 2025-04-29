package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.math.vector.Matrix4f;

@Mixin(Matrix4f.class)
public interface Matrix4fAccessor {

    @Accessor float getM00();
    @Accessor float getM01();
    @Accessor float getM02();
    @Accessor float getM03();
    @Accessor float getM10();
    @Accessor float getM11();
    @Accessor float getM12();
    @Accessor float getM13();
    @Accessor float getM20();
    @Accessor float getM21();
    @Accessor float getM22();
    @Accessor float getM23();
    @Accessor float getM30();
    @Accessor float getM31();
    @Accessor float getM32();
    @Accessor float getM33();
}
