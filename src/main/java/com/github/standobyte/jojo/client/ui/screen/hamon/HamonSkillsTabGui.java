package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_UPPER_BORDER;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonLearnButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket.HamonSkillsTab;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

@SuppressWarnings("deprecation")
public abstract class HamonSkillsTabGui extends HamonTabGui {
    public static final ResourceLocation HAMON_SKILLS = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/hamon_window_2.png");
    
    protected List<IReorderingProcessor> creativeResetButtonTooltip;
    protected final Map<AbstractHamonSkill, HamonSkillElementLearnable> skills = new HashMap<>();
    protected HamonScreenButton learnButton;
    protected HamonScreenButton creativeResetButton;
    @Nullable private HamonSkillElementLearnable selectedSkill = null;
    @Nullable private HamonSkillDescBox selectedSkillDesc = null;
    protected List<HamonSkillGuiElement> skillRequirements = Collections.emptyList();
    protected List<IReorderingProcessor> skillClosedReason = Collections.emptyList();
    @Nullable private HamonSkillElementLearnable lastClickedSkill = null;
    private int lastClickDelay;
    
    HamonSkillsTabGui(Minecraft minecraft, HamonScreen screen, String title, int scrollWidth, int scrollHeight) {
        super(minecraft, screen, title, scrollWidth, scrollHeight);
        creativeResetButtonTooltip = minecraft.font.split(new TranslationTextComponent("hamon.reset_creative_only"), 150);
    }

    @Override
    public void addButtons() {
        addButton(learnButton = new HamonScreenButton(screen.windowPosX() + 150, screen.windowPosY() + 92, 64, 20, new TranslationTextComponent("hamon.learnButton"), button -> {
            if (selectedSkill != null) {
                PacketManager.sendToServer(new ClHamonLearnButtonPacket(selectedSkill.getHamonSkill()));
                screen.clickedOnSkill = true;
            }
        }));
        
        addButton(creativeResetButton = new HamonScreenButton(screen.windowPosX() + 16, screen.windowPosY() + 92, 64, 20, new TranslationTextComponent("hamon.resetButton"), button -> {
            PacketManager.sendToServer(new ClHamonResetSkillsButtonPacket(getSkillsType()));
        }));
    }
    
    protected abstract HamonSkillsTab getSkillsType();

    @Override
    protected void drawOnBackground(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY) {
        if (selectedSkillDesc != null) {
            selectedSkillDesc.renderBg(matrixStack, intScrollX, intScrollY, mouseX, mouseY);
        }
    }

    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        renderSkillTrees(screen, matrixStack, mouseX, mouseY);
    }
    
    private void renderSkillTrees(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY) {
        // skill squares
        this.minecraft.getTextureManager().bind(HAMON_SKILLS);
        HamonSkillElementLearnable hovered = null;
        for (HamonSkillElementLearnable skillElement : skills.values()) {
            skillElement.blitBgSquare(matrixStack, intScrollX, intScrollY);
            if (skillElement.isMouseOver(intScrollX, intScrollY, mouseX, mouseY)) {
                hovered = skillElement;
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        // selected skill (green overlay, left upper corner icon)
        if (selectedSkill != null) {
            selectedSkill.blitBgSquareSelection(matrixStack, intScrollX, intScrollY);
            renderHamonSkillIcon(matrixStack, selectedSkill.getHamonSkill(), intScrollX + 4, intScrollY + 4);
        }
        
        // selected/hovered skill requirements (red overlay)
        HamonSkillGuiElement renderMissing = selectedSkill != null ? selectedSkill : hovered != null ? hovered : null;
        if (renderMissing != null) {
            List<AbstractHamonSkill> missingSkills = renderMissing.getHamonSkill().getRequiredSkills().filter(skill -> 
            !screen.hamon.isSkillLearned(skill)).collect(Collectors.toList());
            for (AbstractHamonSkill skill : missingSkills) {
                if (!GeneralUtil.orElseFalse(findSkillSquare(skill), skillElement -> {
                    minecraft.getTextureManager().bind(HAMON_SKILLS);
                    skillElement.blitBgSquareRequirement(matrixStack, intScrollX, intScrollY);
                    return true;
                })) {
                    screen.forEachTabUntil(tab -> {
                        if (tab != this && tab instanceof HamonSkillsTabGui && 
                                ((HamonSkillsTabGui) tab).findSkillSquare(skill).isPresent()) {
                            screen.addSkillRequirementTab(tab);
                        }
                        return false;
                    });
                }
//                findSkillSquare(skill).ifPresent(skillElement -> {
//                    minecraft.getTextureManager().bind(HAMON_SKILLS);
//                    skillElement.blitBgSquareRequirement(matrixStack, intScrollX, intScrollY);
//                });
            }
        }
        
        // skill icons
        for (HamonSkillGuiElement skillElement : skills.values()) {
            skillElement.renderSkillIcon(matrixStack, intScrollX + 5, intScrollY + 5);
        }
        
        // selected skill requirements (icons) 
        for (HamonSkillGuiElement requirement : skillRequirements) {
            boolean mouseOver = requirement.isMouseOver(intScrollX, intScrollY, mouseX, mouseY);
            boolean learned = screen.hamon.isSkillLearned(requirement.getHamonSkill());
            if (mouseOver) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
            else if (!learned) {
                RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
            }
            else {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.4F);
            }
            requirement.renderSkillIcon(matrixStack, intScrollX, intScrollY);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        RenderSystem.disableBlend();
    }
    
    public static void renderHamonSkillIcon(MatrixStack matrixStack, AbstractHamonSkill skill, int x, int y) {
        TextureAtlasSprite textureAtlasSprite = CustomResources.getHamonSkillSprites().getSprite(skill);
        Minecraft.getInstance().getTextureManager().bind(textureAtlasSprite.atlas().location());
        blit(matrixStack, x, y, 0, 16, 16, textureAtlasSprite);
    }
    
    public Optional<HamonSkillElementLearnable> findSkillSquare(AbstractHamonSkill skill) {
        return Optional.ofNullable(skills.get(skill));
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
        List<IReorderingProcessor> skillName = minecraft.font.split(selectedSkill.name, 120);
        ClientUtil.drawLines(matrixStack, minecraft.font, skillName, 
                intScrollX + 22, intScrollY + 8 - minecraft.font.lineHeight * (skillName.size() - 1) * 0.5F, 
                0, 0xFFFFFF, true, false);
        
        ClientUtil.drawRightAlignedString(matrixStack, minecraft.font, 
                selectedSkill.getHamonSkill().getRewardType().getName(), 
                intScrollX + 205, intScrollY + 8, 0xFFFFFF);
        
        if (selectedSkillDesc != null) {
            selectedSkillDesc.drawDesc(matrixStack, minecraft.font, screen, intScrollX, intScrollY);
        }
    }
    
    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        for (HamonSkillGuiElement skill : skills.values()) {
            if (skill.isMouseOver(intScrollX, intScrollY, mouseX, mouseY)) {
                skill.drawTooltip(screen, matrixStack, mouseX, mouseY);
            }
        }
        for (HamonSkillGuiElement skill : skillRequirements) {
            if (skill.isMouseOver(intScrollX, intScrollY, mouseX, mouseY)) {
                skill.drawTooltip(screen, matrixStack, mouseX, mouseY);
            }
        }
        if (creativeResetButton.visible && creativeResetButton.isMouseOver(
                mouseX + screen.windowPosX() + WINDOW_THIN_BORDER, 
                mouseY + screen.windowPosY() + WINDOW_UPPER_BORDER)) {
            screen.renderTooltip(matrixStack, creativeResetButtonTooltip, mouseX, mouseY);
        }
        if (learnButton.visible && !learnButton.active && skillClosedReason != null) {
            int x = mouseX + screen.windowPosX() + WINDOW_THIN_BORDER;
            int y = mouseY + screen.windowPosY() + WINDOW_UPPER_BORDER;
            if (x >= (double)learnButton.x && x < (double)(learnButton.x + learnButton.getWidth())
                    && y >= (double)learnButton.y && y < (double)(learnButton.y + learnButton.getHeight())) {
                screen.renderTooltip(matrixStack, skillClosedReason, mouseX, mouseY);
            }
        }
        if (selectedSkillDesc != null) {
            selectedSkillDesc.drawTooltips(matrixStack, screen, mouseX, mouseY, scrollX, scrollY);
        }
    }

    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton, boolean mouseInsideWindow) {
        if (mouseInsideWindow && mouseButton == 0) {
            if (selectedSkillDesc != null && selectedSkillDesc.onClick((int) mouseX, (int) mouseY, intScrollX, intScrollY)) {
                return true;
            }
            
            for (HamonSkillElementLearnable skill : skills.values()) {
                if (skill.isMouseOver(intScrollX, intScrollY, (int) mouseX, (int) mouseY)) {
                    selectSkill(skill);
                    screen.clickedOnSkill = true;
                    
                    if (this.lastClickDelay < 7 && this.lastClickedSkill == skill && learnButton.active && learnButton.visible) {
                        learnButton.onPress();
                    }
                    this.lastClickDelay = 0;
                    this.lastClickedSkill = skill;
                    
                    return true;
                }
            }
            
            for (HamonSkillGuiElement requirement : skillRequirements) {
                if (requirement.isMouseOver(intScrollX, intScrollY, (int) mouseX, (int) mouseY)) {
                    AbstractHamonSkill skill = requirement.getHamonSkill();
                    return screen.forEachTabUntil(tab -> {
                        if (tab instanceof HamonSkillsTabGui) {
                            return GeneralUtil.orElseFalse(((HamonSkillsTabGui) tab).findSkillSquare(skill), skillElement -> {
                                screen.selectTab(tab);
                                ((HamonSkillsTabGui) tab).selectSkill(skillElement);
                                screen.clickedOnSkill = true;
                                return true;
                            });
                        }
                        return false;
                    });
                }
            }
        }
        return false;
    }
    
    @Override
    void tick() {
        super.tick();
        lastClickDelay++;
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (selectedSkillDesc != null && selectedSkillDesc.onRelease()) {
            return true;
        }
        if (mouseButton == 1 || !screen.clickedOnSkill && !screen.isDragging() && mouseButton == 0 && 
                !learnButton.isMouseOver(mouseX + screen.windowPosX() + WINDOW_THIN_BORDER, mouseY + screen.windowPosY() + WINDOW_UPPER_BORDER)) {
            selectSkill(null);
            return true;
        }
        return false;
    }

    @Override
    void mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (selectedSkillDesc != null && selectedSkillDesc.isMouseOver(mouseX, mouseY, scrollX, scrollY)) {
            selectedSkillDesc.scroll((float) -scroll * 2.5F);
            return;
        }
        super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    void mouseDragged(double xMovement, double yMovement) {
        if (selectedSkillDesc != null && selectedSkillDesc.onDrag(yMovement)) {
            return;
        }
        super.mouseDragged(xMovement, yMovement);
    }
    
    protected void selectSkill(@Nullable HamonSkillElementLearnable guiElement) {
        selectedSkill = guiElement;
        if (guiElement != null) {
            scrollX = 0;
            scrollY = 0;
        }

        selectedSkillDesc = guiElement != null ? createSkillDesc(guiElement.getHamonSkill()) : null;
        
        updateButtons();
    }
    
    protected HamonSkillDescBox createSkillDesc(@Nonnull AbstractHamonSkill skill) {
        return new HamonSkillDescBox(skill, minecraft.font, 192, 7, 25);
    }
    
    @Nullable protected HamonSkillElementLearnable getSelectedSkill() {
        return selectedSkill;
    }
    
    @Override
    protected void updateButtons() {
        learnButton.visible = false;
        learnButton.active = false;
        if (selectedSkill != null) {
            learnButton.visible = !screen.hamon.isSkillLearned(selectedSkill.getHamonSkill());
            ActionConditionResult canLearnSkill = screen.hamon.canLearnSkill(minecraft.player, selectedSkill.getHamonSkill(), screen.teacherSkills);
            ITextComponent closedReason = canLearnSkill.getWarning();
            if (closedReason instanceof IFormattableTextComponent) {
                ((IFormattableTextComponent) closedReason).withStyle(TextFormatting.RED);
            }
            this.skillClosedReason = closedReason != null ? minecraft.font.split(canLearnSkill.getWarning(), 100) : Collections.emptyList();
            learnButton.active = canLearnSkill.isPositive();
        }
        creativeResetButton.visible = selectedSkill == null && HamonData.canResetTab(screen.getMinecraft().player, getSkillsType());
        
        if (selectedSkill != null && learnButton.visible) {
            int reqCount = (int) selectedSkill.skill.getRequiredSkills().count();
            skillRequirements = Streams.mapWithIndex(selectedSkill.skill.getRequiredSkills(), 
                    (skill, i) -> new HamonSkillElementRequirement(skill, 138 + ((int) i - reqCount) * 20, 77))
                    .collect(Collectors.toList());
        }
        else {
            skillRequirements = Collections.emptyList();
        }
    }
    
    @Override
    void updateTab() {
        if (selectedSkill != null) {
            updateButtons();
        }
        for (HamonSkillElementLearnable skillElement : skills.values()) {
            skillElement.updateState(screen.hamon, minecraft.player, screen.teacherSkills);
        }
    }
}
