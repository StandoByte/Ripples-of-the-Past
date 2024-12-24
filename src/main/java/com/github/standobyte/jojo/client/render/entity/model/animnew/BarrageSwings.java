package com.github.standobyte.jojo.client.render.entity.model.animnew;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandActionAnimation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandPoseData;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class BarrageSwings {
    private List<BarrageSwing> barrageSwings = new LinkedList<>();
    private float loopLast = -1;

    public void addSwing(BarrageSwing swing) {
        barrageSwings.add(swing);
    }
    
    public void updateSwings(Minecraft mc) {
        if (!mc.isPaused() && !barrageSwings.isEmpty()) {
            float timeDelta = mc.getDeltaFrameTime();
            Iterator<BarrageSwing> iter = barrageSwings.iterator();
            while (iter.hasNext()) {
                BarrageSwing swing = iter.next();
                swing.addDelta(timeDelta);
                if (swing.removeSwing()) {
                    iter.remove();
                }
            }
        }
    }
    
    public Iterable<BarrageSwing> getSwings() {
        return barrageSwings;
    }
    
    public void setLoopCount(float loopCount) {
        this.loopLast = loopCount;
    }
    
    public float getLoopCount() {
        return loopLast;
    }
    
    
    
    
    
    public static final Map<String, AddBarrageSwing> BARRAGE_SWING_TYPES = Util.make(new HashMap<>(), map -> {
        map.put("TWO_HANDED", TwoHandedBarrageLoopSwing::addSwing);
    });
    
    public static <T extends StandEntity> boolean onBarrageAnim(String key, T entity, StandEntityModel<T> model, 
            StandActionAnimation barrageAnim, float entityTicks, float curAnimTime) {
        AddBarrageSwing addSwingFunction = BARRAGE_SWING_TYPES.get(key);
        if (addSwingFunction != null) {
            return addSwingFunction.addSwing(entity, model, entity.getBarrageSwings(), barrageAnim, entityTicks, curAnimTime);
        }
        return false;
    }
    
    @FunctionalInterface
    public static interface AddBarrageSwing {
        <T extends StandEntity> boolean addSwing(T entity, StandEntityModel<T> model, BarrageSwings swings, 
                StandActionAnimation barrageAnim, float entityTicks, float curAnimTimeSecs);
    }
    
    
    public abstract static class BarrageSwing {
        protected static final Random RANDOM = new Random();
        protected StandActionAnimation barrageAnim;
        protected float ticks;
        protected float ticksMax;
        
        public BarrageSwing(StandActionAnimation barrageAnim, float startingAnim, float animMax) {
            this.barrageAnim = barrageAnim;
            this.ticks = startingAnim;
            this.ticksMax = animMax;
        }
        
        public void addDelta(float delta) {
            ticks += delta * 0.75F;
        }
        
        public boolean removeSwing() {
            return ticks >= ticksMax * 0.75F;
        }
        
        public abstract <T extends StandEntity> void poseAndRender(T entity, StandEntityModel<T> model, 
                MatrixStack matrixStack, IVertexBuilder buffer, float yRotOffsetDeg, float xRotDeg, 
                int packedLight, int packedOverlay, float red, float green, float blue, float alpha);
    }
    
    
    public static class TwoHandedBarrageLoopSwing extends BarrageSwing {
        protected float animTimeOffset;
        protected final HandSide side;
        protected final Vector3d offset;
        protected final float zRot;
        
        public TwoHandedBarrageLoopSwing(StandActionAnimation barrageAnim, float startingAnim, float animMax, 
                HandSide side, double maxOffset, float animTimeOffset) {
            super(barrageAnim, startingAnim, animMax);
            this.animTimeOffset = animTimeOffset;
            this.side = side;
            double upOffset = (RANDOM.nextDouble() - 0.5) * maxOffset;
            double leftOffset = RANDOM.nextDouble() * maxOffset / 2;
            double frontOffset = RANDOM.nextDouble() * 0.5;
            if (side == HandSide.RIGHT) {
                leftOffset *= -1;
            }
            double atan = MathHelper.atan2(upOffset, leftOffset);
            zRot = maxOffset == 0 ? 0 : MathUtil.wrapRadians((float) (Math.PI / 2 - atan));
            offset = new Vector3d(leftOffset, upOffset, frontOffset);
        }
        
        public static <T extends StandEntity> boolean addSwing(T entity, StandEntityModel<T> model, BarrageSwings swings, 
                StandActionAnimation barrageAnim, float entityTicks, float curAnimTimeSecs) {
            if (!entity.getCurrentTaskPhase().filter(phase -> phase == StandEntityAction.Phase.PERFORM).isPresent()) return false;
            
            float lastLoop = swings.getLoopCount();
            float loopLen = 4;
            float loop = entityTicks / loopLen;
            if (entity.animWasBarraging && loop > lastLoop) {
                float hits = StandStatFormulas.getBarrageHitsPerSecond(entity.getAttackSpeed()) / 20F * Math.min(loop - lastLoop, 1) * loopLen;
                int swingsToAdd = MathUtil.fractionRandomInc(hits);
                if (swingsToAdd > 0) {
                    HandSide side = entity.getPunchingHand();
                    double maxOffset = 1 - entity.getPrecision() / 40;
                    if (entity.getRandom().nextBoolean()) side = side.getOpposite();
                    
                    for (int i = 0; i < swingsToAdd; i++) {
                        float x = ((float) i + (entity.getRandom().nextFloat() - 0.5F) * 0.4F) / swingsToAdd;
                        float f = x * loopLen * 0.5F;
                        float addTime = (side == HandSide.LEFT ? loopLen * 0.5f : 0) + (curAnimTimeSecs - curAnimTimeSecs % loopLen);
                        swings.addSwing(new BarrageSwings.TwoHandedBarrageLoopSwing(barrageAnim, f, loopLen, side, maxOffset, addTime));
                        side = side.getOpposite();
                    }
                }
            }
            swings.setLoopCount(loop);
            return true;
        }
        
        @Override
        public <T extends StandEntity> void poseAndRender(T entity, StandEntityModel<T> model, 
                MatrixStack matrixStack, IVertexBuilder buffer, float yRotOffsetDeg, float xRotDeg, 
                int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            model.setVisibility(entity, side == HandSide.LEFT ? VisibilityMode.LEFT_ARM_ONLY : VisibilityMode.RIGHT_ARM_ONLY, false);
            float loopCompletion = ticks / ticksMax;
            float zMult = loopCompletion < 0.5 ? loopCompletion * 2 : (1 - loopCompletion) * 2;
            double zAdditional = 0.5 * zMult;
            Vector3d offsetRot = new Vector3d(offset.x, -offset.y, offset.z + zAdditional).xRot(xRotDeg * MathUtil.DEG_TO_RAD);
            matrixStack.pushPose();
            matrixStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
            model.resetPose(entity);
            StandPoseData standPose = StandPoseData.start()
                    .standPose(model.standPose)
                    .actionPhase(StandEntityAction.Phase.PERFORM)
                    .animTime(ticks + animTimeOffset)
                    .end();
            barrageAnim.poseStand(entity, model, yRotOffsetDeg, xRotDeg, standPose);
            ModelRenderer arm = model.getArmNoXRot(side);
            arm.zRot = arm.zRot + zMult * zRot;
            model.applyXRotation();
            model.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.75F);
            matrixStack.popPose();
        }
    }

}
