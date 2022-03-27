package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.MultiSoundEvent;
import com.github.standobyte.jojo.util.OstSoundList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JojoMod.MOD_ID);
    
    public static final RegistryObject<SoundEvent> STONE_MASK_ACTIVATION_ENTITY = SOUNDS.register("stone_mask_activation_entity",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "stone_mask_activation_entity")));
    
    public static final RegistryObject<SoundEvent> STONE_MASK_ACTIVATION = SOUNDS.register("stone_mask_activation",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "stone_mask_activation")));
    
    public static final RegistryObject<SoundEvent> STONE_MASK_DEACTIVATION = SOUNDS.register("stone_mask_deactivation",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "stone_mask_deactivation")));
    
    public static final RegistryObject<SoundEvent> BLADE_HAT_THROW = SOUNDS.register("blade_hat_throw",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "blade_hat_throw")));
    
    public static final RegistryObject<SoundEvent> BLADE_HAT_SPINNING = SOUNDS.register("blade_hat_spinning",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "blade_hat_spinning")));
    
    public static final RegistryObject<SoundEvent> BLADE_HAT_ENTITY_HIT = SOUNDS.register("blade_hat_entity_hit",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "blade_hat_entity_hit")));
    
    public static final RegistryObject<SoundEvent> PILLAR_MAN_AWAKENING = SOUNDS.register("pillar_man_awakening",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "pillar_man_awakening")));
    
    public static final RegistryObject<SoundEvent> AJA_STONE_CHARGING = SOUNDS.register("aja_stone_charging",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "aja_stone_charging")));
    
    public static final RegistryObject<SoundEvent> AJA_STONE_BEAM = SOUNDS.register("aja_stone_beam",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "aja_stone_beam")));
    
    public static final RegistryObject<SoundEvent> CLACKERS = SOUNDS.register("clackers", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "clackers")));
    
    public static final RegistryObject<SoundEvent> TOMMY_GUN_SHOT = SOUNDS.register("tommy_gun_shot", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "tommy_gun_shot")));
    
    public static final RegistryObject<SoundEvent> TOMMY_GUN_NO_AMMO = SOUNDS.register("tommy_gun_no_ammo", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "tommy_gun_no_ammo")));
    
    public static final RegistryObject<SoundEvent> KNIFE_THROW = SOUNDS.register("knife_throw",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "knife_throw")));
    
    public static final RegistryObject<SoundEvent> KNIVES_THROW = SOUNDS.register("knives_throw",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "knives_throw")));
    
    public static final RegistryObject<SoundEvent> KNIFE_HIT = SOUNDS.register("knife_hit",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "knife_hit")));


    public static final RegistryObject<SoundEvent> VAMPIRE_BLOOD_DRAIN = SOUNDS.register("vampire_blood_drain",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "vampire_blood_drain")));
    
    public static final RegistryObject<SoundEvent> VAMPIRE_FREEZE = SOUNDS.register("vampire_freeze",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "vampire_freeze")));

    public static final RegistryObject<SoundEvent> VAMPIRE_DARK_AURA = SOUNDS.register("vampire_dark_aura",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "vampire_dark_aura")));

    
    public static final RegistryObject<SoundEvent> HAMON_SPARK = SOUNDS.register("hamon_spark",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "hamon_spark")));
            
    public static final RegistryObject<SoundEvent> HAMON_SPARKS_LONG = SOUNDS.register("hamon_sparks_long",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "hamon_sparks_long")));
    
    public static final RegistryObject<SoundEvent> HAMON_CONCENTRATION = SOUNDS.register("hamon_concentration",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "hamon_concentration")));
    
    public static final RegistryObject<SoundEvent> BREATH_DEFAULT = SOUNDS.register("player_breath", 
            () -> new SoundEvent(new ResourceLocation("entity.player.breath")));
    
    public static final RegistryObject<SoundEvent> BREATH_JONATHAN = SOUNDS.register("jonathan_breath", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_breath")));
    
    public static final RegistryObject<SoundEvent> BREATH_ZEPPELI = SOUNDS.register("zeppeli_breath", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_breath")));
    
    public static final RegistryObject<SoundEvent> BREATH_JOSEPH = SOUNDS.register("joseph_breath", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_breath")));
    
    public static final RegistryObject<SoundEvent> BREATH_CAESAR = SOUNDS.register("caesar_breath", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_breath")));
    
    public static final RegistryObject<SoundEvent> BREATH_LISA_LISA = SOUNDS.register("lisa_lisa_breath", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "lisa_lisa_breath")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_SENDO_OVERDRIVE = SOUNDS.register("jonathan_sendo_overdrive", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_sendo_overdrive")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("jonathan_sunlight_yellow_overdrive", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_sunlight_yellow_overdrive")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_ZOOM_PUNCH = SOUNDS.register("jonathan_zoom_punch", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_zoom_punch")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("zeppeli_sunlight_yellow_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_sunlight_yellow_overdrive"), new ResourceLocation(JojoMod.MOD_ID, "zeppeli_hamon_of_the_sun"), 
                    new ResourceLocation(JojoMod.MOD_ID, "zeppeli_this_is_sendo"), new ResourceLocation(JojoMod.MOD_ID, "zeppeli_this_is_sendo_power")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_ZOOM_PUNCH = SOUNDS.register("zeppeli_zoom_punch", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_zoom_punch")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_LIFE_MAGNETISM_OVERDRIVE = SOUNDS.register("zeppeli_life_magnetism_overdrive", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_life_magnetism_overdrive")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_FORCE_BREATH = SOUNDS.register("zeppeli_force_breath", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_force_breath")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("joseph_sunlight_yellow_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_hamon_overdrive_beat"), 
                    new ResourceLocation(JojoMod.MOD_ID, "joseph_hamon_punch"), new ResourceLocation(JojoMod.MOD_ID, "joseph_rebuff_overdrive")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_ZOOM_PUNCH = SOUNDS.register("joseph_zoom_punch", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_zoom_punch")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_BARRIER = SOUNDS.register("joseph_barrier", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_barrier")));
    
    public static final RegistryObject<SoundEvent> CAESAR_SUNLIGHT_YELLOW_OVERDRIVE = SOUNDS.register("caesar_sunlight_yellow_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_sun_vibration"), new ResourceLocation(JojoMod.MOD_ID, "caesar_hamon_of_the_sun"), 
                    new ResourceLocation(JojoMod.MOD_ID, "caesar_hamon_spark")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_OVERDRIVE_BARRAGE = SOUNDS.register("jonathan_overdrive_barrage", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_overdrive_barrage")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_SCARLET_OVERDRIVE = SOUNDS.register("jonathan_scarlet_overdrive", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_scarlet_overdrive"), new ResourceLocation(JojoMod.MOD_ID, "jonathan_hamon_of_flame")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_PLUCK_SWORD = SOUNDS.register("jonathan_pluck_sword", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_pluck_sword")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_HAMON_CUTTER = SOUNDS.register("zeppeli_hamon_cutter", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_hamon_cutter"), new ResourceLocation(JojoMod.MOD_ID, "zeppeli_popow_pow_pow")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_TORNADO_OVERDRIVE = SOUNDS.register("zeppeli_tornado_overdrive", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_tornado_overdrive")));
    
    public static final RegistryObject<SoundEvent> ZEPPELI_DEEP_PASS = SOUNDS.register("zeppeli_deep_pass", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "zeppeli_deep_pass")));
    
    public static final RegistryObject<SoundEvent> JONATHAN_DEEP_PASS_REACTION = SOUNDS.register("jonathan_deep_pass_reaction", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jonathan_deep_pass_reaction")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_OH_NO = SOUNDS.register("joseph_oh_no", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_oh_no")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_CLACKER_VOLLEY = SOUNDS.register("joseph_clacker_volley", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_clacker_volley"), new ResourceLocation(JojoMod.MOD_ID, "joseph_hamon_clacker_volley")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_CLACKER_BOOMERANG = SOUNDS.register("joseph_clacker_boomerang", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_clacker_boomerang")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_GIGGLE = SOUNDS.register("joseph_giggle", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_giggle")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_RUN_AWAY = SOUNDS.register("joseph_run_away", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_run_away")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_SCREAM_SHOOTING = SOUNDS.register("joseph_scream", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_scream")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_WAR_DECLARATION = SOUNDS.register("joseph_war_declaration", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_war_declaration")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_SHOOT = SOUNDS.register("joseph_shoot", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_shoot")));
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_LAUNCHER = SOUNDS.register("caesar_bubble_launcher", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_bubble_launcher"), new ResourceLocation(JojoMod.MOD_ID, "caesar_secret_hamon_bubble_launcher")));
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_BARRIER = SOUNDS.register("caesar_bubble_barrier", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_bubble_barrier")));
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_CUTTER = SOUNDS.register("caesar_bubble_cutter", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_bubble_cutter"), new ResourceLocation(JojoMod.MOD_ID, "caesar_disc_shaped_hamon_cutter")));
    
    public static final RegistryObject<SoundEvent> CAESAR_BUBBLE_CUTTER_GLIDING = SOUNDS.register("caesar_bubble_cutter_gliding", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_bubble_cutter_gliding")));
    
    public static final RegistryObject<SoundEvent> CAESAR_LAST_HAMON = SOUNDS.register("caesar_last_hamon", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "caesar_last_hamon")));
    
    public static final RegistryObject<SoundEvent> JOSEPH_CRIMSON_BUBBLE_REACTION = SOUNDS.register("joseph_crimson_bubble_reaction", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "joseph_crimson_bubble_reaction")));
    
    public static final RegistryObject<SoundEvent> LISA_LISA_AJA_STONE = SOUNDS.register("lisa_lisa_aja_stone", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "lisa_lisa_aja_stone")));
    
    public static final RegistryObject<SoundEvent> LISA_LISA_SUPER_AJA = SOUNDS.register("lisa_lisa_super_aja", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "lisa_lisa_super_aja")));
    
    public static final RegistryObject<SoundEvent> LISA_LISA_SNAKE_MUFFLER = SOUNDS.register("lisa_lisa_snake_muffler", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "lisa_lisa_snake_muffler")));


    public static final RegistryObject<SoundEvent> STAND_SUMMON_DEFAULT = SOUNDS.register("stand_summon_default", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "stand_summon_default")));
    
    public static final RegistryObject<SoundEvent> STAND_UNSUMMON_DEFAULT = SOUNDS.register("stand_unsummon_default", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "stand_unsummon_default")));
    
    public static final RegistryObject<SoundEvent> PARRY = SOUNDS.register("parry", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "parry")));
    
    
    public static final RegistryObject<SoundEvent> JOTARO_STAR_PLATINUM = SOUNDS.register("jotaro_star_platinum", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jotaro_star_platinum")));

    public static final RegistryObject<SoundEvent> JOTARO_STAR_FINGER = SOUNDS.register("jotaro_star_finger", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jotaro_star_finger")));

    public static final RegistryObject<SoundEvent> JOTARO_THE_WORLD = SOUNDS.register("jotaro_the_world", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jotaro_the_world")));

    public static final RegistryObject<SoundEvent> JOTARO_STAR_PLATINUM_THE_WORLD = SOUNDS.register("jotaro_star_platinum_the_world", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jotaro_star_platinum_the_world")));

    public static final RegistryObject<SoundEvent> JOTARO_TIME_RESUMES = SOUNDS.register("jotaro_time_resumes", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "jotaro_time_resumes_dasu"), new ResourceLocation(JojoMod.MOD_ID, "jotaro_time_resumes_hajimeta")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_SUMMON = SOUNDS.register("star_platinum_summon", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_summon")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_UNSUMMON = SOUNDS.register("star_platinum_unsummon", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_unsummon")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ORA = SOUNDS.register("star_platinum_ora", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_ora")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ORA_LONG = SOUNDS.register("star_platinum_ora_long", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_ora_long")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ORA_ORA_ORA = SOUNDS.register("star_platinum_ora_ora_ora", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_ora_ora_ora")));
    
    public static final RegistryObject<SoundEvent> STAR_PLATINUM_STAR_FINGER = SOUNDS.register("star_platinum_star_finger", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_star_finger")));

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ZOOM = SOUNDS.register("star_platinum_zoom", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_zoom")));

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_ZOOM_CLICK = SOUNDS.register("star_platinum_zoom_click", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_zoom_click")));

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_TIME_STOP = SOUNDS.register("star_platinum_time_stop", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_time_stop")));

    public static final RegistryObject<SoundEvent> STAR_PLATINUM_TIME_RESUME = SOUNDS.register("star_platinum_time_resume", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_time_resume")));
    
    public static final OstSoundList STAR_PLATINUM_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "star_platinum_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> TIME_STOP_BLINK = SOUNDS.register("time_stop_blink", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "time_stop_blink")));

    public static final RegistryObject<SoundEvent> KAKYOIN_HIEROPHANT_GREEN = SOUNDS.register("kakyoin_hierophant_green", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "kakyoin_hierophant_green"), new ResourceLocation(JojoMod.MOD_ID, "kakyoin_hierophant")));

    public static final RegistryObject<SoundEvent> KAKYOIN_EMERALD_SPLASH = SOUNDS.register("kakyoin_emerald_splash", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "kakyoin_emerald_splash")));
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_SUMMON = SOUNDS.register("hierophant_green_summon", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "hierophant_green_summon")));
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_EMERALD_SPLASH = SOUNDS.register("hierophant_green_emerald_splash", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "hierophant_green_emerald_splash")));
    
    public static final RegistryObject<SoundEvent> HIEROPHANT_GREEN_BARRIER_RIPPED = SOUNDS.register("hierophant_green_barrier_ripped", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "hierophant_green_barrier_ripped")));
    
    public static final OstSoundList HIEROPHANT_GREEN_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "hierophant_green_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> DIO_THE_WORLD = SOUNDS.register("dio_the_world", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_the_world")));

    public static final RegistryObject<SoundEvent> DIO_DIE = SOUNDS.register("dio_die", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_die")));

    public static final RegistryObject<SoundEvent> DIO_TIME_STOP = SOUNDS.register("dio_time_stop", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_toki_yo_tomare"), new ResourceLocation(JojoMod.MOD_ID, "dio_tomare_toki_yo")));

    public static final RegistryObject<SoundEvent> DIO_TIME_RESUMES = SOUNDS.register("dio_time_resumes", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_time_resumes"), new ResourceLocation(JojoMod.MOD_ID, "dio_times_up")));

    public static final RegistryObject<SoundEvent> DIO_CANT_MOVE = SOUNDS.register("dio_cant_move", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_cant_move")));

    public static final RegistryObject<SoundEvent> DIO_ROAD_ROLLER = SOUNDS.register("dio_road_roller", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "dio_road_roller")));

    public static final RegistryObject<SoundEvent> THE_WORLD_SUMMON = SOUNDS.register("the_world_summon", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "the_world_summon")));
    
    public static final RegistryObject<SoundEvent> THE_WORLD_MUDA = SOUNDS.register("the_world_muda", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "the_world_muda")));
    
    public static final RegistryObject<SoundEvent> THE_WORLD_MUDA_MUDA_MUDA = SOUNDS.register("the_world_muda_muda_muda", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "the_world_muda_muda_muda")));

    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_STOP = SOUNDS.register("the_world_time_stop", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "the_world_time_stop")));

    public static final RegistryObject<SoundEvent> THE_WORLD_TIME_RESUME = SOUNDS.register("the_world_time_resume", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "the_world_time_resume")));

    public static final RegistryObject<SoundEvent> ROAD_ROLLER_HIT = SOUNDS.register("road_roller_hit", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "road_roller_hit")));

    public static final RegistryObject<SoundEvent> ROAD_ROLLER_LAND = ROAD_ROLLER_HIT;
    
    public static final OstSoundList THE_WORLD_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "the_world_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> POLNAREFF_SILVER_CHARIOT = SOUNDS.register("polnareff_silver_chariot", 
            () -> new MultiSoundEvent(new ResourceLocation(JojoMod.MOD_ID, "polnareff_silver_chariot"), new ResourceLocation(JojoMod.MOD_ID, "polnareff_chariot")));

    public static final RegistryObject<SoundEvent> POLNAREFF_HORA_HORA_HORA = SOUNDS.register("polnareff_hora_hora_hora", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "polnareff_hora_hora_hora")));

    public static final RegistryObject<SoundEvent> POLNAREFF_FENCING = SOUNDS.register("polnareff_fencing", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "polnareff_fencing")));
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_SUMMON = SOUNDS.register("silver_chariot_summon",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_summon")));
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_UNSUMMON = SOUNDS.register("silver_chariot_unsummon",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_unsummon")));
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_RAPIER_SHOT = SOUNDS.register("silver_chariot_rapier_shot",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_rapier_shot")));
    
    public static final RegistryObject<SoundEvent> SILVER_CHARIOT_ARMOR_OFF = SOUNDS.register("silver_chariot_armor_off",
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_armor_off")));
    
    public static final OstSoundList SILVER_CHARIOT_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_ost"), SOUNDS);

    public static final RegistryObject<SoundEvent> AVDOL_MAGICIANS_RED = SOUNDS.register("avdol_magicians_red", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "avdol_magicians_red")));

    public static final RegistryObject<SoundEvent> AVDOL_CROSSFIRE_HURRICANE = SOUNDS.register("avdol_crossfire_hurricane", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "avdol_crossfire_hurricane")));

    public static final RegistryObject<SoundEvent> AVDOL_CROSSFIRE_HURRICANE_SPECIAL = SOUNDS.register("avdol_crossfire_hurricane_special", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "avdol_crossfire_hurricane_special")));

    public static final RegistryObject<SoundEvent> AVDOL_RED_BIND = SOUNDS.register("avdol_red_bind", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "avdol_red_bind")));
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_SUMMON = SOUNDS.register("magicians_red_summon", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "magicians_red_summon")));

    public static final RegistryObject<SoundEvent> MAGICIANS_RED_FIRE_BLAST = SOUNDS.register("magicians_red_fire_ability", 
            () -> new SoundEvent(new ResourceLocation(JojoMod.MOD_ID, "magicians_red_fire_ability")));

    public static final Supplier<SoundEvent> MAGICIANS_RED_FIREBALL = () -> SoundEvents.FIRECHARGE_USE;
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_CROSSFIRE_HURRICANE = MAGICIANS_RED_FIRE_BLAST;
    
    public static final RegistryObject<SoundEvent> MAGICIANS_RED_RED_BIND = MAGICIANS_RED_FIRE_BLAST;
    
    public static final OstSoundList MAGICIANS_RED_OST = new OstSoundList(new ResourceLocation(JojoMod.MOD_ID, "magicians_red_ost"), SOUNDS);
}
