package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.util.text.ITextComponent;

public class PickTechniqueButton extends HamonScreenButton {
    public CharacterHamonTechnique technique;
    
    public PickTechniqueButton(int x, int y, int width, int height, 
            ITextComponent message, IPressable onPress) {
        super(x, y, width, height, message, onPress);
    }
    
    public PickTechniqueButton(int x, int y, int width, int height, 
            ITextComponent message, IPressable onPress, ITooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
    }
    
}
