package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class ChooseLifeformListScreen extends ChooseLifeformScreen {
    private LifeformsList<EntityType<?>> mobList;
    
    public ChooseLifeformListScreen(KeyBinding keyHeld) {
        super(keyHeld);
    }
    
    
    @Override
    protected void init() {
        super.init();
        
        int plantsListX = 4;
        int plantsListWidth = 135;
        
        mobList = new LifeformsMobList(minecraft, 135, 0, 27, height - 27, 13, this);
        mobList.setLeftPos(plantsListX + plantsListWidth + 20);
        addWidget(mobList);
        
        refreshEntityTypes();
        
        int extraUIx = mobList.getLeft() + mobList.getWidth() + 4;
        int maxExtraUIx = width - 101;
        if (extraUIx > maxExtraUIx) {
            int xShift = (extraUIx + 1 - maxExtraUIx) / 2;
            
            int mobsX0 = mobList.getLeft();
            mobList.updateSize(mobList.getWidth() - xShift, mobList.getHeight(), mobList.getTop(), mobList.getBottom());
            mobList.setLeftPos(mobsX0 - xShift);
        }
        
        addCommonWidgets(ViewMode.LIST);
        addSearchField();
    }
    
    @Override
    public void refreshEntityTypes() {
        List<EntityType<?>> entityTypes = ForgeRegistries.ENTITIES.getValues()
                .stream()
                .filter(type -> 
                playerUISettings.didPlayerMeetEntityType(type)
                && GoldExperienceChooseLifeform.isValidLifeform(type, minecraft.level))
                .collect(Collectors.toList());
        mobList.setAllLegalValues(entityTypes);
        mobList.update(entityTypes);
    }

    @Override
    protected void filterEntries(@Nullable Predicate<EntityType<?>> filter) {
        mobList.setFilter(filter);
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
//        renderBackground(matrixStack);
        
        mobList.render(matrixStack, mouseX, mouseY, partialTicks);
        ITextComponent animalsName = new TranslationTextComponent("gold_experience.lifeforms.animals").withStyle(TextFormatting.BOLD);
        minecraft.font.drawShadow(matrixStack, animalsName, mobList.getLeft() + (mobList.getWidth() - minecraft.font.width(animalsName)) / 2, mobList.getTop() - 16, 0xFFFFFF);

        ITextComponent plantsName = new TranslationTextComponent("gold_experience.lifeforms.plants").withStyle(TextFormatting.BOLD);
        minecraft.font.drawShadow(matrixStack, plantsName, 4 + (135 - minecraft.font.width(plantsName)) / 2, mobList.getTop() - 16, 0xFFFFFF);
    }
    
}
