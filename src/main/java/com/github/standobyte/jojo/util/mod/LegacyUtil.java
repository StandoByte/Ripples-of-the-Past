package com.github.standobyte.jojo.util.mod;

import java.util.Optional;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.ResolveLevelsMap;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class LegacyUtil {
    
    // TODO remove 'clientSide' params from StandDiscItem methods
    public static Optional<StandInstance> oldStandDiscInstance(ItemStack disc, boolean clientSide) {
        CompoundNBT nbt = disc.getTag();
        if (nbt != null && nbt.contains("Stand", MCUtil.getNbtId(StringNBT.class))) {
            StandType<?> standType = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(nbt.getString("Stand")));
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
        if (capNbt.contains("StandType", MCUtil.getNbtId(StringNBT.class))) {
            String standName = capNbt.getString("StandType");
            if (standName != IPowerType.NO_POWER_NAME) {
                StandType<?> stand = JojoCustomRegistries.STANDS.getRegistry().getValue(new ResourceLocation(standName));
                return Optional.ofNullable(new StandInstance(stand));
            }
        }
        return Optional.empty();
    }
    
    public static void readOldResolveLevels(CompoundNBT mainCounterNBT, ResolveLevelsMap levelsMap, IStandPower standPower) {
        int resolveLevel = mainCounterNBT.getByte("ResolveLevel");
        int extraLevel = mainCounterNBT.getInt("ExtraLevel");
        
        levelsMap.readOldValues(standPower, resolveLevel, extraLevel);
    }
}
