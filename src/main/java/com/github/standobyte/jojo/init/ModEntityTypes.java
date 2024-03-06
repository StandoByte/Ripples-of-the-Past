package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;
import com.github.standobyte.jojo.entity.EyeOfEnderInsideEntity;
import com.github.standobyte.jojo.entity.FireworkInsideEntity;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;
import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.entity.PillarmanTempleEngravingEntity;
import com.github.standobyte.jojo.entity.RoadRollerEntity;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.damaging.LightBeamEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBlockBulletEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleBarrierEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonCutterEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonTurquoiseBlueOverdriveEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFireballEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFlameEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.SCFlameSwingEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGBarrierEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGGrapplingStringEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfBindingEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SnakeMufflerEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;
import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.entity.itemprojectile.StandArrowEntity;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.entity.mob.StandUserDummyEntity;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsKidEntity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, JojoMod.MOD_ID);
    
//  public static final RegistryObject<EntityType<___Entity>> ___ = ENTITIES.register("___", 
//          () -> EntityType.Builder.<___Entity>of(___Entity::new, EntityClassification.MISC).sized(, )//.noSummon().noSave()
//          .build(new ResourceLocation(JojoMod.MOD_ID, "___").toString()));

    public static final RegistryObject<EntityType<HungryZombieEntity>> HUNGRY_ZOMBIE = ENTITIES.register("hungry_zombie", 
            () -> EntityType.Builder.<HungryZombieEntity>of(HungryZombieEntity::new, EntityClassification.MONSTER).sized(0.6F, 1.95F)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hungry_zombie").toString()));
    
    public static final RegistryObject<EntityType<HamonMasterEntity>> HAMON_MASTER = ENTITIES.register("hamon_master", 
            () -> EntityType.Builder.<HamonMasterEntity>of(HamonMasterEntity::new, EntityClassification.MISC).sized(0.6F, /*1.35F*/1.95F)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_teacher").toString()));
    
    public static final RegistryObject<EntityType<RockPaperScissorsKidEntity>> ROCK_PAPER_SCISSORS_KID = ENTITIES.register("rps_kid", 
            () -> EntityType.Builder.<RockPaperScissorsKidEntity>of(RockPaperScissorsKidEntity::new, EntityClassification.MISC).sized(0.6F, 1.95F)
            .build(new ResourceLocation(JojoMod.MOD_ID, "rps_kid").toString()));

    public static final RegistryObject<EntityType<BladeHatEntity>> BLADE_HAT = ENTITIES.register("blade_hat", 
            () -> EntityType.Builder.<BladeHatEntity>of(BladeHatEntity::new, EntityClassification.MISC).sized(0.6F, 0.375F).setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "blade_hat").toString()));
    
    public static final RegistryObject<EntityType<SpaceRipperStingyEyesEntity>> SPACE_RIPPER_STINGY_EYES = ENTITIES.register("space_ripper_stingy_eyes", 
            () -> EntityType.Builder.<SpaceRipperStingyEyesEntity>of(SpaceRipperStingyEyesEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "space_ripper_stingy_eyes").toString()));
    
    public static final RegistryObject<EntityType<HamonTurquoiseBlueOverdriveEntity>> TURQUOISE_BLUE_OVERDRIVE = ENTITIES.register("turquoise_blue_overdrive", 
            () -> EntityType.Builder.<HamonTurquoiseBlueOverdriveEntity>of(HamonTurquoiseBlueOverdriveEntity::new, EntityClassification.MISC).sized(4F, 4F).noSummon().setUpdateInterval(20).fireImmune()
            .build(new ResourceLocation(JojoMod.MOD_ID, "turquoise_blue_overdrive").toString()));
    
    public static final RegistryObject<EntityType<HamonSendoOverdriveEntity>> SENDO_HAMON_OVERDRIVE = ENTITIES.register("sendo_hamon_overdrive", 
            () -> EntityType.Builder.<HamonSendoOverdriveEntity>of(HamonSendoOverdriveEntity::new, EntityClassification.MISC).sized(4, 4).setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false).fireImmune()
            .build(new ResourceLocation(JojoMod.MOD_ID, "sendo_hamon_overdrive").toString()));
    
    public static final RegistryObject<EntityType<ZoomPunchEntity>> ZOOM_PUNCH = ENTITIES.register("zoom_punch", 
            () -> EntityType.Builder.<ZoomPunchEntity>of(ZoomPunchEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "zoom_punch").toString()));
    
    public static final RegistryObject<EntityType<AfterimageEntity>> AFTERIMAGE = ENTITIES.register("afterimage", 
            () -> EntityType.Builder.<AfterimageEntity>of(AfterimageEntity::new, EntityClassification.MISC).sized(0.6F, 1.8F).noSummon().setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false)
            .build(new ResourceLocation(JojoMod.MOD_ID, "afterimage").toString()));
    
    public static final RegistryObject<EntityType<HamonProjectileShieldEntity>> HAMON_PROJECTILE_SHIELD = ENTITIES.register("hamon_projectile_shield", 
            () -> EntityType.Builder.<HamonProjectileShieldEntity>of(HamonProjectileShieldEntity::new, EntityClassification.MISC).sized(3.0F, 3.0F).setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false).noSummon().noSave().fireImmune()
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_projectile_shield").toString()));
    
    public static final RegistryObject<EntityType<LeavesGliderEntity>> LEAVES_GLIDER = ENTITIES.register("leaves_glider", 
            () -> EntityType.Builder.<LeavesGliderEntity>of(LeavesGliderEntity::new, EntityClassification.MISC).sized(2.5F, 0.125F)
            .build(new ResourceLocation(JojoMod.MOD_ID, "leaves_glider").toString()));
    
    public static final RegistryObject<EntityType<HamonBlockChargeEntity>> HAMON_BLOCK_CHARGE = ENTITIES.register("hamon_block_charge", 
            () -> EntityType.Builder.<HamonBlockChargeEntity>of(HamonBlockChargeEntity::new, EntityClassification.MISC).sized(1.0F, 1.0F).setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false).fireImmune()
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_block_charge").toString()));
    
    public static final RegistryObject<EntityType<LightBeamEntity>> AJA_STONE_BEAM = ENTITIES.register("aja_stone_beam", 
            () -> EntityType.Builder.<LightBeamEntity>of(LightBeamEntity::new, EntityClassification.MISC).sized(0.125F, 0.125F).noSummon().setUpdateInterval(1).fireImmune()
            .build(new ResourceLocation(JojoMod.MOD_ID, "aja_stone_beam").toString()));
    
    public static final RegistryObject<EntityType<HamonCutterEntity>> HAMON_CUTTER = ENTITIES.register("hamon_cutter", 
            () -> EntityType.Builder.<HamonCutterEntity>of(HamonCutterEntity::new, EntityClassification.MISC).sized(0.5F, 0.125F).setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_cutter").toString()));
    
    public static final RegistryObject<EntityType<ClackersEntity>> CLACKERS = ENTITIES.register("clackers", 
            () -> EntityType.Builder.<ClackersEntity>of(ClackersEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "clackers").toString()));
    
    public static final RegistryObject<EntityType<HamonBubbleEntity>> HAMON_BUBBLE = ENTITIES.register("hamon_bubble", 
            () -> EntityType.Builder.<HamonBubbleEntity>of(HamonBubbleEntity::new, EntityClassification.MISC).sized(0.15F, 0.15F).setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_bubble").toString()));
    
    public static final RegistryObject<EntityType<HamonBubbleBarrierEntity>> HAMON_BUBBLE_BARRIER = ENTITIES.register("hamon_bubble_barrier", 
            () -> EntityType.Builder.<HamonBubbleBarrierEntity>of(HamonBubbleBarrierEntity::new, EntityClassification.MISC).sized(2.0F, 2.0F)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_bubble_barrier").toString()));
    
    public static final RegistryObject<EntityType<HamonBubbleCutterEntity>> HAMON_BUBBLE_CUTTER = ENTITIES.register("hamon_bubble_cutter", 
            () -> EntityType.Builder.<HamonBubbleCutterEntity>of(HamonBubbleCutterEntity::new, EntityClassification.MISC).sized(0.325F, 0.0875F).setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hamon_bubble_cutter").toString()));
    
    public static final RegistryObject<EntityType<CrimsonBubbleEntity>> CRIMSON_BUBBLE = ENTITIES.register("crimson_bubble", 
            () -> EntityType.Builder.<CrimsonBubbleEntity>of(CrimsonBubbleEntity::new, EntityClassification.MISC).sized(0.625F, 0.625F).noSummon().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "crimson_bubble").toString()));
    
    public static final RegistryObject<EntityType<SatiporojaScarfEntity>> SATIPOROJA_SCARF = ENTITIES.register("satiporoja_scarf", 
            () -> EntityType.Builder.<SatiporojaScarfEntity>of(SatiporojaScarfEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon()
            .build(new ResourceLocation(JojoMod.MOD_ID, "satiporoja_scarf").toString()));
    
    public static final RegistryObject<EntityType<SatiporojaScarfBindingEntity>> SATIPOROJA_SCARF_BINDING = ENTITIES.register("satiporoja_scarf_binding", 
            () -> EntityType.Builder.<SatiporojaScarfBindingEntity>of(SatiporojaScarfBindingEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon()
            .build(new ResourceLocation(JojoMod.MOD_ID, "satiporoja_scarf_binding").toString()));
    
    public static final RegistryObject<EntityType<SnakeMufflerEntity>> SNAKE_MUFFLER = ENTITIES.register("snake_muffler", 
            () -> EntityType.Builder.<SnakeMufflerEntity>of(SnakeMufflerEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon()
            .build(new ResourceLocation(JojoMod.MOD_ID, "snake_muffler").toString()));
    
    public static final RegistryObject<EntityType<TommyGunBulletEntity>> TOMMY_GUN_BULLET = ENTITIES.register("tommy_gun_bullet", 
            () -> EntityType.Builder.<TommyGunBulletEntity>of(TommyGunBulletEntity::new, EntityClassification.MISC).sized(0.0625F, 0.0625F).clientTrackingRange(4).setUpdateInterval(20).fireImmune()
            .build(new ResourceLocation(JojoMod.MOD_ID, "tommy_gun_bullet").toString()));
    
    public static final RegistryObject<EntityType<KnifeEntity>> KNIFE = ENTITIES.register("knife", 
            () -> EntityType.Builder.<KnifeEntity>of(KnifeEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "knife").toString()));
    
    public static final RegistryObject<EntityType<StandArrowEntity>> STAND_ARROW = ENTITIES.register("stand_arrow", 
            () -> EntityType.Builder.<StandArrowEntity>of(StandArrowEntity::new, EntityClassification.MISC).sized(0.75F, 0.75F).clientTrackingRange(4).updateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "stand_arrow").toString()));
    
    public static final RegistryObject<EntityType<SoulEntity>> SOUL = ENTITIES.register("soul", 
            () -> EntityType.Builder.<SoulEntity>of(SoulEntity::new, EntityClassification.MISC).sized(0.6F, 1.8F).noSummon()
            .build(new ResourceLocation(JojoMod.MOD_ID, "soul").toString()));
    
    public static final RegistryObject<EntityType<StandUserDummyEntity>> STAND_USER_DUMMY = ENTITIES.register("dummy", 
            () -> EntityType.Builder.<StandUserDummyEntity>of(StandUserDummyEntity::new, EntityClassification.MISC).sized(0.6F, 1.95F)
            .build(new ResourceLocation(JojoMod.MOD_ID, "dummy").toString()));

    public static final RegistryObject<EntityType<ObjectEntity>> OBJECT = ENTITIES.register("util_object", 
            () -> EntityType.Builder.<ObjectEntity>of(ObjectEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon()
            .build(new ResourceLocation(JojoMod.MOD_ID, "util_object").toString()));
    
    
    
    public static final RegistryObject<EntityType<PillarmanTempleEngravingEntity>> PILLARMAN_TEMPLE_ENGRAVING = ENTITIES.register("pillarman_temple_engraving", 
            () -> EntityType.Builder.<PillarmanTempleEngravingEntity>of(PillarmanTempleEngravingEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon().setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false)
            .build(new ResourceLocation(JojoMod.MOD_ID, "pillarman_temple_engraving").toString()));
    
    public static final RegistryObject<EntityType<SPStarFingerEntity>> SP_STAR_FINGER = ENTITIES.register("sp_star_finger", 
            () -> EntityType.Builder.<SPStarFingerEntity>of(SPStarFingerEntity::new, EntityClassification.MISC).sized(0.125F, 0.125F).noSummon().noSave().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "sp_star_finger").toString()));
    
    public static final RegistryObject<EntityType<HGStringEntity>> HG_STRING = ENTITIES.register("hg_string", 
            () -> EntityType.Builder.<HGStringEntity>of(HGStringEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().noSave().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hg_string").toString()));
    
    public static final RegistryObject<EntityType<HGEmeraldEntity>> HG_EMERALD = ENTITIES.register("hg_emerald", 
            () -> EntityType.Builder.<HGEmeraldEntity>of(HGEmeraldEntity::new, EntityClassification.MISC).sized(0.5F, 0.25F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hg_emerald").toString()));
    
    public static final RegistryObject<EntityType<HGGrapplingStringEntity>> HG_GRAPPLING_STRING = ENTITIES.register("hg_grappling_string", 
            () -> EntityType.Builder.<HGGrapplingStringEntity>of(HGGrapplingStringEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().noSave().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hg_grappling_string").toString()));
    
    public static final RegistryObject<EntityType<HGBarrierEntity>> HG_BARRIER = ENTITIES.register("hg_barrier", 
            () -> EntityType.Builder.<HGBarrierEntity>of(HGBarrierEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().noSave().setShouldReceiveVelocityUpdates(false).setUpdateInterval(Integer.MAX_VALUE)
            .build(new ResourceLocation(JojoMod.MOD_ID, "hg_barrier").toString()));
    
    public static final RegistryObject<EntityType<SCRapierEntity>> SC_RAPIER = ENTITIES.register("sc_rapier", 
            () -> EntityType.Builder.<SCRapierEntity>of(SCRapierEntity::new, EntityClassification.MISC).sized(0.125F, 0.125F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "sc_rapier").toString()));
    
    public static final RegistryObject<EntityType<SCFlameSwingEntity>> SC_FLAME = ENTITIES.register("sc_flame", 
            () -> EntityType.Builder.<SCFlameSwingEntity>of(SCFlameSwingEntity::new, EntityClassification.MISC).sized(0.0625F, 0.0625F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "sc_flame").toString()));
    
    public static final RegistryObject<EntityType<RoadRollerEntity>> ROAD_ROLLER = ENTITIES.register("road_roller", 
            () -> EntityType.Builder.<RoadRollerEntity>of(RoadRollerEntity::new, EntityClassification.MISC).sized(4.0F, 2.0F).setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "road_roller").toString()));
    
    public static final RegistryObject<EntityType<MRFlameEntity>> MR_FLAME = ENTITIES.register("mr_flame", 
            () -> EntityType.Builder.<MRFlameEntity>of(MRFlameEntity::new, EntityClassification.MISC).sized(0.0625F, 0.0625F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "mr_flame").toString()));
    
    public static final RegistryObject<EntityType<MRFireballEntity>> MR_FIREBALL = ENTITIES.register("mr_fireball", 
            () -> EntityType.Builder.<MRFireballEntity>of(MRFireballEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "mr_fireball").toString()));
    
    public static final RegistryObject<EntityType<MRCrossfireHurricaneEntity>> MR_CROSSFIRE_HURRICANE = ENTITIES.register("mr_crossfire_hurricane", 
            () -> EntityType.Builder.<MRCrossfireHurricaneEntity>of(MRCrossfireHurricaneEntity::new, EntityClassification.MISC).sized(1.25F, 1.8F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "mr_crossfire_hurricane").toString()));
    
    public static final RegistryObject<EntityType<MRCrossfireHurricaneEntity>> MR_CROSSFIRE_HURRICANE_SPECIAL = ENTITIES.register("mr_crossfire_hurricane_special", 
            () -> EntityType.Builder.<MRCrossfireHurricaneEntity>of(MRCrossfireHurricaneEntity::new, EntityClassification.MISC).sized(0.625F, 0.9F).noSummon().noSave().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "mr_crossfire_hurricane_special").toString()));
    
    public static final RegistryObject<EntityType<MRRedBindEntity>> MR_RED_BIND = ENTITIES.register("mr_red_bind", 
            () -> EntityType.Builder.<MRRedBindEntity>of(MRRedBindEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).noSummon().noSave().setUpdateInterval(20)
            .build(new ResourceLocation(JojoMod.MOD_ID, "mr_red_bind").toString()));
    
    public static final RegistryObject<EntityType<MRDetectorEntity>> MR_DETECTOR = ENTITIES.register("mr_detector", 
            () -> EntityType.Builder.<MRDetectorEntity>of(MRDetectorEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon().noSave().setUpdateInterval(Integer.MAX_VALUE).setShouldReceiveVelocityUpdates(false)
            .build(new ResourceLocation(JojoMod.MOD_ID, "mr_detector").toString()));
    
    public static final RegistryObject<EntityType<CDBlockBulletEntity>> CD_BLOCK_BULLET = ENTITIES.register("cd_block_bullet", 
            () -> EntityType.Builder.<CDBlockBulletEntity>of(CDBlockBulletEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "cd_block_bullet").toString()));
    
    public static final RegistryObject<EntityType<CDBloodCutterEntity>> CD_BLOOD_CUTTER = ENTITIES.register("cd_blood_cutter", 
            () -> EntityType.Builder.<CDBloodCutterEntity>of(CDBloodCutterEntity::new, EntityClassification.MISC).sized(0.5F, 0.5F).noSummon().setUpdateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "cd_blood_cutter").toString()));
    
    public static final RegistryObject<EntityType<EyeOfEnderInsideEntity>> EYE_OF_ENDER_INSIDE = ENTITIES.register("eye_of_ender_inside", 
            () -> EntityType.Builder.<EyeOfEnderInsideEntity>of(EyeOfEnderInsideEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(4)
            .build(new ResourceLocation(JojoMod.MOD_ID, "eye_of_ender_inside").toString()));
    
    public static final RegistryObject<EntityType<FireworkInsideEntity>> FIREWORK_INSIDE = ENTITIES.register("firework_inside", 
            () -> EntityType.Builder.<FireworkInsideEntity>of(FireworkInsideEntity::new, EntityClassification.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
            .build(new ResourceLocation(JojoMod.MOD_ID, "firework_inside").toString()));
    
    public static final RegistryObject<EntityType<GETransformationEntity>> GE_LIFEFORM_TRANSFORMATION = ENTITIES.register("ge_lifeform", 
            () -> EntityType.Builder.<GETransformationEntity>of(GETransformationEntity::new, EntityClassification.MISC).sized(1.0F, 1.0F).noSummon()
            .build(new ResourceLocation(JojoMod.MOD_ID, "ge_lifeform").toString()));

    
    
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.HAMON_MASTER.get(), HamonMasterEntity.createAttributes().build());
        event.put(ModEntityTypes.HUNGRY_ZOMBIE.get(), HungryZombieEntity.createAttributes().build());
        event.put(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), VillagerEntity.createAttributes().build());
        event.put(ModEntityTypes.STAND_USER_DUMMY.get(), MobEntity.createMobAttributes().build());
    }
}
