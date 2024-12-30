package com.github.standobyte.jojo.client.render.rendertype;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

@Deprecated
public class ModifiedRenderTypeBuffers2 extends IRenderTypeBuffer.Impl {
    private final UnaryOperator<RenderType> remapFunction;
    private final Map<RenderType, RenderType> renderTypesRemapped;
    
    public static IRenderTypeBuffer.Impl create(BufferBuilder builder, IRenderTypeBuffer.Impl originalBuffers, UnaryOperator<RenderType> remapFunction) {
        Map<RenderType, BufferBuilder> fixedBuffersOriginal = ClientReflection.getFixedBuffers(originalBuffers);
        
        Map<RenderType, RenderType> renderTypesRemapped = fixedBuffersOriginal.keySet().stream()
                .collect(Collectors.toMap(
                        type -> type, 
                        type -> remapFunction.apply(type)));
        
        Map<RenderType, BufferBuilder> fixedBuffersRemapped = fixedBuffersOriginal.entrySet().stream()
            .collect(Collectors.toMap(
                    entry -> renderTypesRemapped.get(entry.getKey()), 
                    entry -> entry.getValue()));
        
        return new ModifiedRenderTypeBuffers2(builder, fixedBuffersRemapped, renderTypesRemapped, remapFunction);
    }

    protected ModifiedRenderTypeBuffers2(BufferBuilder builder, 
            Map<RenderType, BufferBuilder> fixedBuffers, 
            Map<RenderType, RenderType> renderTypesRemapped, 
            UnaryOperator<RenderType> remapFunction) {
        super(builder, fixedBuffers);
        this.renderTypesRemapped = renderTypesRemapped;
        this.remapFunction = remapFunction;
    }

    @Override
    public IVertexBuilder getBuffer(RenderType renderType) {
        if (renderTypesRemapped.containsKey(renderType)) {
            renderType = renderTypesRemapped.get(renderType);
        }
        else {
            renderType = remapFunction.apply(renderType);
        }
        return super.getBuffer(renderType);
    }

    // FIXME this breaks with Optifine - NoSuchFieldError: field_228459_c_ (lastState)
//    @Override
//    public void endBatch() {
//        lastState.ifPresent(renderType -> {
//            if (!fixedBuffers.containsKey(renderType)) {
//                endBatch(renderType);
//            }
//        });
//        for (RenderType renderType : fixedBuffers.keySet()) {
//            endBatch(renderType);
//        }
//    }

}
