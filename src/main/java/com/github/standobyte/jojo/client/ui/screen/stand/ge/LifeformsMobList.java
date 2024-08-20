package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

public class LifeformsMobList extends LifeformsList<LifeformEntityTypeEntry> {

    public LifeformsMobList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight,
            ChooseLifeformListScreen screen) {
        super(mc, width, height, y0, y1, itemHeight, screen);
    }
    
    @Override
    protected String getModName(LifeformEntityTypeEntry lifeformType) {
        return ModInteractionUtil.getModName(lifeformType.entityType.getRegistryName());
    }
    
    @Override
    protected ITextComponent getValueName(LifeformEntityTypeEntry lifeformType) {
        return lifeformType.entityType.getDescription();
    }

    @Override
    protected LifeformsList.LifeformEntry makeLifeformEntry(LifeformEntityTypeEntry lifeformType, ITextComponent name) {
        return new MobEntry(name, lifeformType);
    }
    
    @Override
    protected void select(LifeformEntityTypeEntry lifeformType) {
        screen.playerUISettings.setGEChosenLifeformType(lifeformType.getCurrentSubtype(), true);
    }

    @Override
    protected void addFavorite(LifeformEntityTypeEntry lifeformType) {
        screen.playerUISettings.GELifeformAddFav(lifeformType.entityType);
    }

    @Override
    protected void removeFavorite(LifeformEntityTypeEntry lifeformType) {
        screen.playerUISettings.GELifeformRemoveFav(lifeformType.entityType);
    }

    @Override
    protected boolean isInFavorites(LifeformEntityTypeEntry lifeformType) {
        return screen.playerUISettings.isGELifeformInFavorites(lifeformType.entityType);
    }
    
    @Override
    protected boolean isNew(LifeformEntityTypeEntry lifeformType) {
        return screen.playerUISettings.isGELifeformNew(lifeformType.entityType);
    }
    
    @Override
    protected void renderHoveredTooltip(MatrixStack matrixStack, LifeformEntityTypeEntry entityType, int mouseX, int mouseY) {
        screen.renderHoveredTooltip(matrixStack, entityType.getCurrentSubtype(), mouseX, mouseY);
    }
    
    
    public static class MobEntry extends LifeformsList.LifeformEntry {
        private final LifeformEntityTypeEntry entityType;
        
        public MobEntry(ITextComponent valueName, LifeformEntityTypeEntry entityType) {
            super(valueName);
            this.entityType = entityType;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
                int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            pMatrixStack.pushPose();
            pMatrixStack.scale(0.5F, 0.5F, 1);
            EntityTypeIcon.renderIcon(entityType.getCurrentSubtype(), pMatrixStack, 2 * (pLeft + 13), 2 * (pTop + 2), false);
            pMatrixStack.popPose();
            super.render(pMatrixStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        }
        
    }

}
