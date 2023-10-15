package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClStandManualMovementPacket {
    private final double x;
    private final double y;
    private final double z;
    private final float xRot;
    private final float yRot;
    private final boolean resetDeltaMovement;

    public ClStandManualMovementPacket(double x, double y, double z, float xRot, float yRot, boolean resetDeltaMovement) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.resetDeltaMovement = resetDeltaMovement;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClStandManualMovementPacket> {
    
        @Override
        public void encode(ClStandManualMovementPacket msg, PacketBuffer buf) {
            buf.writeDouble(msg.x);
            buf.writeDouble(msg.y);
            buf.writeDouble(msg.z);
            buf.writeFloat(msg.xRot);
            buf.writeFloat(msg.yRot);
            buf.writeBoolean(msg.resetDeltaMovement);
        }

        @Override
        public ClStandManualMovementPacket decode(PacketBuffer buf) {
            return new ClStandManualMovementPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), 
                    buf.readFloat(), buf.readFloat(), buf.readBoolean());
        }

        @Override
        public void handle(ClStandManualMovementPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            if (!isValid(msg)) {
                player.connection.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_stand_movement"));
            }
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                IStandManifestation standManifestation = power.getStandManifestation();
                if (standManifestation instanceof StandEntity) {
                    StandEntity stand = (StandEntity) standManifestation;
//                    ServerWorld world = player.getLevel();
                    double posX1 = stand.getX(); // d0
                    double posY1 = stand.getY(); // d1
                    double posZ1 = stand.getZ(); // d2
                    double posXcl = msg.x; // d3
                    double posYcl = msg.y; // d4
                    double posZcl = msg.z; // d5
                    float xRot = msg.xRot;
                    float yRot = msg.yRot;
//                    double diffX = posXcl - firstGoodX;
//                    double diffY = posYcl - firstGoodY;
//                    double diffZ = posZcl - firstGoodZ;
                    double diffX = posXcl - posX1; // d6
                    double diffY = posYcl - posY1; // d7
                    double diffZ = posZcl - posZ1; // d8
                    double motionSq = stand.getDeltaMovement().lengthSqr(); // d9
                    double diffSq = diffX * diffX + diffY * diffY + diffZ * diffZ; // d10
                    if (diffSq - motionSq > 100.0D) {
                        JojoMod.getLogger().warn("{} ({}'s stand) moved too quickly! {},{},{}", stand.getName().getString(), player.getName().getString(), diffX, diffY, diffZ);
                        PacketManager.sendToClient(new StandCancelManualMovementPacket(stand.getX(), stand.getY(), stand.getZ()), player);
                        return;
                    }
//                    boolean flag = world.noCollision(stand, stand.getBoundingBox().deflate(0.0625D));
//                    diffX = posXcl - lastGoodX;
//                    diffY = posYcl - lastGoodY;
//                    diffZ = posZcl - lastGoodZ;
                    stand.move(MoverType.PLAYER, new Vector3d(diffX, diffY, diffZ)); // also moves the stand towards user if the client tries to go to far away
//                    diffX = posXcl - entity.getX();
//                    diffY = posYcl - entity.getY();
//                    if (diffY > -0.5D || diffY < 0.5D) {
//                       diffY = 0.0D;
//                    }
//                    diffZ = posZcl - entity.getZ();
//                    diffSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
//                    boolean flag1 = false;
//                    if (diffSq > 0.0625D) {
//                       flag1 = true;
//                       LOGGER.warn("{} ({}'s stand) moved wrongly! {}", stand.getName().getString(), player.getName().getString(), Math.sqrt(diffSq));
//                    }
                    stand.absMoveTo(posXcl, posYcl, posZcl, yRot, xRot);
//                    boolean flag2 = world.noCollision(stand, stand.getBoundingBox().deflate(0.0625D));
//                    if (flag && (flag1 || !flag2)) {
//                       stand.absMoveTo(d0, d1, d2);
//                       PacketManager.sendToClient(new StandCancelManualMovementPacket(stand.getX(), stand.getY(), stand.getZ()), player);
//                       return;
//                    }
//                    lastGoodX = stand.getX();
//                    lastGoodY = stand.getY();
//                    lastGoodZ = stand.getZ();
                    if (msg.resetDeltaMovement) {
                        stand.setDeltaMovement(Vector3d.ZERO);
                    }
                }
            });
        }
    
        private boolean isValid(ClStandManualMovementPacket msg) {
            return Doubles.isFinite(msg.x) && Doubles.isFinite(msg.y) && Doubles.isFinite(msg.z)
                    && Floats.isFinite(msg.xRot) && Floats.isFinite(msg.yRot)
                    && Math.abs(msg.x) < 3.0e7 && Math.abs(msg.y) < 3.0e7 && Math.abs(msg.z) < 3.0e7;
        }

        @Override
        public Class<ClStandManualMovementPacket> getPacketClass() {
            return ClStandManualMovementPacket.class;
        }
    }
}
