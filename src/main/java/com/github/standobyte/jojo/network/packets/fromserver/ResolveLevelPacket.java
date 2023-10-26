package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast;
import com.github.standobyte.jojo.client.ui.toasts.FinisherAttackToast;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolveLevelPacket {
    private final int level;
    private final boolean showUnlockToast;
    
    public ResolveLevelPacket(int level, boolean showUnlockToast) {
        this.level = level;
        this.showUnlockToast = showUnlockToast;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ResolveLevelPacket> {

        @Override
        public void encode(ResolveLevelPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.level);
            buf.writeBoolean(msg.showUnlockToast);
        }

        @Override
        public ResolveLevelPacket decode(PacketBuffer buf) {
            return new ResolveLevelPacket(buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void handle(ResolveLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                // FIXME make the toast show up when getting the level up from /standlevel command
                boolean wasFinisherUnlocked = msg.showUnlockToast && StandUtil.isFinisherUnlocked(power);
                power.setResolveLevel(msg.level, msg.showUnlockToast);
                if (msg.showUnlockToast && !wasFinisherUnlocked && StandUtil.isFinisherUnlocked(power)) {
                    power.getActions(ActionType.ATTACK).getAll().stream()
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
                    .forEach(finisher -> ActionToast.addOrUpdate(
                            Minecraft.getInstance().getToasts(), 
                            FinisherAttackToast.SpecialToastType.FINISHER_HEAVY_ATTACK,
                            finisher, power));
                }
            });
        }

        @Override
        public Class<ResolveLevelPacket> getPacketClass() {
            return ResolveLevelPacket.class;
        }
    }
}
