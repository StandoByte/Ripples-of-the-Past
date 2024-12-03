package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClAngeloRockRespawnPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClAngeloRockRespawnPacket> {

        @Override
        public void encode(ClAngeloRockRespawnPacket msg, PacketBuffer buf) {}

        @Override
        public ClAngeloRockRespawnPacket decode(PacketBuffer buf) {
            return new ClAngeloRockRespawnPacket();
        }

        @Override
        public void handle(ClAngeloRockRespawnPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            Entity possessed = IPlayerPossess.getPossessedEntity(player);
            if (possessed != null && possessed.getType() == ModEntityTypes.ANGELO_ROCK.get()) {
                player.invulnerableTime = 0;
                player.removeEffect(Effects.DAMAGE_RESISTANCE);
                player.hurt(new DamageSource("rockRespawn").bypassArmor().bypassInvul(), Float.MAX_VALUE);
            }
        }

        @Override
        public Class<ClAngeloRockRespawnPacket> getPacketClass() {
            return ClAngeloRockRespawnPacket.class;
        }
    }

}
