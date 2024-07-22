package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopWallClimbPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.OptionalFloat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public class HamonWallClimbing2 extends HamonAction {

    public HamonWallClimbing2(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!(MCUtil.isHandFree(user, Hand.MAIN_HAND) && MCUtil.isHandFree(user, Hand.OFF_HAND))) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        return user.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> {
            if (target.getType() == TargetType.BLOCK && target.getFace() != null && target.getFace().getAxis() != Direction.Axis.Y) {
                Vector3d vecToBlock = Vector3d.atLowerCornerOf(target.getFace().getOpposite().getNormal()).scale(MAX_WALL_DISTANCE);
                Vector3d collide = MCUtil.collide(user, vecToBlock);
                if (!collide.equals(vecToBlock)) {
                    return ActionConditionResult.POSITIVE;
                }
            }
            
            return ActionConditionResult.NEGATIVE;
        })
        .orElse(ActionConditionResult.NEGATIVE);
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return power.getUser().getCapability(LivingUtilCapProvider.CAPABILITY)
                .map(cap -> cap.isWallClimbing()).orElse(false);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                if (target.getType() == TargetType.BLOCK && target.getFace() != null && target.getFace().getAxis() != Direction.Axis.Y) {
                    Direction face = target.getFace();
                    float yRot = 180 - face.toYRot();
                    if (!cap.isWallClimbing() || cap.getWallClimbYRot().orElseGet(() -> yRot) != yRot) {
                        cap.setWallClimbing(true, true, -1, OptionalFloat.of(yRot));
                        if (user instanceof PlayerEntity) {
                            ((PlayerEntity) user).displayClientMessage(new TranslationTextComponent(
                                    "jojo.message.wall_climb.hint_jump", new KeybindTextComponent("key.jump")), true);
                        }
                        return;
                    }
                }
            });
        }
    }
    
    private static final double MAX_WALL_DISTANCE = 0.5;
    public static boolean travelWallClimb(PlayerEntity player, Vector3d inputVec) {
        LazyOptional<LivingUtilCap> playerData = player.getCapability(LivingUtilCapProvider.CAPABILITY);
        boolean isWallClimbing = playerData.map(cap -> cap.isWallClimbing()).orElse(false);
        if (isWallClimbing) {
            player.fallDistance = 0;
            
            LivingUtilCap wallClimbData = playerData.resolve().get();
            float climbYRot = wallClimbData.getWallClimbYRot().orElseGet(() -> player.yBodyRot) * MathUtil.DEG_TO_RAD;
            Vector3d gripVec = new Vector3d(0, 0, MAX_WALL_DISTANCE).yRot(climbYRot);
            
            if (!MCUtil.itemHandFree(player.getItemInHand(Hand.MAIN_HAND)) || !MCUtil.itemHandFree(player.getItemInHand(Hand.OFF_HAND))) {
                stopWallClimbing(player, wallClimbData);
                return false;
            }
            
            Vector3d collide = MCUtil.collide(player, gripVec);
            if (collide.subtract(gripVec).lengthSqr() < 1E-7) {
                stopWallClimbing(player, wallClimbData);
                return false;
            }
            
            if (player.isLocalPlayer()) {
//                double xPrev = player.getX();
//                double yPrev = player.getY();
//                double zPrev = player.getZ();
                double climbSpeed = wallClimbData.getWallClimbSpeed() * player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                boolean canPullUp = false;

                Vector3d movement = new Vector3d(inputVec.x * 0.75, inputVec.z, 0);
                if (movement.lengthSqr() > 1) {
                    movement = movement.normalize();
                }
                movement = movement.scale(climbSpeed);
                if (climbYRot != 0) {
                    movement = movement.yRot(climbYRot);
                }
                
                Vector3d collideAfterMove;
                AxisAlignedBB gripBox = player.getBoundingBox().contract(0, -player.getBbHeight() * 0.75, 0).move(0, -player.getBbHeight() * 0.25, 0);
                if (movement.y < 0) {
                    // stop climbing if standing on a solid block
                    if (player.isOnGround()) {
                        stopWallClimbing(player, wallClimbData);
                        return false;
                    }
                    else {
                        // make sure the player doesn't fall down
                        collideAfterMove = MCUtil.collide(player, 
                                gripBox.move(0, movement.y, 0), 
                                gripVec);
                        if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                            movement = new Vector3d(movement.x, 0, movement.z);
                        }
                    }
                }
                
                // check if the player is at the top of a wall
                collideAfterMove = MCUtil.collide(player, gripBox.move(0, player.getBbHeight() * 0.25, 0), gripVec);
                if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                    if (movement.y > 0) {
                        movement = new Vector3d(movement.x, 0, movement.z);
                    }
                    canPullUp = true;
                }
                
                // make sure the player doesn't fall while moving to the left/right if a horizontal end of a wall is reached
                collideAfterMove = MCUtil.collide(player, 
                        gripBox.move(movement.normalize().scale(movement.length() + player.getBbWidth())),
                        gripVec);
                if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                    movement = new Vector3d(0, movement.y, 0);
                }
                
//                if (player.isLocalPlayer()) {
                    ClientPlayerEntity clientPlayer = (ClientPlayerEntity) player; // monkaS
                    boolean isJumping = clientPlayer.input.jumping;
                    if (isJumping) {
                        stopWallClimbing(player, wallClimbData);
                        if (canPullUp) {
                            // TODO pulling up animation
                            Vector3d pullUpMovement = new Vector3d(0, player.getBbHeight() * 0.75 + 0.1, 0);
                            player.setDeltaMovement(pullUpMovement);
                        }
                        return false;
                    }
                    
                    boolean isMoving = movement.lengthSqr() > 1E-7;
                    ClientUtil.setPlayerHandsBusy(player, isMoving);
                    InputHandler.getInstance().wallClimbClientTick(isMoving, wallClimbData);
//                }
                
                player.setDeltaMovement(movement);
                player.move(MoverType.SELF, player.getDeltaMovement());
                movement = player.getDeltaMovement();
                
                player.calculateEntityAnimation(player, false);

//                player.checkMovementStatistics(player.getX() - xPrev, player.getY() - yPrev, player.getZ() - zPrev); // doesn't do anything anyway
                return true;
            }
        }
        return false;
    }
    
    private static void stopWallClimbing(PlayerEntity player, LivingUtilCap wallClimbing) {
        if (!player.level.isClientSide()) {
            wallClimbing.stopWallClimbing();
        }
        else if (player.isLocalPlayer()) {
            PacketManager.sendToServer(new ClStopWallClimbPacket());
        }
    }
    
    public double getHamonWallClimbSpeed(LivingEntity player) {
        Optional<INonStandPower> powerOptional = INonStandPower.getNonStandPowerOptional(player).resolve();
        Optional<HamonData> hamonOptional = powerOptional.flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
        return hamonOptional.map(hamon -> {
            INonStandPower power = powerOptional.get();
            double speed = (1.2 + hamon.getBreathingLevel() * 0.004 + hamon.getHamonControlLevel() * 0.00667)
                    * hamon.getActionEfficiency(power.getMaxEnergy() / 2, false);
            return speed;
        }).orElse(0.0);
    }
    
    public float getTickEnergyCost(INonStandPower power, boolean isMoving) {
        float cost = getHeldTickEnergyCost(power);
        if (!isMoving) {
            cost *= 4;
        }
        return cost;
    }
    
}
