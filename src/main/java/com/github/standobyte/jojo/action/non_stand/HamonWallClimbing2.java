package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopWallClimbPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
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

public class HamonWallClimbing2 extends HamonAction {

    public HamonWallClimbing2(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.areHandsFree(user, Hand.MAIN_HAND, Hand.OFF_HAND)) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        return user.getCapability(PlayerUtilCapProvider.CAPABILITY).map(cap -> {
            if (target.getType() == TargetType.BLOCK && target.getFace() != null && target.getFace().getAxis() != Direction.Axis.Y) {
                Direction blockFace = target.getFace();
//                BlockPos standingOn = user.blockPosition().below().offset(blockFace.getOpposite().getNormal());
//                if (user.isOnGround() && standingOn.equals(target.getBlockPos())) {
//                    return ActionConditionResult.POSITIVE;
//                }
                Vector3d vecToBlock = Vector3d.atLowerCornerOf(blockFace.getOpposite().getNormal()).scale(MAX_WALL_DISTANCE);
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
        return LivingWallClimbing.getHandler(power.getUser()).map(cap -> cap.isWallClimbing()).orElse(false);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            LivingWallClimbing.getHandler(user).ifPresent(cap -> {
                if (target.getType() == TargetType.BLOCK && target.getFace() != null && target.getFace().getAxis() != Direction.Axis.Y) {
                    Direction face = target.getFace();
                    float yRot = 180 - face.toYRot();
                    if (!cap.isWallClimbing() || cap.getWallClimbYRot().orElseGet(() -> yRot) != yRot) {
                        Vector3d vecToBlock = Vector3d.atLowerCornerOf(face.getOpposite().getNormal()).scale(MAX_WALL_DISTANCE);
                        Vector3d collide = MCUtil.collide(user, vecToBlock);
                        double distanceFromWall = user.getBbWidth() * 0.15;
                        Vector3d moveTo = user.position().add(collide).add(Vector3d.atLowerCornerOf(face.getNormal()).scale(distanceFromWall));
                        user.teleportTo(moveTo.x, moveTo.y, moveTo.z);
                        
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
    
    private static final double SIDE_SPEED_MULT = 0.5;
    private static final double DOWN_SPEED_MULT = 0.5;
    private static final double SIDE_VEC_LEN_MULT = 1 / Math.sqrt(1 + SIDE_SPEED_MULT * SIDE_SPEED_MULT);
    private static final double DOWN_SIDE_VEC_LEN_MULT = Math.max(DOWN_SPEED_MULT, SIDE_SPEED_MULT) / Math.sqrt(DOWN_SPEED_MULT * DOWN_SPEED_MULT + SIDE_SPEED_MULT * SIDE_SPEED_MULT);
    private static final double MAX_WALL_DISTANCE = 0.5;
    public static boolean travelWallClimb(PlayerEntity player, Vector3d inputVec) {
        Optional<LivingWallClimbing> playerData = LivingWallClimbing.getHandler(player);
        boolean isWallClimbing = playerData.map(cap -> cap.isWallClimbing()).orElse(false);
        if (isWallClimbing) {
            player.fallDistance = 0;
            
            LivingWallClimbing wallClimbData = playerData.get();
            float climbYRot = wallClimbData.getWallClimbYRot().orElseGet(() -> player.yBodyRot) * MathUtil.DEG_TO_RAD;
            Vector3d gripVec = new Vector3d(0, 0, MAX_WALL_DISTANCE).yRot(climbYRot);
            
            if (!MCUtil.itemHandFree(player.getItemInHand(Hand.MAIN_HAND)) || !MCUtil.itemHandFree(player.getItemInHand(Hand.OFF_HAND))
                    || player.isSpectator()) {
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
                
                Vector3d movement = new Vector3d(inputVec.x * SIDE_SPEED_MULT, inputVec.z > 0 ? inputVec.z : inputVec.z * DOWN_SPEED_MULT, 0);
                if (movement.lengthSqr() > 1) {
                    movement = movement.normalize();
                }
                if (inputVec.x != 0 && inputVec.z != 0) {
                    if (inputVec.z > 0) {
                        climbSpeed *= SIDE_VEC_LEN_MULT;
                    }
                    else {
                        climbSpeed *= DOWN_SIDE_VEC_LEN_MULT;
                    }
                }
                movement = movement.scale(climbSpeed);
                climbSpeed = movement.length();
                if (climbYRot != 0) {
                    movement = movement.yRot(climbYRot);
                }

                Vector3d horizontalMovementOnly = new Vector3d(movement.x, 0, movement.z);
                Vector3d collideAfterMove;
                AxisAlignedBB gripBox = player.getBoundingBox()
                        .contract(0, -player.getBbHeight() * 0.5, 0);
                if (movement.y < 0) {
                    // stop climbing if standing on a solid block
                    if (player.isOnGround()) {
                        stopWallClimbing(player, wallClimbData);
                        return false;
                    }
                    else {
                        // make sure the player doesn't fall down
                        collideAfterMove = MCUtil.collide(player, 
                                gripBox.move(0, -gripBox.getYsize() + movement.y, 0), 
                                gripVec);
                        if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                            movement = horizontalMovementOnly;
                        }
                    }
                }
                
                // check if the player is at the top of a wall
                collideAfterMove = MCUtil.collide(player, gripBox.move(0, gripBox.getYsize(), 0), gripVec);
                if (collideAfterMove.subtract(gripVec).lengthSqr() < 1E-7) {
                    if (movement.y > 0) {
                        movement = horizontalMovementOnly;
                    }
                    canPullUp = true;
                }
                
                // make sure the player doesn't fall while moving to the left/right if a horizontal end of a wall is reached
                collideAfterMove = MCUtil.collide(player, 
                        gripBox.move(horizontalMovementOnly.normalize()
                                .scale(horizontalMovementOnly.length() + player.getBbWidth() + 0.1)),
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
                            player.move(MoverType.SELF, new Vector3d(0, player.getBbHeight(), 0));
//                            Vector3d pullUpMovement = new Vector3d(0, player.getBbHeight() + 0.1, 0);
//                            player.setDeltaMovement(pullUpMovement);
                            player.setDeltaMovement(new Vector3d(0, 0, 0.1).yRot(climbYRot));
                        }
                        return false;
                    }
                    
                    boolean isMoving = movement.lengthSqr() > 1E-7;
                    ClientUtil.setPlayerHandsBusy(player, isMoving);
                    double up = movement.y;
                    double left = movement.yRot(-climbYRot).x;
                    InputHandler.getInstance().wallClimbClientTick(isMoving, wallClimbData);
                    
                    float animSpeed = (float) climbSpeed / MIN_MOVEMENT_SPEED;
                    ModPlayerAnimations.wallClimbing.tickAnimProperties(player, isMoving, 
                            up, left, animSpeed);
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
    private static final float MIN_MOVEMENT_SPEED = 0.06f;
    
    private static void stopWallClimbing(PlayerEntity player, LivingWallClimbing wallClimbing) {
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
                    * hamon.getActionEfficiency(power.getMaxEnergy() / 2, false, ModHamonSkills.WALL_CLIMBING.get());
            return speed;
        }).orElse(0.0);
    }
    
    public float getTickEnergyCost(INonStandPower power, boolean isMoving) {
        float cost = getHeldTickEnergyCost(power);
        if (!isMoving) {
            cost *= 0.25f;
        }
        return cost;
    }
    
    
    public static void tickWallClimbing(INonStandPower power, HamonData hamon, LivingEntity user) {
        LivingWallClimbing.getHandler(user).ifPresent(wallClimbData -> {
            if (wallClimbData.isHamon()) {
                if (hamon.isSkillLearned(ModHamonSkills.WALL_CLIMBING.get())) {
                    boolean isMoving = false;
                    if (user instanceof PlayerEntity) {
                        isMoving = wallClimbData.wallClimbIsMoving;
                    }

                    if (power.getHeldAction() != ModHamonActions.HAMON_BREATH.get()) {
                        boolean consumedEnergy = false;

                        float energyCost = ModHamonActions.HAMON_WALL_CLIMBING.get().getTickEnergyCost(power, isMoving);
                        float points = Math.min(energyCost, power.getEnergy() * hamon.getActionEfficiency(energyCost, false, ModHamonSkills.WALL_CLIMBING.get()));
                        if (power.hasEnergy(energyCost)) {
                            power.consumeEnergy(energyCost);
                            consumedEnergy = true;
                            if (isMoving && !user.level.isClientSide()) {
                                hamon.hamonPointsFromAction(HamonStat.CONTROL, points);
                            }
                        }
                        
                        if (!consumedEnergy) {
                            if (!user.level.isClientSide()) {
                                wallClimbData.stopWallClimbing();
                            }
                        }
                    }
                    
                    if (user.level.isClientSide()) {
                        HamonSparksLoopSound.playSparkSound(user, new Vector3d(user.getX(), user.getY(0.75), user.getZ()), 1.0F, true);
                    }
                }
                else if (!user.level.isClientSide()) {
                    wallClimbData.stopWallClimbing();
                }
            }
        });
    }
    
}
