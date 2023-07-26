package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.render.block.overlay.TranslucentBlockRenderHelper;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.GlovesLayer;
import com.github.standobyte.jojo.client.render.world.ParticleManagerWrapperTS;
import com.github.standobyte.jojo.client.render.world.TimeStopWeatherHandler;
import com.github.standobyte.jojo.client.render.world.shader.CustomShaderGroup;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.sound.StandOstSound;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.standstats.StandStatsRenderer;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModEntityTypes;
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
import com.github.standobyte.jojo.util.mod.TimeUtil;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IWeatherParticleRenderHandler;
import net.minecraftforge.client.IWeatherRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
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
    private boolean isTimeStopped = false;
    private boolean canSeeInStoppedTime = true;
    private boolean canMoveInStoppedTime = true;
    private float partialTickStoppedAt;
    private int timeStopTicks = 0;
    private int timeStopLength = 0;

    private Random random = new Random();
    private ResourceLocation resolveShader = null;
    private static final ResourceLocation DUMMY = new ResourceLocation("dummy", "dummy");
    private StandOstSound ost;

    private boolean resetShader;
    private double zoomModifier;
    public boolean isZooming;

    private int deathScreenTick;
    private int pauseMenuScreenTick;
    
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



    private boolean isTimeStopped(BlockPos blockPos) {
        return isTimeStopped(new ChunkPos(blockPos));
    }

    private boolean isTimeStopped(ChunkPos chunkPos) {
        return mc.level != null && TimeUtil.isTimeStopped(mc.level, chunkPos);
    }

    public void setTimeStopClientState(boolean canSee, boolean canMove) {
        canSeeInStoppedTime = canSee;
        canMoveInStoppedTime = canSee && canMove;
        partialTickStoppedAt = canMove ? mc.getFrameTime() : 0.0F;
        resetShader = true;
    }

    public void updateCanMoveInStoppedTime(boolean canMove, ChunkPos chunkPos) {
        if (isTimeStopped(chunkPos)) {
            this.canMoveInStoppedTime = canMove;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlaySound(PlaySoundEvent event) {
        if (!canSeeInStoppedTime) {
            ISound sound = event.getResultSound();
            if (sound != null && sound.getAttenuation() == AttenuationType.LINEAR) {
                event.setResultSound(null);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving(RenderLivingEvent.Pre<T, M> event) {
        LivingEntity entity = event.getEntity();
        
        if (entity.hasEffect(ModEffects.FULL_INVISIBILITY.get())) {
            event.setCanceled(true);
            return;
        }
        
        if (isTimeStopped(entity.blockPosition())) {
            if (!entity.canUpdate()) {
                if (event.getPartialRenderTick() != partialTickStoppedAt) {
                    event.getRenderer().render((T) entity, MathHelper.lerp(partialTickStoppedAt, entity.yRotO, entity.yRot), 
                            partialTickStoppedAt, event.getMatrixStack(), event.getBuffers(), event.getLight());
                    event.setCanceled(true);
                }
                return;
            }
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
            if (mc.player.isAlive()) {
                if (isTimeStopped(mc.player.blockPosition())) {
                    if (!canSeeInStoppedTime) {
                        clientTimer.partialTick = partialTickStoppedAt;
                    }
                }
                
                mc.player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getLockedYRot().ifPresent(yRot -> {
                        mc.player.yBodyRot = yRot;
                        mc.player.yBodyRotO = yRot;
                        mc.player.yRot = yRot;
                        mc.player.yRotO = yRot;
                    });
                    cap.getLockedXRot().ifPresent(xRot -> {
                        mc.player.xRot = xRot;
                        mc.player.xRotO = xRot;
                    });
                });
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (mc.level != null) {
            if (event.phase == TickEvent.Phase.START) {
                ActionsOverlayGui.getInstance().tick();
                
                if (!mc.isPaused()) {
                    ClientTicking.tickAll();
                    if (isTimeStopped) {
                        timeStopTicks++;
                    }
                    
                    mc.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.tick();
                    });
                    
                    mc.level.entitiesForRendering().forEach(entity -> {
                        entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                        entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                    });
                }
            }
            
            setTimeStoppedState(isTimeStopped(mc.player.blockPosition()));

            switch (event.phase) {
            case START:
                if (isTimeStopped && !canSeeInStoppedTime) {
                    ClientReflection.pauseClient(mc);
                }
                tickResolveEffect();
                break;
            case END:
                if (resetShader) {
                    mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
                    resetShader = false;
                }
                
                // FIXME make stand actions clickable when player hands are busy
                if (mc.level != null && mc.player != null && mc.player.getVehicle() != null
                        && mc.player.getVehicle().getType() == ModEntityTypes.LEAVES_GLIDER.get()) {
                    ClientReflection.setHandsBusy(mc.player, true);
                }
                break;
            }
        }
        else if (isTimeStopped) {
            setTimeStoppedState(false);
        }

        if (mc.gameRenderer.currentEffect() == null) {
            updateCurrentShader();
        }

        deathScreenTick = mc.screen instanceof DeathScreen ? deathScreenTick + 1 : 0;
        pauseMenuScreenTick = mc.screen instanceof IngameMenuScreen ? pauseMenuScreenTick + 1 : 0;
    }
    
    private final Map<ClientWorld, Pair<IWeatherRenderHandler, IWeatherParticleRenderHandler>> prevWeatherRender = new HashMap<>();
    private final TimeStopWeatherHandler timeStopWeatherHandler = new TimeStopWeatherHandler();
    private Set<ITickable> prevTickableTextures = new HashSet<>();
    private void setTimeStoppedState(boolean isTimeStopped) {
        if (this.isTimeStopped != isTimeStopped) {
            this.isTimeStopped = isTimeStopped;
            
            if (!isTimeStopped) {
                timeStopLength = 0;
            }
            
            if (JojoModConfig.CLIENT.timeStopFreezesVisuals.get()) {
                if (isTimeStopped) {
                    if (mc.level != null) {
                        DimensionRenderInfo effects = mc.level.effects();
                        prevWeatherRender.put(mc.level, Pair.of(effects.getWeatherRenderHandler(), effects.getWeatherParticleRenderHandler()));
                        effects.setWeatherRenderHandler(timeStopWeatherHandler);
                        effects.setWeatherParticleRenderHandler(timeStopWeatherHandler);
    
                        TextureManager textureManager = mc.getTextureManager();
                        prevTickableTextures = ClientReflection.getTickableTextures(textureManager);
                        ClientReflection.setTickableTextures(textureManager, new HashSet<>());
                        
                        ParticleManagerWrapperTS.onTimeStopStart(mc);
                    }
                }
                else {
                    if (mc.level != null && prevWeatherRender.containsKey(mc.level)) {
                        timeStopWeatherHandler.onTimeStopEnd();
                        Pair<IWeatherRenderHandler, IWeatherParticleRenderHandler> prevEffects = prevWeatherRender.get(mc.level);
                        DimensionRenderInfo effects = mc.level.effects();
                        effects.setWeatherRenderHandler(prevEffects.getLeft());
                        effects.setWeatherParticleRenderHandler(prevEffects.getRight());
                    }
    
                    TextureManager textureManager = mc.getTextureManager();
                    Set<ITickable> allTickableTextures = Util.make(new HashSet<>(), set -> {
                        set.addAll(prevTickableTextures);
                        set.addAll(ClientReflection.getTickableTextures(textureManager));
                    });
                    ClientReflection.setTickableTextures(textureManager, allTickableTextures);
                    prevTickableTextures = new HashSet<>();
    
                    ParticleManagerWrapperTS.onTimeStopEnd(mc);
                }
            }
            timeStopTicks = 0;
        }
    }
    
    public void updateCurrentShader() {
        ResourceLocation shader = getCurrentShader();
        if (shader != null && shader != DUMMY) {
            loadShader(shader);
        }
    }
    
    private ResourceLocation getCurrentShader() {
        if (mc.level == null) {
            return null;
        }
        
        if (isTimeStopped && canSeeInStoppedTime) {
            if (timeStopAction == ModStandsInit.STAR_PLATINUM_TIME_STOP.get()) {
                return CustomShaderGroup.TIME_STOP_SP;
            }
            else {
                return CustomShaderGroup.TIME_STOP_TW;
            }
        }
        else {
            tsShaderStarted = false;
        }
        
        if (JojoModConfig.CLIENT.resolveShaders.get() && resolveShader != null) {
            return resolveShader;
        }
        return null;
    }
    
    private void loadShader(ResourceLocation shader) {
        if (CustomShaderGroup.hasCustomParameters(shader)) {
            ClientUtil.loadCustomParametersEffect(mc.gameRenderer, mc, shader);
        }
        else {
            mc.gameRenderer.loadEffect(shader);
        }
    }
    
    public void addTsShaderUniforms(ShaderInstance tsShader, float partialSecond, float tsEffectLength) {
        if (isTimeStopped) {
            float partialTick = MathHelper.frac(partialSecond * 20F);
            float tsTick = timeStopTicks + partialTick;
            tsShader.safeGetUniform("TSTicks") .set(tsTick);
            tsShader.safeGetUniform("TSLength").set(timeStopLength);
            if (!JojoModConfig.CLIENT.timeStopAnimation.get()) {
                tsShader.safeGetUniform("TSEffectLength").set(0);
            }
        }
        else {
            tsShader.safeGetUniform("TSTicks") .set(0);
            tsShader.safeGetUniform("TSLength").set(-1);
        }
    }
    
    private boolean tsShaderStarted;
    // TODO determine the position of the time stopper entity on the screen
    private Entity timeStopper;
    private TimeStop timeStopAction;
    public void setTimeStopVisuals(Entity timeStopper, TimeStop action) {
        if (!tsShaderStarted) {
            this.timeStopper = timeStopper;
            this.timeStopAction = action;
            tsShaderStarted = true;
        }
    }
    
    public void updateTimeStopTicksLeft() {
        if (mc.level != null && mc.player != null) {
            int ticks = TimeUtil.getTimeStopTicksLeft(mc.level, new ChunkPos(mc.player.blockPosition()));
            this.timeStopLength = timeStopTicks + ticks;
        }
        else {
            this.timeStopLength = 0;
        }
    }
    
    
    
    public void onResolveEffectStart(int effectAmplifier) {
        if (resolveShader == null) {
            setResolveShader();
        }

        startPlayingOst(effectAmplifier);
    }

    private void tickResolveEffect() {
        if (mc.player.isAlive() && mc.player.hasEffect(ModEffects.RESOLVE.get())) {
            if (resolveShader == null) {
                setResolveShader();
            }

            if (mc.player.getEffect(ModEffects.RESOLVE.get()).getDuration() == 100) {
                fadeAwayOst(150);
            }
            
            if (mc.player.tickCount % 100 == 0) {
                Minecraft.getInstance().getMusicManager().stopPlaying();
            }
        }
        else {
            stopResolveShader();
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

    private void setResolveShader() {
        resolveShader = CustomResources.getResolveShadersListManager()
                .getRandomShader(IStandPower.getPlayerStandPower(mc.player), random);
        if (resolveShader == null) {
            resolveShader = DUMMY;
        }
        else {
            try {
                @SuppressWarnings("unused")
                ShaderGroup tryLoadShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), resolveShader);
            } catch (JsonSyntaxException e) {
                JojoMod.getLogger().warn("Failed to load shader: {}", resolveShader, e);
                resolveShader = DUMMY;
            } catch (IOException e) {
                JojoMod.getLogger().warn("Failed to parse shader: {}", resolveShader, e);
                resolveShader = DUMMY;
            }
        }
    }

    private void stopResolveShader() {
        if (resolveShader != null) {
            resetShader = true;
            resolveShader = null;
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
            if (GlovesLayer.areGloves(item)) {
                event.setCanceled(true);
                renderHand(Hand.MAIN_HAND, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                        event.getPartialTicks(), event.getInterpolatedPitch(), player);
            }
        }
    }
    
    private void renderHand(Hand hand, MatrixStack matrixStack, IRenderTypeBuffer buffers, int light,
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
                matrixStack, buffers, light, 
                partialTick, interpolatedPitch, 
                swingProgress, equipProgress, entity.getItemInHand(hand))) {
            matrixStack.pushPose();
            ClientReflection.renderPlayerArm(matrixStack, buffers, light, equipProgress, 
                    swingProgress, MCUtil.getHandSide(player, hand), renderer);
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
        if (event.getGui() instanceof DeathScreen) {
            ITextComponent title = event.getGui().getTitle();
            if (title instanceof TranslationTextComponent && ((TranslationTextComponent) title).getKey().endsWith(".hardcore")) {
                return;
            }
            renderToBeContinuedArrow(event.getMatrixStack(), event.getGui(), event.getGui().width, event.getGui().height, event.getRenderPartialTicks());
        }

        else if (event.getGui() instanceof IngameMenuScreen && ClientReflection.showsPauseMenu((IngameMenuScreen) event.getGui())) {
            float alpha = JojoModConfig.CLIENT.standStatsTranslucency.get().floatValue();
            if (alpha <= 0) return;
            int xButtonsRightEdge = event.getGui().width / 2 + 102;
            int windowWidth = event.getGui().width;
            int windowHeight = event.getGui().height;

            if (windowWidth - xButtonsRightEdge >= 167 && windowHeight > 204) {
                StandStatsRenderer.renderStandStats(event.getMatrixStack(), mc, windowWidth - 160, windowHeight - 160, windowWidth, windowHeight,
                        pauseMenuScreenTick, event.getRenderPartialTicks(), alpha, event.getMouseX(), event.getMouseY(), windowWidth - xButtonsRightEdge - 14);
            }
        }
    }

    private void renderToBeContinuedArrow(MatrixStack matrixStack, AbstractGui ui, int screenWidth, int screenHeight, float partialTick) {
        int x = screenWidth - 5 - (int) ((screenWidth - 10) * Math.min(deathScreenTick + partialTick, 20F) / 20F);
        int y = screenHeight - 29;
        mc.textureManager.bind(ClientUtil.ADDITIONAL_UI);
        ui.blit(matrixStack, x, y, 0, 231, 130, 25);
        AbstractGui.drawCenteredString(matrixStack, mc.font, new TranslationTextComponent("jojo.to_be_continued"), x + 61, y + 8, 0x525544);
    }

    @SubscribeEvent
    public void onScreenOpened(GuiOpenEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            String splash = CustomResources.getModSplashes().overrideSplash();
            if (splash != null) {
                ClientReflection.setSplash((MainMenuScreen) event.getGui(), splash);
            }
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
                            mc.player.hasEffect(ModEffects.RESOLVE.get()), mc.player.isShiftKeyDown()));
        }
        
        boolean paused = mc.isPaused();
        if (prevPause && !paused) {
            pausePartialTick = mc.getFrameTime();
        }
        prevPause = paused;
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
