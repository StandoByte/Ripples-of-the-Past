package com.github.standobyte.jojo.potion;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class UncurableEffect extends Effect {

    public UncurableEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

}
