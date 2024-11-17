package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion.CustomExplosionSupplier;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class CustomExplosionPacket {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPos> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ResourceLocation type;
    private CustomExplosion srvExplosion;
    private PacketBuffer extraData;
    
    public CustomExplosionPacket(CustomExplosion explosion, double pX, double pY, double pZ, float pPower, 
            List<BlockPos> pToBlow, @Nullable Vector3d pKnockback, ResourceLocation type) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.power = pPower;
        this.toBlow = Lists.newArrayList(pToBlow);
        if (pKnockback != null) {
            this.knockbackX = (float)pKnockback.x;
            this.knockbackY = (float)pKnockback.y;
            this.knockbackZ = (float)pKnockback.z;
        }
        else {
            this.knockbackX = 0;
            this.knockbackY = 0;
            this.knockbackZ = 0;
        }
        this.type = type;
        this.srvExplosion = explosion;
    }
    
    
    
    public static class Handler implements IModPacketHandler<CustomExplosionPacket> {

        @Override
        public void encode(CustomExplosionPacket msg, PacketBuffer buf) {
            buf.writeFloat((float)msg.x);
            buf.writeFloat((float)msg.y);
            buf.writeFloat((float)msg.z);
            buf.writeFloat(msg.power);
            buf.writeInt(msg.toBlow.size());
            int xInt = MathHelper.floor(msg.x);
            int yInt = MathHelper.floor(msg.y);
            int zInt = MathHelper.floor(msg.z);
            
            for (BlockPos blockPos : msg.toBlow) {
                buf.writeByte(blockPos.getX() - xInt);
                buf.writeByte(blockPos.getY() - yInt);
                buf.writeByte(blockPos.getZ() - zInt);
            }
            
            buf.writeFloat(msg.knockbackX);
            buf.writeFloat(msg.knockbackY);
            buf.writeFloat(msg.knockbackZ);
            
            buf.writeResourceLocation(msg.type);
            msg.srvExplosion.toBuf(buf);
        }

        @Override
        public CustomExplosionPacket decode(PacketBuffer buf) {
            double xPos = (double)buf.readFloat();
            double yPos = (double)buf.readFloat();
            double zPos = (double)buf.readFloat();
            float power = buf.readFloat();
            int blockCount = buf.readInt();
            List<BlockPos> toBlow = Lists.newArrayListWithCapacity(blockCount);
            int xInt = MathHelper.floor(xPos);
            int yInt = MathHelper.floor(yPos);
            int zInt = MathHelper.floor(zPos);
            
            for (int i = 0; i < blockCount; ++i) {
               toBlow.add(new BlockPos(
                       buf.readByte() + xInt, 
                       buf.readByte() + yInt, 
                       buf.readByte() + zInt));
            }
            
            float knockbackX = buf.readFloat();
            float knockbackY = buf.readFloat();
            float knockbackZ = buf.readFloat();
            
            ResourceLocation type = buf.readResourceLocation();
            
            CustomExplosionPacket packet = new CustomExplosionPacket(null, xPos, yPos, zPos, power, toBlow, new Vector3d(knockbackX, knockbackY, knockbackZ), type);
            packet.extraData = buf;
            return packet;
        }

        @Override
        public void handle(CustomExplosionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            CustomExplosionSupplier explosionSupplier = CustomExplosion.Register.REGISTER.get(msg.type);
            if (explosionSupplier != null) {
                CustomExplosion explosion = explosionSupplier.createExplosion(ClientUtil.getClientWorld(), msg.x, msg.y, msg.z, msg.power);
                explosion.getToBlow().addAll(msg.toBlow);
                explosion.fromBuf(msg.extraData);
                explosion.finalizeExplosion(true);
                player.setDeltaMovement(player.getDeltaMovement().add((double)msg.knockbackX, (double)msg.knockbackY, (double)msg.knockbackZ));
            }
        }

        @Override
        public Class<CustomExplosionPacket> getPacketClass() {
            return CustomExplosionPacket.class;
        }
    }

}
