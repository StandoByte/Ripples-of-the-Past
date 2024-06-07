package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.MultiSoundEvent;
import com.github.standobyte.jojo.util.mc.OstSoundList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JojoMod.MOD_ID);
    
    public static final RegistryObject<SoundEvent> STONE_MASK_ACTIVATION_ENTITY = register("stone_mask_activation_entity");
    
    public static final RegistryObject<SoundEvent> STONE_MASK_ACTIVATION = register("stone_mask_activation");
    
    public static final RegistryObject<SoundEvent> STONE_MASK_DEACTIVATION = register("stone_mask_deactivation");
    
    public static final RegistryObject<SoundEvent> BLADE_HAT_THROW = register("blade_hat_throw");
    
    public static final RegistryObject<SoundEvent> BLADE_HAT_SPINNING = register("blade_hat_spinning");
    
    public static final RegistryObject<SoundEvent> BLADE_HAT_ENTITY_HIT = register("blade_hat_entity_hit");
    
    public static final RegistryObject<SoundEvent> PILLAR_MAN_AWAKENING = register("pillar_man_awakening");
    
    public static final RegistryObject<SoundEvent> AJA_STONE_CHARGING = register("aja_stone_charging");
    
    public static final RegistryObject<SoundEvent> AJA_STONE_BEAM = register("aja_stone_beam");
    
    public static final RegistryObject<SoundEvent> CLACKERS = register("clackers");
    
    public static final RegistryObject<SoundEvent> TOMMY_GUN_SHOT = register("tommy_gun_shot");
    
    public static final RegistryObject<SoundEvent> TOMMY_GUN_NO_AMMO = register("tommy_gun_no_ammo");
    
    public static final RegistryObject<SoundEvent> MOLOTOV_THROW = register("molotov_throw");
    
    public static final RegistryObject<SoundEvent> KNIFE_THROW = register("knife_throw");
    
    public static final RegistryObject<SoundEvent> KNIVES_THROW = register("knives_throw");
    
    public static final RegistryObject<SoundEvent> KNIFE_HIT = register("knife_hit");
    
    public static final RegistryObject<SoundEvent> WATER_SPLASH = register("water_splash");
    
    public static final RegistryObject<SoundEvent> WALKMAN_REWIND = register("walkman_rewind");
    
    public static final RegistryObject<SoundEvent> CASSETTE_WHITE = register("cassette_white");
    public static final RegistryObject<SoundEvent> CASSETTE_ORANGE = register("cassette_orange");
    public static final RegistryObject<SoundEvent> CASSETTE_MAGENTA = register("cassette_magenta");
    public static final RegistryObject<SoundEvent> CASSETTE_LIGHT_BLUE = register("cassette_light_blue");
    public static final RegistryObject<SoundEvent> CASSETTE_YELLOW = register("cassette_yellow");
    public static final RegistryObject<SoundEvent> CASSETTE_LIME = register("cassette_lime");
    public static final RegistryObject<SoundEvent> CASSETTE_PINK = register("cassette_pink");
    public static final RegistryObject<SoundEvent> CASSETTE_GRAY = register("cassette_gray");
    public static final RegistryObject<SoundEvent> CASSETTE_LIGHT_GRAY = register("cassette_light_gray");
    public static final RegistryObject<SoundEvent> CASSETTE_CYAN = register("cassette_cyan");
    public static final RegistryObject<SoundEvent> CASSETTE_PURPLE = register("cassette_purple");
    public static final RegistryObject<SoundEvent> CASSETTE_BLUE = register("cassette_blue");
    public static final RegistryObject<SoundEvent> CASSETTE_BROWN = register("cassette_brown");
    public static final RegistryObject<SoundEvent> CASSETTE_GREEN = register("cassette_green");
    public static final RegistryObject<SoundEvent> CASSETTE_RED = register("cassette_red");
    public static final RegistryObject<SoundEvent> CASSETTE_BLACK = register("cassette_black");

    public static final RegistryObject<SoundEvent> MAP_BOUGHT_METEORITE = register("map_bought_snowy");
    public static final RegistryObject<SoundEvent> MAP_BOUGHT_HAMON_TEMPLE = register("map_bought_mountain");
    public static final RegistryObject<SoundEvent> MAP_BOUGHT_PILLAR_MAN_TEMPLE = register("map_bought_jungle");

    public static final RegistryObject<SoundEvent> VAMPIRE_BLOOD_DRAIN = register("vampire_blood_drain");
    
    public static final RegistryObject<SoundEvent> VAMPIRE_FREEZE = register("vampire_freeze");

    public static final RegistryObject<SoundEvent> VAMPIRE_EVIL_ATMOSPHERE = register("vampire_dark_aura");

    public static final RegistryObject<SoundEvent> VAMPIRE_CURE_START = register("vampire_cure_start");

    public static final RegistryObject<SoundEvent> VAMPIRE_CURE_END = register("vampire_cure_end");


    public static final RegistryObject<SoundEvent> HAMON_PICK_JONATHAN = register("hamon_pick_jonathan");
    public static final RegistryObject<SoundEvent> HAMON_PICK_ZEPPELI = register("hamon_pick_zeppeli");
    public static final RegistryObject<SoundEvent> HAMON_PICK_JOSEPH = register("hamon_pick_joseph");
    public static final RegistryObject<SoundEvent> HAMON_PICK_CAESAR = register("hamon_pick_caesar");
    public static final RegistryObject<SoundEvent> HAMON_PICK_LISA_LISA = register("hamon_pick_lisa_lisa");
    
    public static final RegistryObject<SoundEvent> HAMON_SPARK = register("hamon_spark");
            
    public static final RegistryObject<SoundEvent> HAMON_SPARKS_LONG = register("hamon_sparks_long");
    
    public static final RegistryObject<SoundEvent> HAMON_SPARK_SHORT = register("hamon_spark_short");
    
    public static final RegistryObject<SoundEvent> HAMON_CONCENTRATION = register("hamon_concentration");
    
    public static final RegistryObject<SoundEvent> HAMON_SYO_CHARGE = register("hamon_syo_charge");
    
    public static final RegistryObject<SoundEvent> HAMON_SYO_PUNCH = register("hamon_syo_punch");
    
    public static final RegistryObject<SoundEvent> HAMON_SYO_SWING = register("hamon_syo_swing");
    
    public static final RegistryObject<SoundEvent> GLIDER_FLIGHT = register("glider_flight");
    
    public static final RegistryObject<SoundEvent> HAMON_DETECTOR = register("hamon_detector");
    
    public static final RegistryObject<SoundEvent> BREATH_DEFAULT = SOUNDS.register("player_breath", 
            () -> new SoundEvent(new ResourceLocation("entity.player.breath")));
    
    public static final RegistryObject<SoundEvent> BREATH_JONATHAN = register("jonathan_breath");
    
    public static final RegistryObject<SoundEvent> BREATH_ZEPPELI = register("zeppeli_breath");
    
    public static final RegistryObject<SoundEvent> BREATH_JOSEPH = register("joseph_breath");
    
    public static final RegistryObject<SoundEvent> BREATH_CAESAR = register("caesar_breath");
    
    public static final RegistryObject<SoundEvent> BREATH_LISA_LISA = register("lisa_lisa_breath");
    
    public static final RegistryObject<SoundEvent> JONATHAN_SENDO_OVERDRIVE = register("jonathan_sendo_overdrive");
    
    public static final RegistryObject<SoundEvent> JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE = register("jonathan_sunlight_yellow_overdrive");
    
    public static final RegistryObject<SoundEvent> JONATHAN_ZOOM_PUNCH = register("jonathan_zoom_punch");
    
    public static final RegistryObject<SoundEvent> ZEPPELI_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("zeppeli_sunlight_yellow_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_sunlight_yellow_overdrive"), new ResourceLocation(JojoMod.MOD_ID, "zeppeli_hamon_of_the_sun"), 
                    new ResourceLocation(JojoMod.MOD_ID, "zeppeli_this_is_sendo"), new ResourceLocation(JojoMod.MOD_ID, "zeppeli_this_is_sendo_power")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_ZOOM_PUNCH = register("zeppeli_zoom_punch");
    
    public static final RegistryObject<SoundEvent> ZEPPELI_LIFE_MAGNETISM_OVERDRIVE = register("zeppeli_life_magnetism_overdrive");
    
    public static final RegistryObject<SoundEvent> ZEPPELI_FORCE_BREATH = register("zeppeli_force_breath");
    
    public static final RegistryObject<SoundEvent> JOSEPH_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("joseph_sunlight_yellow_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_hamon_overdrive_beat"), 
                    new ResourceLocation(JojoMod.MOD_ID, "joseph_hamon_punch")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_ZOOM_PUNCH = register("joseph_zoom_punch");
    
    public static final RegistryObject<SoundEvent> JOSEPH_BARRIER = register("joseph_barrier");
    
    public static final RegistryObject<SoundEvent> CAESAR_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("caesar_sunlight_yellow_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_sun_vibration"), new ResourceLocation(JojoMod.MOD_ID, "caesar_hamon_of_the_sun"), 
                    new ResourceLocation(JojoMod.MOD_ID, "caesar_hamon_spark")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_OVERDRIVE_BARRAGE = register("jonathan_overdrive_barrage");
    
    public static final RegistryObject<SoundEvent> JONATHAN_SYO_BARRAGE_START = register("jonathan_syo_barrage_start");
    
    public static final RegistryObject<SoundEvent> JONATHAN_SYO_BARRAGE = register("jonathan_syo_barrage");
    
    public static final RegistryObject<SoundEvent> JONATHAN_SCARLET_OVERDRIVE = SOUNDS.register("jonathan_scarlet_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_scarlet_overdrive"), new ResourceLocation(JojoMod.MOD_ID, "jonathan_hamon_of_flame")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_PLUCK_SWORD = register("jonathan_pluck_sword");
    
    public static final RegistryObject<SoundEvent> ZEPPELI_HAMON_CUTTER = SOUNDS.register("zeppeli_hamon_cutter", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_hamon_cutter"), new ResourceLocation(JojoMod.MOD_ID, "zeppeli_popow_pow_pow")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_SENDO_WAVE_KICK = register("zeppeli_sendo_wave_kick");
    
    public static final RegistryObject<SoundEvent> ZEPPELI_TORNADO_OVERDRIVE = register("zeppeli_tornado_overdrive");
    
    public static final RegistryObject<SoundEvent> ZEPPELI_DEEP_PASS = register("zeppeli_deep_pass");
    
    public static final RegistryObject<SoundEvent> JONATHAN_DEEP_PASS_REACTION = register("jonathan_deep_pass_reaction");
    
    public static final RegistryObject<SoundEvent> JOSEPH_REBUFF_OVERDRIVE = register("joseph_rebuff_overdrive");
    
    public static final RegistryObject<SoundEvent> JOSEPH_OH_NO = register("joseph_oh_no");
    
    public static final RegistryObject<SoundEvent> JOSEPH_CLACKER_VOLLEY = SOUNDS.register("joseph_clacker_volley", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_clacker_volley"), new ResourceLocation(JojoMod.MOD_ID, "joseph_hamon_clacker_volley")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_CLACKER_BOOMERANG = register("joseph_clacker_boomerang");
    
    public static final RegistryObject<SoundEvent> JOSEPH_GIGGLE = register("joseph_giggle");
    
    public static final RegistryObject<SoundEvent> JOSEPH_RUN_AWAY = register("joseph_run_away");
    
    public static final RegistryObject<SoundEvent> JOSEPH_SCREAM_SHOOTING = register("joseph_scream");
    
    public static final RegistryObject<SoundEvent> JOSEPH_WAR_DECLARATION = register("joseph_war_declaration");
    
    public static final RegistryObject<SoundEvent> JOSEPH_SHOOT = register("joseph_shoot");
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_LAUNCHER = SOUNDS.register("caesar_bubble_launcher", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_bubble_launcher"), new ResourceLocation(JojoMod.MOD_ID, "caesar_secret_hamon_bubble_launcher")));
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_BARRIER = register("caesar_bubble_barrier");
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_CUTTER = SOUNDS.register("caesar_bubble_cutter", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_bubble_cutter"), new ResourceLocation(JojoMod.MOD_ID, "caesar_disc_shaped_hamon_cutter")));
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_CUTTER_GLIDING = register("caesar_bubble_cutter_gliding");
    
    public static final RegistryObject<SoundEvent> CAESAR_LAST_HAMON = register("caesar_last_hamon");
    
    public static final RegistryObject<SoundEvent> JOSEPH_CRIMSON_BUBBLE_REACTION = register("joseph_crimson_bubble_reaction");
    
    public static final RegistryObject<SoundEvent> LISA_LISA_AJA_STONE = register("lisa_lisa_aja_stone");
    
    public static final RegistryObject<SoundEvent> LISA_LISA_SUPER_AJA = register("lisa_lisa_super_aja");
    
    public static final RegistryObject<SoundEvent> LISA_LISA_SNAKE_MUFFLER = register("lisa_lisa_snake_muffler");


    public static final RegistryObject<SoundEvent> STAND_SUMMON_DEFAULT = register("stand_summon_default");
    
    public static final RegistryObject<SoundEvent> STAND_UNSUMMON_DEFAULT = register("stand_unsummon_default");

    public static final RegistryObject<SoundEvent> STAND_DAMAGE_BLOCK = register("stand_damage_block");
    
    public static final RegistryObject<SoundEvent> STAND_PUNCH_LIGHT = register("stand_punch_light");
    
    public static final RegistryObject<SoundEvent> STAND_PUNCH_HEAVY = register("stand_punch_heavy");

    public static final RegistryObject<SoundEvent> STAND_PUNCH_SWING = register("stand_punch_swing");

    public static final RegistryObject<SoundEvent> STAND_PUNCH_HEAVY_SWING = register("stand_punch_heavy_swing");

    public static final RegistryObject<SoundEvent> STAND_PUNCH_BARRAGE_SWING = register("stand_punch_barrage_swing");

    public static final RegistryObject<SoundEvent> STAND_PARRY = register("stand_parry");

    public static final RegistryObject<SoundEvent> STAND_LEAP = register("stand_leap");
    
    
    public static final RegistryObject<SoundEvent> JOTARO_STAR_PLATINUM = register("jotaro_star_platinum");

    public static final RegistryObject<SoundEvent> JOTARO_STAR_FINGER = register("jotaro_star_finger");

    public static final RegistryObject<SoundEvent> JOTARO_STAR_PLATINUM_THE_WORLD = register("jotaro_star_platinum_the_world");

    public static final RegistryObject<SoundEvent> JOTARO_TIME_RESUMES = SOUNDS.register("jotaro_time_resumes", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jotaro_time_resumes_dasu"), new ResourceLocation(JojoMod.MOD_ID, "jotaro_time_resumes_hajimeta")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_SUMMON = register("star_platinum_summon");
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_UNSUMMON = register("star_platinum_unsummon");
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ORA = register("star_platinum_ora");
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ORA_LONG = register("star_platinum_ora_long");
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ORA_ORA_ORA = register("star_platinum_ora_ora_ora");
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_STAR_FINGER = register("star_platinum_star_finger");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ZOOM = register("star_platinum_zoom");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ZOOM_CLICK = register("star_platinum_zoom_click");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_INHALE = register("star_platinum_inhale");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_TIME_STOP = register("star_platinum_time_stop");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_TIME_RESUME = register("star_platinum_time_resume");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_TIME_STOP_BLINK = register("star_platinum_time_stop_blink");
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_PUNCH_LIGHT = register("star_platinum_punch_light");

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_PUNCH_HEAVY = register("star_platinum_punch_heavy");

    public static final Supplier<SoundEvent> STAR_PLATINUM_PUNCH_BARRAGE = register("star_platinum_punch_barrage");
    
    public static final OstSoundList STAR_PLATINUM_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> KAKYOIN_HIEROPHANT_GREEN = SOUNDS.register("kakyoin_hierophant_green", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "kakyoin_hierophant_green"), new ResourceLocation(JojoMod.MOD_ID, "kakyoin_hierophant")));

    public static final RegistryObject<SoundEvent> KAKYOIN_EMERALD_SPLASH = register("kakyoin_emerald_splash");
    
    public static final RegistryObject<SoundEvent> RERO = register("rero");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_SUMMON = register("hierophant_green_summon");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_UNSUMMON = register("hierophant_green_unsummon");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_TENTACLES = register("hierophant_green_tentacles");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_EMERALD_SPLASH = register("hierophant_green_emerald_splash");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_BARRIER_PLACED = register("hierophant_green_barrier_placed");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_BARRIER_RIPPED = register("hierophant_green_barrier_ripped");
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_GRAPPLE_CATCH = register("hierophant_green_grapple_catch");
    
    public static final OstSoundList HIEROPHANT_GREEN_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "hierophant_green_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> DIO_THE_WORLD = register("dio_the_world");
    
    public static final RegistryObject<SoundEvent> DIO_MUDA = register("dio_muda");
    
    public static final RegistryObject<SoundEvent> DIO_MUDA_MUDA = register("dio_muda_muda");
    
    public static final RegistryObject<SoundEvent> DIO_WRY = register("dio_wry");

    public static final RegistryObject<SoundEvent> DIO_DIE = register("dio_die");

    public static final RegistryObject<SoundEvent> DIO_THIS_IS_THE_WORLD = register("dio_this_is_the_world");

    public static final RegistryObject<SoundEvent> DIO_TIME_STOP = SOUNDS.register("dio_time_stop", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_toki_yo_tomare"), new ResourceLocation(JojoMod.MOD_ID, "dio_tomare_toki_yo")));

    public static final RegistryObject<SoundEvent> DIO_TIME_RESUMES = register("dio_time_resumes");

    public static final RegistryObject<SoundEvent> DIO_TIMES_UP = SOUNDS.register("dio_times_up", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_time_resumes"), 
                    new ResourceLocation(JojoMod.MOD_ID, "dio_times_up"), new ResourceLocation(JojoMod.MOD_ID, "dio_zero")));

    public static final RegistryObject<SoundEvent> DIO_5_SECONDS = register("dio_5_seconds");
    
    public static final RegistryObject<SoundEvent> DIO_ONE_MORE = register("dio_one_more");
    
    public static final RegistryObject<SoundEvent> DIO_CANT_MOVE = register("dio_cant_move");

    public static final RegistryObject<SoundEvent> DIO_ROAD_ROLLER = register("dio_road_roller");

    public static final RegistryObject<SoundEvent> JONATHAN_THE_WORLD = register("jonathan_the_world");

    public static final RegistryObject<SoundEvent> THE_WORLD_SUMMON = register("the_world_summon");

    public static final RegistryObject<SoundEvent> THE_WORLD_UNSUMMON = register("the_world_unsummon");
    
    public static final RegistryObject<SoundEvent> THE_WORLD_MUDA_MUDA_MUDA = register("the_world_muda_muda_muda");

    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_STOP = register("the_world_time_stop");

    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_RESUME = register("the_world_time_resume");

    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_STOP_BLINK = register("the_world_time_stop_blink");

    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_STOP_UNREVEALED = register("the_world_time_stop_unrevealed");
    
    public static final RegistryObject<SoundEvent> THE_WORLD_PUNCH_LIGHT = register("the_world_punch_light");

    public static final RegistryObject<SoundEvent> THE_WORLD_PUNCH_HEAVY = register("the_world_punch_heavy");

    public static final RegistryObject<SoundEvent> THE_WORLD_PUNCH_HEAVY_ENTITY = register("the_world_punch_heavy_entity");

    public static final RegistryObject<SoundEvent> THE_WORLD_PUNCH_HEAVY_TS_IMPACT = register("the_world_punch_heavy_ts_impact");

    public static final RegistryObject<SoundEvent> THE_WORLD_KICK_HEAVY = register("the_world_kick_heavy");

    public static final Supplier<SoundEvent> THE_WORLD_PUNCH_BARRAGE = THE_WORLD_PUNCH_LIGHT;

    public static final RegistryObject<SoundEvent> ROAD_ROLLER_HIT = register("road_roller_hit");

    public static final RegistryObject<SoundEvent> ROAD_ROLLER_LAND = register("road_roller_land");
    
    public static final OstSoundList THE_WORLD_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "the_world_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> POLNAREFF_SILVER_CHARIOT = SOUNDS.register("polnareff_silver_chariot", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "polnareff_silver_chariot"), new ResourceLocation(JojoMod.MOD_ID, "polnareff_chariot"))); 

    public static final RegistryObject<SoundEvent> POLNAREFF_HORA_HORA_HORA = register("polnareff_hora_hora_hora");

    public static final RegistryObject<SoundEvent> POLNAREFF_FENCING = register("polnareff_fencing");
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_SUMMON = register("silver_chariot_summon");
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_UNSUMMON = register("silver_chariot_unsummon");

    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_SWEEP_LIGHT = register("silver_chariot_sweep_light");

    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_BARRAGE_SWIPE = register("silver_chariot_barrage_swipe");

    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_DASH = register("silver_chariot_dash");

    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_SWEEP_HEAVY = register("silver_chariot_sweep_heavy");

    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_BLOCK = register("silver_chariot_block");
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_RAPIER_SHOT = register("silver_chariot_rapier_shot");
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_ARMOR_OFF = register("silver_chariot_armor_off");
    
    public static final OstSoundList SILVER_CHARIOT_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> AVDOL_MAGICIANS_RED = register("avdol_magicians_red");

    public static final RegistryObject<SoundEvent> AVDOL_HELL_2_U = register("avdol_hell_2_u");

    public static final RegistryObject<SoundEvent> AVDOL_CROSSFIRE_HURRICANE = register("avdol_crossfire_hurricane");

    public static final RegistryObject<SoundEvent> AVDOL_CROSSFIRE_HURRICANE_SPECIAL = register("avdol_crossfire_hurricane_special");

    public static final RegistryObject<SoundEvent> AVDOL_RED_BIND = register("avdol_red_bind");
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_SUMMON = register("magicians_red_summon");
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_UNSUMMON = register("magicians_red_unsummon");

    public static final RegistryObject<SoundEvent> MAGICIANS_RED_FIRE_BLAST = register("magicians_red_fire_ability");

    public static final Supplier<SoundEvent> MAGICIANS_RED_FIREBALL = () -> SoundEvents.FIRECHARGE_USE;
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_CROSSFIRE_HURRICANE = MAGICIANS_RED_FIRE_BLAST;
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_RED_BIND = MAGICIANS_RED_FIRE_BLAST;
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_PUNCH_LIGHT = register("magicians_red_punch_light");

    public static final RegistryObject<SoundEvent> MAGICIANS_RED_PUNCH_HEAVY = register("magicians_red_punch_heavy");

    public static final RegistryObject<SoundEvent> MAGICIANS_RED_KICK_HEAVY = register("magicians_red_kick_heavy");
    
    public static final OstSoundList MAGICIANS_RED_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "magicians_red_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> JOSUKE_CRAZY_DIAMOND = register("josuke_crazy_diamond");

    public static final RegistryObject<SoundEvent> JOSUKE_FIX = register("josuke_fix");

    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_SUMMON = register("crazy_diamond_summon");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_UNSUMMON = register("crazy_diamond_unsummon");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_PUNCH_LIGHT = register("crazy_diamond_punch_light");

    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_PUNCH_HEAVY = register("crazy_diamond_punch_heavy");

    public static final Supplier<SoundEvent> CRAZY_DIAMOND_PUNCH_BARRAGE = CRAZY_DIAMOND_PUNCH_LIGHT;
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_DORA = register("crazy_diamond_dora");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_DORA_LONG = register("crazy_diamond_dora_long");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_DORARARA = register("crazy_diamond_dorarara");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_FIX_STARTED = register("crazy_diamond_fix_started");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_FIX_LOOP = register("crazy_diamond_fix_loop");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_FIX_ENDED = register("crazy_diamond_fix_ended");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_BULLET_SHOT = register("crazy_diamond_bullet_shot");
    
    public static final RegistryObject<SoundEvent> CRAZY_DIAMOND_BLOOD_CUTTER_SHOT = register("crazy_diamond_blood_cutter_shot");
    
    public static final OstSoundList CRAZY_DIAMOND_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "crazy_diamond_ost"), SOUNDS);
    
    
    
    
    
    private static RegistryObject<SoundEvent> register(String regPath) {
        return SOUNDS.register(regPath, () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, regPath)));
    }
}
