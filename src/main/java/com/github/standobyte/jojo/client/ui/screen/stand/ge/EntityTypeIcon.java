package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class EntityTypeIcon {
    private static final Map<EntityType<?>, ResourceLocation> ICONS_CACHE = new HashMap<>();
    private static final ResourceLocation UNKNOWN = new ResourceLocation("textures/entity_icon/unknown.png");

    public static void renderIcon(EntityType<?> entityType, MatrixStack matrixStack, float x, float y) {
        ResourceLocation icon = getIcon(entityType);
        if (icon != UNKNOWN) {
            Minecraft.getInstance().getTextureManager().bind(icon);
            BlitFloat.blitFloat(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
        }
        else {
            String name = entityType.getDescription().getString();
            if (!name.isEmpty()) {
                FontRenderer font = Minecraft.getInstance().font;
                
                ITextComponent firstLetter = StringTextComponent.EMPTY;
                int widthNext = 0;
                for (int i = 1; i <= name.length() && widthNext < 12; i++) {
                    firstLetter = new StringTextComponent(name.substring(0, i));
                    widthNext = font.width(firstLetter);
                }

                RenderSystem.disableDepthTest();
                font.draw(matrixStack, firstLetter, x + (16 - widthNext) / 2, y + (16 - font.lineHeight + 1) / 2, 0xFFFFFF);
                RenderSystem.enableDepthTest();
                RenderSystem.enableBlend();
            }
        }
    }

    public static ResourceLocation getIcon(EntityType<?> entityType) {
        return ICONS_CACHE.computeIfAbsent(entityType, EntityTypeIcon::createIconPath);
    }

    private static ResourceLocation createIconPath(EntityType<?> entityType) {
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation entityTex = getEntityTexture(entityType);
        if (entityTex == null) return UNKNOWN;
        
        String path = entityTex.getPath();
        if (path.contains("/entity/")) {
            if (path.contains("/model/entity/")) {
                path = path.replace("/model/entity/", "/entity_icon/");
            }
            else {
                path = path.replace("/entity/", "/entity_icon/");
            }
            entityTex = new ResourceLocation(entityTex.getNamespace(), path);
            if (mc.getResourceManager().hasResource(entityTex)) {
                return entityTex;
            }
        }
        
        return UNKNOWN;
    }
    
    @Nullable
    private static <T extends Entity> ResourceLocation getEntityTexture(EntityType<T> entityType) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderer<? super T> renderer = (EntityRenderer<? super T>) mc.getEntityRenderDispatcher().renderers.get(entityType);
        T entity = EntityTypeToInstance.getEntityInstance(entityType, mc.level);
        try {
            return renderer.getTextureLocation(entity);
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public static void onResourceReload() {
        ICONS_CACHE.clear();
    }
}
