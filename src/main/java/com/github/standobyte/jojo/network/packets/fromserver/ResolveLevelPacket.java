package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolveLevelPacket {
    private final int level;
    private final boolean fromEffect;
    
    public ResolveLevelPacket(int level, boolean fromEffect) {
        this.level = level;
        this.fromEffect = fromEffect;
    }
    
    public static void encode(ResolveLevelPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.level);
        buf.writeBoolean(msg.fromEffect);
    }
    
    public static ResolveLevelPacket decode(PacketBuffer buf) {
        return new ResolveLevelPacket(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(ResolveLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                boolean wasComboUnlocked = msg.fromEffect && StandUtil.isComboUnlocked(power);
                power.setResolveLevel(msg.level, msg.fromEffect);
                if (msg.fromEffect && !wasComboUnlocked && StandUtil.isComboUnlocked(power)) {
                    power.getAttacks().stream()
                    .flatMap(attack -> attack.hasShiftVariation() ? Stream.of(attack, attack.getShiftVariationIfPresent()) : Stream.of(attack))
                    .flatMap(attack -> {
                        if (attack instanceof StandEntityHeavyAttack) {
                            StandEntityHeavyAttack finisher = ((StandEntityHeavyAttack) attack).getFinisherVariation();
                            if (finisher != null) {
                                return Stream.of(finisher);
                            }
                        }
                        return Stream.empty();
                    })
                    .distinct()
                    .filter(attack -> attack.isUnlocked(power))
                    .forEach(finisher -> ActionToast.addOrUpdate(Minecraft.getInstance().getToasts(), 
                            ActionToast.SpecialToastType.FINISHER_HEAVY_ATTACK, finisher, power.getType()));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
