package com.github.standobyte.jojo.potion;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class StatusEffect extends Effect {
    private boolean isUncurable = false;

    public StatusEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends StatusEffect> T setUncurable() {
        this.isUncurable = true;
        return (T) this;
    }
    
    @Override
    public List<ItemStack> getCurativeItems() {
        if (isUncurable) {
            return Collections.emptyList();
        }
        return super.getCurativeItems();
    }
    
    public boolean isUncurable() {
        return isUncurable;
    }
}
