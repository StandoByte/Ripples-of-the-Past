package com.github.standobyte.jojo.action.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

public class CrazyDiamondLeaveObject extends StandEntityActionModifier {
    
    static final Map<Predicate<ItemStack>, BiConsumer<LivingEntity, ItemStack>> ITEM_ACTION = Util.make(new HashMap<>(), map -> {
        
    });

    public CrazyDiamondLeaveObject(Builder builder) {
        super(builder);
    }
}
