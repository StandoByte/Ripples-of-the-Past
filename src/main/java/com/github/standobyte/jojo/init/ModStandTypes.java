package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsKidEntity;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.power.stand.type.BoyIIManStandType;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModStandTypes {
    private static final ITextComponent PART_3_NAME = new TranslationTextComponent("jojo.story_part.3").withStyle(TextFormatting.DARK_PURPLE);
    private static final ITextComponent PART_4_NAME = new TranslationTextComponent("jojo.story_part.4").withStyle(TextFormatting.RED);
//    private static final ITextComponent PART_5_NAME = new TranslationTextComponent("jojo.story_part.5").withStyle(TextFormatting.GOLD);
//    private static final ITextComponent PART_6_NAME = new TranslationTextComponent("jojo.story_part.6").withStyle(TextFormatting.AQUA);
//    
//    private static final ITextComponent PART_7_NAME = new TranslationTextComponent("jojo.story_part.7").withStyle(TextFormatting.LIGHT_PURPLE);
//    private static final ITextComponent PART_8_NAME = new TranslationTextComponent("jojo.story_part.8").withStyle(TextFormatting.WHITE);
    
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), JojoMod.MOD_ID);
    
    public static final RegistryObject<StandType<TimeStopperStandStats>> STAR_PLATINUM = STANDS.register("star_platinum", 
            () -> new EntityStandType<>(0x8E45FF, PART_3_NAME,
                    new StandAction[] {ModActions.STAR_PLATINUM_PUNCH.get(), ModActions.STAR_PLATINUM_BARRAGE.get(), ModActions.STAR_PLATINUM_STAR_FINGER.get()},
                    new StandAction[] {ModActions.STAR_PLATINUM_BLOCK.get(), ModActions.STAR_PLATINUM_ZOOM.get(), ModActions.STAR_PLATINUM_INHALE.get(), ModActions.STAR_PLATINUM_TIME_STOP.get()},
                    TimeStopperStandStats.class, new TimeStopperStandStats.Builder()
                    .tier(6)
                    .power(16.0)
                    .speed(16.0)
                    .range(2.0, 10.0)
                    .durability(16.0)
                    .precision(16.0)
                    .timeStopMaxTicks(100, 180)
                    .timeStopLearningPerTick(0.25F)
                    .timeStopDecayPerDay(0F)
                    .timeStopCooldownPerTick(3F)
                    .build("Star Platinum"), 
                    ModEntityTypes.STAR_PLATINUM)
            .addSummonShout(ModSounds.JOTARO_STAR_PLATINUM)
            .addOst(ModSounds.STAR_PLATINUM_OST));
    
    public static final RegistryObject<StandType<TimeStopperStandStats>> THE_WORLD = STANDS.register("the_world", 
            () -> new EntityStandType<>(0xFFD800, PART_3_NAME,
                    new StandAction[] {ModActions.THE_WORLD_PUNCH.get(), ModActions.THE_WORLD_BARRAGE.get(), ModActions.THE_WORLD_TS_PUNCH.get()},
                    new StandAction[] {ModActions.THE_WORLD_BLOCK.get(), ModActions.THE_WORLD_TIME_STOP.get()},
                    TimeStopperStandStats.class, new TimeStopperStandStats.Builder()
                    .tier(6)
                    .power(16.0)
                    .speed(16.0)
                    .range(2.0, 10.0)
                    .durability(16.0)
                    .precision(12.0)
                    .timeStopMaxTicks(100, 180)
                    .timeStopLearningPerTick(0.1F)
                    .timeStopDecayPerDay(0F)
                    .timeStopCooldownPerTick(3F)
                    .build("The World"), 
                    ModEntityTypes.THE_WORLD)
            .addSummonShout(ModSounds.DIO_THE_WORLD)
            .addOst(ModSounds.THE_WORLD_OST)
            /*.addItemOnResolveLevel(4, new ItemStack(ModItems.ROAD_ROLLER.get()))*/);
    
    public static final RegistryObject<StandType<StandStats>> HIEROPHANT_GREEN = STANDS.register("hierophant_green", 
            () -> new EntityStandType<>(0x00B319, PART_3_NAME,
                    new StandAction[] {ModActions.HIEROPHANT_GREEN_STRING_ATTACK.get(), ModActions.HIEROPHANT_GREEN_EMERALD_SPLASH.get()},
                    new StandAction[] {ModActions.HIEROPHANT_GREEN_BLOCK.get(), ModActions.HIEROPHANT_GREEN_GRAPPLE.get(), ModActions.HIEROPHANT_GREEN_BARRIER.get()},
                    StandStats.class, new StandStats.Builder()
                    .tier(5)
                    .power(8.0)
                    .speed(12.0)
                    .range(50.0, 100.0)
                    .durability(10.0)
                    .precision(8.0)
                    .build("Hierophant Green"), 
                    ModEntityTypes.HIEROPHANT_GREEN)
            .addSummonShout(ModSounds.KAKYOIN_HIEROPHANT_GREEN)
            .addOst(ModSounds.HIEROPHANT_GREEN_OST));
    
    public static final RegistryObject<StandType<StandStats>> SILVER_CHARIOT = STANDS.register("silver_chariot", 
            () -> new EntityStandType<>(0xBEC8D6, PART_3_NAME,
                    new StandAction[] {ModActions.SILVER_CHARIOT_ATTACK.get(), ModActions.SILVER_CHARIOT_BARRAGE.get(), ModActions.SILVER_CHARIOT_RAPIER_LAUNCH.get()},
                    new StandAction[] {ModActions.SILVER_CHARIOT_BLOCK.get(), ModActions.SILVER_CHARIOT_TAKE_OFF_ARMOR.get()},
                    StandStats.class, new StandStats.Builder()
                    .tier(5)
                    .power(9.0)
                    .speed(14.0)
                    .range(10.0)
                    .durability(12.0)
                    .precision(16.0)
                    .build("Silver Chariot"), 
                    ModEntityTypes.SILVER_CHARIOT)
            .addSummonShout(ModSounds.POLNAREFF_SILVER_CHARIOT)
            .addOst(ModSounds.SILVER_CHARIOT_OST));
    
    public static final RegistryObject<StandType<StandStats>> MAGICIANS_RED = STANDS.register("magicians_red", 
            () -> new EntityStandType<>(0xDE203A, PART_3_NAME,
                    new StandAction[] {ModActions.MAGICIANS_RED_PUNCH.get(), ModActions.MAGICIANS_RED_FLAME_BURST.get(), ModActions.MAGICIANS_RED_FIREBALL.get(), ModActions.MAGICIANS_RED_CROSSFIRE_HURRICANE.get()},
                    new StandAction[] {ModActions.MAGICIANS_RED_BLOCK.get(), ModActions.MAGICIANS_RED_RED_BIND.get(), ModActions.MAGICIANS_RED_DETECTOR.get()},
                    StandStats.class, new StandStats.Builder()
                    .tier(5)
                    .power(12.0)
                    .speed(12.0)
                    .range(5.0, 10.0)
                    .durability(12.0)
                    .precision(8.0)
                    .build("Magician's Red"), 
                    ModEntityTypes.MAGICIANS_RED)
            .addSummonShout(ModSounds.AVDOL_MAGICIANS_RED)
            .addOst(ModSounds.MAGICIANS_RED_OST));
    
    
    public static final RegistryObject<StandType<StandStats>> BOY_II_MAN = STANDS.register("boy_ii_man", 
            () -> {
                StandArrowEntity.EntityPierce.addBehavior(
                        () -> RockPaperScissorsKidEntity::canTurnFromArrow, 
                        () -> RockPaperScissorsKidEntity::turnFromArrow);
                return new BoyIIManStandType<>(0x749FA5, PART_4_NAME,
                        new StandAction[] {},
                        new StandAction[] {},
                        StandStats.class, new StandStats.Builder()
                        .tier(0)
                        .power(0)
                        .speed(0)
                        .range(0)
                        .durability(0)
                        .precision(0)
                        .build("Boy II Man"))
                        .setPlayerAccess(false);
            });
    
    public static final Supplier<EntityStandType<StandStats>> KILLER_QUEEN = () -> null;
    
    
    
    public static class Registry {
        private static Supplier<IForgeRegistry<StandType<?>>> REGISTRY_SUPPLIER = null;
        
        public static void initRegistry() {
            if (REGISTRY_SUPPLIER == null) {
                REGISTRY_SUPPLIER = ModStandTypes.STANDS.makeRegistry("stand_type", () -> new RegistryBuilder<>());
            }
        }
        
        public static IForgeRegistry<StandType<?>> getRegistry() {
            return REGISTRY_SUPPLIER.get();
        }
        
        @Nonnull
        public static String getKeyAsString(StandType<?> standType) {
            ResourceLocation resourceLocation = getRegistry().getKey(standType);
            if (resourceLocation == null) {
               return IPowerType.NO_POWER_NAME;
            }
            return resourceLocation.toString();
        }
        
        public static int getNumericId(ResourceLocation regName) {
            return ((ForgeRegistry<StandType<?>>) getRegistry()).getID(regName);
        }
    }
}
