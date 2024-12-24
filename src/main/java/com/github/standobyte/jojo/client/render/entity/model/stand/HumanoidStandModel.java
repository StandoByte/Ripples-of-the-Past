package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.particle.custom.StandCrumbleParticle;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseSided;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.StandTwoHandedBarrageAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.TargetHitPart;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

// Made with Blockbench 3.9.2


public class HumanoidStandModel<T extends StandEntity> extends StandEntityModel<T> {
    protected ModelRenderer root;
    protected ModelRenderer head;
    protected ModelRenderer body;
    protected ModelRenderer upperPart;
    protected ModelRenderer torso;
    @Deprecated protected XRotationModelRenderer leftArm;
    protected ModelRenderer leftArmXRot;
    protected ModelRenderer leftArmBone;
    protected ModelRenderer leftArmJoint;
    protected ModelRenderer leftForeArm;
    @Deprecated protected XRotationModelRenderer rightArm;
    protected ModelRenderer rightArmXRot;
    protected ModelRenderer rightArmBone;
    protected ModelRenderer rightArmJoint;
    protected ModelRenderer rightForeArm;
    @Deprecated protected XRotationModelRenderer leftLeg;
    protected ModelRenderer leftLegXRot;
    protected ModelRenderer leftLegBone;
    protected ModelRenderer leftLegJoint;
    protected ModelRenderer leftLowerLeg;
    @Deprecated protected XRotationModelRenderer rightLeg;
    protected ModelRenderer rightLegXRot;
    protected ModelRenderer rightLegBone;
    protected ModelRenderer rightLegJoint;
    protected ModelRenderer rightLowerLeg;
    

    public HumanoidStandModel() {
        this(128, 128);
    }
    
    public HumanoidStandModel(int textureWidth, int textureHeight) {
        this(RenderType::entityTranslucent, textureWidth, textureHeight);
    }
    
    public static <T extends StandEntity> HumanoidStandModel<T> createBasic() {
        HumanoidStandModel<T> model = new HumanoidStandModel<>();
        model.addHumanoidBaseBoxes(null);
        return model;
    }
    
    public HumanoidStandModel(Function<ResourceLocation, RenderType> renderType, int textureWidth, int textureHeight) {
        super(renderType, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);


        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);


        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);

        leftArm = convertLimb(new ModelRenderer(this));
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);

        rightArm = convertLimb(new ModelRenderer(this));
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);

        leftLeg = convertLimb(new ModelRenderer(this));
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        body.addChild(leftLeg);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);

        rightLeg = convertLimb(new ModelRenderer(this));
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        body.addChild(rightLeg);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        
        
        baseHumanoidBoxGenerators = ImmutableMap.<Supplier<ModelRenderer>, Consumer<ModelRenderer>>builder()
                .put(() -> head, part ->          part.texOffs(0, 0)    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false))
                .put(() -> torso, part ->         part.texOffs(0, 64)   .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false))
                .put(() -> leftArm, part ->       part.texOffs(32, 108) .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> leftArmJoint, part ->  part.texOffs(32, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true))
                .put(() -> leftForeArm, part ->   part.texOffs(32, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(() -> rightArm, part ->      part.texOffs(0, 108)  .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> rightArmJoint, part -> part.texOffs(0, 102)  .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false))
                .put(() -> rightForeArm, part ->  part.texOffs(0, 118)  .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(() -> leftLeg, part ->       part.texOffs(96, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> leftLegJoint, part ->  part.texOffs(96, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true))
                .put(() -> leftLowerLeg, part ->  part.texOffs(96, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(() -> rightLeg, part ->      part.texOffs(64, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(() -> rightLegJoint, part -> part.texOffs(64, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false))
                .put(() -> rightLowerLeg, part -> part.texOffs(64, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .build();
    }
    
    @Deprecated
    protected final XRotationModelRenderer convertLimb(ModelRenderer limbModelPart) {
        return new XRotationModelRenderer(this);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        
        if (root == null) {
            root = new ModelRenderer(this);
            root.setPos(0.0F, 0.0F, 0.0F);
            root.addChild(head);
            root.addChild(body);
        }
        
        putNamedModelPart("head", head);
        putNamedModelPart("body", body);
        putNamedModelPart("upperPart", upperPart);
        putNamedModelPart("torso", torso);
        putNamedModelPart("leftArm", leftArm);
        putNamedModelPart("leftArmXRot", leftArmXRot);
        putNamedModelPart("leftArmBone", leftArmBone);
        putNamedModelPart("leftForeArm", leftForeArm);
        putNamedModelPart("rightArm", rightArm);
        putNamedModelPart("rightArmXRot", rightArmXRot);
        putNamedModelPart("rightArmBone", rightArmBone);
        putNamedModelPart("rightForeArm", rightForeArm);
        putNamedModelPart("leftLeg", leftLeg);
        putNamedModelPart("leftLegXRot", leftLegXRot);
        putNamedModelPart("leftLegBone", leftLegBone);
        putNamedModelPart("leftLowerLeg", leftLowerLeg);
        putNamedModelPart("rightLeg", rightLeg);
        putNamedModelPart("rightLegXRot", rightLegXRot);
        putNamedModelPart("rightLegBone", rightLegBone);
        putNamedModelPart("rightLowerLeg", rightLowerLeg);
    }

    @Deprecated private final Map<Supplier<ModelRenderer>, Consumer<ModelRenderer>> baseHumanoidBoxGenerators;
    @Deprecated
    protected final void addHumanoidBaseBoxes(@Nullable Predicate<ModelRenderer> partPredicate) {
        for (Map.Entry<Supplier<ModelRenderer>, Consumer<ModelRenderer>> entry : baseHumanoidBoxGenerators.entrySet()) {
            ModelRenderer modelRenderer = entry.getKey().get();
            if (partPredicate == null || partPredicate.test(modelRenderer)) {
                entry.getValue().accept(modelRenderer);
            }
        }
    }

    @Override
    public void updatePartsVisibility(VisibilityMode mode) {
        VisibilityMode baseMode = mode.baseMode;
        boolean setVisible = !mode.isInverted;
        
        ModelRenderer leftArm = getArm(HandSide.LEFT);
        ModelRenderer rightArm = getArm(HandSide.RIGHT);
        ModelRenderer leftLeg = getLeg(HandSide.LEFT);
        ModelRenderer rightLeg = getLeg(HandSide.RIGHT);
        
        if (baseMode == VisibilityMode.ALL) {
            head.visible = setVisible;
            torso.visible = setVisible;
            leftLeg.visible = setVisible;
            rightLeg.visible = setVisible;
            leftArm.visible = setVisible;
            rightArm.visible = setVisible;
        }
        else {
            head.visible = !setVisible;
            torso.visible = !setVisible;
            leftLeg.visible = !setVisible;
            rightLeg.visible = !setVisible;
            switch (baseMode) {
            case ARMS_ONLY:
                leftArm.visible = setVisible;
                rightArm.visible = setVisible;
                break;
            case LEFT_ARM_ONLY:
                leftArm.visible = setVisible;
                rightArm.visible = !setVisible;
                break;
            case RIGHT_ARM_ONLY:
                leftArm.visible = !setVisible;
                rightArm.visible = setVisible;
                break;
            case NONE:
                leftArm.visible = !setVisible;
                rightArm.visible = !setVisible;
            default:
                break;
            }
        }
    }

    @Override
    protected void partMissing(StandPart standPart) {
        switch (standPart) {
        case MAIN_BODY:
            head.visible = false;
            torso.visible = false;
            break;
        case ARMS:
            getArm(HandSide.LEFT).visible = false;
            getArm(HandSide.RIGHT).visible = false;
            break;
        case LEGS:
            getLeg(HandSide.LEFT).visible = false;
            getLeg(HandSide.RIGHT).visible = false;
            break;
        }
    }

    @Override
    public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, 
            int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        if (root.visible) {
            pMatrixStack.pushPose();
            root.translateAndRotate(pMatrixStack);
            super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
            pMatrixStack.popPose();
        }
    }
    
    
    
    public void addCrumbleParticleAt(HumanoidPart humanoidPart, ResourceLocation texture, Vector3d pos) {
        Minecraft mc = Minecraft.getInstance();
        StandCrumbleParticle particle = new StandCrumbleParticle(mc.level, pos.x, pos.y, pos.z, 0, 0, 0);
        
        ModelRenderer mainPart;
        switch (humanoidPart) {
        case HEAD: 
            mainPart = head;
            break;
        case TORSO: 
            mainPart = torso;
            break;
        case LEFT_ARM: 
            mainPart = leftArm;
            break;
        case RIGHT_ARM: 
            mainPart = rightArm;
            break;
        case LEFT_LEG: 
            mainPart = leftLeg;
            break;
        case RIGHT_LEG: 
            mainPart = rightLeg;
            break;
        default:
            throw new IllegalArgumentException();
        }
        Random random = new Random();
        List<ModelRenderer> allModelParts = new ArrayList<>();
        addChildren(mainPart, allModelParts);
        ModelRenderer randomPart = allModelParts.get(random.nextInt(allModelParts.size()));
        ObjectList<ModelRenderer.ModelBox> cubes = randomPart.cubes;
        if (!cubes.isEmpty()) {
            ModelRenderer.ModelBox cube = cubes.get(random.nextInt(cubes.size()));
            ModelRenderer.TexturedQuad[] polygons = cube.polygons;
            ModelRenderer.TexturedQuad polygon = polygons[random.nextInt(polygons.length)];
            if (polygon != null) {
                ModelRenderer.PositionTextureVertex[] vertices = polygon.vertices;
                if (vertices.length > 0) {
                    float u0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).min().getAsDouble();
                    float v0 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).min().getAsDouble();
                    float u1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.u).max().getAsDouble();
                    float v1 = (float) Arrays.stream(vertices).mapToDouble(vertex -> vertex.v).max().getAsDouble();
                    particle.setTextureAndUv(texture, u0, v0, u1, v1);
                    mc.particleEngine.add(particle);
                }
            }
        }
    }
    
    private void addChildren(ModelRenderer parent, Collection<ModelRenderer> collection) {
        collection.add(parent);
        for (ModelRenderer child : parent.children) {
            addChildren(child, collection);
        }
    }
    
    private enum HumanoidPart {
        HEAD,
        TORSO,
        LEFT_ARM,
        RIGHT_ARM,
        LEFT_LEG,
        RIGHT_LEG;
    }
    
    
    
    @Override
    @Deprecated
    protected void initActionPoses() {
        super.initActionPoses();
        
        RotationAngle[] jabRightAngles1 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -15, 0),
                RotationAngle.fromDegrees(leftArm, -7.5F, 0, -15),
                RotationAngle.fromDegrees(leftForeArm, -100, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -105, 0, -15)
        };
        RotationAngle[] jabRightAngles2 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -20F, 0),
                RotationAngle.fromDegrees(leftArm, 30F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -107.5F, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        RotationAngle[] jabRightAngles3 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -12.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -17.5F, 0),
                RotationAngle.fromDegrees(leftArm, 37.5F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -115, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, -81.9244F, 11.0311F, 70.2661F),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        RotationAngle[] jabRightAngles4 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -3.75F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -3.75F, 0),
                RotationAngle.fromDegrees(leftArm, 5.63F, 0, -20.62F),
                RotationAngle.fromDegrees(leftForeArm, -103.75F, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        
        ModelAnim<T> armsRotation = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            float xRot = xRotRad * rotationAmount;
            setSecondXRot(leftArm, xRot);
            setSecondXRot(rightArm, xRot);
        };
        
        ModelAnim<T> armsRotationFull = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            setSecondXRot(leftArm, xRotRad);
            setSecondXRot(rightArm, xRotRad);
        };
        
        ModelAnim<T> armsRotationBack = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            float xRot = xRotRad * (1 - rotationAmount);
            setSecondXRot(leftArm, xRot);
            setSecondXRot(rightArm, xRot);
        };
        
        IModelPose<T> jabStart = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles1)),
                new ModelPose<T>(jabRightAngles1));
        
        IModelPose<T> jabArmTurn = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles2)).setAdditionalAnim(armsRotation),
                new ModelPose<T>(jabRightAngles2).setAdditionalAnim(armsRotation));
        
        IModelPose<T> jabImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles3)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(jabRightAngles3).setAdditionalAnim(armsRotationFull)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabArmTurnBack = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles4)).setAdditionalAnim(armsRotationBack),
                new ModelPose<T>(jabRightAngles4).setAdditionalAnim(armsRotationBack)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabEnd = new ModelPoseSided<>(
                new ModelPose<T>(jabRightAngles1),
                new ModelPose<T>(mirrorAngles(jabRightAngles1)));
        
        actionAnim.putIfAbsent(StandPose.LIGHT_ATTACK, 
                new PosedActionAnimation.Builder<T>()
                
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<T>(jabStart)
                        .addPose(0.5F, jabArmTurn)
                        .addPose(0.75F, jabImpact)
                        .build(jabImpact))
                
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<T>(jabImpact)
                        .addPose(0.25F, jabImpact)
                        .addPose(0.5F, jabArmTurnBack)
                        .build(jabEnd))
                
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<T>(jabEnd)
                        .addPose(0.75F, jabEnd)
                        .build(idlePose))
                
                .build(idlePose));

        
        
        RotationAngle[] heavyRightStart = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 15, 0),
                RotationAngle.fromDegrees(upperPart, 0, 15, 0),
                RotationAngle.fromDegrees(leftArm, -90, 0, -90),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 60),
                RotationAngle.fromDegrees(rightForeArm, -135, 0, 0)
        };
        RotationAngle[] heavyRightBackswing = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 26.25F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 26.25F, 0),
                RotationAngle.fromDegrees(leftArm, -67.5F, 0, -90),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 30, 0, 60),
                RotationAngle.fromDegrees(rightForeArm, -120, 0, 0)
        };
        RotationAngle[] heavyRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -26.25F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -26.25F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, -135, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, -67.5F, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        
        IModelPose<T> heavyStart = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightStart)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightStart).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> heavyBackswing = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightBackswing)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightBackswing).setAdditionalAnim(armsRotationFull)).setEasing(sw -> sw * sw);
        
        IModelPose<T> heavyImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightImpact)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightImpact).setAdditionalAnim(armsRotationFull)).setEasing(sw -> sw * sw * sw);
        
        PosedActionAnimation<T> heavyAttackAnim = new PosedActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<T>(heavyStart)
                        .addPose(0.95F, heavyBackswing)
                        .build(heavyImpact))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<T>(heavyImpact, idlePose)
                        .setEasing(pr -> Math.max(2F * (pr - 1) + 1, 0F)))
                .build(idlePose);
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK, heavyAttackAnim);
        
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK_FINISHER, heavyAttackAnim);
        
        
        
        actionAnim.putIfAbsent(StandPose.BLOCK, new PosedActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<T>(new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(rightForeArm, -90, 30, -90),
                        RotationAngle.fromDegrees(leftForeArm, -90, -30, 90)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
                    float blockXRot = MathHelper.clamp(xRotRad, -60 * MathUtil.DEG_TO_RAD, 60 * MathUtil.DEG_TO_RAD) / 2;
                    rightArm.xRot = -1.5708F + blockXRot;
                    leftArm.xRot = rightArm.xRot;

                    rightArm.yRot = -blockXRot / 2;
                    leftArm.yRot = -rightArm.yRot;

                    rightArm.zRot = -Math.abs(blockXRot) / 2 + 0.7854F;
                    leftArm.zRot = -rightArm.zRot;
                }))
                .build(idlePose));
        
        

        RotationAngle[] barrageRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -30, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, -135, 0, 0),
                RotationAngle.fromDegrees(rightArm, -90, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        
        IModelPose<T> barrageHitStart = new ModelPoseSided<>(
                new ModelPose<T>(barrageRightImpact).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> barrageHitImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(barrageRightImpact).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> barrageRecovery = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -22.5F),
                RotationAngle.fromDegrees(leftForeArm, -75, 7.5F, 22.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -75, -7.5F, -22.5F)
        });
        
        actionAnim.putIfAbsent(StandPose.BARRAGE, new StandTwoHandedBarrageAnimation<T>(this, 
                new ModelPoseTransition<T>(barrageHitStart, barrageHitImpact).setEasing(HumanoidStandModel::barrageHitEasing), 
                new ModelPoseTransitionMultiple.Builder<T>(new ModelPose<T>(
                        RotationAngle.fromDegrees(body, 0, 0, 0),
                        RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                        RotationAngle.fromDegrees(leftArm, -33.75F, 0, -75),
                        RotationAngle.fromDegrees(leftForeArm, -67.5F, 0, 0),
                        RotationAngle.fromDegrees(rightArm, -33.75F, 0, 75),
                        RotationAngle.fromDegrees(rightForeArm, -67.5F, 0, 0)).setAdditionalAnim(armsRotationFull))
                .addPose(0.25F, barrageRecovery)
                .addPose(0.5F, barrageRecovery)
                .build(idlePose)));
    }
    
    public static float barrageHitEasing(float loopProgress) {
        if (loopProgress < 0.5F) {
            return loopProgress * loopProgress * loopProgress * 8;
        }
        if (loopProgress < 1.0F) {
            float halfSw = 2 * loopProgress - 1;
            return 1 - halfSw * halfSw * halfSw;
        }
        return 0F;
    }
    
    protected RotationAngle[] mirrorAngles(RotationAngle[] angles) {
        RotationAngle[] mirrored = new RotationAngle[angles.length];
        for (int i = 0; i < angles.length; i++) {
            RotationAngle angle = angles[i];
            mirrored[i] = new RotationAngle(getOppositeHandside(angle.modelRenderer), angle.angleX, -angle.angleY, -angle.angleZ);
        }
        return mirrored;
    }
    
    @Override
    public ModelRenderer getArm(HandSide side) {
        switch (side) {
        case LEFT:
            return leftArmXRot != null ? leftArmXRot : leftArm;
        case RIGHT:
            return rightArmXRot != null ? rightArmXRot : rightArm;
        }
        return null;
    }
    
    @Override
    public ModelRenderer getArmNoXRot(HandSide side) {
        switch (side) {
        case LEFT:
            return leftArmBone != null ? leftArmBone : leftArm;
        case RIGHT:
            return rightArmBone != null ? rightArmBone : rightArm;
        }
        return null;
    }
    
    protected ModelRenderer getForeArm(HandSide side) {
        switch (side) {
        case LEFT:
            return leftForeArm;
        case RIGHT:
            return rightForeArm;
        }
        return null;
    }
    
    public ModelRenderer getHead() {
        return head;
    }
    
    public ModelRenderer getTorso() {
        return torso;
    }
    
    public ModelRenderer getLeg(HandSide side) {
        switch (side) {
        case LEFT:
            return leftLegXRot != null ? leftLegXRot : leftLeg;
        case RIGHT:
            return rightLegXRot != null ? rightLegXRot : rightLeg;
        }
        return null;
    }
    
    public ModelRenderer getLegNoXRot(HandSide side) {
        switch (side) {
        case LEFT:
            return leftLegBone != null ? leftLegBone : leftLeg;
        case RIGHT:
            return rightLegBone != null ? rightLegBone : rightLeg;
        }
        return null;
    }

    @Override
    protected ModelPose<T> initPoseReset() {
        return new ModelPose<T>(
                new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0, 0, 0),
                        new RotationAngle(torso, 0, 0, 0),
                        new RotationAngle(rightArm, 0, 0, 0),
                        new RotationAngle(rightForeArm, 0, 0, 0),
                        new RotationAngle(leftArm, 0, 0, 0),
                        new RotationAngle(leftForeArm, 0, 0, 0),
                        new RotationAngle(rightLeg, 0, 0, 0),
                        new RotationAngle(rightLowerLeg, 0, 0, 0),
                        new RotationAngle(leftLeg, 0, 0, 0),
                        new RotationAngle(leftLowerLeg, 0, 0, 0)
                });
    }

    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        
        if (ClientModSettings.getSettingsReadOnly()._standMotionTilt) {
            motionTilt(entity, ticks);
        }
        
        rotateJoint(leftArmJoint, leftForeArm);
        rotateJoint(rightArmJoint, rightForeArm);
        rotateJoint(leftLegJoint, leftLowerLeg);
        rotateJoint(rightLegJoint, rightLowerLeg);
    }

    protected void motionTilt(T entity, float ticks) {
        if (entity.getStandPose() != StandPose.SUMMON) {
            Vector3d tiltVec;
            if (MathHelper.floor(entity.lastMotionTiltTick) != MathHelper.floor(ticks)) {
                Vector3d motion = entity.position().subtract(entity.xOld, entity.yOld, entity.zOld);
                
                tiltVec = motion.yRot(entity.yBodyRot * MathUtil.DEG_TO_RAD).scale(2);
                tiltVec = new Vector3d(tiltVec.z, 0, tiltVec.x);
                double motionSqr = tiltVec.lengthSqr();
                if (motionSqr > Math.pow(Math.PI / 4, 2)) {
                    tiltVec = tiltVec.normalize().scale(Math.PI / 4);
                }
                
//                Vector3d tiltDiff = tiltVec.subtract(entity.prevTiltVec);
//                if (tiltDiff.lengthSqr() > 1.0E-4) {
//                    double maxDiff;
//                    if (entity.motionDist >= entity.prevMotionDist) {
//                        maxDiff = 0.1;
//                    }
//                    else {
//                        maxDiff = 0.4;
//                    }
//                    if (tiltDiff.lengthSqr() > maxDiff * maxDiff) {
//                        tiltDiff = tiltDiff.normalize().scale(maxDiff);
//                    }
//                    tiltVec = entity.prevTiltVec.add(tiltDiff);
//                }
                
//                entity.prevMotionDist = entity.motionDist;
//                entity.motionVec = motion;
//                entity.motionDist = motion.length();
                entity.prevTiltVec = entity.tiltVec;
                entity.tiltVec = tiltVec;
                entity.lastMotionTiltTick = ticks;
            }
            else {
                tiltVec = entity.tiltVec;
            }
            
            float partialTick = MathHelper.frac(ticks);
            tiltVec = new Vector3d(
                    MathHelper.lerp(partialTick, entity.prevTiltVec.x, entity.tiltVec.x),
                    MathHelper.lerp(partialTick, entity.prevTiltVec.y, entity.tiltVec.y),
                    MathHelper.lerp(partialTick, entity.prevTiltVec.z, entity.tiltVec.z));
            
            double tiltSqr = tiltVec.lengthSqr();
            if (tiltSqr > 1.0E-4) {
                double tilt = Math.sqrt(tiltSqr);
                double d1 = MathHelper.clamp(1 - tilt / Math.PI * 4, 0, 1);
                boolean idlePose = entity.getStandPose() == StandPose.IDLE;

                body.xRot += tiltVec.x;
                if (idlePose) {
                    body.zRot += tiltVec.z;
                    body.yRot *= d1;
                }

                double d = MathHelper.clamp(1 - 1.5 * tilt / Math.PI, 0, 1);
                leftLowerLeg.xRot *= d;
                rightLowerLeg.xRot *= d;
                leftLowerLeg.yRot *= d;
                rightLowerLeg.yRot *= d;
                leftLowerLeg.zRot *= d;
                rightLowerLeg.zRot *= d;
                if (idlePose) {
                    leftForeArm.xRot *= d;
                    rightForeArm.xRot *= d;
                    leftForeArm.yRot *= d;
                    rightForeArm.yRot *= d;
                    leftForeArm.zRot *= d;
                    rightForeArm.zRot *= d;
                }
                
                double d2 = MathHelper.clamp(1 - tilt / (2 * Math.PI), 0, 1);
                leftLeg.xRot *= d2;
                rightLeg.xRot *= d2;
                leftLeg.yRot *= d2;
                rightLeg.yRot *= d2;
                leftLeg.zRot *= d2;
                rightLeg.zRot *= d2;
                if (idlePose) {
                    leftArm.xRot *= d2;
                    rightArm.xRot *= d2;
                    leftArm.yRot *= d2;
                    rightArm.yRot *= d2;
                    leftArm.zRot *= d2;
                    rightArm.zRot *= d2;
                }
                else {
                    addSecondXRot(leftArm, (float) -tiltVec.x);
                    addSecondXRot(rightArm, (float) -tiltVec.x);
                }
            }
        }
    }

    protected void rotateJoint(ModelRenderer joint, ModelRenderer limbPart) {
        if (joint != null) {
            joint.xRot = limbPart.xRot / 2;
            joint.yRot = limbPart.yRot / 2;
            joint.zRot = limbPart.zRot / 2;
        }
    }

    @Override
    public Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(head);
    }

    @Override
    public Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body);
    }
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftArm, rightArm);
        oppositeHandside.put(leftForeArm, rightForeArm);
        oppositeHandside.put(leftLeg, rightLeg);
        oppositeHandside.put(leftLowerLeg, rightLowerLeg);
    }
    
    @Override
    public void translateToHand(HandSide handSide, MatrixStack matrixStack) {
        matrixStack.translate(handSide == HandSide.LEFT ? -0.0625 : 0.0625, 0, 0);
        body.translateAndRotate(matrixStack);
        upperPart.translateAndRotate(matrixStack);
        
        ModelRenderer arm = getArm(handSide);
        arm.translateAndRotate(matrixStack);

        ModelRenderer foreArm = getForeArm(handSide);
        foreArm.translateAndRotate(matrixStack);
        matrixStack.translate(
                (double)(-foreArm.x / 16.0F), 
                (double)(-foreArm.y / 16.0F), 
                (double)(-foreArm.z / 16.0F));
    }
    
    
    protected Map<TargetHitPart, List<ModelRenderer.ModelBox>> cubesCache;
    @Override
    public ModelRenderer.ModelBox getRandomCubeAt(TargetHitPart entityPart) {
        if (cubesCache == null) {
            cacheCubes();
        }
        List<ModelRenderer.ModelBox> cubes = cubesCache.get(entityPart);
        if (cubes != null && !cubes.isEmpty()) {
            return cubes.get(RANDOM.nextInt(cubes.size()));
        }
        return null;
    }
    
    protected void cacheCubes() {
        cubesCache = new EnumMap<>(TargetHitPart.class);
        List<ModelRenderer> headParts = new ArrayList<>();
        List<ModelRenderer> legsParts = new ArrayList<>();
        List<ModelRenderer> middleParts = new ArrayList<>();
        addChildrenRecursive(head, headParts);
        addChildrenRecursive(leftLeg, legsParts);
        addChildrenRecursive(rightLeg, legsParts);
        addChildrenRecursive(torso, middleParts);
        addChildrenRecursive(leftArm, middleParts);
        addChildrenRecursive(rightArm, middleParts);
        cubesCache.put(TargetHitPart.HEAD, allCubes(headParts));
        cubesCache.put(TargetHitPart.TORSO_ARMS, allCubes(middleParts));
        cubesCache.put(TargetHitPart.LEGS, allCubes(legsParts));
    }
    
    public static void addChildrenRecursive(ModelRenderer modelPart, Collection<ModelRenderer> collection) {
        collection.add(modelPart);
        for (ModelRenderer child : modelPart.children) {
            addChildrenRecursive(child, collection);
        }
    }
    
    public static List<ModelRenderer.ModelBox> allCubes(List<ModelRenderer> modelParts) {
        List<ModelRenderer.ModelBox> cubes = modelParts.stream()
                .flatMap(modelPart -> modelPart.cubes.stream())
                .collect(Collectors.toList());
        return cubes;
    }
    
    // TODO select quads with weight depending on their size
    public static ModelRenderer.TexturedQuad getRandomQuad(ModelRenderer.ModelBox cube) {
        if (cube == null) return null;
        ModelRenderer.TexturedQuad[] polygons = cube.polygons;
        ModelRenderer.TexturedQuad polygon = polygons[RANDOM.nextInt(polygons.length)];
        return polygon;
    }
    
}