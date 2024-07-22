package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.network.NetworkEvent;

public class PhotoForOtherPlayerPacket {
    private final int giveToPlayerId;
    
    public PhotoForOtherPlayerPacket(int giveToPlayerId) {
        this.giveToPlayerId = giveToPlayerId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<PhotoForOtherPlayerPacket> {

        @Override
        public void encode(PhotoForOtherPlayerPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.giveToPlayerId);
        }

        @Override
        public PhotoForOtherPlayerPacket decode(PacketBuffer buf) {
            int giveToPlayerId = buf.readInt();
            return new PhotoForOtherPlayerPacket(giveToPlayerId);
        }

        @Override
        public void handle(PhotoForOtherPlayerPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            float randomAngle = player.getRandom().nextFloat() * (float) (2 * Math.PI);
            Vector3d playerPos = player.getEyePosition(1.0F);
            Vector3d cameraPos = playerPos.add(new Vector3d(0, 0, 2).yRot(randomAngle));
            PolaroidHelper.takePicture(cameraPos, rot -> new Vector3f(0, 180 - randomAngle * MathUtil.RAD_TO_DEG, 0), true, msg.giveToPlayerId);
        }

        @Override
        public Class<PhotoForOtherPlayerPacket> getPacketClass() {
            return PhotoForOtherPlayerPacket.class;
        }
    }
}
