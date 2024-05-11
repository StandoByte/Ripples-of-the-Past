package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_THIN_BORDER;
import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.widgets.utils.WidgetExtension;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonCharacterTechniqueBox {
    final CharacterHamonTechnique technique;
    private final List<IReorderingProcessor> name;
    private final List<HamonSkillElementTechniquePerk> perks;
    private final List<HamonSkillElementLearnable> skills = new ArrayList<>();
    private HamonScreenButton pickTechniqueButton;
    private int x;
    private int y;
    private final int width;
    private final int height;
    
    public HamonCharacterTechniqueBox(CharacterHamonTechnique technique, int y, List<IReorderingProcessor> name, FontRenderer font) {
        this.technique = technique;
        this.x = 8;
        this.y = y;
        this.name = name;
        this.perks = Streams.mapWithIndex(
                technique.getPerksOnPick().filter(perk -> perk != null), 
                (skill, i) -> new HamonSkillElementTechniquePerk(skill, this.x + (int) i * 18, this.y, font))
                .collect(Collectors.toCollection(ArrayList::new));
        if (technique == ModHamonSkills.CHARACTER_CAESAR.get()) {
            perks.add(new HamonSkillElementTechniquePerk(this.x + (perks.size() - 1) * 18, this.y, font, 
                    new TranslationTextComponent("hamon.caesar_soap_hint.name"),
                    new TranslationTextComponent("hamon.caesar_soap_hint.desc").withStyle(TextFormatting.ITALIC))
                    .withItemIcon(new ItemStack(ModItems.SOAP.get())));
        }
        this.width = WINDOW_WIDTH - WINDOW_THIN_BORDER * 2 - 16;
        this.height = Math.max(name.size() * 9 + 26, 36);
    }

    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrixStack, HamonData hamon, int x, int y, int mouseX, int mouseY, boolean selected) {
        int col1 = selected ? 0x80101000 : 0x80100010;
        int col2 = selected ? 0x5050FF00 : 0x505000FF;
        int col3 = selected ? 0x50287F00 : 0x5028007F;
        ClientUtil.drawTooltipRectangle(matrixStack, 
                this.x + x, this.y + y, width, height, 
                col1, col2, col3, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (renderPerks(hamon)) {
            for (HamonSkillElementTechniquePerk perk : perks) {
                if (perk.isVisible()) {
                    CharacterHamonTechnique userTechnique = hamon.getCharacterTechnique();
                    if (!perk.isMouseOver(x, y, mouseX, mouseY)) {
                        if (userTechnique == this.technique) {
                            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.4F);
                        }
                        else {
                            RenderSystem.color4f(0.0F, 0.0F, 0.0F, 1.0F);
                        }
                    }
                    perk.renderSkillIcon(matrixStack, x, y);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
        RenderSystem.disableBlend();
    }
    
    public void drawText(MatrixStack matrixStack, FontRenderer font, HamonData hamon, int x, int y) {
        int perksCount = renderPerks(hamon) ? (int) perks.stream().filter(HamonSkillElementTechniquePerk::isVisible).count() : 0;
        ClientUtil.drawLines(matrixStack, font, name, 
                this.x + x + perksCount * 18 + 3, this.y + y + 1, 
                0, 0xFFFFFF, true, false);
    }
    
    public void drawTooltip(HamonScreen hamonScreen, MatrixStack matrixStack, int x, int y, int mouseX, int mouseY) {
        x += this.x;
        y += this.y;
        
        if (renderPerks(hamonScreen.hamon)) {
            for (HamonSkillElementTechniquePerk perk : perks) {
                if (perk.isVisible() && perk.isMouseOver(x, y, this.x + mouseX, this.y + mouseY) ) {
                    perk.drawTooltip(hamonScreen, matrixStack, mouseX, mouseY);
                }
            }
        }
    }
    
    int getHeight() {
        return height;
    }

    private boolean renderPerks(HamonData hamon) {
        CharacterHamonTechnique userTechnique = hamon.getCharacterTechnique();
        return userTechnique == null || userTechnique == this.technique;
    }
    
    public void addSkill(HamonSkillElementLearnable skill) {
        skills.add(skill);
    }
    
    public void addPickButton(HamonScreenButton pickTechniqueButton) {
        this.pickTechniqueButton = pickTechniqueButton;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        int yDiff = y - this.y;
        for (HamonSkillGuiElement skill : skills) {
            skill.setY(skill.getY() + yDiff);
        }
        for (HamonSkillGuiElement perk : perks) {
            perk.setY(perk.getY() + yDiff);
        }
        if (pickTechniqueButton != null) {
            WidgetExtension ext = pickTechniqueButton.getWidgetExtension();
            ext.setY(ext.getYStarting() + yDiff);
        }
        this.y = y;
    }
}
