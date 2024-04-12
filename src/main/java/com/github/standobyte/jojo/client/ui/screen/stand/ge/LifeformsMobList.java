package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.util.text.ITextComponent;

public class LifeformsMobList extends LifeformsList<EntityType<?>> {

    public LifeformsMobList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight,
            ChooseLifeformListScreen screen) {
        super(mc, width, height, y0, y1, itemHeight, screen);
    }

    @Override
    protected LifeformsList.LifeformEntry makeLifeformEntry(EntityType<?> lifeformType, ITextComponent name) {
        return new MobEntry(name, lifeformType);
    }
    
    @Override
    protected void select(EntityType<?> lifeformType) {
        screen.playerUISettings.setGEChosenLifeformType(lifeformType, true);
    }

    @Override
    protected void addFavorite(EntityType<?> lifeformType) {
        screen.playerUISettings.GELifeformAddFav(lifeformType);
    }

    @Override
    protected void removeFavorite(EntityType<?> lifeformType) {
        screen.playerUISettings.GELifeformRemoveFav(lifeformType);
    }

    @Override
    protected boolean isInFavorites(EntityType<?> lifeformType) {
        return screen.playerUISettings.isGELifeformInFavorites(lifeformType);
    }
    
    @Override
    protected boolean isNew(EntityType<?> lifeformType) {
        return screen.playerUISettings.isGELifeformNew(lifeformType);
    }
    
    @Override
    protected void renderHoveredTooltip(MatrixStack matrixStack, EntityType<?> entityType, int mouseX, int mouseY) {
        screen.renderHoveredTooltip(matrixStack, entityType, mouseX, mouseY);
    }
    
    
    public static class MobEntry extends LifeformsList.LifeformEntry {
        private final EntityType<?> entityType;
        
        public MobEntry(ITextComponent valueName, EntityType<?> entityType) {
            super(valueName);
            this.entityType = entityType;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
                int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            pMatrixStack.pushPose();
            pMatrixStack.scale(0.5F, 0.5F, 1);
            EntityTypeIcon.renderIcon(entityType, pMatrixStack, 2 * (pLeft + 13), 2 * (pTop + 2), false);
            pMatrixStack.popPose();
            super.render(pMatrixStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        }
        
    }

}
