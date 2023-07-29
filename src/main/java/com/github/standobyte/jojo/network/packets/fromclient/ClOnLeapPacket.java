package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClOnLeapPacket {
    private final PowerClassification classification;
    
    public ClOnLeapPacket(PowerClassification classification) {
        this.classification = classification;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClOnLeapPacket> {
        
        @Override
        public void encode(ClOnLeapPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
        }

        @Override
        public ClOnLeapPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            return new ClOnLeapPacket(power);
        }

        @Override
        public void handle(ClOnLeapPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                if (power.canLeap()) {
                    float leapStrength = power.leapStrength();
                    if (leapStrength > 0) {
                        player.setShiftKeyDown(false);
                        player.hasImpulse = true;
                        power.onLeap();
                        BlockPos posOn = getOnPos(player);
                        World world = player.level;
                        if (!player.level.isEmptyBlock(posOn)) {
                            BlockState blockState = world.getBlockState(posOn);
                            int particlesCount = (int) (150.0F * Math.min(0.2F + leapStrength / 3.0F, 2.5F));
                            if (!blockState.addLandingEffects((ServerWorld) world, posOn, blockState, player, particlesCount)) {
                                ((ServerWorld) world).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, blockState)
                                        .setPos(posOn), player.getX(), player.getY(), player.getZ(), particlesCount, 0.0, 0.0, 0.0, 0.15);
                            }
                            if (!player.isSilent()) {
                                SoundType soundType = blockState.getSoundType(world, posOn, player);
                                player.playSound(soundType.getBreakSound(), soundType.getVolume() * 0.25F * leapStrength, soundType.getPitch() * 0.75F);
                            }
                        }
                        
                        Entity vehicle = player.getVehicle();
                        if (vehicle != null && vehicle.getControllingPassenger() != player) {
                            MCUtil.leap(vehicle, leapStrength);
                        }
                    }
                }
            });
        }
        
        private BlockPos getOnPos(Entity entity) {
            BlockPos blockPos = new BlockPos(
                    MathHelper.floor(entity.getX()), 
                    MathHelper.floor(entity.getY() - (double)0.2F), 
                    MathHelper.floor(entity.getZ()));
            if (entity.level.isEmptyBlock(blockPos)) {
                BlockPos below = blockPos.below();
                if (entity.level.getBlockState(below).collisionExtendsVertically(entity.level, below, entity)) {
                    return below;
                }
            }
            return blockPos;
        }

        @Override
        public Class<ClOnLeapPacket> getPacketClass() {
            return ClOnLeapPacket.class;
        }
    }
}
