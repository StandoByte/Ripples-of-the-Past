package com.github.standobyte.jojo.util.mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.text.JojoTextComponentWrapper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public enum StoryPart {
    PHANTOM_BLOOD           (1, new TranslationTextComponent("jojo.story_part.1").withStyle(TextFormatting.DARK_BLUE)),
    BATTLE_TENDENCY         (2, new TranslationTextComponent("jojo.story_part.2").withStyle(TextFormatting.GREEN)),
    STARDUST_CRUSADERS      (3, new TranslationTextComponent("jojo.story_part.3").withStyle(TextFormatting.DARK_PURPLE)),
    DIAMOND_IS_UNBREAKABLE  (4, new TranslationTextComponent("jojo.story_part.4").withStyle(TextFormatting.RED)),
    GOLDEN_WIND             (5, new TranslationTextComponent("jojo.story_part.5").withStyle(TextFormatting.GOLD)),
    STONE_OCEAN             (6, new TranslationTextComponent("jojo.story_part.6").withStyle(TextFormatting.AQUA)),
    STEEL_BALL_RUN          (7, new TranslationTextComponent("jojo.story_part.7").withStyle(TextFormatting.LIGHT_PURPLE)),
    JOJOLION                (8, new TranslationTextComponent("jojo.story_part.8").withStyle(TextFormatting.WHITE)),
    THE_JOJOLANDS           (9, new TranslationTextComponent("jojo.story_part.9").withStyle(TextFormatting.BLUE)),
    OTHER                   (-1, new TranslationTextComponent("jojo.story_part.none").withStyle(TextFormatting.GRAY));
    
    private final ITextComponent name;
    private final ITextComponent tooltipName;
    @Nullable private ResourceLocation sprite;
    
    private StoryPart(int partNumber, IFormattableTextComponent name) {
        if (partNumber > 0) {
            this.sprite = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/story_part/" + partNumber + ".png");
        }
        this.name = name;
        this.tooltipName = new JojoTextComponentWrapper(name).setStoryPartSprite(this);
    }
    
    public ITextComponent getName() {
        return tooltipName;
    }
    
    public ITextComponent getNonTooltipName() {
        return name;
    }
    
    public ResourceLocation getSprite() {
        return sprite;
    }
    
    
    public static final List<ITextComponent> PART_NAMES_SORTED = Arrays.stream(StoryPart.values())
            .map(StoryPart::getName)
            .collect(Collectors.toCollection(ArrayList::new));
    
    public static Comparator<ITextComponent> partNamesComparator() {
        return (n1, n2) -> {
            int index1 = PART_NAMES_SORTED.indexOf(n1);
            int index2 = PART_NAMES_SORTED.indexOf(n2);
            if (index2 < 0) return -1;
            if (index1 < 0) return 1;
            return index1 - index2;
        };
    }
    
    /**
     * Example:<br><br>
     * 
     * <code>public static final ITextComponent PHF_NAME = new TranslationTextComponent("purple_haze_feedback_name");<br>
     * static { StoryPart.addToOrder(PHF_NAME, StoryPart.GOLDEN_WIND.getName()); }</code>
     */
    public static void addToOrder(ITextComponent storyPartName, ITextComponent putAfter) {
        if (!PART_NAMES_SORTED.contains(storyPartName)) {
            int index = PART_NAMES_SORTED.indexOf(putAfter);
            if (index > -1) {
                PART_NAMES_SORTED.add(index + 1, storyPartName);
            }
        }
    }
}
