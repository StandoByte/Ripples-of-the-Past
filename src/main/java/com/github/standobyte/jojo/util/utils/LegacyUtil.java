package com.github.standobyte.jojo.util.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.stand.ActionLearningProgressMap;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.common.collect.Streams;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class LegacyUtil {
    
    // TODO remove 'clientSide' params from StandDiscItem methods
    public static Optional<StandInstance> oldStandDiscInstance(ItemStack disc, boolean clientSide) {
        CompoundNBT nbt = disc.getTag();
        if (nbt.contains("Stand", JojoModUtil.getNbtId(StringNBT.class))) {
            StandType<?> standType = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(nbt.getString("Stand")));
            if (standType != null) {
                StandInstance standInstance = new StandInstance(standType);
                if (!clientSide) {
                    nbt.put("Stand", standInstance.writeNBT());
                }
                return Optional.of(standInstance);
            }
        }
        return Optional.empty();
    }
    
    public static Optional<StandInstance> readOldStandCapType(CompoundNBT capNbt) {
        if (capNbt.contains("StandType", JojoModUtil.getNbtId(StringNBT.class))) {
            String standName = capNbt.getString("StandType");
            if (standName != IPowerType.NO_POWER_NAME) {
                StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standName));
                return Optional.ofNullable(new StandInstance(stand));
            }
        }
        return Optional.empty();
    }
}
