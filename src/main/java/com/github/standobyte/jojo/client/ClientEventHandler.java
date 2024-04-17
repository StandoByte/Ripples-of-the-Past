package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.EXPERIENCE;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.client.render.block.overlay.TranslucentBlockRenderHelper;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.FrozenLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.GlovesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.HamonBurnLayer;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.sound.StandOstSound;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.ClientModSettingsScreen;
import com.github.standobyte.jojo.client.ui.screen.controls.HudLayoutEditingScreen;
import com.github.standobyte.jojo.client.ui.screen.controls.vanilla.CategoryWithButtonsEntry;
import com.github.standobyte.jojo.client.ui.screen.controls.vanilla.ControlSettingToggleButton;
import com.github.standobyte.jojo.client.ui.screen.controls.vanilla.HoldToggleKeyEntry;
import com.github.standobyte.jojo.client.ui.screen.widgets.HeightScaledSlider;
import com.github.standobyte.jojo.client.ui.screen.widgets.ImageVanillaButton;
import com.github.standobyte.jojo.client.ui.standstats.StandStatsRenderer;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.OilItem;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandArrowHandler;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Timer;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class ClientEventHandler {
    private static ClientEventHandler instance = null;

    private final Minecraft mc;
    
    private float pausePartialTick;
    private boolean prevPause = false;

    private Timer clientTimer;
    
    private StandOstSound ost;
    
    private double zoomModifier;
    public boolean isZooming;

    private int deathScreenTick;
    private int standStatsTick;
    
    private ClientEventHandler(Minecraft mc) {
        this.mc = mc;
        this.clientTimer = ClientReflection.getTimer(mc);
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ClientEventHandler(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }

    public static ClientEventHandler getInstance() {
        return instance;
    }
    


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlaySound(PlaySoundEvent event) {
        ISound sound = event.getResultSound();
        if (ClientTimeStopHandler.getInstance().shouldCancelSound(sound)) {
            event.setResultSound(null);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving(RenderLivingEvent.Pre<T, M> event) {
        LivingEntity entity = event.getEntity();
        
        if (entity.hasEffect(ModStatusEffects.FULL_INVISIBILITY.get())) {
            event.setCanceled(true);
            return;
        }
        
        float partialTick = event.getPartialRenderTick();
        float changePartialTick = ClientTimeStopHandler.getInstance().getConstantEntityPartialTick(entity, partialTick);
        if (partialTick != changePartialTick) {
            event.setCanceled(true);
            event.getRenderer().render((T) entity, MathHelper.lerp(changePartialTick, entity.yRotO, entity.yRot), 
                    changePartialTick, event.getMatrixStack(), event.getBuffers(), event.getLight());
            return;
        }
        
        M model = event.getRenderer().getModel();
        if (model instanceof BipedModel) {
            BipedModel<?> bipedModel = (BipedModel<?>) model;
            correctHeldItemPose(entity, bipedModel, HandSide.RIGHT);
            correctHeldItemPose(entity, bipedModel, HandSide.LEFT);
        }
        // FIXME (vampire\curing) shake vampire while curing
        // yRot += (float) (Math.cos((double)entity.tickCount * 3.25) * Math.PI * 0.4);
    }
    
    private void correctHeldItemPose(LivingEntity entity, BipedModel<?> model, HandSide handSide) {
        Hand hand = entity.getMainArm() == handSide ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack item = entity.getItemInHand(hand);
        if (!item.isEmpty() && 
                GlovesLayer.areGloves(item)) {
            switch (handSide) {
            case LEFT:
                model.leftArmPose = BipedModel.ArmPose.EMPTY;
                break;
            case RIGHT:
                model.rightArmPose = BipedModel.ArmPose.EMPTY;
                break;
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving2(RenderLivingEvent.Pre<T, M> event) {
        LivingEntity entity = event.getEntity();

        INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
            if (power.getHeldAction(true) == ModHamonActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                event.getMatrixStack().mulPose(Vector3f.YP.rotation((power.getHeldActionTicks() + event.getPartialRenderTick()) * 2F % 360F));
            }
        });
        
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isUsingZoomPunch()) {
                M model = event.getRenderer().getModel();
                if (model instanceof BipedModel) {
                    ModelRenderer arm = entity.getMainArm() == HandSide.LEFT ? ((BipedModel<?>) model).leftArm : ((BipedModel<?>) model).rightArm;
                    arm.visible = false;
                    if (model instanceof PlayerModel) {
                        arm = entity.getMainArm() == HandSide.LEFT ? ((PlayerModel<?>) model).leftSleeve : ((PlayerModel<?>) model).rightSleeve;
                        arm.visible = false;
                    }
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderNameplate(RenderNameplateEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                if (power.getHeldAction(true) == ModHamonActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                    event.getMatrixStack().mulPose(Vector3f.YP.rotation((power.getHeldActionTicks() + event.getPartialTicks()) * -2F % 360F));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(RenderTickEvent event) {
        if (mc.level != null && event.phase == TickEvent.Phase.START) {
            ClientUtil.canSeeStands = StandUtil.playerCanSeeStands(mc.player);
            ClientUtil.canHearStands = /*StandUtil.playerCanHearStands(mc.player)*/ ClientUtil.canSeeStands;
            
            ClientTimeStopHandler timeStopHandler = ClientTimeStopHandler.getInstance();
            if (mc.player.isAlive()) {
                timeStopHandler.setConstantPartialTick(clientTimer);
                
                mc.player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.applyLockedRotation();
                });
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (mc.level != null) {
            switch (event.phase) {
            case START:
                ActionsOverlayGui.getInstance().tick();
                
                if (!mc.isPaused()) {
                    ClientTicking.tickAll();
                    
                    mc.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.tick();
                    });
                    
                    mc.level.entitiesForRendering().forEach(entity -> {
                        entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                        entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                        entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                    });
                }
                
                tickResolveEffect();
                break;
            case END:
                ShaderEffectApplier.getInstance().shaderTick();
                
                // FIXME make stand actions clickable when player hands are busy
                if (mc.level != null && mc.player != null && mc.player.getVehicle() != null
                        && mc.player.getVehicle().getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
                    ClientReflection.setHandsBusy(mc.player, true);
                }
                break;
            }
        }
        
        if (event.phase == TickEvent.Phase.START) {
            ClientTimeStopHandler.getInstance().tickPauseIrrelevant();
            
            deathScreenTick = mc.screen instanceof DeathScreen ? deathScreenTick + 1 : 0;
            standStatsTick = mc.screen instanceof IngameMenuScreen && doStandStatsRender(mc.screen) ? standStatsTick + 1 : 0;
        }
    }
    
    public static void onMouseTargetChanged(RayTraceResult newTarget) {
        if (newTarget instanceof EntityRayTraceResult) {
            Entity entity = ((EntityRayTraceResult) newTarget).getEntity();
            if (entity instanceof PlayerEntity) {
                PlayerEntity clientPlayer = Minecraft.getInstance().player;
                PlayerEntity targetPlayer = (PlayerEntity) entity;
                Optional<HamonData> playerHamon = INonStandPower.getNonStandPowerOptional(clientPlayer)
                        .resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
                if (playerHamon.isPresent()) {
                    if (playerHamon.get().playerWantsToLearn(targetPlayer)) {
                        ClientUtil.setOverlayMessage(new TranslationTextComponent(
                                "jojo.chat.message.new_hamon_learner", 
                                targetPlayer.getDisplayName(), 
                                new KeybindTextComponent(InputHandler.getInstance().hamonSkillsWindow.getName())));
                    }
                }
                else {
                    Optional<HamonData> targetHamon = INonStandPower.getNonStandPowerOptional(targetPlayer)
                            .resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
                    if (targetHamon.isPresent()) {
                        boolean hasAskedAlready = targetHamon.get().playerWantsToLearn(clientPlayer);
                        if (hasAskedAlready) {
                            ClientUtil.setOverlayMessage(new TranslationTextComponent(
                                    "jojo.chat.message.asked_hamon_teacher", 
                                    targetPlayer.getDisplayName()));
                        }
                        else {
                            ClientUtil.setOverlayMessage(new TranslationTextComponent(
                                    "jojo.chat.message.ask_hamon_teacher", 
                                    new KeybindTextComponent(InputHandler.getInstance().hamonSkillsWindow.getName()), 
                                    targetPlayer.getDisplayName()));
                        }
                    }
                }
            }
        }
    }
    
    
    
    public void onResolveEffectStart(int effectAmplifier) {
        ShaderEffectApplier.getInstance().setRandomResolveShader();
        startPlayingOst(effectAmplifier);
    }

    private void tickResolveEffect() {
        if (mc.player.isAlive() && mc.player.hasEffect(ModStatusEffects.RESOLVE.get())) {
            ShaderEffectApplier.getInstance().setRandomResolveShader();
            
            if (mc.player.getEffect(ModStatusEffects.RESOLVE.get()).getDuration() == 100) {
                fadeAwayOst(150);
            }
            
            if (mc.player.tickCount % 100 == 0) {
                Minecraft.getInstance().getMusicManager().stopPlaying();
            }
        }
        else {
            ShaderEffectApplier.getInstance().stopResolveShader();
            fadeAwayOst(20);
        }
    }

    private void startPlayingOst(int level) {
        mc.getMusicManager().stopPlaying();

        if (ost == null || ost.isStopped()) {
            ost = null;
            IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
                if (stand.hasPower()) {
                    OstSoundList ostList = stand.getType().getOst();
                    if (ostList != null) {
                        SoundEvent ostSound = ostList.get(level);
                        if (ostSound != null) {
                            ost = new StandOstSound(ostSound, mc);
                            mc.getSoundManager().play(ost);
                        }
                    }
                }
            });
        }
    }

    private void fadeAwayOst(int fadeAwayTicks) {
        if (ost != null) {
            if (!ost.isStopped()) {
                ost.setFadeAway(fadeAwayTicks);
            }
            else {
                mc.getSoundManager().stop(ost);
            }
            ost = null;
        }
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void zoom(EntityViewRenderEvent.FOVModifier event) {
        if (isZooming) {
            zoomModifier = Math.min(zoomModifier + mc.getDeltaFrameTime() / 3F, 60);
        }
        else if (zoomModifier > 1) {
            zoomModifier = Math.max(zoomModifier - mc.getDeltaFrameTime() * 2F, 1);
        }
        if (zoomModifier > 1) {
            event.setFOV(event.getFOV() / zoomModifier);
        }
    }
    
    @SubscribeEvent
    public static void cameraSetup(EntityViewRenderEvent.CameraSetup event) {
        PolaroidHelper.pictureCameraSetup(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void disableFoodBar(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == FOOD && !ModInteractionUtil.isModLoaded("vampirism") || event.getType() == AIR) {
            INonStandPower.getNonStandPowerOptional(mc.player).ifPresent(power -> {
                if (power.getType() == ModPowers.VAMPIRISM.get()) {
                    event.setCanceled(true);
                }
            });
        }
        
        if (event.getType() == EXPERIENCE && mc.gameMode.hasExperience()
                && mc.player.hasEffect(ModStatusEffects.STAND_VIRUS.get())) {
            IStandPower.getStandPowerOptional(mc.player).ifPresent(power -> {
                StandArrowHandler handler = power.getStandArrowHandler();
                int standArrowLevels = handler.getXpLevelsTakenByArrow();
                if (standArrowLevels > 0) {
                    MatrixStack matrixStack = event.getMatrixStack();
                    renderExperienceBar(matrixStack, standArrowLevels, event.getWindow());
                    event.setCanceled(true);
                }
            });
        }
    }
    
    @SuppressWarnings("deprecation")
    private void renderExperienceBar(MatrixStack matrixStack, int standArrowLevels, MainWindow window) {
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        FontRenderer font = mc.font;
        int xPos = screenWidth / 2 - 91;
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        
        mc.getProfiler().push("expBar");
        mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
        int i = mc.player.getXpNeededForNextLevel();
        if (i > 0) {
            int k = (int)(mc.player.experienceProgress * 183.0F);
            int yPos = screenHeight - 32 + 3;
            mc.gui.blit(matrixStack, xPos, yPos, 0, 64, 182, 5);
            if (k > 0) {
                mc.gui.blit(matrixStack, xPos, yPos, 0, 69, k, 5);
            }
        }
        
        mc.getProfiler().pop();
        
        mc.getProfiler().push("expLevel");
        
        String xpLevels = mc.player.experienceLevel > 0 ? "" + mc.player.experienceLevel + " " : "";
        String arrowLevels = "(" + standArrowLevels + ")";
        
        float xpNumX = (screenWidth - font.width(xpLevels + arrowLevels)) / 2F;
        float arrowXpNumX = xpNumX + font.width(xpLevels);
        float numberY = screenHeight - 31 - 4;
        
        font.draw(matrixStack, xpLevels, xpNumX + 1, numberY, 0);
        font.draw(matrixStack, xpLevels, xpNumX - 1, numberY, 0);
        font.draw(matrixStack, xpLevels, xpNumX, numberY + 1, 0);
        font.draw(matrixStack, xpLevels, xpNumX, numberY - 1, 0);
        font.draw(matrixStack, xpLevels, xpNumX, numberY, 0x80FF20);
        
        font.draw(matrixStack, arrowLevels, arrowXpNumX + 1, numberY, 0);
        font.draw(matrixStack, arrowLevels, arrowXpNumX - 1, numberY, 0);
        font.draw(matrixStack, arrowLevels, arrowXpNumX, numberY + 1, 0);
        font.draw(matrixStack, arrowLevels, arrowXpNumX, numberY - 1, 0);
        font.draw(matrixStack, arrowLevels, arrowXpNumX, numberY, 0xFFD820);
        
        mc.getProfiler().pop();
        
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelHandRender(RenderHandEvent event) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            
            if (event.getHand() == Hand.MAIN_HAND) {
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    if (cap.isUsingZoomPunch()) {
                        event.setCanceled(true);
                    }
                });
            }
            
            if (!event.isCanceled()) {
                INonStandPower.getNonStandPowerOptional(livingEntity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        if (hamon.isMeditating()) {
                            event.setCanceled(true);
                        }
                    });
                });
            }
        }
    }
    
    private boolean modPostedEvent = false;
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHand(RenderHandEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        Hand hand = event.getHand();
        ItemStack item = player.getItemInHand(hand);
        if (!event.isCanceled() && !modPostedEvent) {
            if (hand == Hand.MAIN_HAND) {
                if (!player.isInvisible()) {
                    INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
                        if ((hud.isActionSelectedAndEnabled(ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE.get(), 
                                ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()))
                                && MCUtil.isHandFree(player, Hand.MAIN_HAND) && MCUtil.isHandFree(player, Hand.OFF_HAND)) {
                            renderHand(Hand.OFF_HAND, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                                    event.getPartialTicks(), event.getInterpolatedPitch(), player);
                        }
                    });
                    
                    if (GlovesLayer.areGloves(item) || item.isEmpty() && !player.isInvisible() && 
                            (player.hasEffect(ModStatusEffects.HAMON_SPREAD.get()) || player.hasEffect(ModStatusEffects.FREEZE.get()))) {
                        event.setCanceled(true);
                        renderHand(Hand.MAIN_HAND, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                                event.getPartialTicks(), event.getInterpolatedPitch(), player);
                    }
                }
            }
            
//            if (!item.isEmpty() && item.getItem() == ModItems.PHOTO.get()) {
//                event.setCanceled(true);
//                PolaroidHelper.renderPhotoInHand(event.getMatrixStack(), event.getBuffers(), event.getLight(), 
//                        event.getEquipProgress(), MCUtil.getHandSide(player, hand), event.getSwingProgress(), item);
//            }
        }
    }
    
    private void renderHand(Hand hand, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light,
            float partialTick, float interpolatedPitch, LivingEntity entity) {
        FirstPersonRenderer renderer = mc.getItemInHandRenderer();
        ClientPlayerEntity player = mc.player;
        Hand swingingArm = MoreObjects.firstNonNull(player.swingingArm, Hand.MAIN_HAND);
        float swingProgress = swingingArm == hand ? player.getAttackAnim(partialTick) : 0.0F;
        float equipProgress = hand == Hand.MAIN_HAND ?
                1.0F - MathHelper.lerp(partialTick, ClientReflection.getMainHandHeightPrev(renderer), ClientReflection.getMainHandHeight(renderer))
                : 1.0F - MathHelper.lerp(partialTick, ClientReflection.getOffHandHeightPrev(renderer), ClientReflection.getOffHandHeight(renderer));
        
        modPostedEvent = true;
        if (!ForgeHooksClient.renderSpecificFirstPersonHand(hand, 
                matrixStack, buffer, light, 
                partialTick, interpolatedPitch, 
                swingProgress, equipProgress, entity.getItemInHand(hand))) {
            HandSide handSide = MCUtil.getHandSide(player, hand);
            
            matrixStack.pushPose();
            ClientReflection.renderPlayerArm(matrixStack, buffer, light, equipProgress, 
                    swingProgress, handSide, renderer);
            HamonBurnLayer.renderFirstPerson(handSide, matrixStack, buffer, light, player);
            FrozenLayer.renderFirstPerson(handSide, matrixStack, buffer, light, player);
            GlovesLayer.renderFirstPerson(handSide, matrixStack, buffer, light, player);
            matrixStack.popPose();
            // i've won... but at what cost?
        }
        modPostedEvent = false;
    }
    
    public static boolean mainHandRendered;
    public static boolean offHandRendered;
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void resetHandsRenderFlags(RenderHandEvent event) {
        mainHandRendered = false;
        offHandRendered = false;
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void areHandsRendered(RenderHandEvent event) {
        switch (event.getHand()) {
        case MAIN_HAND:
            mainHandRendered = true;
            break;
        case OFF_HAND:
            offHandRendered = true;
            break;
        }
    }
    
    
    
    @SubscribeEvent
    public void afterScreenRender(DrawScreenEvent.Post event) {
        Screen screen = event.getGui();
        float partialTick = screen.getMinecraft().getFrameTime();
        if (screen instanceof DeathScreen) {
            ITextComponent title = screen.getTitle();
            if (title instanceof TranslationTextComponent && ((TranslationTextComponent) title).getKey().endsWith(".hardcore")) {
                return;
            }
            renderToBeContinuedArrow(event.getMatrixStack(), screen, screen.width, screen.height, partialTick);
        }

        else if (screen instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) screen)) {
            float alpha = ClientModSettings.getSettingsReadOnly().standStatsTranslucency;
            int xButtonsRightEdge = screen.width / 2 + 102;
            int windowWidth = screen.width;
            int windowHeight = screen.height;
            
            if (doStandStatsRender(screen)) {
                StandStatsRenderer.renderStandStats(event.getMatrixStack(), mc, windowWidth - 160, windowHeight - 160, windowWidth, windowHeight,
                        standStatsTick, partialTick, alpha, event.getMouseX(), event.getMouseY(), windowWidth - xButtonsRightEdge - 14);
            }
        }
    }
    
    private Boolean renderStandStats;
    private boolean doStandStatsRender(Screen screen) {
        if (renderStandStats != null) {
            return renderStandStats;
        }
        int xButtonsRightEdge = screen.width / 2 + 102;
        int windowWidth = screen.width;
        int windowHeight = screen.height;
        return windowWidth - xButtonsRightEdge >= 167 && windowHeight > 204;
    }

    private void renderToBeContinuedArrow(MatrixStack matrixStack, AbstractGui ui, int screenWidth, int screenHeight, float partialTick) {
        int x = screenWidth - 5 - (int) ((screenWidth - 10) * Math.min(deathScreenTick + partialTick, 20F) / 20F);
        int y = screenHeight - 29;
        mc.textureManager.bind(ClientUtil.ADDITIONAL_UI);
        ui.blit(matrixStack, x, y, 0, 231, 130, 25);
        AbstractGui.drawCenteredString(matrixStack, mc.font, new TranslationTextComponent("jojo.to_be_continued"), x + 61, y + 8, 0x525544);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void addToScreen(InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) screen)) {
            IStandPower.getStandPowerOptional(mc.player).ifPresent(power -> {
                if (power.hasPower()) {
                    AbstractSlider statsBgAlphaSlider = new HeightScaledSlider(
                            screen.width - 160, screen.height - 6, 153, 6, StringTextComponent.EMPTY, 0.0D) {
                        {
                            this.value = MathHelper.inverseLerp(
                                    ClientModSettings.getSettingsReadOnly().standStatsTranslucency, 
                                    0.1, 1.0);
                            updateMessage();
                        }
                        
                        @Override
                        protected void updateMessage() {
                            setMessage(StringTextComponent.EMPTY);
                        }
                        
                        @Override
                        protected void applyValue() {
                            ClientModSettings.getInstance().editSettings(settings -> 
                            settings.standStatsTranslucency = (float) MathHelper.clampedLerp(0.1, 1.0, this.value));
                        }
                    };
                    statsBgAlphaSlider.visible = doStandStatsRender(screen);
                    event.addWidget(statsBgAlphaSlider);
                    
                    Button standStatsToggleButton = new ImageVanillaButton(screen.width - 28, screen.height - 28, 
                            20, 20, 236, 236, StandStatsRenderer.STAND_STATS_UI, 256, 256, 
                            button -> {
                                renderStandStats = !doStandStatsRender(screen);
                                statsBgAlphaSlider.visible = doStandStatsRender(screen);
                            }, 
                            (button, matrixStack, x, y) -> {
                                ITextComponent message = doStandStatsRender(screen) ? 
                                        new TranslationTextComponent("jojo.stand_stat.button.hide")
                                        : new TranslationTextComponent("jojo.stand_stat.button.show");
                                screen.renderTooltip(matrixStack, message, x, y);
                            }, 
                            StringTextComponent.EMPTY);
                    event.addWidget(standStatsToggleButton);
                }
            });
        }
        
        else if (screen instanceof OptionsScreen) {
            event.addWidget(ClientModSettingsScreen.addSettingsButton(screen, event.getWidgetList()));
        }
        
        else if (screen instanceof ControlsScreen) {
            KeyBindingList controlList = ClientReflection.getControlList((ControlsScreen) screen);
            List<KeyBindingList.Entry> keyEntries = controlList.children();
            
            ListIterator<KeyBindingList.Entry> entriesIter = keyEntries.listIterator();
            ClientModSettings modSettings = ClientModSettings.getInstance();
            ClientModSettings.Settings modSettingsRead = ClientModSettings.getSettingsReadOnly();
            
            boolean addHudScreenButtons;
            LazyOptional<IStandPower> spOptional;
            LazyOptional<INonStandPower> nspOptional;
            if (mc.player != null) {
                spOptional = IStandPower.getStandPowerOptional(mc.player);
                nspOptional = INonStandPower.getNonStandPowerOptional(mc.player);
                addHudScreenButtons = spOptional.map(IPower::hasPower).orElse(false) || nspOptional.map(IPower::hasPower).orElse(false);
            }
            else {
                addHudScreenButtons = false;
                spOptional = LazyOptional.empty();
                nspOptional = LazyOptional.empty();
            }
            
            while (entriesIter.hasNext()) {
                KeyBindingList.Entry entry = entriesIter.next();
                if (entry instanceof KeyBindingList.KeyEntry) {
                    KeyBindingList.KeyEntry keyEntry = (KeyBindingList.KeyEntry) entry;
                    KeyBinding key = ClientReflection.getKey(keyEntry);
                    if (key == InputHandler.getInstance().attackHotbar) {
                        entriesIter.set(new HoldToggleKeyEntry(keyEntry, ClientReflection.getChangeButton(keyEntry), new ControlSettingToggleButton(40, 20, 
                                button -> {
                                    modSettings.editSettings(s -> s.toggleLmbHotbar = !s.toggleLmbHotbar);
                                    InputHandler.getInstance().setToggledHotbarControls(ControlScheme.Hotbar.LEFT_CLICK, false);
                                },
                                () -> modSettingsRead.toggleLmbHotbar)));
                    }
                    else if (key == InputHandler.getInstance().abilityHotbar) {
                        entriesIter.set(new HoldToggleKeyEntry(keyEntry, ClientReflection.getChangeButton(keyEntry), new ControlSettingToggleButton(40, 20, 
                                button -> {
                                    modSettings.editSettings(s -> s.toggleRmbHotbar = !s.toggleRmbHotbar);
                                    InputHandler.getInstance().setToggledHotbarControls(ControlScheme.Hotbar.RIGHT_CLICK, false);
                                },
                                () -> modSettingsRead.toggleRmbHotbar)));
                    }
                    else if (key == InputHandler.getInstance().disableHotbars) {
                        entriesIter.set(new HoldToggleKeyEntry(keyEntry, ClientReflection.getChangeButton(keyEntry), new ControlSettingToggleButton(40, 20, 
                                button -> {
                                    modSettings.editSettings(s -> s.toggleDisableHotbars = !s.toggleDisableHotbars);
                                    InputHandler.getInstance().setToggleHotbarsDisabled(false);
                                },
                                () -> modSettingsRead.toggleDisableHotbars)));
                    }
                }
                else if (addHudScreenButtons && entry instanceof KeyBindingList.CategoryEntry) {
                    KeyBindingList.CategoryEntry categoryEntry = (KeyBindingList.CategoryEntry) entry;
                    ITextComponent categoryName = ClientReflection.getName(categoryEntry);
                    
                    IStandPower standPower = spOptional.resolve().get();
                    INonStandPower nonStandPower = nspOptional.resolve().get();
                    Button[] hudScreenButtons = new Button[standPower.hasPower() && nonStandPower.hasPower() ? 2 : 1];
                    int i = 0;
                    if (standPower.hasPower()) {
                        ITextComponent tooltip = new TranslationTextComponent("jojo.key.edit_hud.power_name", standPower.getName());
                        hudScreenButtons[i++] = new ImageVanillaButton((screen.width + mc.font.width(categoryName) + 10) / 2, -21, 
                                20, 20, 
                                0, 0, 16, 16, standPower.clGetPowerTypeIcon(), 16, 16, 
                                button -> {
                                    HudLayoutEditingScreen hudScreen = new HudLayoutEditingScreen(PowerClassification.STAND);
                                    mc.setScreen(hudScreen);
                                }, 
                                (button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY),
                                tooltip);
                    }
                    if (nonStandPower.hasPower()) {
                        ITextComponent tooltip = new TranslationTextComponent("jojo.key.edit_hud.power_name", nonStandPower.getName());
                        hudScreenButtons[i] = new ImageVanillaButton((screen.width + mc.font.width(categoryName) + 10) / 2 + (i++) * 24, -21, 
                                20, 20, 
                                0, 0, 16, 16, nonStandPower.clGetPowerTypeIcon(), 16, 16, 
                                button -> {
                                    HudLayoutEditingScreen hudScreen = new HudLayoutEditingScreen(PowerClassification.NON_STAND);
                                    mc.setScreen(hudScreen);
                                }, 
                                (button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY),
                                tooltip);
                    }
                    
                    if (InputHandler.HUD_CATEGORY.equals(((TranslationTextComponent) categoryName).getKey())) {
                        entriesIter.set(new CategoryWithButtonsEntry(controlList, categoryName, hudScreenButtons));
                    }
                }
            }
            
            if (HudLayoutEditingScreen.scrollCtrlListTo != null) {
                Predicate<KeyBindingList.Entry> scrollTo = HudLayoutEditingScreen.scrollCtrlListTo;
                HudLayoutEditingScreen.scrollCtrlListTo = null;
                OptionalInt index = IntStream.range(0, controlList.children().size())
                        .filter(i -> {
                            KeyBindingList.Entry entry = controlList.children().get(i);
                            return scrollTo.test(entry);
                        })
                        .findFirst();
                index.ifPresent(i -> {
                    controlList.setScrollAmount(ClientReflection.getRowTop(controlList, i) - controlList.getTop());
                });
            }
        }
    }

    @SubscribeEvent
    public void onScreenOpened(GuiOpenEvent event) {
        Screen screen = event.getGui();
        if (screen instanceof MainMenuScreen) {
            String splash = CustomResources.getModSplashes().overrideSplash();
            if (splash != null) {
                ClientReflection.setSplash((MainMenuScreen) screen, splash);
            }
        }
        else if (screen == null) {
            onScreenClosed();
        }
    }
    
    private void onScreenClosed() {
        if (renderStandStats != null && renderStandStats) {
            renderStandStats = null;
        }
    }

    @SubscribeEvent
    public void renderBlocksOverlay(RenderWorldLastEvent event) {
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        if (hud.showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_RESTORE_TERRAIN.get())) {
            MatrixStack matrixStack = event.getMatrixStack();
            IStandPower stand = ActionsOverlayGui.getInstance().standUiMode.getPower();
            Entity entity = CrazyDiamondRestoreTerrain.restorationCenterEntity(mc.player, stand);
            Vector3i pos = CrazyDiamondRestoreTerrain.eyePos(entity);
            Vector3d lookVec = entity.getLookAngle();
            Vector3d eyePosD = entity.getEyePosition(1.0F);
            TranslucentBlockRenderHelper.renderCDRestorationTranslucentBlocks(matrixStack, mc, 
                    CrazyDiamondRestoreTerrain.getBlocksInRange(mc.level, mc.player, pos, 32, 
                            block -> CrazyDiamondRestoreTerrain.blockCanBePlaced(mc.level, block.pos, block.state)),
                    block -> CrazyDiamondRestoreTerrain.blockPosSelectedForRestoration(block, entity, lookVec, eyePosD, pos, 
                            mc.player.hasEffect(ModStatusEffects.RESOLVE.get()), mc.player.isShiftKeyDown()));
        }
//        else {
//            hud.getSelectedEnabledActions()
//            .filter(action -> action instanceof TimeStopInstant)
//            .findFirst()
//            .ifPresent(action -> {
//                TimeStopInstant tpAction = (TimeStopInstant) action;
//                IStandPower stand = ActionsOverlayGui.getInstance().standUiMode.getPower();
//                Vector3d pos = tpAction.calcBlinkPos(mc.player, stand, ActionTarget.fromRayTraceResult(mc.hitResult));
//                if (pos != null) {
//                    // TODO render translucent player model at the position
//                }
//            });
//        }
        
        boolean paused = mc.isPaused();
        if (prevPause && !paused) {
            pausePartialTick = mc.getFrameTime();
        }
        prevPause = paused;
        
        ShaderEffectApplier.getInstance().updateTimeStopperScreenPos(
                event.getMatrixStack(), event.getProjectionMatrix(), mc.gameRenderer.getMainCamera());
    }
    
    public float getPartialTick() {
        return mc.isPaused() ? pausePartialTick : mc.getFrameTime();
    }
    
    @SubscribeEvent
    public void addTooltipLines(ItemTooltipEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(player.level, event.getItemStack()).ifPresent(pos -> {
                if (IStandPower.getStandPowerOptional(player).map(power -> power.getType() == ModStands.CRAZY_DIAMOND.getStandType()).orElse(false)) {
                    event.getToolTip().add(new TranslationTextComponent("jojo.crazy_diamond.block_checkpoint.tooltip", 
                            pos.getX(), pos.getY(), pos.getZ()).withStyle(TextFormatting.RED));
                }
            });
            
           OilItem.remainingOiledUses(event.getItemStack()).ifPresent(uses -> {
               if (uses > 0) {
                   event.getToolTip().add(new TranslationTextComponent("item.jojo.oil.uses", uses).withStyle(TextFormatting.GOLD));
               }
           });
        }

        if (event.getItemStack().getItem() instanceof EnchantedBookItem && !ModList.get().isLoaded("enchdesc")) {
            EnchantedBookItem.getEnchantments(event.getItemStack()).forEach(nbt -> {
                if (nbt.getId() == MCUtil.getNbtId(CompoundNBT.class)) {
                    CompoundNBT enchNbt = (CompoundNBT) nbt;
                    ResourceLocation enchId = ResourceLocation.tryParse(enchNbt.getString("id"));
                    if (enchId != null && enchId.getNamespace().equals(JojoMod.MOD_ID)) {
                        event.getToolTip().add(new TranslationTextComponent(
                                String.format("enchantment.%s.%s.desc", enchId.getNamespace(), enchId.getPath()))
                                .withStyle(TextFormatting.GRAY));
                    }
                }
            });
        }
    }
    

    //    @SubscribeEvent(priority = EventPriority.LOWEST)
    //    public void onUseItemStart(LivingEntityUseItemEvent.Start event) {
    //        if (event.getEntity().level.isClientSide() && event.getItem().isEdible()) {
    //            String itemName = event.getItem().getItem().getRegistryName().getPath();
    //            if (((itemName.contains("berry") || itemName.contains("berries")) && event.getEntityLiving().getRandom().nextFloat() < 0.125F ||
    //                (itemName.contains("cherry") || itemName.contains("cherries")))
    //                    && IStandPower.getStandPowerOptional(event.getEntityLiving()).map(stand -> {
    //                        return stand.getType() == ModStandTypes.HIEROPHANT_GREEN.get();
    //                    }).orElse(false)) {
    //                ClientTickingSoundsHelper.playItemUseSound(event.getEntityLiving(), ModSounds.RERO.get(), 1.0F, 1.0F, true, event.getItem());
    //            }
    //        }
    //    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMount(EntityMountEvent event) {
        if (event.getWorldObj().isClientSide() && event.getEntityMounting() instanceof PlayerEntity) {
            Entity mounted = event.getEntityBeingMounted();
            EntityType<?> mountedType = event.isMounting() && mounted != null ? mounted.getType() : null;
            event.getEntityMounting().getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(
                    cap -> cap.setVehicleType(mountedType));
        }
    }
}
