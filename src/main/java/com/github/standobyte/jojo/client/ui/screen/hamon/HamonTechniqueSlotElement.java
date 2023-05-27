package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterTechniqueHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.HamonTechniqueManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonTechniqueSlotElement {
    private final int index;
    private final int x;
    private final int y;
    private Optional<HamonSkillElementLearnable> skillElement = Optional.empty();
    private State state;

    public HamonTechniqueSlotElement(int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }
    
    public void setSkill(HamonSkillElementLearnable skillElement) {
        this.skillElement = Optional.ofNullable(skillElement);
    }
    
    public static List<HamonTechniqueSlotElement> createSlots(HamonScreen screen, Function<Integer, HamonTechniqueSlotElement> createSlot) {
        HamonData hamon = screen.hamon;
        Minecraft mc = screen.getMinecraft();
        boolean freeSlot = true;
        List<HamonTechniqueSlotElement> slots = new ArrayList<>();
        Iterator<CharacterTechniqueHamonSkill> learnedSkills = hamon.getTechniqueData().getLearnedSkills().iterator();
        for (int i = 0; i < HamonTechniqueManager.techniqueSlotsCount(true); i++) {
            HamonTechniqueSlotElement slot = createSlot.apply(i);
            slots.add(slot);
            
            State state;
            if (!hamon.hasTechniqueLevel(i, true)) {
                state = State.LOCKED;
            }
            else if (learnedSkills.hasNext()) {
                CharacterTechniqueHamonSkill skill = learnedSkills.next();
                
                state = State.HAS_SKILL;
                slot.setSkill(new HamonSkillElementLearnable(skill, 
                        hamon, mc.player, screen.teacherSkills, 
                        false, slot.x, slot.y));
            }
            else {
                state = freeSlot ? State.EMPTY_NEXT : State.EMPTY;
                freeSlot = false;
            }
            
            slot.state = state;
        }
        return slots;
    }
    
    void renderSlot(MatrixStack matrixStack, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        x += this.x;
        y += this.y;
        Minecraft.getInstance().getTextureManager().bind(HamonSkillsTabGui.HAMON_SKILLS);
        AbstractGui.blit(matrixStack, x, y, 0, 156, 28, 28, 256, 256);
        
        x++;
        y++;
        switch (state) {
        case LOCKED:
            AbstractGui.blit(matrixStack, x, y, 
                    52, 0, 26, 26, 256, 256);
            break;
        case EMPTY_NEXT:
            AbstractGui.blit(matrixStack, x, y, 
                    0, 26, 26, 26, 256, 256);
            break;
        case HAS_SKILL:
            HamonSkillElementLearnable skillIcon = skillElement.get();
            x -= skillIcon.getX();
            y -= skillIcon.getY();
            skillIcon.blitBgSquare(matrixStack, x, y);
            skillIcon.renderSkillIcon(matrixStack, x + 5, y + 5);
            break;
        case EMPTY:
            AbstractGui.blit(matrixStack, x, y, 
                    0, 0, 26, 26, 256, 256);
            break;
        }

        RenderSystem.disableBlend();
    }
    
    void drawTooltip(MatrixStack matrixStack, HamonScreen screen, int scrollX, int scrollY, int mouseX, int mouseY) {
        if (isMouseOver(scrollX, scrollY, mouseX, mouseY)) {
            switch (state) {
            case LOCKED:
                screen.renderTooltip(matrixStack, screen.getMinecraft().font.split(
                        new TranslationTextComponent("hamon.technique_slot.locked", HamonTechniqueManager.techniqueSkillRequirement(index, true)), 
                        170), mouseX, mouseY);
                break;
            case EMPTY_NEXT:
                screen.renderTooltip(matrixStack, new TranslationTextComponent("hamon.technique_slot.free"), mouseX, mouseY);
                break;
            case HAS_SKILL:
                getSkillElement().ifPresent(skill -> {
                    if (skill.isMouseOver(scrollX, scrollY, mouseX, mouseY)) {
                        skill.drawTooltip(screen, matrixStack, mouseX, mouseY);
                    }
                });
                break;
            case EMPTY:
                break;
            }
        }
    }
    
    boolean isMouseOver(int intScrollX, int intScrollY, int mouseX, int mouseY) {
        double realX = intScrollX + this.x + 1;
        double realY = intScrollY + this.y + 1;
        return mouseX >= realX && mouseX < realX + 26 && mouseY >= realY && mouseY < realY + 26;
    }
    
    Optional<HamonSkillElementLearnable> getSkillElement() {
        return skillElement;
    }
    
    
    
    private enum State {
        LOCKED,
        EMPTY_NEXT,
        EMPTY,
        HAS_SKILL
    }
}
