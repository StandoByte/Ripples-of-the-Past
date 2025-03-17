package com.github.standobyte.jojo.util.mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.text.JojoTextComponentWrapper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class StoryPart {
    private static final List<StoryPart> ALL_VALUES = new ArrayList<>();
    private static List<ITextComponent> TOOLTIPS_WITH_ICONS = new ArrayList<>();
    private static boolean updateOrder = false;
    private static final Map<String, StoryPart> ALL_VALUES_MAP = new HashMap<>();
    
    public static final StoryPart PHANTOM_BLOOD           = canon(1, "jojo.story_part.1", name -> name.withStyle(TextFormatting.DARK_BLUE));
    public static final StoryPart BATTLE_TENDENCY         = canon(2, "jojo.story_part.2", name -> name.withStyle(TextFormatting.GREEN));
    public static final StoryPart STARDUST_CRUSADERS      = canon(3, "jojo.story_part.3", name -> name.withStyle(TextFormatting.DARK_PURPLE));
    public static final StoryPart DIAMOND_IS_UNBREAKABLE  = canon(4, "jojo.story_part.4", name -> name.withStyle(TextFormatting.RED));
    public static final StoryPart GOLDEN_WIND             = canon(5, "jojo.story_part.5", name -> name.withStyle(TextFormatting.GOLD));
    public static final StoryPart STONE_OCEAN             = canon(6, "jojo.story_part.6", name -> name.withStyle(TextFormatting.AQUA));
    public static final StoryPart STEEL_BALL_RUN          = canon(7, "jojo.story_part.7", name -> name.withStyle(TextFormatting.LIGHT_PURPLE));
    public static final StoryPart JOJOLION                = canon(8, "jojo.story_part.8", name -> name.withStyle(TextFormatting.WHITE));
    public static final StoryPart THE_JOJOLANDS           = canon(9, "jojo.story_part.9", name -> name.withStyle(TextFormatting.BLUE));
    public static final StoryPart OTHER                   = canon(-1, "jojo.story_part.none", name -> name.withStyle(TextFormatting.GRAY));
    public static final StoryPart[] CANON_PARTS = { PHANTOM_BLOOD, BATTLE_TENDENCY, STARDUST_CRUSADERS, DIAMOND_IS_UNBREAKABLE, GOLDEN_WIND, STONE_OCEAN, STEEL_BALL_RUN, JOJOLION, THE_JOJOLANDS };
    
    private final ITextComponent name;
    private final ITextComponent tooltipName;
    @Nullable private ResourceLocation sprite;
    private OptionalInt canonPart = OptionalInt.empty();
    
    private static StoryPart canon(int partNumber, String name, Consumer<IFormattableTextComponent> style) {
        ResourceLocation sprite = new ResourceLocation(JojoMod.MOD_ID, String.valueOf(partNumber));
        StoryPart storyPart = create(partNumber > 0 ? sprite : null, name, style);
        storyPart.canonPart = OptionalInt.of(partNumber);
        return storyPart;
    }
    
    public static StoryPart create(ResourceLocation sprite, String name, @Nullable Consumer<IFormattableTextComponent> style) {
        IFormattableTextComponent nameComponent = new TranslationTextComponent(name);
        if (style != null) style.accept(nameComponent);
        return new StoryPart(spritePath(sprite), name, nameComponent);
    }
    
    protected static ResourceLocation spritePath(ResourceLocation id) {
        return id == null ? null : new ResourceLocation(id.getNamespace(), "textures/gui/story_part/" + id.getPath() + ".png");
    }
    
    protected StoryPart(ResourceLocation sprite, String id, IFormattableTextComponent name) {
        this.sprite = sprite;
        this.name = name;
        this.tooltipName = new JojoTextComponentWrapper(name).setStoryPartSprite(this);
        ALL_VALUES.add(this);
        ALL_VALUES_MAP.put(id, this);
        TOOLTIPS_WITH_ICONS.add(tooltipName);
    }
    
    
    /**
     * Example:<br><br>
     * 
     * <code>public static final StoryPart PURPLE_HAZE_FEEDBACK = StoryPart.create(
            new ResourceLocation(PHFAddon.MOD_ID, "phf"), 
            "phf.part_name", 
            name -> name.withStyle(style -> style.withColor(Color.parseColor("#8f3fb1"))))
            .placeAfter(StoryPart.GOLDEN_WIND);</code>
     */
    public StoryPart placeAfter(StoryPart prevPart) {
        if (ALL_VALUES.contains(prevPart)) {
            ALL_VALUES.remove(this);
            ALL_VALUES.add(ALL_VALUES.indexOf(prevPart) + 1, this);
            updateOrder = true;
        }
        return this;
    }
    
    public StoryPart placeBefore(StoryPart nextPart) {
        if (ALL_VALUES.contains(nextPart)) {
            ALL_VALUES.remove(this);
            ALL_VALUES.add(ALL_VALUES.indexOf(nextPart), this);
            updateOrder = true;
        }
        return this;
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
    
    public OptionalInt canonPartNumber() {
        return canonPart;
    }
    
    
    public static Iterable<StoryPart> values() {
        return ALL_VALUES;
    }
    
    public static StoryPart getFromName(String name) {
        return ALL_VALUES_MAP.get(name);
    }
    
    public static Comparator<ITextComponent> partNamesComparator() {
        if (updateOrder) {
            TOOLTIPS_WITH_ICONS = ALL_VALUES.stream().map(StoryPart::getName).collect(Collectors.toCollection(ArrayList::new));
            updateOrder = false;
        }
        return (n1, n2) -> {
            int index1 = TOOLTIPS_WITH_ICONS.indexOf(n1);
            int index2 = TOOLTIPS_WITH_ICONS.indexOf(n2);
            if (index2 < 0) return -1;
            if (index1 < 0) return 1;
            return index1 - index2;
        };
    }
    
}
