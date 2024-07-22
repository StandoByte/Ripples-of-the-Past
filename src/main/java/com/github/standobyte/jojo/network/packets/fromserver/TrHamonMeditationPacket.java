package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonMeditationPacket {
    private final int userId;
    private final boolean meditation;
    
    public TrHamonMeditationPacket(int userId, boolean value) {
        this.userId = userId;
        this.meditation = value;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonMeditationPacket> {

        @Override
        public void encode(TrHamonMeditationPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.userId);
            buf.writeBoolean(msg.meditation);
        }

        @Override
        public TrHamonMeditationPacket decode(PacketBuffer buf) {
            return new TrHamonMeditationPacket(buf.readInt(), buf.readBoolean());
        }

        @Override
        public void handle(TrHamonMeditationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity userEntity = ClientUtil.getEntityById(msg.userId);
            if (userEntity instanceof LivingEntity) {
                LivingEntity userLiving = (LivingEntity) userEntity;
                INonStandPower.getNonStandPowerOptional(userLiving).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setIsMeditating(userLiving, msg.meditation);
                        if (userLiving instanceof PlayerEntity) {
                            PlayerEntity userPlayer = (PlayerEntity) userLiving;
                            PlayerAnimationHandler.getPlayerAnimator().onMeditationSet(userPlayer, msg.meditation);
                            if (msg.meditation && userPlayer == ClientUtil.getClientPlayer()) {
                                ClientUtil.setThirdPerson();
                            }
                        }
                    });
                });
            }
        }

        @Override
        public Class<TrHamonMeditationPacket> getPacketClass() {
            return TrHamonMeditationPacket.class;
        }
    }
}
