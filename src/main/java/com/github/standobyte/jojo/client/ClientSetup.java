package com.github.standobyte.jojo.client;

import java.util.Map;
import java.util.function.UnaryOperator;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.particle.AirStreamParticle;
import com.github.standobyte.jojo.client.particle.BloodParticle;
import com.github.standobyte.jojo.client.particle.CDRestorationParticle;
import com.github.standobyte.jojo.client.particle.MeteoriteVirusParticle;
import com.github.standobyte.jojo.client.particle.OneTickFlameParticle;
import com.github.standobyte.jojo.client.particle.OnomatopoeiaParticle;
import com.github.standobyte.jojo.client.render.armor.ArmorModelRegistry;
import com.github.standobyte.jojo.client.render.armor.model.BladeHatArmorModel;
import com.github.standobyte.jojo.client.render.armor.model.BreathControlMaskModel;
import com.github.standobyte.jojo.client.render.armor.model.SatiporojaScarfArmorModel;
import com.github.standobyte.jojo.client.render.armor.model.StoneMaskModel;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.KnifeLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.TornadoOverdriveEffectLayer;
import com.github.standobyte.jojo.client.render.entity.renderer.AfterimageRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.CrimsonBubbleRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.HamonBlockChargeRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.HamonProjectileShieldRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.LeavesGliderRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.MRDetectorRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.PillarmanTempleEngravingRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.RoadRollerRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.SoulRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.MRFlameRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.SCFlameRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam.LightBeamRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam.SpaceRipperStingyEyesRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.HGBarrierRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.HGGrapplingStringRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.HGStringRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.MRRedBindRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SPStarFingerRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SatiporojaScarfBindingRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SatiporojaScarfRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending.SnakeMufflerRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBlockBulletRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBloodCutterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HGEmeraldRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonBubbleBarrierRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonBubbleCutterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonBubbleRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.HamonCutterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MRCrossfireHurricaneRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MRCrossfireHurricaneSpecialRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.MRFireballRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.SCRapierRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.TommyGunBulletRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.stretching.ZoomPunchRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.BladeHatRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.ClackersRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.KnifeRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile.StandArrowRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.HamonMasterRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.HungryZombieRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.mob.RockPaperScissorsKidRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.CrazyDiamondRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.HierophantGreenRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.MagiciansRedRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.SilverChariotRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StarPlatinumRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.TheWorldRenderer;
import com.github.standobyte.jojo.client.render.item.RoadRollerBakedModel;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.hud.marker.CrazyDiamondAnchorMarker;
import com.github.standobyte.jojo.client.ui.hud.marker.CrazyDiamondBloodHomingMarker;
import com.github.standobyte.jojo.client.ui.hud.marker.HierophantGreenBarrierDetectionMarker;
import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.stand.ModStandActions;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.item.ClackersItem;
import com.github.standobyte.jojo.item.StandArrowItem;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.item.StoneMaskItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.CloudParticle;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.FireworkRocketRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.Items;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static final IItemPropertyGetter STAND_ITEM_INVISIBLE = (itemStack, clientWorld, livingEntity) -> {
        return !ClientUtil.canSeeStands() ? 1 : 0;
    };
    
    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.BLADE_HAT.get(), BladeHatRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SPACE_RIPPER_STINGY_EYES.get(), SpaceRipperStingyEyesRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.ZOOM_PUNCH.get(), ZoomPunchRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.AFTERIMAGE.get(), AfterimageRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_PROJECTILE_SHIELD.get(), HamonProjectileShieldRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.LEAVES_GLIDER.get(), LeavesGliderRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_BLOCK_CHARGE.get(), HamonBlockChargeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.AJA_STONE_BEAM.get(), LightBeamRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_CUTTER.get(), HamonCutterRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.CLACKERS.get(), ClackersRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_BUBBLE.get(), HamonBubbleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_BUBBLE_BARRIER.get(), HamonBubbleBarrierRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_BUBBLE_CUTTER.get(), HamonBubbleCutterRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.CRIMSON_BUBBLE.get(), CrimsonBubbleRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SATIPOROJA_SCARF.get(), SatiporojaScarfRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SATIPOROJA_SCARF_BINDING.get(), SatiporojaScarfBindingRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SNAKE_MUFFLER.get(), SnakeMufflerRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.TOMMY_GUN_BULLET.get(), TommyGunBulletRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.KNIFE.get(), KnifeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.STAND_ARROW.get(), StandArrowRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SOUL.get(), SoulRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PILLARMAN_TEMPLE_ENGRAVING.get(), PillarmanTempleEngravingRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SP_STAR_FINGER.get(), SPStarFingerRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HG_STRING.get(), HGStringRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HG_EMERALD.get(), HGEmeraldRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HG_GRAPPLING_STRING.get(), HGGrapplingStringRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HG_BARRIER.get(), HGBarrierRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SC_RAPIER.get(), SCRapierRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SC_FLAME.get(), SCFlameRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.ROAD_ROLLER.get(), RoadRollerRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MR_FLAME.get(), MRFlameRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MR_FIREBALL.get(), MRFireballRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MR_CROSSFIRE_HURRICANE.get(), MRCrossfireHurricaneRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MR_CROSSFIRE_HURRICANE_SPECIAL.get(), MRCrossfireHurricaneSpecialRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MR_RED_BIND.get(), MRRedBindRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MR_DETECTOR.get(), MRDetectorRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.CD_BLOOD_CUTTER.get(), CDBloodCutterRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.CD_BLOCK_BULLET.get(), CDBlockBulletRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.EYE_OF_ENDER_INSIDE.get(), manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 1.0F, true));
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.FIREWORK_INSIDE.get(), manager -> new FireworkRocketRenderer(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HUNGRY_ZOMBIE.get(), HungryZombieRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.HAMON_MASTER.get(), HamonMasterRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), RockPaperScissorsKidRenderer::new);
        
        RenderingRegistry.registerEntityRenderingHandler(ModStands.STAR_PLATINUM.getEntityType(), StarPlatinumRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModStands.THE_WORLD.getEntityType(), TheWorldRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModStands.HIEROPHANT_GREEN.getEntityType(), HierophantGreenRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModStands.SILVER_CHARIOT.getEntityType(), SilverChariotRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModStands.MAGICIANS_RED.getEntityType(), MagiciansRedRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModStands.CRAZY_DIAMOND.getEntityType(), CrazyDiamondRenderer::new);
        
        ArmorModelRegistry.registerArmorModel(StoneMaskModel::new, ModItems.STONE_MASK.get());
        ArmorModelRegistry.registerArmorModel(BladeHatArmorModel::new, ModItems.BLADE_HAT.get());
        ArmorModelRegistry.registerArmorModel(BreathControlMaskModel::new, ModItems.BREATH_CONTROL_MASK.get());
        ArmorModelRegistry.registerArmorModel(SatiporojaScarfArmorModel::new, ModItems.SATIPOROJA_SCARF.get());

        event.enqueueWork(() -> {
            Minecraft mc = event.getMinecraftSupplier().get();

            ItemModelsProperties.register(ModItems.METEORIC_SCRAP.get(), new ResourceLocation(JojoMod.MOD_ID, "icon"), (itemStack, clientWorld, livingEntity) -> {
                return itemStack.getOrCreateTag().getInt("Icon");
            });
            ItemModelsProperties.register(ModItems.KNIFE.get(), new ResourceLocation(JojoMod.MOD_ID, "count"), (itemStack, clientWorld, livingEntity) -> {
                return livingEntity != null ? itemStack.getCount() : 1;
            });
            ItemModelsProperties.register(ModItems.STONE_MASK.get(), new ResourceLocation(JojoMod.MOD_ID, "stone_mask_activated"), (itemStack, clientWorld, livingEntity) -> {
                return itemStack.getTag().getByte(StoneMaskItem.NBT_ACTIVATION_KEY) > 0 ? 1 : 0;
            });
            ItemModelsProperties.register(ModItems.CLACKERS.get(), new ResourceLocation(JojoMod.MOD_ID, "clackers_spin"), (itemStack, clientWorld, livingEntity) -> {
                if (livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack) {
                    int ticksUsed = itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks();
                    return ClackersItem.clackersTexVariant(ticksUsed, ClackersItem.TICKS_MAX_POWER);
                }
                return 0;
            });
            ItemModelsProperties.register(ModItems.TOMMY_GUN.get(), new ResourceLocation(JojoMod.MOD_ID, "swing"), (itemStack, clientWorld, livingEntity) -> {
                return livingEntity != null && livingEntity.swinging && livingEntity.getItemInHand(livingEntity.swingingArm) == itemStack ? 1 : 0;
            });
            ItemModelsProperties.register(Items.BOW, new ResourceLocation(JojoMod.MOD_ID, "stand_arrow"), (itemStack, clientWorld, livingEntity) -> {
                return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack
                        && livingEntity.getProjectile(itemStack).getItem() instanceof StandArrowItem ? 1 : 0;
            });
            ItemModelsProperties.register(Items.CROSSBOW, new ResourceLocation(JojoMod.MOD_ID, "stand_arrow"), (itemStack, clientWorld, livingEntity) -> {
                return livingEntity != null && CrossbowItem.isCharged(itemStack) && (
                        CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW.get()) || 
                        CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW_BEETLE.get())) ? 1 : 0;
            });
            ItemModelsProperties.register(ModItems.STAND_DISC.get(), new ResourceLocation(JojoMod.MOD_ID, "stand_id"), (itemStack, clientWorld, livingEntity) -> {
                return StandDiscItem.validStandDisc(itemStack, true) ? ModStandActions.STANDS.getNumericId(StandDiscItem.getStandFromStack(itemStack, true).getType().getRegistryName()) : -1;
            });
//            ItemModelsProperties.register(ModItems.EMPEROR.get(), new ResourceLocation(JojoMod.MOD_ID, "stand_invisible"), STAND_ITEM_INVISIBLE);

            RenderTypeLookup.setRenderLayer(ModBlocks.STONE_MASK.get(), RenderType.cutoutMipped());
            RenderTypeLookup.setRenderLayer(ModBlocks.SLUMBERING_PILLARMAN.get(), RenderType.cutoutMipped());

            ClientEventHandler.init(mc);
            ActionsOverlayGui.init(mc);
            StandController.init(mc);
            SoulController.init(mc);
            InputHandler.init(mc);
            InputHandler.getInstance().setActionsOverlay(ActionsOverlayGui.getInstance());
            
            Map<String, PlayerRenderer> skinMap = mc.getEntityRenderDispatcher().getSkinMap();
            addLayers(skinMap.get("default"));
            addLayers(skinMap.get("slim"));

            MarkerRenderer.Handler.addRenderer(new HierophantGreenBarrierDetectionMarker(mc));
            MarkerRenderer.Handler.addRenderer(new CrazyDiamondAnchorMarker(mc));
            MarkerRenderer.Handler.addRenderer(new CrazyDiamondBloodHomingMarker(mc));
        });
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void loadCustomArmorModels(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ArmorModelRegistry.loadArmorModels();
        });
    }

    private static void addLayers(PlayerRenderer renderer) {
        renderer.addLayer(new KnifeLayer<>(renderer));
        renderer.addLayer(new TornadoOverdriveEffectLayer<>(renderer));
    }

    @SubscribeEvent
    public static void registerItemColoring(ColorHandlerEvent.Item event) {
        ItemColors itemColors = event.getItemColors();
        itemColors.register((stack, layer) -> layer == 0 ? -1 : 
            ClientUtil.discColor(StandDiscItem.getColor(stack)), ModItems.STAND_DISC.get());
    }
    
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        registerCustomBakedModel(ModItems.ROAD_ROLLER.get().getRegistryName(), event.getModelRegistry(), 
                model -> new RoadRollerBakedModel(model));
    }
    
    private static void registerCustomBakedModel(ResourceLocation resLoc, 
            Map<ResourceLocation, IBakedModel> modelRegistry, UnaryOperator<IBakedModel> newModel) {
        ModelResourceLocation modelResLoc = new ModelResourceLocation(resLoc, "inventory");
        IBakedModel existingModel = modelRegistry.get(modelResLoc);
        if (existingModel == null) {
            throw new RuntimeException("Did not find original model in registry");
        }
        else if (existingModel.isCustomRenderer()) {
            throw new RuntimeException("Tried to replace model twice");
        }
        else {
            modelRegistry.put(modelResLoc, newModel.apply(existingModel));
        }
    }

    @SubscribeEvent
    public static void onMcConstructor(ParticleFactoryRegisterEvent event) {
        Minecraft mc = Minecraft.getInstance();
        mc.particleEngine.register(ModParticles.BLOOD.get(), BloodParticle.Factory::new);
        mc.particleEngine.register(ModParticles.HAMON_SPARK.get(), CritParticle.Factory::new);
        mc.particleEngine.register(ModParticles.METEORITE_VIRUS.get(), MeteoriteVirusParticle.Factory::new);
        mc.particleEngine.register(ModParticles.MENACING.get(), OnomatopoeiaParticle.GoFactory::new);
        mc.particleEngine.register(ModParticles.RESOLVE.get(), OnomatopoeiaParticle.DoFactory::new);
        mc.particleEngine.register(ModParticles.SOUL_CLOUD.get(), SoulCloudParticleFactory::new);
        mc.particleEngine.register(ModParticles.AIR_STREAM.get(), AirStreamParticle.Factory::new);
        mc.particleEngine.register(ModParticles.FLAME_ONE_TICK.get(), OneTickFlameParticle.Factory::new);
        mc.particleEngine.register(ModParticles.CD_RESTORATION.get(), CDRestorationParticle.Factory::new);
        // yep...
        CustomResources.initCustomResourceManagers(mc);
    }

    private static class SoulCloudParticleFactory extends CloudParticle.Factory {

        public SoulCloudParticleFactory(IAnimatedSprite sprite) {
            super(sprite);
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
           Particle particle = super.createParticle(type, world, x, y, z, xSpeed, ySpeed, zSpeed);
           particle.setColor(1.0F, 1.0F, 0.25F);
           return particle;
        }
    }
}
