package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolveBoostsPacket {
    private final float boostAttack;
    private final float boostRemoteControl;
    private final float boostChat;
    private final float hp;
    
    public ResolveBoostsPacket(float boostAttack, float boostRemoteControl, float boostChat, float hp) {
        this.boostAttack = boostAttack;
        this.boostRemoteControl = boostRemoteControl;
        this.boostChat = boostChat;
        this.hp = hp;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ResolveBoostsPacket> {

        @Override
        public void encode(ResolveBoostsPacket msg, PacketBuffer buf) {
            buf.writeFloat(msg.boostAttack);
            buf.writeFloat(msg.boostRemoteControl);
            buf.writeFloat(msg.boostChat);
            buf.writeFloat(msg.hp);
        }

        @Override
        public ResolveBoostsPacket decode(PacketBuffer buf) {
            return new ResolveBoostsPacket(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public void handle(ResolveBoostsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().setBoosts(msg.boostAttack, msg.boostRemoteControl, msg.boostChat);
                power.getResolveCounter().setHpOnAttack(msg.hp);
            });
        }

        @Override
        public Class<ResolveBoostsPacket> getPacketClass() {
            return ResolveBoostsPacket.class;
        }
    }
}
