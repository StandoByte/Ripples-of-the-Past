package com.github.standobyte.jojo.client.ui.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.util.mod.StoryPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class JojoTextComponentWrapper implements IFormattableTextComponent {
    private static final ITextComponent[] SPRITE_OFFSET = Util.make(new ITextComponent[8], array -> {
        for (int i = 0; i < array.length; i++) {
            array[i] = new StringTextComponent(StringUtils.repeat(" ", (i + 1) * 2));
        }
    });
    private List<Either<ResourceLocation, TextureAtlasSprite>> sprites = new ArrayList<>();
    private final IFormattableTextComponent component;
    
    public JojoTextComponentWrapper(IFormattableTextComponent component) {
        this.component = component;
    }
    
    
    public JojoTextComponentWrapper setStoryPartSprite(StoryPart storyPart) {
        return addSprite(storyPart != null ? storyPart.getSprite() : null);
    }
    
    public JojoTextComponentWrapper addSprite(ResourceLocation sprite) {
        sprites.add(Either.left(sprite));
        return this;
    }
    
    public JojoTextComponentWrapper addSprite(TextureAtlasSprite sprite) {
        sprites.add(Either.right(sprite));
        return this;
    }
    
    // FIXME fix the icon not rendering if any line from the tooltip is wrapped
    public void tooltipRenderExtra(MatrixStack matrixStack, float x, float y) {
        for (Either<ResourceLocation, TextureAtlasSprite> sprite : sprites) {
            float spriteX = x - 1;
            sprite
            .ifLeft(texLocation -> {
                Minecraft.getInstance().textureManager.bind(texLocation);
                BlitFloat.blitFloat(matrixStack, spriteX, y, 0, 0, 8, 8, 8, 8);
            })
            .ifRight(atlasSprite -> {
                Minecraft.getInstance().getTextureManager().bind(atlasSprite.atlas().location());
                BlitFloat.blitFloat(matrixStack, spriteX, y, 0, 8, 8, atlasSprite);
            });
            x += 10;
        }
    }
    
    @Override
    public <T> Optional<T> visit(ITextProperties.IStyledTextAcceptor<T> pAcceptor, Style pStyle) {
        if (!sprites.isEmpty()) {
            int index = Math.min(sprites.size(), SPRITE_OFFSET.length) - 1;
            SPRITE_OFFSET[index].visit(pAcceptor, pStyle);
        }
        return component.visit(pAcceptor, pStyle);
    }

    @Override
    public <T> Optional<T> visit(ITextProperties.ITextAcceptor<T> pAcceptor) {
        if (!sprites.isEmpty()) {
            int index = Math.min(sprites.size(), SPRITE_OFFSET.length) - 1;
            SPRITE_OFFSET[index].visit(pAcceptor);
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
        JojoTextComponentWrapper copy = new JojoTextComponentWrapper(component.copy());
        copy.sprites.addAll(this.sprites);
        return copy;
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
