package com.github.standobyte.jojo.power.impl.stand;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PreviousStandTypesPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class PreviousStandsSet {
    private final Set<StandType<?>> stands = new HashSet<>();
    
    
    public void addStand(StandType<?> standType, LivingEntity user) {
        if (stands.add(standType)) {
            if (user instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(PreviousStandTypesPacket.newStand(standType), (ServerPlayerEntity) user);
            }
        }
    }
    
    public void clear() {
        stands.clear();
    }
    
    
    public boolean contains(StandType<?> standType) {
        return stands.contains(standType);
    }
    
    public boolean containsAll(Collection<StandType<?>> standsToCheck) {
        return stands.containsAll(standsToCheck);
    }
    
    
    public List<StandType<?>> rigForUnusedStands(List<StandType<?>> availableStands) {
        List<StandType<?>> notUsedStands = availableStands.stream()
                .filter(stand -> !stands.contains(stand))
                .collect(Collectors.toList());
        return !notUsedStands.isEmpty() ? notUsedStands : availableStands;
    }
    
    
    public void syncWithUser(ServerPlayerEntity user) {
        PacketManager.sendToClient(PreviousStandTypesPacket.allStands(stands), user);
    }
    
    public void handlePacket(PreviousStandTypesPacket packet) {
        if (packet.clear) {
            clear();
        }
        else if (packet.sendingAll) {
            if (packet.allStands != null) {
                stands.addAll(packet.allStands);
            }
        }
        else {
            if (packet.newStand != null) {
                stands.add(packet.newStand);
            }
        }
    }
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        ListNBT standsListNbt = new ListNBT();
        stands.forEach(stand -> {
            standsListNbt.add(StringNBT.valueOf(JojoCustomRegistries.STANDS.getKeyAsString(stand)));
        });
        nbt.put("Stands", standsListNbt);
        
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("Stands", MCUtil.getNbtId(ListNBT.class))) {
            ListNBT standsListNbt = nbt.getList("Stands", MCUtil.getNbtId(StringNBT.class));
            standsListNbt.forEach(standNameNbt -> {
                String standName = ((StringNBT) standNameNbt).getAsString();
                StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standName));
                if (standType != null) {
                    stands.add(standType);
                }
            });
        }
    }
}
