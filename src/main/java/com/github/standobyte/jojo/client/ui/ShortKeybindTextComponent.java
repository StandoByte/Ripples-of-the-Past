package com.github.standobyte.jojo.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ShortKeybindTextComponent extends TextComponent {
    protected static final Map<String, ITextComponent> SHORT_NAMES = new HashMap<>();
    protected final KeyBinding key;
    protected Supplier<ITextComponent> nameResolver;

    public ShortKeybindTextComponent(@Nonnull KeyBinding key) {
        this.key = key;
    }
    
    protected ITextComponent getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = resolveKey(key);
        }

        return this.nameResolver.get();
    }
    
    protected Supplier<ITextComponent> resolveKey(KeyBinding key) {
        return () -> key.getKeyModifier().getCombinedName(key.getKey(), () -> getDisplayName(key.getKey()));
    }
    
    protected ITextComponent getDisplayName(InputMappings.Input input) {
        if (SHORT_NAMES.containsKey(input.getName())) {
//            return SHORT_NAMES.get(input.getName());
        }
        ITextComponent translatedName;
        int value = input.getValue();
        String name = input.getName();
        switch (input.getType()) {
        case KEYSYM:
            String s = GLFW.glfwGetKeyName(value, -1);
            if (s != null) {
                translatedName = new StringTextComponent(s);
            }
            else {
                translatedName = new TranslationTextComponent(ClientUtil.getShortenedTranslationKey(name));
            }
            break;
        case SCANCODE:
            String s2 = GLFW.glfwGetKeyName(-1, value);
            if (s2 != null) {
                translatedName = new StringTextComponent(s2);
            }
            else {
                translatedName = new TranslationTextComponent(ClientUtil.getShortenedTranslationKey(name));
            }
            break;
        case MOUSE:
            if (LanguageMap.getInstance().has(name)) {
                translatedName = new TranslationTextComponent(ClientUtil.getShortenedTranslationKey(name));
            }
            else {
                translatedName = new TranslationTextComponent(ClientUtil.getShortenedTranslationKey("key.mouse"), value + 1);
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
        SHORT_NAMES.put(input.getName(), translatedName);
        return translatedName;
    }

    @Override
    public <T> Optional<T> visitSelf(ITextProperties.ITextAcceptor<T> pConsumer) {
        return getNestedComponent().visit(pConsumer);
    }

    @Override
    public <T> Optional<T> visitSelf(ITextProperties.IStyledTextAcceptor<T> pConsumer, Style pStyle) {
        return getNestedComponent().visit(pConsumer, pStyle);
    }

    @Override
    public ShortKeybindTextComponent plainCopy() {
        return new ShortKeybindTextComponent(key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof KeybindTextComponent)) {
            return false;
        } else {
            return this.key.equals(((ShortKeybindTextComponent) obj).key) && super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return "ShortKeybindComponent{keybind='" + key.getName() + '\'' + ", siblings=" + siblings + ", style=" + getStyle() + '}';
    }

}
