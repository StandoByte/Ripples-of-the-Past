package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonLearnButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.HamonSkillType;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class HamonSkillsTabGui extends HamonTabGui {
    private final List<IReorderingProcessor> creativeResetButtonTooltip;
    
    protected HamonSkillGuiElement[][] skillArrays;
    
    protected Button learnButton;
    protected Button creativeResetButton;
    protected HamonSkillGuiElement selectedSkill = null;
    protected List<IReorderingProcessor> skillClosedReason = Collections.emptyList();
    
    HamonSkillsTabGui(Minecraft minecraft, HamonScreen screen, int index, String title, int scrollWidth, int scrollHeight) {
        super(minecraft, screen, index, title, scrollWidth, scrollHeight);
        creativeResetButtonTooltip = minecraft.font.split(new TranslationTextComponent("hamon.reset_creative_only"), 100);
    }

    @Override
    void addButtons() {
        learnButton = new Button(screen.windowPosX() + 150, screen.windowPosY() + 86, 64, 20, new TranslationTextComponent("hamon.learnButton"), button -> {
            if (selectedSkill != null) {
                PacketManager.sendToServer(new ClHamonLearnButtonPacket(selectedSkill.skill));
                screen.clickedOnSkill = true;
            }
        });
        screen.addButton(learnButton);

        creativeResetButton = new Button(screen.windowPosX() + 16, screen.windowPosY() + 86, 64, 20, new TranslationTextComponent("hamon.resetButton"), button -> {
            PacketManager.sendToServer(new ClHamonResetSkillsButtonPacket(getSkillsType()));
        });
        screen.addButton(creativeResetButton);

        buttonY = learnButton.y;
        updateButton();
    }
    
    protected abstract HamonSkillType getSkillsType();
    
    @Override
    List<Widget> getButtons() {
        return ImmutableList.of(learnButton, creativeResetButton);
    }
    
    protected boolean isLocked() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void drawActualContents(MatrixStack matrixStack) {
        int yOffset = isLocked() ? -40 : 0;
        // skill squares
        this.minecraft.getTextureManager().bind(HamonScreen.WINDOW);
        for (HamonSkillGuiElement[] skillLine : skillArrays) {
            for (HamonSkillGuiElement skillElement : skillLine) {
                blit(matrixStack, skillElement.x + intScrollX, skillElement.y + intScrollY + yOffset, 
                        skillElement.getState().getTextureX(), skillElement.getState().getTextureY(), 26, 26);
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (selectedSkill != null) {
            int texY = selectedSkill.isFinal() ? 190 : 164;
            blit(matrixStack, selectedSkill.x + intScrollX, selectedSkill.y + intScrollY + yOffset, 
                    selectedSkill.getState().getTextureX(), texY, 26, 26);
            TextureAtlasSprite textureAtlasSprite = CustomResources.getHamonSkillSprites().getSprite(selectedSkill.skill);
            minecraft.getTextureManager().bind(textureAtlasSprite.atlas().location());
            blit(matrixStack, intScrollX + 4, intScrollY + 4, 0, 16, 16, textureAtlasSprite);
        }
        // skill icons
        for (HamonSkillGuiElement[] skillLine : skillArrays) {
            for (HamonSkillGuiElement skillElement : skillLine) {
                TextureAtlasSprite textureAtlasSprite = CustomResources.getHamonSkillSprites().getSprite(skillElement.skill);
                minecraft.getTextureManager().bind(textureAtlasSprite.atlas().location());
                blit(matrixStack, skillElement.x + intScrollX + 5, skillElement.y + intScrollY + 5 + yOffset, 0, 16, 16, textureAtlasSprite);
            }
        }
        RenderSystem.disableBlend();
    }
    
    @Override
    protected void drawDesc(MatrixStack matrixStack) {
        if (selectedSkill != null) {
            drawSkillDesc(matrixStack);
        }
        else {
            super.drawDesc(matrixStack);
        }
    }
    
    protected void drawSkillDesc(MatrixStack matrixStack) {
        drawString(matrixStack, minecraft.font, selectedSkill.name, intScrollX + 22, intScrollY + 5, 0xFFFFFF);
        ClientUtil.drawRightAlignedString(matrixStack, minecraft.font, selectedSkill.skill.getRewardType().getName(), intScrollX + 205, intScrollY + 5, 0xFFFFFF);
        for (int i = 0; i < selectedSkill.description.size(); i++) {
            minecraft.font.draw(matrixStack, selectedSkill.description.get(i), intScrollX + 6, intScrollY + 22 + i * 9, 0xFFFFFF);
        }
        if (learnButton.visible && !learnButton.active) {
            for (int i = 0; i < skillClosedReason.size(); i++) {
                ClientUtil.drawRightAlignedString(matrixStack, minecraft.font, skillClosedReason.get(i), intScrollX + 135, intScrollY + 67 + i * 9, 0xFF0000);
            }
        }
    }
    
    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        for (HamonSkillGuiElement[] skillTree : skillArrays) {
            for (HamonSkillGuiElement skill : skillTree) {
                if (skill.isMouseOver(intScrollX, intScrollY, mouseX, mouseY)) {
                    screen.renderTooltip(matrixStack, skill.name, mouseX, mouseY);
                }
            }
        }
        if (creativeResetButton.visible && creativeResetButton.isMouseOver(mouseX + screen.windowPosX() + HamonScreen.WINDOW_THIN_BORDER, mouseY + screen.windowPosY() + HamonScreen.WINDOW_UPPER_BORDER)) {
            screen.renderTooltip(matrixStack, creativeResetButtonTooltip, mouseX, mouseY);
        }
    }
    
    @Override
    void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        learnButton.render(matrixStack, mouseX, mouseY, partialTick);
        creativeResetButton.render(matrixStack, mouseX, mouseY, partialTick);
    }

    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (HamonSkillGuiElement[] skillTree : skillArrays) {
                for (HamonSkillGuiElement skill : skillTree) {
                    if (skill.isMouseOver(intScrollX, intScrollY, mouseX, mouseY)) {
                        selectSkill(skill);
                        screen.clickedOnSkill = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1 || !screen.clickedOnSkill && !screen.isDragging() && mouseButton == 0 && 
                !learnButton.isMouseOver(mouseX + screen.windowPosX() + HamonScreen.WINDOW_THIN_BORDER, mouseY + screen.windowPosY() + HamonScreen.WINDOW_UPPER_BORDER)) {
            selectSkill(null);
            return true;
        }
        return false;
    }
    
    @Override
    void scroll(double xMovement, double yMovement) {
        super.scroll(xMovement, yMovement);
        learnButton.y = buttonY + (int) scrollY;
        creativeResetButton.y = buttonY + (int) scrollY;
    }
    
    protected void selectSkill(@Nullable HamonSkillGuiElement guiElement) {
        selectedSkill = guiElement;
        if (guiElement != null) {
            scrollX = 0;
            scrollY = 0;
            learnButton.y = buttonY + (int) scrollY;
        }
        else {
            creativeResetButton.y = buttonY + (int) scrollY;
        }
        updateButton();
    }
    
    protected void updateButton() {
        learnButton.visible = selectedSkill != null && !screen.hamon.isSkillLearned(selectedSkill.skill);
        learnButton.active = selectedSkill != null && screen.hamon.canLearnSkill(selectedSkill.skill, screen.teacherSkills);
        if (selectedSkill != null) {
            skillClosedReason = minecraft.font.split(new TranslationTextComponent(
                    screen.hamon.skillClosedReason(selectedSkill.skill, screen.isTeacherNearby, screen.teacherSkills)), 125);
        }
        creativeResetButton.visible = selectedSkill == null && screen.getMinecraft().player.abilities.instabuild;
    }
    
    @Override
    void updateTab() {
        if (selectedSkill != null) {
            updateButton();
        }
        for (HamonSkillGuiElement[] skillTree : skillArrays) {
            for (HamonSkillGuiElement skillElement : skillTree) {
                HamonSkill skill = skillElement.skill;
                skillElement.updateState(screen.hamon.canLearnSkill(skill, screen.teacherSkills), screen.hamon.isSkillLearned(skill));
            }
        }
    }
}
