package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.actions.HamonAction;
import com.github.standobyte.jojo.action.actions.HamonBubbleBarrier;
import com.github.standobyte.jojo.action.actions.HamonBubbleCutter;
import com.github.standobyte.jojo.action.actions.HamonBubbleLauncher;
import com.github.standobyte.jojo.action.actions.HamonCutter;
import com.github.standobyte.jojo.action.actions.HamonDetector;
import com.github.standobyte.jojo.action.actions.HamonHealing;
import com.github.standobyte.jojo.action.actions.HamonLifeMagnetism;
import com.github.standobyte.jojo.action.actions.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.actions.HamonOverdrive;
import com.github.standobyte.jojo.action.actions.HamonOverdriveBarrage;
import com.github.standobyte.jojo.action.actions.HamonPlantInfusion;
import com.github.standobyte.jojo.action.actions.HamonProjectileShield;
import com.github.standobyte.jojo.action.actions.HamonRepellingOverdrive;
import com.github.standobyte.jojo.action.actions.HamonScarletOverdrive;
import com.github.standobyte.jojo.action.actions.HamonSendoOverdrive;
import com.github.standobyte.jojo.action.actions.HamonSpeedBoost;
import com.github.standobyte.jojo.action.actions.HamonSunlightYellowOverdrive;
import com.github.standobyte.jojo.action.actions.HamonTornadoOverdrive;
import com.github.standobyte.jojo.action.actions.HamonWallClimbing;
import com.github.standobyte.jojo.action.actions.HamonZoomPunch;
import com.github.standobyte.jojo.action.actions.HierophantGreenBarrier;
import com.github.standobyte.jojo.action.actions.HierophantGreenEmeraldSplash;
import com.github.standobyte.jojo.action.actions.HierophantGreenGrapple;
import com.github.standobyte.jojo.action.actions.HierophantGreenStringAttack;
import com.github.standobyte.jojo.action.actions.MagiciansRedCrossfireHurricane;
import com.github.standobyte.jojo.action.actions.MagiciansRedDetector;
import com.github.standobyte.jojo.action.actions.MagiciansRedFireball;
import com.github.standobyte.jojo.action.actions.MagiciansRedFlameBurst;
import com.github.standobyte.jojo.action.actions.MagiciansRedKick;
import com.github.standobyte.jojo.action.actions.MagiciansRedRedBind;
import com.github.standobyte.jojo.action.actions.NonStandAction;
import com.github.standobyte.jojo.action.actions.SilverChariotDashAttack;
import com.github.standobyte.jojo.action.actions.SilverChariotLightAttack;
import com.github.standobyte.jojo.action.actions.SilverChariotMeleeBarrage;
import com.github.standobyte.jojo.action.actions.SilverChariotRapierLaunch;
import com.github.standobyte.jojo.action.actions.SilverChariotSweepingAttack;
import com.github.standobyte.jojo.action.actions.SilverChariotTakeOffArmor;
import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.action.actions.StandEntityAction.Phase;
import com.github.standobyte.jojo.action.actions.StandEntityBlock;
import com.github.standobyte.jojo.action.actions.StandEntityComboHeavyAttack;
import com.github.standobyte.jojo.action.actions.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.actions.StandEntityLightAttack;
import com.github.standobyte.jojo.action.actions.StandEntityMeleeBarrage;
import com.github.standobyte.jojo.action.actions.StandEntityUnsummon;
import com.github.standobyte.jojo.action.actions.StarPlatinumInhale;
import com.github.standobyte.jojo.action.actions.StarPlatinumStarFinger;
import com.github.standobyte.jojo.action.actions.StarPlatinumZoom;
import com.github.standobyte.jojo.action.actions.TheWorldBarrage;
import com.github.standobyte.jojo.action.actions.TheWorldTSHeavyAttack;
import com.github.standobyte.jojo.action.actions.TheWorldTimeStop;
import com.github.standobyte.jojo.action.actions.TheWorldTimeStopInstant;
import com.github.standobyte.jojo.action.actions.TimeResume;
import com.github.standobyte.jojo.action.actions.TimeStop;
import com.github.standobyte.jojo.action.actions.TimeStopInstant;
import com.github.standobyte.jojo.action.actions.VampirismAction;
import com.github.standobyte.jojo.action.actions.VampirismBloodDrain;
import com.github.standobyte.jojo.action.actions.VampirismBloodGift;
import com.github.standobyte.jojo.action.actions.VampirismDarkAura;
import com.github.standobyte.jojo.action.actions.VampirismFreeze;
import com.github.standobyte.jojo.action.actions.VampirismHamonSuicide;
import com.github.standobyte.jojo.action.actions.VampirismSpaceRipperStingyEyes;
import com.github.standobyte.jojo.action.actions.VampirismZombieSummon;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModActions {
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), JojoMod.MOD_ID);
    
    public static final RegistryObject<HamonAction> HAMON_OVERDRIVE = ACTIONS.register("hamon_overdrive", 
            () -> new HamonOverdrive(new HamonAction.Builder().energyCost(750F)));
     
    public static final RegistryObject<HamonAction> HAMON_SUNLIGHT_YELLOW_OVERDRIVE = ACTIONS.register("hamon_sunlight_yellow_overdrive", 
            () -> new HamonSunlightYellowOverdrive(new HamonAction.Builder().energyCost(1500F).emptyMainHand()
                    .shout(Technique.JONATHAN, ModSounds.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shout(Technique.ZEPPELI, ModSounds.ZEPPELI_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shout(Technique.JOSEPH, ModSounds.JOSEPH_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shout(Technique.CAESAR, ModSounds.CAESAR_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shiftVariationOf(HAMON_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_SENDO_OVERDRIVE = ACTIONS.register("hamon_sendo_overdrive", 
            () -> new HamonSendoOverdrive(new HamonAction.Builder().energyCost(1000F).emptyMainHand().swingHand()
                    .shout(Technique.JONATHAN, ModSounds.JONATHAN_SENDO_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_ZOOM_PUNCH = ACTIONS.register("hamon_zoom_punch", 
            () -> new HamonZoomPunch(new HamonAction.Builder().energyCost(800F).cooldown(14, 0).emptyMainHand()
                    .shout(Technique.JONATHAN, ModSounds.JONATHAN_ZOOM_PUNCH)
                    .shout(Technique.ZEPPELI, ModSounds.ZEPPELI_ZOOM_PUNCH)
                    .shout(Technique.JOSEPH, ModSounds.JOSEPH_ZOOM_PUNCH)));

    public static final RegistryObject<HamonAction> HAMON_SPEED_BOOST = ACTIONS.register("hamon_speed_boost", 
            () -> new HamonSpeedBoost(new HamonAction.Builder().energyCost(600F)));
    
    public static final RegistryObject<HamonAction> HAMON_PLANT_INFUSION = ACTIONS.register("hamon_plant_infusion", 
            () -> new HamonPlantInfusion(new HamonAction.Builder().energyCost(200F).emptyMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_ORGANISM_INFUSION = ACTIONS.register("hamon_organism_infusion", 
            () -> new HamonOrganismInfusion(new HamonAction.Builder().energyCost(200F).emptyMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_HEALING = ACTIONS.register("hamon_healing", 
            () -> new HamonHealing(new HamonAction.Builder().energyCost(670F).emptyMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_WALL_CLIMBING = ACTIONS.register("hamon_wall_climbing", 
            () -> new HamonWallClimbing(new HamonAction.Builder().holdType().holdEnergyCost(15F).emptyMainHand()));
    
    public static final RegistryObject<HamonAction> HAMON_DETECTOR = ACTIONS.register("hamon_detector", 
            () -> new HamonDetector(new HamonAction.Builder().holdType().holdEnergyCost(7.5F).heldSlowDownFactor(0.5F)));
    
    public static final RegistryObject<HamonAction> HAMON_LIFE_MAGNETISM = ACTIONS.register("hamon_life_magnetism", 
            () -> new HamonLifeMagnetism(new HamonAction.Builder().energyCost(200F).emptyMainHand().shout(Technique.ZEPPELI, ModSounds.ZEPPELI_LIFE_MAGNETISM_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_PROJECTILE_SHIELD = ACTIONS.register("hamon_projectile_shield", 
            () -> new HamonProjectileShield(new HamonAction.Builder().holdType().holdEnergyCost(30F).heldSlowDownFactor(0.5F).shout(Technique.JOSEPH, ModSounds.JOSEPH_BARRIER)));
    
    public static final RegistryObject<HamonAction> HAMON_REPELLING_OVERDRIVE = ACTIONS.register("hamon_repelling_overdrive", 
            () -> new HamonRepellingOverdrive(new HamonAction.Builder().energyCost(1000F)));

    public static final RegistryObject<HamonAction> JONATHAN_OVERDRIVE_BARRAGE = ACTIONS.register("jonathan_overdrive_barrage", 
            () -> new HamonOverdriveBarrage(new HamonAction.Builder().holdType().holdEnergyCost(70F).heldSlowDownFactor(0.5F)
                    .itemCheck(Hand.MAIN_HAND, ItemStack::isEmpty, "hands").itemCheck(Hand.OFF_HAND, ItemStack::isEmpty, "hands").shout(ModSounds.JONATHAN_OVERDRIVE_BARRAGE)));

    public static final RegistryObject<HamonAction> JONATHAN_SCARLET_OVERDRIVE = ACTIONS.register("jonathan_scarlet_overdrive", 
            () -> new HamonScarletOverdrive(new HamonAction.Builder().energyCost(150F).emptyMainHand()
                    .shout(ModSounds.JONATHAN_SCARLET_OVERDRIVE).swingHand().doNotCancelClick()));
    
    public static final RegistryObject<HamonAction> ZEPPELI_HAMON_CUTTER = ACTIONS.register("zeppeli_hamon_cutter", 
            () -> new HamonCutter(new HamonAction.Builder().energyCost(400F).shout(ModSounds.ZEPPELI_HAMON_CUTTER)));
    
    public static final RegistryObject<HamonAction> ZEPPELI_TORNADO_OVERDRIVE = ACTIONS.register("zeppeli_tornado_overdrive", 
            () -> new HamonTornadoOverdrive(new HamonAction.Builder().shout(ModSounds.ZEPPELI_TORNADO_OVERDRIVE).holdType().holdEnergyCost(75F)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_LAUNCHER = ACTIONS.register("caesar_bubble_launcher", 
            () -> new HamonBubbleLauncher(new HamonAction.Builder().holdType().holdEnergyCost(50F).heldSlowDownFactor(0.3F).shout(ModSounds.CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_BARRIER = ACTIONS.register("caesar_bubble_barrier", 
            () -> new HamonBubbleBarrier(new HamonAction.Builder().holdToFire(20, false).holdEnergyCost(50F).heldSlowDownFactor(0.3F)
                    .shout(ModSounds.CAESAR_BUBBLE_BARRIER).shiftVariationOf(CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER = ACTIONS.register("caesar_bubble_cutter", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().energyCost(500F).swingHand().shout(ModSounds.CAESAR_BUBBLE_CUTTER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER_GLIDING = ACTIONS.register("caesar_bubble_cutter_gliding", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().energyCost(600F).cooldown(0, 40).swingHand()
                    .shout(ModSounds.CAESAR_BUBBLE_CUTTER_GLIDING).shiftVariationOf(CAESAR_BUBBLE_CUTTER)));
    

    public static final RegistryObject<VampirismAction> VAMPIRISM_BLOOD_DRAIN = ACTIONS.register("vampirism_blood_drain", 
            () -> new VampirismBloodDrain(new NonStandAction.Builder().emptyMainHand().holdType()));
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_FREEZE = ACTIONS.register("vampirism_freeze", 
            () -> new VampirismFreeze(new NonStandAction.Builder().emptyMainHand()
                    .holdType().holdEnergyCost(0.5F).heldSlowDownFactor(0.75F)));
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_SPACE_RIPPER_STINGY_EYES = ACTIONS.register("vampirism_space_ripper_stingy_eyes", 
            () -> new VampirismSpaceRipperStingyEyes(new NonStandAction.Builder().ignoresPerformerStun()
                    .holdType(20).holdEnergyCost(20F).cooldown(0, 50).heldSlowDownFactor(0.3F)));
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_BLOOD_GIFT = ACTIONS.register("vampirism_blood_gift", 
            () -> new VampirismBloodGift(new NonStandAction.Builder().emptyMainHand()
                    .holdToFire(60, false).holdEnergyCost(5F).heldSlowDownFactor(0.3F)));
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_ZOMBIE_SUMMON = ACTIONS.register("vampirism_zombie_summon", 
            () -> new VampirismZombieSummon(new NonStandAction.Builder().energyCost(100F).cooldown(0, 100)));
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_DARK_AURA = ACTIONS.register("vampirism_dark_aura", 
            () -> new VampirismDarkAura(new NonStandAction.Builder().ignoresPerformerStun().energyCost(25F).cooldown(0, 300)));
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_HAMON_SUICIDE = ACTIONS.register("vampirism_hamon_suicide", 
            () -> new VampirismHamonSuicide(new NonStandAction.Builder().ignoresPerformerStun().holdToFire(100, false)));

    
    public static final RegistryObject<StandEntityAction> STAND_ENTITY_UNSUMMON = ACTIONS.register("stand_entity_unsummon", 
            () -> new StandEntityUnsummon());

    public static final RegistryObject<StandEntityAction> STAND_ENTITY_BLOCK = ACTIONS.register("stand_entity_block", 
            () -> new StandEntityBlock() {
                @Override
                public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, ActionTarget target) {
                    return null;
                }
            });
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_PUNCH = ACTIONS.register("star_platinum_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder().standSound(Phase.WINDUP, ModSounds.STAR_PLATINUM_ORA)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_BARRAGE = ACTIONS.register("star_platinum_barrage", 
            () -> new StandEntityMeleeBarrage(new StandEntityMeleeBarrage.Builder().standSound(ModSounds.STAR_PLATINUM_ORA_ORA_ORA)));
    
    public static final RegistryObject<StandEntityComboHeavyAttack> STAR_PLATINUM_UPPERCUT = ACTIONS.register("star_platinum_uppercut", 
    		() -> new StandEntityComboHeavyAttack(new StandEntityComboHeavyAttack.Builder().standSound(Phase.WINDUP, ModSounds.STAR_PLATINUM_ORA_LONG)
    				.targetPunchProperties((punch, stand, punchTarget) -> {
    					return punch.get()
    					.addKnockback(0.5F + stand.getLastHeavyPunchCombo())
    					.knockbackXRot(-60F)
    					.disableBlocking((float) stand.getProximityRatio(punchTarget) - 0.25F);
    				})));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_HEAVY_PUNCH = ACTIONS.register("star_platinum_heavy_punch", 
            () -> new StandEntityHeavyAttack(new StandEntityHeavyAttack.Builder().standSound(Phase.WINDUP, ModSounds.STAR_PLATINUM_ORA_LONG)
                    .shiftVariationOf(STAR_PLATINUM_PUNCH).shiftVariationOf(STAR_PLATINUM_BARRAGE), STAR_PLATINUM_UPPERCUT));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_STAR_FINGER = ACTIONS.register("star_platinum_star_finger", 
            () -> new StarPlatinumStarFinger(new StandEntityAction.Builder().standPerformDuration(20).staminaCost(375).cooldown(20, 60)
                    .ignoresPerformerStun().resolveLevelToUnlock(3).standOffsetFront()
                    .standPose(StandPose.RANGED_ATTACK).shout(ModSounds.JOTARO_STAR_FINGER).standSound(Phase.PERFORM, ModSounds.STAR_PLATINUM_STAR_FINGER)
                    .xpRequirement(300)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_BLOCK = ACTIONS.register("star_platinum_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_ZOOM = ACTIONS.register("star_platinum_zoom", 
            () -> new StarPlatinumZoom(new StandEntityAction.Builder().holdType().ignoresPerformerStun()
                    .standOffsetFromUser(-0.25, -0.25, -0.3)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_INHALE = ACTIONS.register("star_platinum_inhale", 
            () -> new StarPlatinumInhale(new StandEntityAction.Builder().holdType(80).cooldown(0, 200).staminaCostTick(2F)
            		.ignoresPerformerStun().resolveLevelToUnlock(2).standOffsetFromUser(0, -0.25).standSound(ModSounds.STAR_PLATINUM_INHALE)));
    
    public static final RegistryObject<TimeStop> STAR_PLATINUM_TIME_STOP = ACTIONS.register("star_platinum_time_stop", 
            () -> new TimeStop(new StandAction.Builder().holdToFire(40, false)
                    .staminaCost(250).staminaCostTick(7.5F)
                    .resolveLevelToUnlock(4).isTrained().ignoresPerformerStun().autoSummonStand().shout(ModSounds.JOTARO_STAR_PLATINUM_THE_WORLD)
                    .xpRequirement(950))
            .timeStopSound(ModSounds.STAR_PLATINUM_TIME_STOP)
            .addTimeResumeVoiceLine(ModSounds.JOTARO_TIME_RESUMES).timeResumeSound(ModSounds.STAR_PLATINUM_TIME_RESUME));
    
    public static final RegistryObject<TimeResume> TIME_RESUME = ACTIONS.register("time_resume", 
            () -> new TimeResume(new StandAction.Builder()));
    
    public static final RegistryObject<StandAction> STAR_PLATINUM_TIME_STOP_BLINK = ACTIONS.register("star_platinum_ts_blink", 
            () -> new TimeStopInstant(new StandAction.Builder()
                    .resolveLevelToUnlock(4).isTrained().ignoresPerformerStun().shiftVariationOf(STAR_PLATINUM_TIME_STOP)
                    .xpRequirement(950), 
                    STAR_PLATINUM_TIME_STOP, ModSounds.STAR_PLATINUM_TIME_STOP_BLINK));
    

    public static final RegistryObject<StandEntityAction> THE_WORLD_PUNCH = ACTIONS.register("the_world_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder().standSound(Phase.WINDUP, ModSounds.DIO_MUDA)));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_BARRAGE = ACTIONS.register("the_world_barrage", 
            () -> new TheWorldBarrage(new StandEntityMeleeBarrage.Builder()
                    .standSound(ModSounds.THE_WORLD_MUDA_MUDA_MUDA).shout(ModSounds.DIO_MUDA_MUDA), ModSounds.DIO_WRY));

    public static final RegistryObject<StandEntityComboHeavyAttack> THE_WORLD_KICK = ACTIONS.register("the_world_kick", 
    		() -> new StandEntityComboHeavyAttack(new StandEntityComboHeavyAttack.Builder().shout(ModSounds.DIO_DIE)
    				.targetPunchProperties((punch, stand, punchTarget) -> {
    					StandAttackProperties kick = punch.get();
    					return kick
    							.addKnockback(4)
    							.knockbackYRotDeg(60)
    							.disableBlocking((float) stand.getProximityRatio(punchTarget) - 0.25F)
    							.sweepingAttack(0.5, 0, 0.5, kick.getDamage() * 0.5F);
			})));
    
    public static final RegistryObject<StandEntityHeavyAttack> THE_WORLD_HEAVY_PUNCH = ACTIONS.register("the_world_heavy_punch", 
            () -> new StandEntityHeavyAttack(new StandEntityHeavyAttack.Builder().shout(ModSounds.DIO_DIE).targetPunchProperties((punch, stand, punchTarget) -> {
				return punch.get()
						.armorPiercing((float) stand.getAttackDamage() * 0.01F)
						.addKnockback(6);
			}).shiftVariationOf(THE_WORLD_PUNCH).shiftVariationOf(THE_WORLD_BARRAGE), THE_WORLD_KICK));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_BLOCK = ACTIONS.register("the_world_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<TimeStop> THE_WORLD_TIME_STOP = ACTIONS.register("the_world_time_stop", 
            () -> new TheWorldTimeStop(new StandAction.Builder().holdToFire(30, false)
                    .staminaCost(250).staminaCostTick(7.5F)
                    .resolveLevelToUnlock(2).isTrained().ignoresPerformerStun().shout(ModSounds.DIO_THE_WORLD)
                    .xpRequirement(500))
            .voiceLineWithStandSummoned(ModSounds.DIO_TIME_STOP).timeStopSound(ModSounds.THE_WORLD_TIME_STOP)
            .addTimeResumeVoiceLine(ModSounds.DIO_TIME_RESUMES, true).addTimeResumeVoiceLine(ModSounds.DIO_TIMES_UP, false)
            .timeResumeSound(ModSounds.THE_WORLD_TIME_RESUME));
    
    public static final RegistryObject<TimeStopInstant> THE_WORLD_TIME_STOP_BLINK = ACTIONS.register("the_world_ts_blink", 
            () -> new TheWorldTimeStopInstant(new StandAction.Builder()
                    .resolveLevelToUnlock(2).isTrained().ignoresPerformerStun()
                    .shiftVariationOf(THE_WORLD_TIME_STOP)
                    .xpRequirement(500), 
                    THE_WORLD_TIME_STOP, ModSounds.THE_WORLD_TIME_STOP_BLINK));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_TS_PUNCH = ACTIONS.register("the_world_ts_punch", 
            () -> new TheWorldTSHeavyAttack(new StandEntityAction.Builder().resolveLevelToUnlock(3).standUserSlowDownFactor(1.0F)
                    .standKeepsTarget().standPose(TheWorldTSHeavyAttack.TS_PUNCH_POSE).standWindupDuration(5).cooldown(0, 50)
                    .targetPunchProperties((punch, stand, punchTarget) -> {
                    	return punch.get()
                    			.damage(StandStatFormulas.getHeavyAttackDamage(stand.getAttackDamage()))
                    			.addKnockback(4)
                    			.disableBlocking(1.0F)
                                .setStandInvulTime(10)
                                .setPunchSound(ModSounds.STAND_STRONG_ATTACK.get());
                    }), THE_WORLD_HEAVY_PUNCH, THE_WORLD_TIME_STOP_BLINK));
    

    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_STRING_ATTACK = ACTIONS.register("hierophant_green_attack", 
            () -> new HierophantGreenStringAttack(new StandEntityAction.Builder().staminaCost(75).standPerformDuration(10)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_STRING_BIND = ACTIONS.register("hierophant_green_attack_binding", 
            () -> new HierophantGreenStringAttack(new StandEntityAction.Builder().staminaCost(75).standPerformDuration(25).cooldown(25, 100, 0.5F)
                    .shiftVariationOf(HIEROPHANT_GREEN_STRING_ATTACK)
                    .xpRequirement(200)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_EMERALD_SPLASH = ACTIONS.register("hierophant_green_emerald_splash", 
            () -> new HierophantGreenEmeraldSplash(new StandEntityAction.Builder()
            		.standPerformDuration(30).standRecoveryTicks(20).staminaCostTick(3)
                    .resolveLevelToUnlock(1).isTrained().standOffsetFront()
                    .standPose(StandPose.RANGED_ATTACK).shout(ModSounds.KAKYOIN_EMERALD_SPLASH).standSound(ModSounds.HIEROPHANT_GREEN_EMERALD_SPLASH)
                    .xpRequirement(50)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED = ACTIONS.register("hierophant_green_es_concentrated", 
            () -> new HierophantGreenEmeraldSplash(new StandEntityAction.Builder()
                    .standPerformDuration(5).standRecoveryTicks(20).cooldown(5, 60).staminaCostTick(6)
                    .resolveLevelToUnlock(-1).standOffsetFront()
                    .standPose(StandPose.RANGED_ATTACK).shout(ModSounds.KAKYOIN_EMERALD_SPLASH).standSound(ModSounds.HIEROPHANT_GREEN_EMERALD_SPLASH)
                    .shiftVariationOf(HIEROPHANT_GREEN_EMERALD_SPLASH)
                    .xpRequirement(400)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_BLOCK = ACTIONS.register("hierophant_green_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_GRAPPLE = ACTIONS.register("hierophant_green_grapple", 
            () -> new HierophantGreenGrapple(new StandEntityAction.Builder().staminaCostTick(0).resolveLevelToUnlock(2)
                    .holdType().standUserSlowDownFactor(1.0F).standPose(HierophantGreenGrapple.GRAPPLE_POSE).standOffsetFromUser(-0.5, 0.25)
                    .xpRequirement(100)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_GRAPPLE_ENTITY = ACTIONS.register("hierophant_green_grapple_entity", 
            () -> new HierophantGreenGrapple(new StandEntityAction.Builder().staminaCostTick(0).resolveLevelToUnlock(2)
                    .holdType().standUserSlowDownFactor(1.0F).standPose(HierophantGreenGrapple.GRAPPLE_POSE)
                    .shiftVariationOf(HIEROPHANT_GREEN_GRAPPLE)
                    .xpRequirement(100)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_BARRIER = ACTIONS.register("hierophant_green_barrier", 
            () -> new HierophantGreenBarrier(new StandEntityAction.Builder().resolveLevelToUnlock(3)
                    .xpRequirement(700)));

    
    public static final RegistryObject<StandEntityLightAttack> SILVER_CHARIOT_NO_RAPIER_ATTACK = ACTIONS.register("silver_chariot_no_rapier_attack", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_ATTACK = ACTIONS.register("silver_chariot_attack", 
            () -> new SilverChariotLightAttack(new StandEntityLightAttack.Builder(), SILVER_CHARIOT_NO_RAPIER_ATTACK));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_BARRAGE = ACTIONS.register("silver_chariot_barrage", 
            () -> new SilverChariotMeleeBarrage(new StandEntityMeleeBarrage.Builder().shout(ModSounds.POLNAREFF_HORA_HORA_HORA).targetPunchProperties((punch, stand, punchTarget) -> {
            	StandAttackProperties stabBarrage = punch.get();
            	if (punchTarget instanceof SkeletonEntity) {
            		stabBarrage.damage(stabBarrage.getDamage() * 0.75F);
            	}
				return stabBarrage;
			})));
    
    public static final RegistryObject<StandEntityComboHeavyAttack> SILVER_CHARIOT_SWEEPING_ATTACK = ACTIONS.register("silver_chariot_sweeping_attack", 
            () -> new SilverChariotSweepingAttack(new StandEntityComboHeavyAttack.Builder().standPerformDuration(3).targetPunchProperties((punch, stand, punchTarget) -> {
            	return punch.get()
            			.setPunchSound(null)
            			.addKnockback(1);
			})));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_DASH_ATTACK = ACTIONS.register("silver_chariot_dash_attack", 
    		() -> new SilverChariotDashAttack(new StandEntityHeavyAttack.Builder()
    				.shiftVariationOf(SILVER_CHARIOT_ATTACK).shiftVariationOf(SILVER_CHARIOT_BARRAGE).targetPunchProperties((punch, stand, punchTarget) -> {
    					StandAttackProperties stab = punch.get();
    					stab.setPunchSound(null);
    					if (stand.getAttackSpeed() < 24) {
    						boolean left = MathHelper.wrapDegrees(
    								MathUtil.yRotDegFromVec(stand.getLookAngle())
    								- MathUtil.yRotDegFromVec(punchTarget.position().subtract(stand.position())))
    								< 0;
    						return stab
    								.addKnockback(1.5F)
    								.knockbackYRotDeg((60F + stand.getRandom().nextFloat() * 30F) * (left ? 1 : -1));
    					}
    					else {
    						return stab
    								.addKnockback(0.25F)
    								.knockbackXRot(-90F)
    								.setPunchSound(null);
    					}
    				}), SILVER_CHARIOT_SWEEPING_ATTACK));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_RAPIER_LAUNCH = ACTIONS.register("silver_chariot_rapier_launch", 
            () -> new SilverChariotRapierLaunch(new StandEntityAction.Builder().cooldown(0, 100)
                    .ignoresPerformerStun().resolveLevelToUnlock(2).standOffsetFromUser(0, 0.25)
                    .standPose(StandPose.RANGED_ATTACK).standSound(ModSounds.SILVER_CHARIOT_RAPIER_SHOT)
                    .xpRequirement(200)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_BLOCK = ACTIONS.register("silver_chariot_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_TAKE_OFF_ARMOR = ACTIONS.register("silver_chariot_take_off_armor", 
            () -> new SilverChariotTakeOffArmor(new StandEntityAction.Builder().resolveLevelToUnlock(3).standSound(ModSounds.SILVER_CHARIOT_ARMOR_OFF)
                    .xpRequirement(600)));
    

    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_PUNCH = ACTIONS.register("magicians_red_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()));

    public static final RegistryObject<StandEntityComboHeavyAttack> MAGICIANS_RED_KICK = ACTIONS.register("magicians_red_kick", 
            () -> new MagiciansRedKick(new StandEntityComboHeavyAttack.Builder().targetPunchProperties((punch, stand, punchTarget) -> {
				return punch.get()
						.addKnockback(4);
			})));

    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_HEAVY_PUNCH = ACTIONS.register("magicians_red_heavy_punch", 
            () -> new StandEntityHeavyAttack(new StandEntityHeavyAttack.Builder().shiftVariationOf(MAGICIANS_RED_PUNCH), MAGICIANS_RED_KICK));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_FLAME_BURST = ACTIONS.register("magicians_red_flame_burst", 
            () -> new MagiciansRedFlameBurst(new StandEntityAction.Builder().staminaCostTick(3).holdType()
                    .standOffsetFront().standPose(StandPose.RANGED_ATTACK)
                    .xpRequirement(50)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_FIREBALL = ACTIONS.register("magicians_red_fireball", 
            () -> new MagiciansRedFireball(new StandEntityAction.Builder().staminaCost(75).resolveLevelToUnlock(2).standPerformDuration(3)
                    .standPose(StandPose.RANGED_ATTACK).standSound(ModSounds.MAGICIANS_RED_FIREBALL)
                    .xpRequirement(150)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_CROSSFIRE_HURRICANE = ACTIONS.register("magicians_red_crossfire_hurricane", 
            () -> new MagiciansRedCrossfireHurricane(new StandEntityAction.Builder().staminaCost(500)
                    .resolveLevelToUnlock(4).isTrained().holdToFire(30, false).standPose(StandPose.RANGED_ATTACK)
                    .shout(ModSounds.AVDOL_CROSSFIRE_HURRICANE).standSound(ModSounds.MAGICIANS_RED_FIRE_BLAST)
                    .xpRequirement(700)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_CROSSFIRE_HURRICANE_SPECIAL = ACTIONS.register("magicians_red_ch_special", 
            () -> new MagiciansRedCrossfireHurricane(new StandEntityAction.Builder().staminaCost(600)
                    .resolveLevelToUnlock(-1).holdToFire(40, false).standPose(StandPose.RANGED_ATTACK)
                    .shout(ModSounds.AVDOL_CROSSFIRE_HURRICANE_SPECIAL).standSound(ModSounds.MAGICIANS_RED_FIRE_BLAST)
                    .shiftVariationOf(MAGICIANS_RED_CROSSFIRE_HURRICANE)
                    .xpRequirement(1000)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_BLOCK = ACTIONS.register("magicians_red_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_RED_BIND = ACTIONS.register("magicians_red_red_bind", 
            () -> new MagiciansRedRedBind(new StandEntityAction.Builder().staminaCostTick(1).resolveLevelToUnlock(1).holdType().heldSlowDownFactor(0.3F)
                    .standPose(MagiciansRedRedBind.RED_BIND_POSE).shout(ModSounds.AVDOL_RED_BIND).standSound(ModSounds.MAGICIANS_RED_FIRE_BLAST)
                    .xpRequirement(450)));
    
    public static final RegistryObject<StandAction> MAGICIANS_RED_DETECTOR = ACTIONS.register("magicians_red_detector", 
            () -> new MagiciansRedDetector(new StandAction.Builder().resolveLevelToUnlock(3).autoSummonStand()
                    .xpRequirement(500)));
    


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeActionsInit(RegistryEvent.Register<Action<?>> event) {
        Action.initShiftVariationsMap();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void afterActionsInit(RegistryEvent.Register<Action<?>> event) {
        Action.initShiftVariations();
    }
    
    
    
    public static class Registry {
        private static Supplier<IForgeRegistry<Action<?>>> REGISTRY_SUPPLIER = null;
        
        public static void initRegistry() {
            if (REGISTRY_SUPPLIER == null) {
                REGISTRY_SUPPLIER = ModActions.ACTIONS.makeRegistry("action", () -> new RegistryBuilder<>());
            }
        }
        
        public static IForgeRegistry<Action<?>> getRegistry() {
            return REGISTRY_SUPPLIER.get();
        }
    }

}
