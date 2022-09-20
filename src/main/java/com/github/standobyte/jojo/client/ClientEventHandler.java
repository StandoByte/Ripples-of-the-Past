package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.sound.StandOstSound;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.packets.fromserver.EntityTimeResumeSoundPacket.SoundPos;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.github.standobyte.jojo.util.utils.TimeUtil;
import com.google.common.base.MoreObjects;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.OutlineLayerBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
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
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static ClientEventHandler instance = null;

    private Minecraft mc;

    private Timer clientTimer;
    private boolean isTimeStopped = false;
    private boolean canSeeInStoppedTime = true;
    private boolean canMoveInStoppedTime = true;
    private float partialTickStoppedAt;
    private static final ResourceLocation SHADER_TIME_STOP = new ResourceLocation("shaders/post/desaturate.json");
    private final List<SoundPos> timeResumeSounds = new ArrayList<>();

    private Random random = new Random();
    private ResourceLocation resolveShader = null;
    private static final ResourceLocation DUMMY = new ResourceLocation("dummy", "dummy");
    private StandOstSound ost;

    private boolean resetShader;
    private double zoomModifier;
    public boolean isZooming;

    private int deathScreenTick;

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

        // FIXME (!) reset the glowing flag after outline is no longer needed
        int outlineColor = outlineColor(entity);
        if (outlineColor > 0) {
            entity.setGlowing(true);
            if (event.getBuffers() instanceof OutlineLayerBuffer) {
                ((OutlineLayerBuffer) event.getBuffers()).setColor(outlineColor >> 16 & 255, outlineColor >> 8 & 255, outlineColor & 255, 255);
            }
        }

        INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
            if (power.getHeldAction(true) == ModActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                event.getMatrixStack().mulPose(Vector3f.YP.rotation((power.getHeldActionTicks() + event.getPartialRenderTick()) * 2F % 360F));
            }
            if (power.isActionOnCooldown(ModActions.HAMON_ZOOM_PUNCH.get())) {
                M model = event.getRenderer().getModel();
                if (model instanceof BipedModel) {
                    ModelRenderer arm = entity.getMainArm() == HandSide.LEFT ? ((BipedModel<?>) model).leftArm : ((BipedModel<?>) model).rightArm;
                    arm.visible = false;
                    if (model instanceof PlayerModel) {
                        arm = entity.getMainArm() == HandSide.LEFT ? ((PlayerModel<?>) model).leftArm : ((PlayerModel<?>) model).rightArm;
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
                if (power.getHeldAction(true) == ModActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                    event.getMatrixStack().mulPose(Vector3f.YP.rotation((power.getHeldActionTicks() + event.getPartialTicks()) * -2F % 360F));
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(RenderTickEvent event) {
        if (mc.level != null && mc.player.isAlive() && isTimeStopped(mc.player.blockPosition()) && event.phase == TickEvent.Phase.START) {
            if (!canSeeInStoppedTime) {
                clientTimer.partialTick = 0.0F;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (mc.level != null) {
            if (event.phase == TickEvent.Phase.START) {
                ActionsOverlayGui.getInstance().tick();
            }
            boolean timeWasStopped = isTimeStopped;
            isTimeStopped = isTimeStopped(mc.player.blockPosition());
            if (timeWasStopped && !isTimeStopped) {
                onTimeResume();
            }

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
                break;
            }
        }

        if (mc.gameRenderer.currentEffect() == null) {
            ResourceLocation shader = getCurrentShader();
            if (shader != null && shader != DUMMY) {
                mc.gameRenderer.loadEffect(shader);
            }
        }

        if (mc.screen instanceof DeathScreen) {
            deathScreenTick++;
        }
        else {
            deathScreenTick = 0;
        }
    }

    public ResourceLocation getCurrentShader() {
        if (mc.level == null) {
            return null;
        }
        if (isTimeStopped && canSeeInStoppedTime) {
            return SHADER_TIME_STOP;
        }
        if (JojoModConfig.CLIENT.resolveShaders.get() && resolveShader != null) {
            return resolveShader;
        }
        return null;
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
                    SoundEvent ostSound = stand.getType().getOst(level);
                    if (ostSound != null) {
                        ost = new StandOstSound(ostSound, mc);
                        mc.getSoundManager().play(ost);
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
    
    public void addTimeResumeSound(SoundPos soundPos) {
        if (isTimeStopped) {
            timeResumeSounds.add(soundPos);
        }
    }
    
    private void onTimeResume() {
        timeResumeSounds.forEach(soundPos -> mc.level.playLocalSound(soundPos.pos.x, soundPos.pos.y, soundPos.pos.z, 
                soundPos.sound, SoundCategory.PLAYERS, 1, 1, false));
        timeResumeSounds.clear();
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
                if (power.getType() == ModNonStandPowers.VAMPIRISM.get()) {
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHand(RenderHandEvent event) {
        if (event.getHand() == Hand.MAIN_HAND) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                INonStandPower.getNonStandPowerOptional(livingEntity).ifPresent(power -> {
                    if (power.isActionOnCooldown(ModActions.HAMON_ZOOM_PUNCH.get())) {
                        event.setCanceled(true);
                    }
                    else {
                        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
                        if (hud.getSelectedAction(ActionType.ATTACK) == ModActions.JONATHAN_OVERDRIVE_BARRAGE.get()
                                && livingEntity.getMainHandItem().isEmpty() && livingEntity.getOffhandItem().isEmpty()) {
                            FirstPersonRenderer renderer = mc.getItemInHandRenderer();
                            ClientPlayerEntity player = mc.player;
                            Hand swingingArm = MoreObjects.firstNonNull(player.swingingArm, Hand.MAIN_HAND);
                            float f6 = swingingArm == Hand.OFF_HAND ? player.getAttackAnim(event.getPartialTicks()) : 0.0F;
                            float f7 = 1.0F - MathHelper.lerp(event.getPartialTicks(), ClientReflection.getOffHandHeightPrev(renderer), ClientReflection.getOffHandHeight(renderer));
                            MatrixStack matrixStack = event.getMatrixStack();
                            matrixStack.pushPose();
                            ClientReflection.renderPlayerArm(matrixStack, event.getBuffers(), event.getLight(), f7, f6, player.getMainArm().getOpposite(), renderer);
                            matrixStack.popPose();
                            // i've won... but at what cost?
                        }
                    }
                });
            }
        }
    }



    @SubscribeEvent
    public void afterScreenRender(DrawScreenEvent.Post event) {
        if (event.getGui() instanceof DeathScreen) {
            ITextComponent title = event.getGui().getTitle();
            if (title instanceof TranslationTextComponent && ((TranslationTextComponent) title).getKey().endsWith(".hardcore")) {
                return;
            }
            int x = event.getGui().width - 5 - 
                    (int) ((event.getGui().width - 10) * Math.min(deathScreenTick + event.getRenderPartialTicks(), 20F) / 20F);
            int y = event.getGui().height - 29;
            mc.textureManager.bind(ClientUtil.ADDITIONAL_UI);
            event.getGui().blit(event.getMatrixStack(), x, y, 0, 231, 130, 25);
            AbstractGui.drawCenteredString(event.getMatrixStack(), mc.font, new TranslationTextComponent("jojo.to_be_continued"), x + 61, y + 8, 0x525544);
        }
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
        if (ActionsOverlayGui.getInstance().getSelectedAction(ActionType.ABILITY) == ModActions.CRAZY_DIAMOND_RESTORE_TERRAIN.get()) {
            MatrixStack matrixStack = event.getMatrixStack();
            IStandPower stand = ActionsOverlayGui.getInstance().standUiMode.getPower();
            Entity entity = CrazyDiamondRestoreTerrain.restorationCenterEntity(mc.player, stand);
            Vector3i pos = CrazyDiamondRestoreTerrain.eyePos(entity);
            Vector3d lookVec = entity.getLookAngle();
            Vector3d eyePosD = entity.getEyePosition(1.0F);
            TranslucentBlockRenderHelper.renderCDRestorationTranslucentBlocks(matrixStack, mc, 
                    CrazyDiamondRestoreTerrain.getBlocksInRange(mc.level, mc.player, pos, 32, 
                            block -> CrazyDiamondRestoreTerrain.blockCanBePlaced(mc.level, block.pos, block.state)),
                    block -> CrazyDiamondRestoreTerrain.blockPosSelectedForRestoration(block, entity, lookVec, eyePosD, pos, mc.player.hasEffect(ModEffects.RESOLVE.get())));
        }
    }

    @SubscribeEvent
    public void addTooltipLines(ItemTooltipEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(player.level, event.getItemStack()).ifPresent(pos -> {
                if (IStandPower.getStandPowerOptional(player).map(power -> power.getType() == ModStandTypes.CRAZY_DIAMOND.get()).orElse(false)) {
                    event.getToolTip().add(new TranslationTextComponent("jojo.crazy_diamond.block_checkpoint.tooltip", 
                            pos.getX(), pos.getY(), pos.getZ()).withStyle(TextFormatting.RED));
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
}
