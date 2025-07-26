package com.github.standobyte.jojo.client.render.entity.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelCubeWeightedList {
    
    public static ModelCubeWeightedList fromModel(EntityModel<?> model) {
        Stream<ModelRenderer> modelParts;
        if (model instanceof SegmentedModel) {
            modelParts = StreamSupport.stream(((SegmentedModel<?>) model).parts().spliterator(), false);
        }
        else if (model instanceof AgeableModel) {
            AgeableModel<?> ageable = (AgeableModel<?>) model;
            modelParts = Stream.concat(
                    StreamSupport.stream(ClientReflection.getHeadParts(ageable).spliterator(), false), 
                    StreamSupport.stream(ClientReflection.getBodyParts(ageable).spliterator(), false));
        }
        else {
            modelParts = FieldUtils.getAllFieldsList(model.getClass()).stream()
                    .flatMap(field -> {
                        if (ModelRenderer.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            ModelRenderer inModModelPart;
                            try {
                                inModModelPart = (ModelRenderer) field.get(model);
                                return Stream.of(inModModelPart);
                            } catch (IllegalArgumentException | IllegalAccessException ignored) {}
                        }
                        
                        else if (ModelRenderer[].class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            ModelRenderer[] inModModelParts;
                            try {
                                inModModelParts = (ModelRenderer[]) field.get(model);
                                return Arrays.stream(inModModelParts);
                            } catch (IllegalArgumentException | IllegalAccessException ignored) {}
                        }
                        
                        return Stream.empty();
                    });
        }
        return fromModelParts(modelParts);
    }
    
    public static ModelCubeWeightedList fromModelParts(Stream<ModelRenderer> modelParts) {
        Map<ModelRenderer, ModelPartParents> inModModelParts = modelParts.collect(Collectors.toMap(
                Function.identity(), ModelPartParents::new, (po,huy) -> huy, HashMap::new));
        
        List<ModelPartParents> prevGen = new ArrayList<>(inModModelParts.values());
        List<ModelPartParents> thisGen = new ArrayList<>();
        do {
            for (ModelPartParents parent : prevGen) {
                List<ModelRenderer> children = ClientReflection.getChildren(parent.modelPart);
                children.stream().map(parent::withChild).forEach(thisGen::add);
                
                for (ModelPartParents modelPart : thisGen) {
                    ModelPartParents alrRecorded = inModModelParts.get(modelPart.modelPart);
                    if (alrRecorded == null || alrRecorded.parents.size() < modelPart.parents.size()) {
                        inModModelParts.put(modelPart.modelPart, modelPart);
                    }
                }
            }
            prevGen = thisGen;
            thisGen = new ArrayList<>();
        }
        while (!prevGen.isEmpty());
        
        
        List<ModelCube> modelCubes = inModModelParts.values().stream()
                .flatMap(modelPart -> {
                    List<ModelRenderer.ModelBox> cubes = ClientReflection.getCubes(modelPart.modelPart);
                    return cubes.stream().map(cube -> new ModelCube(cube, modelPart));
                })
                .collect(Collectors.toList());
        return new ModelCubeWeightedList(modelCubes);
    }
    
    private static final ModelCubeWeightedList EMPTY = new ModelCubeWeightedList(ImmutableList.of());
    public static ModelCubeWeightedList empty() {
        return EMPTY;
    }
    
    private final List<ModelCube> modelCubes;
    private List<ModelCube> visibleModelCubes;
    private float totalArea;
    private ModelCubeWeightedList(List<ModelCube> modelCubes) {
        this.modelCubes = modelCubes;
    }
    
    public boolean cacheVisibleCubes() {
        visibleModelCubes = modelCubes.stream().filter(modelCube -> modelCube.modelPart.modelPart.visible).collect(Collectors.toList());
        totalArea = modelCubes.stream().map(cube -> cube.area).reduce(Float::sum).orElse(0f);
        return !visibleModelCubes.isEmpty() && totalArea > 0;
    }
    
    public ModelCube getRandomCube(Random random) {
        float num = random.nextFloat() * totalArea;
        for (ModelCube modelCube : modelCubes) {
            if (num < modelCube.area) {
                return modelCube;
            }
            num -= modelCube.area;
        }
        throw new IllegalStateException();
    }
    
    
    
    public static class ModelCube {
        public final ModelRenderer.ModelBox cube;
        private final float area;
        private final ModelPartParents modelPart;
        
        private ModelCube(ModelRenderer.ModelBox cube, ModelPartParents modelPart) {
            this.cube = cube;
            float x = cube.maxX - cube.minX;
            float y = cube.maxY - cube.minY;
            float z = cube.maxZ - cube.minZ;
            this.area = 2 * (x * y + y * z + z * x);
            this.modelPart = modelPart;
        }
        
        public ModelRenderer.ModelBox cube() {
            return cube;
        }
        
        public void translateAndRotate(MatrixStack matrixStack) {
            modelPart.translateAndRotate(matrixStack);
        }
    }
    
    private static class ModelPartParents {
        private final ModelRenderer modelPart;
        private final List<ModelRenderer> parents;
        
        public void translateAndRotate(MatrixStack matrixStack) {
            for (ModelRenderer parent : parents) {
                parent.translateAndRotate(matrixStack);
            }
            modelPart.translateAndRotate(matrixStack);
        }
        
        
        private ModelPartParents(ModelRenderer modelPart) {
            this(modelPart, ImmutableList.of());
        }
        
        private ModelPartParents(ModelRenderer modelPart, List<ModelRenderer> parents) {
            this.modelPart = modelPart;
            this.parents = parents;
        }
        
        private ModelPartParents withChild(ModelRenderer child) {
            return new ModelPartParents(child, new ImmutableList.Builder<ModelRenderer>()
                    .addAll(this.parents).add(this.modelPart).build());
        }
    }
}
