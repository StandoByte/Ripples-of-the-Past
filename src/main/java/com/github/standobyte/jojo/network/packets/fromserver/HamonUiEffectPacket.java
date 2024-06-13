package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonUiEffectPacket {
    private final Type effectType;
    
    public HamonUiEffectPacket(Type effectType) {
        this.effectType = effectType;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonUiEffectPacket> {

        @Override
        public void encode(HamonUiEffectPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.effectType);
        }

        @Override
        public HamonUiEffectPacket decode(PacketBuffer buf) {
            return new HamonUiEffectPacket(buf.readEnum(Type.class));
        }

        @Override
        public void handle(HamonUiEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
            switch (msg.effectType) {
            case NO_ENERGY:
                BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(4);
                break;
            case OUT_OF_BREATH:
                ActionsOverlayGui.getInstance().setOutOfBreath(false);
                BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
                break;
            case OUT_OF_BREATH_MASK:
                ActionsOverlayGui.getInstance().setOutOfBreath(true);
                BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
                break;
            }
        }

        @Override
        public Class<HamonUiEffectPacket> getPacketClass() {
            return HamonUiEffectPacket.class;
        }
    }
    
    public static enum Type {
        OUT_OF_BREATH,
        OUT_OF_BREATH_MASK,
        NO_ENERGY
    }
}
