package com.github.standobyte.jojo.client.ui.text;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.util.mod.StoryPart;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class JojoTextComponentWrapper implements IFormattableTextComponent {
    private static final ITextComponent[] SPACE_OFFSET = Util.make(new ITextComponent[8], array -> {
        for (int i = 0; i < array.length; i++) {
            array[i] = new StringTextComponent(StringUtils.repeat(" ", i));
        }
    });
    private ResourceLocation storyPartSprite;
    private final IFormattableTextComponent component;
    
    public JojoTextComponentWrapper(IFormattableTextComponent component) {
        this.component = component;
    }
    
    
    public JojoTextComponentWrapper setStoryPartSprite(StoryPart storyPart) {
        return setStoryPartSprite(storyPart != null ? storyPart.getSprite() : null);
    }
    
    public JojoTextComponentWrapper setStoryPartSprite(ResourceLocation storyPartSprite) {
        this.storyPartSprite = storyPartSprite;
        return this;
    }
    
    
    public void tooltipRenderExtra(MatrixStack matrixStack, float x, float y) {
        if (storyPartSprite != null) {
            Minecraft.getInstance().textureManager.bind(storyPartSprite);
            BlitFloat.blitFloat(matrixStack, x - 1, y, 
                    0, 0, 8, 8, 8, 8);
        }
    }
    
    @Override
    public <T> Optional<T> visit(ITextProperties.IStyledTextAcceptor<T> pAcceptor, Style pStyle) {
        if (storyPartSprite != null) {
            SPACE_OFFSET[2].visit(pAcceptor, pStyle);
        }
        return component.visit(pAcceptor, pStyle);
    }

    @Override
    public <T> Optional<T> visit(ITextProperties.ITextAcceptor<T> pAcceptor) {
        if (storyPartSprite != null) {
            SPACE_OFFSET[2].visit(pAcceptor);
        }
        return component.visit(pAcceptor);
    }
    

    @Override
    public Style getStyle() {
        return component.getStyle();
    }

    @Override
    public String getContents() {
        return component.getContents();
    }

    @Override
    public List<ITextComponent> getSiblings() {
        return component.getSiblings();
    }

    @Override
    public IFormattableTextComponent plainCopy() {
        return component.plainCopy();
    }

    @Override
    public IFormattableTextComponent copy() {
        return new JojoTextComponentWrapper(component.copy())
                .setStoryPartSprite(storyPartSprite);
    }

    @Override
    public IReorderingProcessor getVisualOrderText() {
        return component.getVisualOrderText();
    }

    @Override
    public IFormattableTextComponent setStyle(Style pStyle) {
        return component.setStyle(pStyle);
    }

    @Override
    public IFormattableTextComponent append(ITextComponent pSibling) {
        return component.append(pSibling);
    }

    
    @Override
    public String getString() {
        return component.getString();
    }
    
    @Override
    public String getString(int pMaxLength) {
        return component.getString(pMaxLength);
    }

}
