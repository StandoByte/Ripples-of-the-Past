package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;

import java.util.Set;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.render.block.overlay.TranslucentBlockRenderHelper;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.GlovesLayer;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.HamonBurnLayer;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.sound.StandOstSound;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.client.ui.screen.widgets.HeightScaledSlider;
import com.github.standobyte.jojo.client.ui.standstats.StandStatsRenderer;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
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

        // FIXME reset the glowing flag after outline is no longer needed
        int outlineColor = outlineColor(entity);
        if (outlineColor > 0) {
            entity.setGlowing(true);
            if (event.getBuffers() instanceof OutlineLayerBuffer) {
                ((OutlineLayerBuffer) event.getBuffers()).setColor(outlineColor >> 16 & 255, outlineColor >> 8 & 255, outlineColor & 255, 255);
            }
        }

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

    private int outlineColor(LivingEntity entity) {
        return -1;
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
        }

        deathScreenTick = mc.screen instanceof DeathScreen ? deathScreenTick + 1 : 0;
        standStatsTick = mc.screen instanceof IngameMenuScreen && doStandStatsRender(mc.screen) ? standStatsTick + 1 : 0;
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void disableFoodBar(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == FOOD || event.getType() == AIR) {
            INonStandPower.getNonStandPowerOptional(mc.player).ifPresent(power -> {
                if (power.getType() == ModPowers.VAMPIRISM.get()) {
                    event.setCanceled(true);
                }
            });
        }
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
    @SuppressWarnings("resource")
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHand(RenderHandEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (!event.isCanceled() && !modPostedEvent && event.getHand() == Hand.MAIN_HAND && !player.isInvisible()) {
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
                if ((hud.isActionSelectedAndEnabled(ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE.get()))
                        && MCUtil.isHandFree(player, Hand.MAIN_HAND) && MCUtil.isHandFree(player, Hand.OFF_HAND)) {
                    renderHand(Hand.OFF_HAND, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                            event.getPartialTicks(), event.getInterpolatedPitch(), player);
                }
            });
            
            ItemStack item = player.getItemInHand(Hand.MAIN_HAND);
            if (GlovesLayer.areGloves(item) || player.hasEffect(ModStatusEffects.HAMON_SPREAD.get())) {
                event.setCanceled(true);
                renderHand(Hand.MAIN_HAND, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                        event.getPartialTicks(), event.getInterpolatedPitch(), player);
            }
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
        if (screen instanceof DeathScreen) {
            ITextComponent title = screen.getTitle();
            if (title instanceof TranslationTextComponent && ((TranslationTextComponent) title).getKey().endsWith(".hardcore")) {
                return;
            }
            renderToBeContinuedArrow(event.getMatrixStack(), screen, screen.width, screen.height, event.getRenderPartialTicks());
        }

        else if (screen instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) screen)) {
            float alpha = ClientModSettings.getInstance().getSettingsReadOnly().standStatsTranslucency;
            int xButtonsRightEdge = screen.width / 2 + 102;
            int windowWidth = screen.width;
            int windowHeight = screen.height;
            
            if (doStandStatsRender(screen)) {
                StandStatsRenderer.renderStandStats(event.getMatrixStack(), mc, windowWidth - 160, windowHeight - 160, windowWidth, windowHeight,
                        standStatsTick, event.getRenderPartialTicks(), alpha, event.getMouseX(), event.getMouseY(), windowWidth - xButtonsRightEdge - 14);
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
    
    @SubscribeEvent
    public void addToScreen(InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        if (screen instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) screen)) {
            IStandPower.getStandPowerOptional(mc.player).ifPresent(power -> {
                if (power.hasPower()) {
                    AbstractSlider statsBgAlphaSlider = new HeightScaledSlider(
                            screen.width - 160, screen.height - 6, 153, 6, StringTextComponent.EMPTY, 0.0D) {
                        {
                            this.value = MathHelper.inverseLerp(
                                    ClientModSettings.getInstance().getSettingsReadOnly().standStatsTranslucency, 
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
                    
                    Button standStatsToggleButton = new ImageButton(screen.width - 28, screen.height - 28, 
                            20, 20, 236, 216, 20, StandStatsRenderer.STAND_STATS_UI, 256, 256, 
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
                    
                    Button standSkinsButton = new ImageButton(screen.width - 28, screen.height - 159, 
                            20, 20, 236, 216, 20, StandSkinsScreen.TEXTURE_MAIN_WINDOW, 256, 256, 
                            button -> {
                                StandSkinsScreen.openScreen(screen);
                            }, 
                            (button, matrixStack, x, y) -> {
                                screen.renderTooltip(matrixStack, new TranslationTextComponent("jojo.stand_skins.button"), x, y);
                            }, 
                            StringTextComponent.EMPTY);
                    event.addWidget(standSkinsButton);
                }
            });
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
        if (hud.isActionSelectedAndEnabled(ModStandsInit.CRAZY_DIAMOND_RESTORE_TERRAIN.get())) {
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

    private static final Set<ResourceLocation> ENCHANTMENTS_DESC = ImmutableSet.of(
            new ResourceLocation(JojoMod.MOD_ID, "virus_inhibition"));
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
        }

        if (event.getItemStack().getItem() instanceof EnchantedBookItem && !ModList.get().isLoaded("enchdesc")) {
            EnchantedBookItem.getEnchantments(event.getItemStack()).forEach(nbt -> {
                if (nbt.getId() == MCUtil.getNbtId(CompoundNBT.class)) {
                    CompoundNBT enchNbt = (CompoundNBT) nbt;
                    ResourceLocation enchId = ResourceLocation.tryParse(enchNbt.getString("id"));
                    if (enchId != null && ENCHANTMENTS_DESC.contains(enchId)) {
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
