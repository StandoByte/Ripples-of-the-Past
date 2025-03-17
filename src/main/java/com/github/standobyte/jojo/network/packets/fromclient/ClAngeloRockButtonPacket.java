package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClAngeloRockButtonPacket {
    private final PacketType type;
    
    private ClAngeloRockButtonPacket(PacketType type) {
        this.type = type;
    }
    
    public static ClAngeloRockButtonPacket respawn() {
        return new ClAngeloRockButtonPacket(PacketType.RESPAWN);
    }
    
    public static ClAngeloRockButtonPacket grunt() {
        return new ClAngeloRockButtonPacket(PacketType.GRUNT);
    }
    
    private enum PacketType {
        RESPAWN,
        GRUNT
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClAngeloRockButtonPacket> {

        @Override
        public void encode(ClAngeloRockButtonPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.type);
        }

        @Override
        public ClAngeloRockButtonPacket decode(PacketBuffer buf) {
            return new ClAngeloRockButtonPacket(buf.readEnum(PacketType.class));
        }

        @Override
        public void handle(ClAngeloRockButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            Entity possessed = IPlayerPossess.getPossessedEntity(player);
            if (possessed != null && possessed.getType() == ModEntityTypes.ANGELO_ROCK.get()) {
                switch (msg.type) {
                case RESPAWN:
                    player.invulnerableTime = 0;
                    player.removeEffect(Effects.DAMAGE_RESISTANCE);
                    player.hurt(new DamageSource("rockRespawn").bypassArmor().bypassInvul(), Float.MAX_VALUE);
                    break;
                case GRUNT:
                    possessed.playSound(ModSounds.ANGELO_ROCK_GRUNT.get(), 1, 1);
                    break;
                }
            }
        }

        @Override
        public Class<ClAngeloRockButtonPacket> getPacketClass() {
            return ClAngeloRockButtonPacket.class;
        }
    }

}
