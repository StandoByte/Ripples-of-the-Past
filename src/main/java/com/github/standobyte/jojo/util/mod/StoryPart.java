package com.github.standobyte.jojo.util.mod;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public enum StoryPart {
    PHANTOM_BLOOD           (new TranslationTextComponent("jojo.story_part.1").withStyle(TextFormatting.DARK_BLUE)),
    BATTLE_TENDENCY         (new TranslationTextComponent("jojo.story_part.2").withStyle(TextFormatting.GREEN)),
    STARDUST_CRUSADERS      (new TranslationTextComponent("jojo.story_part.3").withStyle(TextFormatting.DARK_PURPLE)),
    DIAMOND_IS_UNBREAKABLE  (new TranslationTextComponent("jojo.story_part.4").withStyle(TextFormatting.RED)),
    GOLDEN_WIND             (new TranslationTextComponent("jojo.story_part.5").withStyle(TextFormatting.GOLD)),
    STONE_OCEAN             (new TranslationTextComponent("jojo.story_part.6").withStyle(TextFormatting.AQUA)),
    STEEL_BALL_RUN          (new TranslationTextComponent("jojo.story_part.7").withStyle(TextFormatting.LIGHT_PURPLE)),
    JOJOLION                (new TranslationTextComponent("jojo.story_part.8").withStyle(TextFormatting.WHITE)),
    THE_JOJOLANDS           (new TranslationTextComponent("jojo.story_part.9").withStyle(TextFormatting.BLUE)),
    OTHER                   (new TranslationTextComponent("jojo.story_part.none").withStyle(TextFormatting.GRAY));
    
    private final ITextComponent name;
    
    private StoryPart(ITextComponent name) {
        this.name = name;
    }
    
    public ITextComponent getName() {
        return name;
    }
}
