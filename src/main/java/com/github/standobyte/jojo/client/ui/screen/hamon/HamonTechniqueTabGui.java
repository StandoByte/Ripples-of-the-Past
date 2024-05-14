package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.widgets.utils.IExtendedWidget;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonPickTechniquePacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket.HamonSkillsTab;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterTechniqueHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.HamonTechniqueManager;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.LazyUnmodifiableArrayList;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonTechniqueTabGui extends HamonSkillsTabGui {
    private CharacterHamonTechnique technique;
    private Map<CharacterHamonTechnique, HamonCharacterTechniqueBox> availableHamonTechniques = Collections.emptyMap();
    private CharacterHamonTechnique selectedTechnique = null;
    private List<HamonScreenButton> pickTechniqueButtons = Collections.emptyList();
    private final List<IReorderingProcessor> availableTechniqueSkillLines;
    private final List<IReorderingProcessor> tabLockedLines;
    private List<HamonTechniqueSlotElement> techniqueSkillSlots = Collections.emptyList();
    
    HamonTechniqueTabGui(Minecraft minecraft, HamonScreen screen, String title) {
        super(minecraft, screen, title, -1, -1);
        if (!isLocked()) {
            fillSkillLines();
        }
        availableTechniqueSkillLines = minecraft.font.split(new TranslationTextComponent("hamon.technique_available")
                .withStyle(TextFormatting.ITALIC, TextFormatting.GRAY), 100);
        tabLockedLines = HamonTechniqueManager.techniquesEnabled(true) ? 
                minecraft.font.split(new TranslationTextComponent("hamon.techniques_locked", 
                        HamonTechniqueManager.techniqueSkillRequirement(0, true)), 200) 
                : Collections.emptyList();
        
        // FIXME tmp
        creativeResetButtonTooltip = minecraft.font.split(new TranslationTextComponent("hamon.reset_tmp"), 150);
    }
    
    @Override
    protected ITextComponent createTabDescription(String key) {
        TmpHamonWipTabGui.makeCompilerShoutAtMeWhenIDeleteThis(); // uncomment this
        return new TranslationTextComponent(key, 
                JojoModConfig.getCommonConfigInstance(true).mixHamonTechniques.get() ? ""
                        : new TranslationTextComponent("hamon.techniques.tab.desc.only_one"));
    }
    
    private int techniqueYStarting() {
        return 135 + (techniqueSkillSlots.size() - 1) / MAX_ROW_SKILL_SLOTS * SKILL_SLOTS_ROW_HEIGHT;
    }
    private static final int TECHNIQUE_Y_GAP = 10;
    private static final int MAX_ROW_SKILL_SLOTS = 7;
    private static final int SKILL_SLOTS_ROW_HEIGHT = 32;
    
    private void fillSkillLines() {
        skills.clear();
        
        // available techniques
        List<CharacterHamonTechnique> techniques;
        this.technique = screen.hamon.getCharacterTechnique();
        if (technique != null && !JojoModConfig.getCommonConfigInstance(true).mixHamonTechniques.get()) {
            techniques = Util.make(new ArrayList<>(), list -> list.add(technique));
        }
        else {
            techniques = new ArrayList<>(JojoCustomRegistries.HAMON_CHARACTER_TECHNIQUES.getRegistry().getValues());
            Collections.sort(techniques, TECHNIQUES_ORDER);
        }
        
        List<HamonScreenButton> newButtons = new ArrayList<>();
        
        // technique skill slots
        int slotsCount = HamonTechniqueManager.techniqueSlotsCount(true);
        int rowsCount = slotsCount / MAX_ROW_SKILL_SLOTS;
        techniqueSkillSlots = HamonTechniqueSlotElement.createSlots(screen, i -> {
            int row = i / MAX_ROW_SKILL_SLOTS;
            float skillsInRow = row < rowsCount ? MAX_ROW_SKILL_SLOTS : slotsCount % MAX_ROW_SKILL_SLOTS;
            int x = (int) ((i % MAX_ROW_SKILL_SLOTS + 0.5F) * (float) (WINDOW_WIDTH - WINDOW_THIN_BORDER * 2) / skillsInRow) - 14;
            int y = 97 + row * SKILL_SLOTS_ROW_HEIGHT;
            return new HamonTechniqueSlotElement(i, x, y);
        });
        
        // technique names, buttons, skill squares and y coordinates
        availableHamonTechniques = new LinkedHashMap<>();
        int techniqueY = techniqueYStarting();
        for (CharacterHamonTechnique technique : techniques) {
            List<IReorderingProcessor> name = minecraft.font.split(new TranslationTextComponent("hamon.technique." + technique.getName()), 192);
            HamonCharacterTechniqueBox techniqueBox = new HamonCharacterTechniqueBox(technique, techniqueY, name, minecraft.font);
            availableHamonTechniques.put(technique, techniqueBox);
            
            HamonScreenButton pickButton = new HamonScreenButton(
                    screen.windowPosX() + 16, screen.windowPosY() + techniqueY + techniqueBox.getHeight() - 1, 
                    80, 20, 
                    new TranslationTextComponent("hamon.pick_technique"), 
                    button -> {
                        PacketManager.sendToServer(new ClHamonPickTechniquePacket(technique));
                    });
            newButtons.add(pickButton);
            techniqueBox.addPickButton(pickButton);
            
            List<CharacterTechniqueHamonSkill> skills = technique.getSkills().collect(Collectors.toList());
            int j = 0;
            for (CharacterTechniqueHamonSkill skill : skills) {
                int x = WINDOW_WIDTH - 21 - (skills.size() - j) * 28;
                int y = techniqueY + name.size() * 9 + 4;
                HamonSkillElementLearnable skillSquare = new HamonSkillElementLearnable(skill, 
                        screen.hamon, minecraft.player, screen.teacherSkills, 
                        false, x, y);
                this.skills.put(skill, skillSquare);
                techniqueBox.addSkill(skillSquare);
                j++;
            }
            
            techniqueY += techniqueBox.getHeight() + TECHNIQUE_Y_GAP;
        }
        
        setMaxY(techniqueY);
        
        if (getSelectedSkill() != null) {
            selectSkill(skills.get(getSelectedSkill().getHamonSkill()));
        }
        
        pickTechniqueButtons.forEach(screen::removeButton);
        pickTechniqueButtons = newButtons;
        newButtons.forEach(screen::addButton);
    }
    
    @Override
    protected List<IExtendedWidget> getWidgets() {
        return Stream.concat(
                super.getWidgets().stream(), 
                pickTechniqueButtons.stream())
                .collect(Collectors.toList());
    }
    
    @Override
    protected HamonSkillsTab getSkillsType() {
        return HamonSkillsTab.TECHNIQUE;
    }
    
    @Override
    void drawIcon(MatrixStack matrixStack, int windowX, int windowY, ItemRenderer itemRenderer) {
        TmpHamonWipTabGui.makeCompilerShoutAtMeWhenIDeleteThis(); // uncomment this
//        if (screen.hamon.getTechniqueData().canLearnNewTechniqueSkill(screen.hamon, minecraft.player)) {
//            int x = tabPositioning.getIconX(windowX, index, WINDOW_WIDTH);
//            int y = tabPositioning.getIconY(windowY, index, WINDOW_HEIGHT);
//            
//            minecraft.getTextureManager().bind(HamonScreen.WINDOW);
//            
//            blit(matrixStack, x - 6, y - 3, 248, 206, 8, 8);
//        }
    }

    @Override
    protected void drawDesc(MatrixStack matrixStack) {
        if (getSelectedSkill() != null) {
            drawSkillDesc(matrixStack);
        }
        else {
            ClientUtil.drawLines(matrixStack, minecraft.font, descLines, 
                    (float) scrollX + 6, (float) scrollY + 5, 0, 0xFFFFFF, false, false);
        }
    }
    
    @Override
    List<IReorderingProcessor> additionalTabNameTooltipInfo() {
        TmpHamonWipTabGui.makeCompilerShoutAtMeWhenIDeleteThis(); // uncomment this
//        if (screen.hamon.getTechniqueData().canLearnNewTechniqueSkill(screen.hamon, minecraft.player)) {
//            return availableTechniqueSkillLines;
//        }
        return super.additionalTabNameTooltipInfo();
    }
    
    private boolean isLocked() {
        return !HamonTechniqueManager.techniquesEnabled(true)
                || screen.hamon.getCharacterTechnique() == null && !screen.hamon.hasTechniqueLevel(0, true);
    }
    
    @Override
    protected void updateButtons() {
        if (isLocked()) {
            learnButton.visible = false;
            creativeResetButton.visible = false;
            pickTechniqueButtons.forEach(button -> button.visible = false);
        }
        else {
            super.updateButtons();
            this.technique = screen.hamon.getCharacterTechnique();
            if (learnButton.visible && technique == null
                    && getSelectedSkill().getHamonSkill() instanceof CharacterTechniqueHamonSkill) {
                learnButton.visible = false;
                skillRequirements = skillRequirements.stream().map(skillIcon -> 
                new HamonSkillElementRequirement(skillIcon.getHamonSkill(), skillIcon.getX() + learnButton.getWidth() + 0, skillIcon.getY()))
                        .collect(Collectors.toList());
            }
            pickTechniqueButtons.forEach(button -> button.visible = technique == null);
            
            CharacterHamonTechnique technique = null;
            if (getSelectedSkill() != null && getSelectedSkill().getHamonSkill() instanceof CharacterTechniqueHamonSkill) {
                technique = ((CharacterTechniqueHamonSkill) getSelectedSkill().getHamonSkill()).getTechnique();
            }
            reorderTechniqueBoxes(technique);
        }
    }
    
    @Override
    protected void drawText(MatrixStack matrixStack) {
        if (!isLocked()) {
            drawDesc(matrixStack);
            availableHamonTechniques.values().forEach(technique -> technique.drawText(matrixStack, minecraft.font, screen.hamon, intScrollX, intScrollY));
        }
        else {
            for (int i = 0; i < tabLockedLines.size(); i++) {
                ClientUtil.drawCenteredString(matrixStack, minecraft.font, tabLockedLines.get(i), 
                        (float) (scrollX - WINDOW_THIN_BORDER + WINDOW_WIDTH / 2), (float) (scrollY + 22 + i * 9), 0xFFFFFF);
            }
        }
    }
    
    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!isLocked()) {
            availableHamonTechniques.values().forEach(technique -> 
            technique.render(matrixStack, screen.hamon, intScrollX, intScrollY, mouseX, mouseY, selectedTechnique == technique.technique));
            super.drawActualContents(screen, matrixStack, mouseX, mouseY, partialTick);
        }        drawTechniqueSlots(matrixStack, mouseX, mouseY);
    }
    
    private void drawTechniqueSlots(MatrixStack matrixStack, int mouseX, int mouseY) {
        for (HamonTechniqueSlotElement slot : techniqueSkillSlots) {
            slot.renderSlot(matrixStack, intScrollX, intScrollY);
        }
    }
    
    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton, boolean mouseInsideWindow) {
        if (isLocked()) return false;
        
        if (mouseButton == 0) {
            for (HamonTechniqueSlotElement slot : techniqueSkillSlots) {
                if (GeneralUtil.orElseFalse(slot.getSkillElement(), skillSlot -> {
                    if (skillSlot.isMouseOver(intScrollX, intScrollY, (int) mouseX, (int) mouseY)) {
                        AbstractHamonSkill skill = skillSlot.getHamonSkill();
                        screen.forEachTabUntil(tab -> {
                            if (tab instanceof HamonSkillsTabGui) {
                                Map<AbstractHamonSkill, HamonSkillElementLearnable> tabSkills = ((HamonSkillsTabGui) tab).skills;
                                if (tabSkills.containsKey(skill)) {
                                    HamonSkillElementLearnable skillElement = tabSkills.get(skill);
                                    screen.selectTab(tab);
                                    ((HamonSkillsTabGui) tab).selectSkill(skillElement);
                                    screen.clickedOnSkill = true;
                                    return true;
                                }
                            }
                            return false;
                        });
                    }
                    return false;
                })) {
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, mouseButton, mouseInsideWindow);
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return isLocked() ? false : super.mouseReleased(mouseX, mouseY, mouseButton);
    }
    
    @Override
    void scroll(double xMovement, double yMovement) {
        if (!isLocked()) {
            super.scroll(xMovement, yMovement);
        }
    }
    
    @Override
    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {
        if (!isLocked()) {
            super.drawToolTips(matrixStack, mouseX, mouseY, windowPosX, windowPosY);
            availableHamonTechniques.values().forEach(technique -> technique.drawTooltip(
                    screen, matrixStack, intScrollX, intScrollY, mouseX, mouseY));
            
            for (HamonTechniqueSlotElement skillSlot : techniqueSkillSlots) {
                skillSlot.drawTooltip(matrixStack, screen, intScrollX, intScrollY, mouseX, mouseY);
            }
        }
    }
    
    @Override
    void updateTab() {
        if (!isLocked()) {
            super.updateTab();
            CharacterHamonTechnique newTechnique = screen.hamon.getCharacterTechnique();
            this.technique = newTechnique;
            fillSkillLines();
            updateButtons();
            

//            for (HamonSkillElementLearnable skillElement : skills.values()) {
//                skillElement.updateState(screen.hamon, minecraft.player, screen.teacherSkills);
//            }
        }
    }

    
    private void reorderTechniqueBoxes(@Nullable CharacterHamonTechnique firstTechnique) {
        selectedTechnique = firstTechnique;
        if (availableHamonTechniques.size() <= 1) {
            return;
        }
        List<CharacterHamonTechnique> techniques = new ArrayList<>(JojoCustomRegistries.HAMON_CHARACTER_TECHNIQUES.getRegistry().getValues());
        Collections.sort(techniques, TECHNIQUES_ORDER);
        if (firstTechnique != null) {
            Collections.sort(techniques, (t1, t2) -> t1 == firstTechnique ? -1 : t2 == firstTechnique ? 1 : 0);
        }
        
        int y = techniqueYStarting();
        for (CharacterHamonTechnique technique : techniques) {
            HamonCharacterTechniqueBox characterBox = availableHamonTechniques.get(technique);
            if (characterBox != null) {
                characterBox.setY(y);
                y += characterBox.getHeight() + TECHNIQUE_Y_GAP;
            }
        }
    }

    private static final LazyUnmodifiableArrayList<CharacterHamonTechnique> MOD_TECHNIQUES_ORDER = LazyUnmodifiableArrayList.of(
            Util.make(new ArrayList<>(), list -> {
                list.add(ModHamonSkills.CHARACTER_JONATHAN);
                list.add(ModHamonSkills.CHARACTER_ZEPPELI);
                list.add(ModHamonSkills.CHARACTER_JOSEPH);
                list.add(ModHamonSkills.CHARACTER_CAESAR);
                list.add(ModHamonSkills.CHARACTER_LISA_LISA);
            }));
    private static final Comparator<CharacterHamonTechnique> TECHNIQUES_ORDER = (technique1, technique2) -> {
        int t1 = MOD_TECHNIQUES_ORDER.indexOf(technique1);
        int t2 = MOD_TECHNIQUES_ORDER.indexOf(technique2);
        // one is from base mod, other isn't, base mod techniques are first
        if (t1 > -1 ^ t2 > -1) {
            return t1 > -1 ? -1 : 1;
        }
        else {
            // specific order for base mod techniques
            if (t1 > -1 /*&& t2 > -1*/) {
                return t1 - t2;
            }
            // the rest are in alphabetical order
            else /*if (t1 == -1 && t2 == -1)*/ {
                return technique1.getRegistryName().toString().compareTo(technique2.getRegistryName().toString());
            }
        }
    };
}
