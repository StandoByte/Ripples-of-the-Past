package com.github.standobyte.jojo.client.renderer.player;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.LazyOptional;

public class ModdedPlayerModel extends PlayerModel<AbstractClientPlayerEntity> {
    private boolean barrage;
    float yRotation;

    public ModdedPlayerModel(float boxesInflation, boolean slim) {
        super(boxesInflation, slim);
    }

    @Override
    public void setupAnim(AbstractClientPlayerEntity player, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        
        LazyOptional<INonStandPower> powerOptional = INonStandPower.getNonStandPowerOptional(player);
        powerOptional.ifPresent(power -> {
            barrage = power.getHeldAction() == ModActions.JONATHAN_OVERDRIVE_BARRAGE.get();
            if (barrage) {
                attackTime = ticks % 2 / 2F;
            }
        });
        
        super.setupAnim(player, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        
        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
            leftArm.xRot = 3.1416F;
            leftArm.yRot = 0;
            leftArm.zRot = 0;
            rightArm.xRot = 3.1416F;
            rightArm.yRot = 0;
            rightArm.zRot = 0;
        }
        
        powerOptional.ifPresent(power -> {
            float partialTick = ticks - player.tickCount;
            if (power.isActionOnCooldown(ModActions.HAMON_ZOOM_PUNCH.get())) {
                HandSide side = player.getMainArm();
                switch (side) {
                case LEFT:
                    leftArm.visible = false;
                    leftSleeve.visible = false;
                    break;
                case RIGHT:
                    rightArm.visible = false;
                    rightSleeve.visible = false;
                    break;
                }
            }
            
            Action heldAction = power.getHeldAction(true);
            if (heldAction == ModActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                leftLeg.xRot = 0;
                leftLeg.yRot = 0;
                leftLeg.zRot += 0.2;
                rightLeg.xRot = 0;
                rightLeg.yRot = 0;
                rightLeg.zRot -= 0.2;
                yRotation = (power.getHeldActionTicks() + partialTick) * 2F % 360F;
            }
            else {
                yRotation = 0;
                if (heldAction == ModActions.VAMPIRISM_BLOOD_DRAIN.get() || heldAction == ModActions.VAMPIRISM_BLOOD_GIFT.get()) {
                    HandSide side = player.getMainArm();
                    switch (side) {
                    case LEFT:
                        leftArm.xRot = (xRotation - 90F) * MathUtil.DEG_TO_RAD;
                        break;
                    case RIGHT:
                        rightArm.xRot = (xRotation - 90F) * MathUtil.DEG_TO_RAD;
                        break;
                    }
                }
                else if (heldAction == ModActions.VAMPIRISM_FREEZE.get()) {
                    leftArm.xRot = (xRotation - 90F) * MathUtil.DEG_TO_RAD;
                    rightArm.xRot = (xRotation - 90F) * MathUtil.DEG_TO_RAD;
                }
            }
            
            leftPants.copyFrom(leftLeg);
            rightPants.copyFrom(rightLeg);
            leftSleeve.copyFrom(leftArm);
            rightSleeve.copyFrom(rightArm);
            jacket.copyFrom(body);
        });
    }

    @Override
    protected void setupAttackAnimation(AbstractClientPlayerEntity player, float ticks) {
        if (barrage) {
            HandSide side = getAttackArm(player);
            ModelRenderer swingingArm = getArm(side);
            float f = attackTime;
            body.yRot = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
            if (side == HandSide.LEFT) {
                body.yRot *= -1.0F;
            }
            rightArm.z = MathHelper.sin(body.yRot) * 5.0F;
            rightArm.x = -MathHelper.cos(body.yRot) * 5.0F;
            leftArm.z = -MathHelper.sin(body.yRot) * 5.0F;
            leftArm.x = MathHelper.cos(body.yRot) * 5.0F;
            rightArm.yRot += body.yRot;
            leftArm.yRot += body.yRot;
            leftArm.xRot = -1.4678F;
            rightArm.xRot = -1.4678F;
            f = 1.0F - attackTime;
            f = f * f;
            f = f * f;
            f = 1.0F - f;
            float f1 = MathHelper.sin(f * (float)Math.PI);
            float f2 = MathHelper.sin(attackTime * (float)Math.PI) * -(head.xRot - 0.7F) * 0.75F;
            swingingArm.xRot += -(f1 * 1.2F + f2) * 0.5F;
            swingingArm.yRot += body.yRot * 2.0F;
            swingingArm.zRot += MathHelper.sin(attackTime * (float)Math.PI) * -0.4F;
            if (side == HandSide.LEFT) {
                swingingArm.zRot *= -1;
            }
        }
        else {
            super.setupAttackAnimation(player, ticks);
        }
    }
}
