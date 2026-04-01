package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.EXPERIENCE;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.living.LivingWallClimbing;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil.PosOnScreen;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.particle.custom.FirstPersonHamonAura;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.polaroid.PhotosCache;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.client.render.armor.model.BladeHatArmorModel;
import com.github.standobyte.jojo.client.render.block.overlay.TranslucentBlockRenderHelper;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.GlovesLayer;
import com.github.standobyte.jojo.client.render.item.InventoryItemHighlight;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.StandOstSound;
import com.github.standobyte.jojo.client.sound.barrage.StandCrySoundHandler;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.ClientModSettingsScreen;
import com.github.standobyte.jojo.client.ui.screen.IJojoScreen;
import com.github.standobyte.jojo.client.ui.screen.controls.HudLayoutEditingScreen;
import com.github.standobyte.jojo.client.ui.screen.controls.vanilla.CategoryWithButtonsEntry;
import com.github.standobyte.jojo.client.ui.screen.controls.vanilla.ControlSettingToggleButton;
import com.github.standobyte.jojo.client.ui.screen.controls.vanilla.HoldToggleKeyEntry;
import com.github.standobyte.jojo.client.ui.screen.widgets.HeightScaledSlider;
import com.github.standobyte.jojo.client.ui.screen.widgets.ImageMutableButton;
import com.github.standobyte.jojo.client.ui.screen.widgets.ImageVanillaButton;
import com.github.standobyte.jojo.client.ui.standstats.StandStatsRenderer;
import com.github.standobyte.jojo.client.ui.text.JojoTextComponentWrapper;
import com.github.standobyte.jojo.client.ui.toasts.MetEntityTypeToast;
import com.github.standobyte.jojo.client.ui.tooltip.CustomTooltipRender;
import com.github.standobyte.jojo.client.ui.tooltip.ITooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.IconTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.MultiTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.TextTooltipLine;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.OilItem;
import com.github.standobyte.jojo.mechanics.speechbubble.clowning.WorldTypingPlayers;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClAngeloRockButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClMetEntityTypePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ServerIdPacket;
import com.github.standobyte.jojo.potion.BleedingEffect;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandArrowHandler;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.OptionalFloat;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class ClientEventHandler {
    private static ClientEventHandler instance = null;

    private final Minecraft mc;

    private List<ITextComponent> multiLineOverlayMessage = new ArrayList<>();
    private int multiLineOverlayMessageTime;
    private boolean multiLineOverlayFadeOut;
    private boolean multiLineOverlayAnimateMessageColor;
    
    private float pausePartialTick;
    private boolean prevPause = false;

    private Timer clientTimer;
    
    private StandOstSound ost;
    
    private double zoomModifier;
    public boolean isZooming;

    public int tickCount = 0;
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
        
        ClientTimeStopHandler ts = ClientTimeStopHandler.getInstance();
        if (ts != null) {
            if (ts.shouldCancelSound(sound)) {
                event.setResultSound(null);
            }
        }
        
        if (mc.player != null && sound instanceof LocatableSound && sound.getAttenuation() == AttenuationType.LINEAR) {
            mc.player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(player -> {
                if (player.isDyingBody() && !mc.player.isSpectator() && !mc.player.isDeadOrDying()) {
                    float progress = player.getDyingBodyProgress();
                    if (progress > 0.8F) {
                        float volumeMult;
                        if (progress < 0.84F) {
                            volumeMult = 20 * (1 - progress) - 3;
                        }
                        else {
                            volumeMult = 1.25F * (1 - progress);
                        }
                        ((LocatableSound) sound).volume *= volumeMult;
                    }
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving(RenderLivingEvent.Pre<T, M> event) {
        LivingEntity entity = event.getEntity();
        
        if (entity.hasEffect(ModStatusEffects.FULL_INVISIBILITY.get())) {
            event.setCanceled(true);
            return;
        }
        
//        float partialTick = event.getPartialRenderTick();
//        float changePartialTick = ClientTimeStopHandler.getInstance().getConstantEntityPartialTick(entity, partialTick);
//        if (partialTick != changePartialTick) {
//            event.setCanceled(true);
//            event.getRenderer().render((T) entity, MathHelper.lerp(changePartialTick, entity.yRotO, entity.yRot), 
//                    changePartialTick, event.getMatrixStack(), event.getBuffers(), event.getLight());
//            return;
//        }
        
        M model = event.getRenderer().getModel();
        if (model instanceof BipedModel) {
            BipedModel<?> bipedModel = (BipedModel<?>) model;
            correctHeldItemPose(entity, bipedModel, HandSide.RIGHT);
            correctHeldItemPose(entity, bipedModel, HandSide.LEFT);
        }
        
        if (model instanceof PlayerModel) {
            INonStandPower.getNonStandPowerOptional(event.getEntity()).map(power -> {
                if (power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).map(
                        pillarmanData -> pillarmanData.isStoneFormEnabled()).orElse(false)) {
                    PlayerModel<?> playerModel = (PlayerModel<?>) model;
                    playerModel.leftSleeve.visible = false;
                    playerModel.rightSleeve.visible = false;
                    playerModel.leftPants.visible = false;
                    playerModel.rightPants.visible = false;
                    playerModel.hat.visible = false;
                    playerModel.jacket.visible = false;
                    return true;
                }
                return false;
            });
        }
        // FIXME (vampire\curing) shake vampire while curing
        // yRot += (float) (Math.cos((double)entity.tickCount * 3.25) * Math.PI * 0.4);
    }
    
    private void correctHeldItemPose(LivingEntity entity, BipedModel<?> model, HandSide handSide) {
        Hand hand = entity.getMainArm() == handSide ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack item = entity.getItemInHand(hand);
        if (!item.isEmpty() && GlovesLayer.areGloves(item)) {
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
        MatrixStack matrixStack = event.getMatrixStack();
        if (entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                if (power.getHeldAction(true) == ModHamonActions.ZEPPELI_TORNADO_OVERDRIVE.get()) {
                    matrixStack.mulPose(Vector3f.YP.rotation((power.getHeldActionTicks() + event.getPartialTicks()) * -2F % 360F));
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        PlayerEntity entity = event.getPlayer();
        PlayerRenderer renderer = event.getRenderer();
        if (mc.player != entity) {
            float partialTick = event.getPartialRenderTick();
            event.getPlayer().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.limitPlayerHeadRot();
            });
            ContinuousActionInstance.getCurrentAction(event.getPlayer()).ifPresent(action -> action.onPreRender(partialTick));
        }
        BladeHatArmorModel.modifyOuterLayer(renderer.getModel(), entity);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(RenderTickEvent event) {
        if (mc.level != null) {
            float partialTick = ClientUtil.getPartialTick();
            switch (event.phase) {
            case START:
                ClientUtil.canSeeStands = StandUtil.playerCanSeeStands(mc.player);
                ClientUtil.canHearStands = /*StandUtil.playerCanHearStands(mc.player)*/ ClientUtil.canSeeStands;
                
                if (mc.player.isAlive()) {
                    ClientTimeStopHandler timeStopHandler = ClientTimeStopHandler.getInstance();
                    if (timeStopHandler != null) {
                        timeStopHandler.setConstantPartialTick(clientTimer);
                    }
                    
                    mc.player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.limitPlayerHeadRot();
                    });
                    mc.player.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.applyLockedRotation();
                    });
                    ContinuousActionInstance.getCurrentAction(mc.player).ifPresent(action -> action.onPreRender(partialTick));
                }
                
                PlayerAnimationHandler.getPlayerAnimator().onRenderFrameStart(partialTick);
                
                if (mc.player.isSpectator() && mc.options.keySpectatorOutlines.isDown()) {
                    JojoModUtil.getActualGameModeWhilePossessing(mc.player).ifPresent(actualGameMode -> {
                        if (actualGameMode != GameType.SPECTATOR) {
                            mc.options.keySpectatorOutlines.setDown(false);
                        }
                    });
                }
                break;
            case END:
                PlayerAnimationHandler.getPlayerAnimator().onRenderFrameEnd(partialTick);
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(ClientTickEvent event) {
        if (mc.level != null) {
            switch (event.phase) {
            case START:
                boolean yap = false;
                WorldTypingPlayers typingPlayers = WorldTypingPlayers.get(mc.level);
                if (typingPlayers != null && !typingPlayers.players.isEmpty()) {
                    yap = true;
                }
                
                ActionsOverlayGui.getInstance().tick();
                
                if (!mc.isPaused()) {
                    ClientTicking.tickAll();
                    ClientTickingSoundsHelper.tickBossMusic();
                    
                    StandCrySoundHandler.tickAll();
                    
                    mc.level.getCapability(WorldUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.tick();
                    });
                    
                    mc.level.entitiesForRendering().forEach(entity -> {
                        entity.getCapability(EntityUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                        entity.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                        entity.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> cap.tick());
                    });
                    
                    FirstPersonHamonAura.getInstance().tick();
                    InventoryItemHighlight.tick();
                    tickAfterChat();
                }
                
                if (mc.player != null && mc.player.tickCount == 200) {
                    extraModsReminder();
                }
                
                tickResolveEffect();
                PhotosCache.tick();
                break;
            case END:
                ShaderEffectApplier.getInstance().shaderTick();
                tickDodgeCameraRoll();
                
                // FIXME make stand actions clickable when player hands are busy
                if (mc.level != null && mc.player != null && (
                        mc.player.getVehicle() != null && mc.player.getVehicle().getType() == ModEntityTypes.LEAVES_GLIDER.get())) {
                    ClientReflection.setHandsBusy(mc.player, true);
                }
                
                break;
            }
        }
        
        if (event.phase == TickEvent.Phase.START) {
            ClientTimeStopHandler ts = ClientTimeStopHandler.getInstance();
            if (ts != null) {
                ts.tickPauseIrrelevant();
            }
            
            ++tickCount;
            deathScreenTick = mc.screen instanceof DeathScreen ? deathScreenTick + 1 : 0;
            standStatsTick = mc.screen instanceof IngameMenuScreen && doStandStatsRender(mc.screen) ? standStatsTick + 1 : 0;
            
            NetworkUtil.blockPacketsToServer = mc.player != null && mc.player.hasEffect(ModStatusEffects.SENSORY_OVERLOAD.get());
            
            if (!mc.isPaused()) {
                if (multiLineOverlayMessageTime > 0) multiLineOverlayMessageTime--;
                if (multiLineOverlayMessageTime == 0) {
                    multiLineOverlayMessage.clear();
                }
            }
            
            aimedBlockMessage();
        }
    }
    
    private void aimedBlockMessage() {
        if (mc.level != null && mc.hitResult != null && mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockRayTraceResult) mc.hitResult).getBlockPos();
            BlockState blockState = mc.level.getBlockState(blockPos);
            if (blockState.getBlock() == ModBlocks.MR_PRESIDENT_EXIT.get()) {
                setMultiLineOverlayMessage(Util.make(new ArrayList<>(), list -> list.add(new TranslationTextComponent("hint.mr_president_exit"))), false);
                multiLineOverlayMessageTime = 1;
                multiLineOverlayFadeOut = false;
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelable() && NetworkUtil.blockPacketsToServer) {
            event.setCanceled(true);
        }
    }
    
    private static final List<String[]> MOD_LINKS = Util.make(new ArrayList<>(), list -> {
        list.add(new String[] { 
                "playeranimator", 
                "playerAnimator ", 
                "forge-0.4.0+1.16.5 ", 
                "https://www.curseforge.com/minecraft/mc-mods/playeranimator/files/4111516" });
        list.add(new String[] { 
                "bendylib", 
                "bendy-lib ", 
                "forge-1.2.1 ", 
                "https://www.curseforge.com/minecraft/mc-mods/bendy-lib/files/3930015" });
    });
    public static boolean seenExtraModsReminder = false;
    private void extraModsReminder() {
        if (!seenExtraModsReminder) {
            seenExtraModsReminder = true;
            
            List<String[]> missingMods = MOD_LINKS.stream()
                    .filter(modEntry -> !ModInteractionUtil.isModLoaded(modEntry[0]))
                    .collect(Collectors.toList());
            if (!missingMods.isEmpty()) {
                NewChatGui chat = mc.gui.getChat();
                chat.addMessage(new TranslationTextComponent("jojo.player_anim_reminder.1")
                        .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                chat.addMessage(new TranslationTextComponent("jojo.player_anim_reminder.2")
                        .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                for (String[] modEntry : missingMods) {
                    IFormattableTextComponent line = new StringTextComponent("  ").withStyle(TextFormatting.ITALIC)
                            .append(modEntry[1])
                            .append(modEntry[2])
                            .append(new TranslationTextComponent("jojo.player_anim_reminder.cf_link")
                                    .withStyle(TextFormatting.GREEN)
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, modEntry[3]))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.link.open")))));
                    chat.addMessage(line);
                }
                chat.addMessage(new TranslationTextComponent("jojo.player_anim_reminder.3")
                        .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                chat.addMessage(new TranslationTextComponent("jojo.player_anim_reminder.4")
                        .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
            }
        }
    }
    
    public static void onMouseTargetChanged(RayTraceResult newTarget) {
        Minecraft mc = Minecraft.getInstance();
        if (newTarget.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) newTarget).getEntity();
            
            // Hamon learning player interaction hints
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
            
            // learning new lifeforms for Gold Experience
            mc.player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                EntitySubtype.getMatchingSubtypes(entity)
                .forEach(subType -> metNewMobSubtype(cap, subType, entity, mc.player));
            });
        }
    }
    
    private static void metNewMobSubtype(PlayerUtilCap metMobsData, EntitySubtype<?> subtype, Entity entity, PlayerEntity player) {
        if (metMobsData.addMetEntityType(subtype)) {
            PacketManager.sendToServer(new ClMetEntityTypePacket(entity.getId()));

            if (GoldExperienceChooseLifeform.isValidLifeform(subtype, entity.level)) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    if (ModStandsInit.GOLD_EXPERIENCE_CHOOSE_LIFEFORM.get().isUnlocked(power)) {
                        Minecraft mc = Minecraft.getInstance();
                        mc.getSoundManager().play(new SimpleSound(SoundEvents.UI_BUTTON_CLICK, 
                                SoundCategory.MASTER, 0.5F, 2.0F, 
                                entity.getX(), entity.getY(0.5), entity.getZ()));
                        MetEntityTypeToast.addOrUpdate(mc.getToasts(), entity.getType());
                    }
                });
            }

            if (entity.getType() == ModEntityTypes.COCO_JUMBO_TURTLE.get()
                    && !((IMobStandUser) entity).getStandPower().hasPower()) {
                player.displayClientMessage(new TranslationTextComponent("coco_jumbo.stand_arrow_hint").withStyle(TextFormatting.ITALIC), false);
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
            
            if (mc.player.getEffect(ModStatusEffects.RESOLVE.get()).getDuration() == 40) {
                fadeAwayOst(100);
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
                    OstSoundList ostList = stand.getType().getOst(mc.player);
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
    
    
    private HandSide dodgeCameraRollSide;
    private float dodgeCameraRollMaxAngle;
    private float dodgeCameraRollLength;
    private int dodgeCameraRollTimer;
    public void setDodgeCameraRoll(HandSide side, float maxAngle, float length) {
        this.dodgeCameraRollSide = side;
        this.dodgeCameraRollMaxAngle = maxAngle;
        this.dodgeCameraRollLength = length;
        this.dodgeCameraRollTimer = 0;
    }
    
    private void tickDodgeCameraRoll() {
        if (dodgeCameraRollSide != null && ++dodgeCameraRollTimer >= dodgeCameraRollLength) {
            this.dodgeCameraRollSide = null;
            this.dodgeCameraRollMaxAngle = 0;
            this.dodgeCameraRollLength = 0;
            this.dodgeCameraRollTimer = 0;
        }
    }
    
    private OptionalFloat calcDodgeCameraRoll(float partialTick) {
        if (dodgeCameraRollSide == null) return OptionalFloat.empty();
        
        float tick = dodgeCameraRollTimer + partialTick;
        if (tick >= dodgeCameraRollLength) return OptionalFloat.empty();
        
        float angle = tick / dodgeCameraRollLength;
        if (angle < 0.5f) angle = 2 * angle;
        else              angle = (1 - angle) * 2;
        angle *= angle;
        angle *= dodgeCameraRollMaxAngle;
        if (dodgeCameraRollSide == HandSide.RIGHT) {
            angle *= -1;
        }
        return OptionalFloat.of(angle);
    }



    @SubscribeEvent(priority = EventPriority.LOW)
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
    public void cameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (PolaroidHelper.pictureCameraSetup(event)) {
            return;
        }
        if (mc.options.getCameraType().isFirstPerson()) {
            OptionalFloat cameraRoll = calcDodgeCameraRoll((float) event.getRenderPartialTicks());
            cameraRoll.ifPresent(roll -> {
                event.setRoll(roll);
                
                ActiveRenderInfo camera = event.getInfo();
                Vector3d look = new Vector3d(camera.getLookVector());
                look = look.scale(1.25 * Math.abs(roll) / dodgeCameraRollMaxAngle);
                look = look.yRot(dodgeCameraRollSide == HandSide.LEFT ? (float)-Math.PI / 2 : (float)Math.PI / 2);
                camera.setPosition(camera.getPosition().add(look));
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void photoFOV(EntityViewRenderEvent.FOVModifier event) {
        if (PolaroidHelper.isTakingPhoto()) {
            event.setFOV(70);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void disableFoodBar(RenderGameOverlayEvent.Pre event) {
        boolean isVampirismModVampire = OptionalDependencyHelper.vampirism().isEntityVampire(mc.player);
        if (event.getType() == FOOD && !isVampirismModVampire || event.getType() == AIR) {
            INonStandPower.getNonStandPowerOptional(mc.player).ifPresent(power -> {
                if (power.getType() == ModPowers.VAMPIRISM.get() || power.getType() == ModPowers.ZOMBIE.get() 
                        || power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).map(pillarMan -> pillarMan.getEvolutionStage() > 1).orElse(false)) {
                    event.setCanceled(true);
                }
            });
        }
        if (JojoModUtil.isDyingBody(mc.player) && (
                event.getType() == HEALTH || 
                event.getType() == FOOD || 
                event.getType() == AIR)) {
            event.setCanceled(true);
        }
        
        if (event.getType() == EXPERIENCE && mc.gameMode.hasExperience()) {
            if (mc.player.hasEffect(ModStatusEffects.STAND_VIRUS.get())) {
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
    }
    
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void renderUI(RenderGameOverlayEvent.Pre event) {
        switch (event.getType()) {
        case HELMET:
            renderLosingVision(event.getMatrixStack(), event.getPartialTicks());
            break;
        case ALL:
            hudRenderEntityGEDetectorData(event.getMatrixStack());
            break;
        default:
            break;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderHpWithBleeding(RenderGameOverlayEvent.Pre event) {
        if (ModInteractionUtil.isModLoaded("healthoverlay")) return;
        
        switch (event.getType()) {
        case HEALTH:
        case HEALTHMOUNT:
            Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
            if (cameraEntity instanceof PlayerEntity) {
                boolean mount = event.getType() == RenderGameOverlayEvent.ElementType.HEALTHMOUNT;
                LivingEntity entity = (LivingEntity) cameraEntity;
                if (mount) {
                    Entity mountEntity = entity.getVehicle();
                    entity = mountEntity instanceof LivingEntity ? (LivingEntity) mountEntity : null;
                }
                if (entity != null && entity.hasEffect(ModStatusEffects.BLEEDING.get())) {
                    IngameGui gui = mc.gui;
                    int width = mc.getWindow().getGuiScaledWidth();
                    int height = mc.getWindow().getGuiScaledHeight();
                    
                    if (mount) {
                        renderMountHealthWithBleeding(entity, event.getMatrixStack(), gui, event, width, height);
                    }
                    else {
                        renderHealthWithBleeding(entity, event.getMatrixStack(), gui, event, width, height);
                    }
                    event.setCanceled(true);
                }
            }
            break;
        default:
            break;
        }
    }

    private Random rand = new Random();
    private int entityHealth;
    private int lastEntityHealth;
    private long lastSystemTime;
    private long healthUpdateCounter;
    public void renderHealthWithBleeding(LivingEntity entity, MatrixStack matrixStack, IngameGui gui, 
            RenderGameOverlayEvent event, int width, int height) {
        mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
        mc.getProfiler().push("health");
        RenderSystem.enableBlend();

        int ticks = gui.getGuiTicks();
        int health = MathHelper.ceil(entity.getHealth());
        boolean highlight = this.healthUpdateCounter > (long)ticks && 
                (this.healthUpdateCounter - (long)ticks) / 3L %2L == 1L;

        if (health < this.entityHealth && entity.invulnerableTime > 0)
        {
            this.lastSystemTime = Util.getMillis();
            this.healthUpdateCounter = (long)(ticks + 20);
        }
        else if (health > this.entityHealth && entity.invulnerableTime > 0)
        {
            this.lastSystemTime = Util.getMillis();
            this.healthUpdateCounter = (long)(ticks + 10);
        }

        if (Util.getMillis() - this.lastSystemTime > 1000L)
        {
            this.entityHealth = health;
            this.lastEntityHealth = health;
            this.lastSystemTime = Util.getMillis();
        }

        this.entityHealth = health;
        int healthLast = this.lastEntityHealth;

        ModifiableAttributeInstance attrMaxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        float healthWithoutBleedMax = BleedingEffect.getMaxHealthWithoutBleeding(entity); // !
        float healthMax = (float)attrMaxHealth.getValue();
        float absorb = MathHelper.ceil(entity.getAbsorptionAmount());

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.rand.setSeed((long)(ticks * 312871));

        int left = width / 2 - 91;
        int top = height - ForgeIngameGui.left_height;
        ForgeIngameGui.left_height += (healthRows * rowHeight);
        if (rowHeight != 10) ForgeIngameGui.left_height += 10 - rowHeight;

        int regen = -1;
        if (entity.hasEffect(Effects.REGENERATION))
        {
            regen = ticks % 25;
        }

        final int TOP =  9 * (mc.level.getLevelData().isHardcore() ? 5 : 0);
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (entity.hasEffect(Effects.POISON))      MARGIN += 36;
        else if (entity.hasEffect(Effects.WITHER)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthWithoutBleedMax + absorb) / 2.0F) - 1; i >= 0; --i) // !
        {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += this.rand.nextInt(2);
            if (i == regen) y -= 2;

            gui.blit(matrixStack, x, y, BACKGROUND, TOP, 9, 9);

            if (highlight)
            {
                if (i * 2 + 1 < healthLast)
                    gui.blit(matrixStack, x, y, MARGIN + 54, TOP, 9, 9); //6
                else if (i * 2 + 1 == healthLast)
                    gui.blit(matrixStack, x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F)
            {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                {
                    gui.blit(matrixStack, x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                }
                else
                {
                    gui.blit(matrixStack, x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            }
            else
            {
                if (i * 2 + 1 < health)
                    gui.blit(matrixStack, x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    gui.blit(matrixStack, x, y, MARGIN + 45, TOP, 9, 9); //5
                
                // !
                if (i * 2 + 1 >= healthMax) {
                    mc.getTextureManager().bind(ClientUtil.ADDITIONAL_UI);
                    gui.blit(matrixStack, x, y, 64, 0, 9, 9);
                    mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
                }
            }
        }

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }
    
    public void renderMountHealthWithBleeding(LivingEntity entity, MatrixStack matrixStack, IngameGui gui, 
            RenderGameOverlayEvent event, int width, int height) {
        mc.textureManager.bind(AbstractGui.GUI_ICONS_LOCATION);

        boolean unused = false;
        int left_align = width / 2 + 91;

        mc.getProfiler().popPush("mountHealth");
        RenderSystem.enableBlend();
        int health = (int)Math.ceil((double)entity.getHealth());
        float healthWithoutBleedMax = BleedingEffect.getMaxHealthWithoutBleeding(entity); // !
        float healthMax = entity.getMaxHealth();
        int hearts = (int)(healthWithoutBleedMax + 0.5F) / 2; // !

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20)
        {
            int top = height - ForgeIngameGui.right_height;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i)
            {
                int x = left_align - i * 8 - 9;
                gui.blit(matrixStack, x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    gui.blit(matrixStack, x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    gui.blit(matrixStack, x, top, HALF, 9, 9, 9);
                
                // !
                if (i * 2 + 1 + heart >= healthMax) {
                    mc.getTextureManager().bind(ClientUtil.ADDITIONAL_UI);
                    gui.blit(matrixStack, x, top, 73, 0, 9, 9);
                    mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
                }
            }

            ForgeIngameGui.right_height += 10;
        }
        RenderSystem.disableBlend();
    }
    
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    @SuppressWarnings("deprecation")
    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderCarriedCocoJumboSlot(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR && !mc.player.isSpectator()) {
            for (Entity passenger : mc.player.getPassengers()) {
                if (CocoJumboTurtleEntity.isCarriedTurtle(passenger, mc.player)) {
                    ItemStack turtleItemIcon = new ItemStack(ModItems.METEORIC_SCRAP.get());
                    turtleItemIcon.getOrCreateTag().put("Icon", IntNBT.valueOf(22));
                    
                    MatrixStack matrixStack = event.getMatrixStack();
                    HandSide offHand = mc.player.getMainArm().getOpposite();
                    int screenHeight = event.getWindow().getGuiScaledHeight();
                    int halfWidth = event.getWindow().getGuiScaledWidth() / 2;
                    IngameGui gui = mc.gui;
                    int blitOffs = gui.getBlitOffset();
                    
                    mc.getTextureManager().bind(WIDGETS_LOCATION);
                    gui.setBlitOffset(-90);
                    if (offHand == HandSide.LEFT) {
                        gui.blit(matrixStack, halfWidth - 91 - 29, screenHeight - 23, 24, 22, 29, 24);
                    } else {
                        gui.blit(matrixStack, halfWidth + 91,      screenHeight - 23, 53, 22, 29, 24);
                    }
                    
                    gui.setBlitOffset(blitOffs);
                    RenderSystem.enableRescaleNormal();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    
                    int itemX = offHand == HandSide.LEFT ? halfWidth - 91 - 26 : halfWidth + 91 + 10;
                    int itemY = screenHeight - 16 - 3;
                    mc.getItemRenderer().renderAndDecorateItem(mc.player, turtleItemIcon, itemX, itemY);
//                    mc.getItemRenderer().renderGuiItemDecorations(mc.font, turtleItemIcon, itemX, itemY);
                    
                    break;
                }
            }
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
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderOverlayMessage(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.SUBTITLES) {
            MainWindow window = mc.getWindow();
            renderMultiLineMessage(event.getMatrixStack(), event.getPartialTicks(), 
                    window.getGuiScaledWidth(), window.getGuiScaledHeight());
        }
    }
    
    public void setMultiLineOverlayMessage(Collection<ITextComponent> message, boolean animateColor) {
        multiLineOverlayMessage.clear();
        multiLineOverlayMessage.addAll(message);
        multiLineOverlayMessageTime = 60;
        multiLineOverlayAnimateMessageColor = animateColor;
    }
    
    @SuppressWarnings("deprecation")
    private void renderMultiLineMessage(MatrixStack matrixStack, float partialTick, int width, int height) {
        if (!mc.options.hideGui) {
            IngameGui vanillaGui = mc.gui;
            if (vanillaGui.overlayMessageTime > 0 || multiLineOverlayMessage.isEmpty()) {
                this.multiLineOverlayMessageTime = 0;
                return;
            }
            mc.getProfiler().push("overlayMessage");
            int opacity;
            float hue;
            if (multiLineOverlayFadeOut) {
                hue = (float)multiLineOverlayMessageTime - partialTick;
                opacity = (int)(hue * 255.0F / 20.0F);
                if (opacity > 255) opacity = 255;
            }
            else {
                opacity = 255;
                hue = 0;
            }

            if (opacity > 8) {
                RenderSystem.pushMatrix();
                RenderSystem.translatef((float)(width / 2), (float)(height - 68), 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                FontRenderer font = vanillaGui.getFont();
                int color = (multiLineOverlayAnimateMessageColor ? MathHelper.hsvToRgb(hue / 50.0F, 0.7F, 0.6F) & 0xFFFFFF : 0xFFFFFF);
                for (int i = multiLineOverlayMessage.size() - 1; i >= 0; i--) {
                    ITextComponent line = multiLineOverlayMessage.get(i);
                    int lineWidth = font.width(line);
                    ClientUtil.drawBackdrop(matrixStack, -font.width(line) / 2, -4, lineWidth, 1 - opacity);
                    font.draw(matrixStack, line.getVisualOrderText(), -font.width(line) / 2, -4, color | (opacity << 24));
                    RenderSystem.translatef(0, -13, 0);
                }
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }

            mc.getProfiler().pop();
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
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHand(RenderHandEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        Hand hand = event.getHand();
        ItemStack item = player.getItemInHand(hand);
        boolean renderOtherHand = false;
        if (!event.isCanceled() && !modPostedEvent) {
            if (hand == Hand.MAIN_HAND && !player.isInvisible()) {
                ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
                
                Stream<Action<? extends IPower<? extends IPower<?, ?>, ?>>> /* what the absolute fuck, java? */ curActions = Stream.concat(
                        hud.getSelectedEnabledActions(),
                        ContinuousActionInstance.getCurrentAction(player)
                            .map(ContinuousActionInstance::getAction)
                            .filter(action -> action instanceof Action<?>)
                            .map(action -> (Action<?>) action)
                            .map(Stream::of).orElseGet(Stream::empty))
                        .filter(Objects::nonNull);
                if (curActions.anyMatch(action -> !action.needsFreeMainHand && action.needsFreeOffHand)) {
                    renderOtherHand = true;
                }
                
                if (MCUtil.areHandsFree(player, Hand.MAIN_HAND, Hand.OFF_HAND) && hud.isActionSelectedAndEnabled(
                        ModHamonActions.JONATHAN_OVERDRIVE_BARRAGE.get(), 
                        ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get(),
                        ModHamonActions.HAMON_WALL_CLIMBING.get(),
                        ModPillarmanActions.PILLARMAN_ERRATIC_BLAZE_KING.get(),
                        ModPillarmanActions.PILLARMAN_DIVINE_SANDSTORM.get())
                        || LivingWallClimbing.getHandler(player).map(cap -> cap.isWallClimbing()).orElse(false)) {
                    renderHand(Hand.OFF_HAND, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                            event.getPartialTicks(), event.getInterpolatedPitch(), player);
                    renderOtherHand = false;
                }
                
                if (renderOtherHand || GlovesLayer.areGloves(player.getItemInHand(Hand.MAIN_HAND)) || GlovesLayer.areGloves(player.getItemInHand(Hand.OFF_HAND))) {
                    Hand handToRender = renderOtherHand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                    if (MCUtil.isHandFree(player, handToRender)) {
                        event.setCanceled(true);
                        renderHand(handToRender, event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                                event.getPartialTicks(), event.getInterpolatedPitch(), player);
                    }
                }
            }
            
            if (!item.isEmpty() && item.getItem() == ModItems.PHOTO.get()) {
                event.setCanceled(true);
                PolaroidHelper.renderPhotoInHand(event.getMatrixStack(), event.getBuffers(), event.getLight(), 
                        event.getEquipProgress(), MCUtil.getHandSide(player, hand), event.getSwingProgress(), item, event.getPartialTicks());
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
//            HamonBurnLayer.renderFirstPerson(handSide, matrixStack, buffer, light, player);
//            FrozenLayer.renderFirstPerson(handSide, matrixStack, buffer, light, player);
//            GlovesLayer.renderFirstPerson(handSide, matrixStack, buffer, light, player);
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
    public void onHandRenderFinal(RenderArmEvent event) {
        Hand hand = event.getArm() == event.getPlayer().getMainArm() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        if (MCUtil.isHandFree(event.getPlayer(), hand)) {
            FirstPersonHamonAura.getInstance().renderParticles(event.getPoseStack(), event.getMultiBufferSource(), event.getArm());
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
            boolean invertBnW = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW;
            int xButtonsRightEdge = screen.width / 2 + 102;
            int windowWidth = screen.width;
            int windowHeight = screen.height;
            
            if (doStandStatsRender(screen)) {
                StandStatsRenderer.renderStandStats(event.getMatrixStack(), mc, 
                        windowWidth - StandStatsRenderer.statsWidth - 7, windowHeight - StandStatsRenderer.statsHeight - 7, 
                        windowWidth, windowHeight,
                        standStatsTick, partialTick, 
                        alpha, invertBnW,
                        event.getMouseX(), event.getMouseY(), windowWidth - xButtonsRightEdge - 14);
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
    public void onTooltipRender(RenderTooltipEvent.PostText event) {
        List<? extends ITextProperties> lines = event.getLines();
        int x = event.getX();
        int y = event.getY();
        for (int i = 0; i < lines.size(); i++) {
            ITextProperties line = lines.get(i);
            if (line instanceof JojoTextComponentWrapper) {
                ((JojoTextComponentWrapper) line).tooltipRenderExtra(event.getMatrixStack(), x, y - 0.5f);
            }
            if (i == 0) {
                y += 2;
            }
            y += 10;
        }
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
                            ClientModSettings.getInstance().editSettings(settings -> {
                                settings.standStatsTranslucency = (float) MathHelper.clampedLerp(0.1, 1.0, this.value);
                            });
                        }
                    };
                    statsBgAlphaSlider.visible = doStandStatsRender(screen);
                    event.addWidget(statsBgAlphaSlider);
                    
                    ImageMutableButton invertBnWButton = new ImageMutableButton(screen.width - 8, screen.height - 7, 
                            8, 8, 464, 496, 8, StandStatsRenderer.STAND_STATS_UI, 512, 512, 
                            button -> {
                                ClientModSettings.getInstance().editSettings(settings -> {
                                    settings.standStatsInvertBnW = !settings.standStatsInvertBnW;
                                    ((ImageMutableButton) button).xTexStart = settings.standStatsInvertBnW ? 472 : 464;
                                });
                            });
                    invertBnWButton.xTexStart = ClientModSettings.getSettingsReadOnly().standStatsInvertBnW ? 472 : 464;
                    invertBnWButton.visible = doStandStatsRender(screen);
                    event.addWidget(invertBnWButton);
                    
                    Button standStatsToggleButton = new ImageVanillaButton(screen.width - 28, screen.height - 28, 
                            20, 20, 492, 492, StandStatsRenderer.STAND_STATS_UI, 512, 512, 
                            button -> {
                                renderStandStats = !doStandStatsRender(screen);
                                statsBgAlphaSlider.visible = doStandStatsRender(screen);
                                invertBnWButton.visible = doStandStatsRender(screen);
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
        
        else if (screen instanceof ChatScreen) {
            Entity possessed = IPlayerPossess.getPossessedEntity(mc.player);
            if (possessed != null && possessed.getType() == ModEntityTypes.ANGELO_ROCK.get()) {
                int x = screen.width / 2 - 100;
                int y = screen.height - 40;
                Button angeloRockDieButton = new Button(x, y, 200, 20, 
                        new TranslationTextComponent(mc.level.getLevelData().isHardcore() ? "deathScreen.spectate" : "deathScreen.respawn"), 
                        button -> PacketManager.sendToServer(ClAngeloRockButtonPacket.respawn()));
                event.addWidget(angeloRockDieButton);
                
                Button angeloRockGruntButton = new ImageVanillaButton(x - 24, y, 20, 20, 
                        238, 150, 
                        ClientUtil.ADDITIONAL_UI, 256, 256,
                        button -> PacketManager.sendToServer(ClAngeloRockButtonPacket.grunt())) {
                    @Override public void playDownSound(SoundHandler pHandler) {}
                };
                event.addWidget(angeloRockGruntButton);
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
        else if (screen instanceof IngameMenuScreen) {
            IStandPower.getStandPowerOptional(mc.player).resolve()
            .map(StandStatsRenderer.ICosmeticStandStats::getHandler)
            .ifPresent(StandStatsRenderer.ICosmeticStandStats::onPauseScreenOpened);
        }
        else if (screen == null) {
            onScreenClosed();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onScreenOpened2(GuiOpenEvent event) {
        IJojoScreen.rememberScreenTab(event.getGui());
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
        
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        MatrixStack matrixStack = event.getMatrixStack();
        Matrix4f projMatrix = event.getProjectionMatrix();
        float partialTick = event.getPartialTicks();
        findEntitiesOnScreen(matrixStack, projMatrix, camera, partialTick);
        ShaderEffectApplier.getInstance().updateTimeStopperScreenPos(matrixStack, projMatrix, camera, partialTick);
    }
    
    public float getPartialTick() {
        return mc.isPaused() ? pausePartialTick : mc.getFrameTime();
    }
    
    @SubscribeEvent
    public void addTooltipLines(ItemTooltipEvent event) {
        PlayerEntity player = event.getPlayer();
        ItemStack item = event.getItemStack();
        if (player != null) {
            Optional<IStandPower> powerOptional = IStandPower.getStandPowerOptional(player).resolve();
            CrazyDiamondBlockCheckpointMake.getBlockPosMoveTo(player.level, item).ifPresent(pos -> {
                if (powerOptional.map(power -> power.getType() == ModStands.CRAZY_DIAMOND.getStandType()).orElse(false)) {
                    event.getToolTip().add(new TranslationTextComponent("jojo.crazy_diamond.block_checkpoint.tooltip", 
                            pos.getX(), pos.getY(), pos.getZ()).withStyle(TextFormatting.RED));
                }
            });
            
            if (GEItemMarkEffect.isItemMarked(item, player)) {
                event.getToolTip().add(
                        new TranslationTextComponent("jojo.ge_item_marked")
                        .withStyle(Style.EMPTY.withColor(
                                Color.fromRgb(ActionsOverlayGui.getPowerUiColor(powerOptional.get())))));
                event.getToolTip().add(
                        new TranslationTextComponent("jojo.ge_item_marked.2")
                        .withStyle(TextFormatting.DARK_GRAY));
            }
            
            OilItem.remainingOiledUses(item).ifPresent(uses -> {
               if (uses > 0) {
                   event.getToolTip().add(new TranslationTextComponent("item.jojo.oil.uses", uses).withStyle(TextFormatting.GOLD));
               }
           });
        }

        if (item.getItem() instanceof EnchantedBookItem && !ModList.get().isLoaded("enchdesc")) {
            EnchantedBookItem.getEnchantments(item).forEach(nbt -> {
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
    
    
    
    private void renderLosingVision(MatrixStack matrixStack, float partialTick) {
        if (mc.player != null) {
            mc.player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(player -> {
                if (player.isDyingBody() && !mc.player.isSpectator() && !mc.player.isDeadOrDying()) {
                    int timeLeft = player.getDyingBodyTicksLeft();
                    if (timeLeft > 0) {
                        --timeLeft;
                        float vignette = 0;
                        if (timeLeft <= 60) {
                            float vignetteTime = (60 - timeLeft) + partialTick;
                            if (timeLeft > 20) {
                                vignette = (MathHelper.cos(vignetteTime / 10 * (float) Math.PI) + 1) / 2;
                            }
                            else {
                                vignette = (MathHelper.cos(vignetteTime / 20 * (float) Math.PI) + 1) / 2;
                            }
                        }
                        else {
                            float progress = player.getDyingBodyProgress();
                            if (progress > 0.8F) {
                                vignette = 1 - 5 * (1 - progress);
                            }
                        }
                        
                        if (vignette > 0) {
                            vignette = Math.min(vignette, 1);
                            ActionsOverlayGui.getInstance().renderVignette(matrixStack, vignette, vignette, vignette);
                        }
                    }
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dyingBodyLostVision(EntityViewRenderEvent.FogDensity event) {
        if (mc.player != null) {
            mc.player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(player -> {
                if (player.isDyingBody() && !mc.player.isSpectator() && !mc.player.isDeadOrDying()) {
                    int timeLeft = player.getDyingBodyTicksLeft();
                    if (timeLeft > 0) {
                        --timeLeft;
                        if (timeLeft <= 20) {
                            float lerp = (20 - timeLeft + (float) event.getRenderPartialTicks()) / 20.0F;
                            event.setDensity(MathHelper.lerp(lerp, event.getDensity(), 1));
                            event.setCanceled(true);
                        }
                    }
                    else {
                        event.setDensity(1);
                        event.setCanceled(true);
                    }
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dyingBodyVisionDark(EntityViewRenderEvent.FogColors event) {
        if (mc.player != null) {
            mc.player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(player -> {
                if (player.isDyingBody() && !mc.player.isSpectator() && !mc.player.isDeadOrDying() && player.getDyingBodyTicksLeft() <= 21) {
                    event.setRed(0);
                    event.setGreen(0);
                    event.setBlue(0);
                }
            });
        }
    }
    
    
    private Set<Entity> GEDetectorEntities = new HashSet<>();
    private Entity GEDetectorShowHpEntity;
    private PosOnScreen GEDetectorShowHpEntityPos;

    public void addGEDetectedEntity(Entity entity) {
        this.GEDetectorEntities.add(entity);
    }
    
    public void removeGEDetectedEntity(Entity entity) {
        this.GEDetectorEntities.remove(entity);
    }
    
    private void findEntitiesOnScreen(MatrixStack worldRenderMatrixStack, Matrix4f projectionMatrix, ActiveRenderInfo camera, float partialTick) {
        GEDetectorShowHpEntity = getEntityGEDetectHp();
        if (GEDetectorShowHpEntity != null) {
            Entity entity = GEDetectorShowHpEntity;
            Vector3d pos = entity.getPosition(partialTick).add(0, entity.getBbHeight() * 0.5f, 0);
            GEDetectorShowHpEntityPos = ClientUtil.posOnScreen(pos, camera, worldRenderMatrixStack, projectionMatrix);
        }
    }
    
    @Nullable
    private Entity getEntityGEDetectHp() {
        if (mc.player != null) {
            Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            Vector3d lookVec = mc.player.getLookAngle();
            return GEDetectorEntities.stream()
                    .filter(Entity::isAlive)
                    .max(Comparator.comparingDouble(entity -> {
                        Vector3d entityPos = entity.getBoundingBox().getCenter();
                        Vector3d vecToPos = entityPos.subtract(cameraPos).normalize();
                        return vecToPos.dot(lookVec);
                    }))
                    .orElse(null);
        }
        return null;
    }
    
    private void hudRenderEntityGEDetectorData(MatrixStack matrixStack) {
        Entity entity = GEDetectorShowHpEntity;
        PosOnScreen entityPos = GEDetectorShowHpEntityPos;
        if (entity == null || entityPos == null || !entityPos.isOnScreen) return;

        List<ITooltipLine> tooltip = new ArrayList<>();
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            int iconWidth = 17;
            
            if (living.getMaxHealth() > 0) {
                float hpRatio = living.getHealth() / living.getMaxHealth();
                ITextComponent text = new StringTextComponent(String.valueOf((int) (hpRatio * 100) + "%"));
                IconTooltipLine icon = new IconTooltipLine(IconTooltipLine.Icon.HEALTH);
                ITooltipLine line = new MultiTooltipLine(icon.withRightSideSpace(iconWidth - icon.getWidth(mc.font)), new TextTooltipLine(text));
                tooltip.add(line);
            }
            
            INonStandPower.getNonStandPowerOptional(living).resolve().ifPresent(power -> {
                if (power.hasPower() && power.getMaxEnergy() > 0) {
                    float energyRatio = power.getEnergy() / power.getMaxEnergy();
                    ITextComponent text = new StringTextComponent(String.valueOf((int) (energyRatio * 100) + "%"));
                    IconTooltipLine icon = IconTooltipLine.powerEnergy(power.getType());
                    ITooltipLine line = new MultiTooltipLine(icon.withRightSideSpace(iconWidth - icon.getWidth(mc.font)), new TextTooltipLine(text));
                    tooltip.add(line);
                }
            });
            
            IStandPower.getStandPowerOptional(living).resolve().ifPresent(power -> {
                if (power.hasPower() && power.usesStamina() && power.getMaxStamina() > 0) {
                    float staminaRatio = power.getStamina() / power.getMaxStamina();
                    ITextComponent text = new StringTextComponent(String.valueOf((int) (staminaRatio * 100) + "%"));
                    IconTooltipLine icon = new IconTooltipLine(IconTooltipLine.Icon.STAND_STAMINA);
                    ITooltipLine line = new MultiTooltipLine(icon.withRightSideSpace(iconWidth - icon.getWidth(mc.font)), new TextTooltipLine(text));
                    tooltip.add(line);
                }
                if (power.hasPower() && power.usesResolve() && power.getMaxResolve() > 0) {
                    float resolveRatio = living.hasEffect(ModStatusEffects.RESOLVE.get()) ? 1 : power.getResolve() / power.getMaxResolve();
                    ITextComponent text = new StringTextComponent(String.valueOf((int) (resolveRatio * 100)) + "%");
                    IconTooltipLine icon = new IconTooltipLine(IconTooltipLine.Icon.STAND_RESOLVE);
                    ITooltipLine line = new MultiTooltipLine(icon.withRightSideSpace(iconWidth - icon.getWidth(mc.font)), new TextTooltipLine(text));
                    tooltip.add(line);
                }
            });
        }
        else if (entity instanceof SoulEntity) {
            SoulEntity soul = (SoulEntity) entity;
            float seconds = soul.tickCount / 20f;
            float maxTime = soul.lifeSpan / 20f;
            
            // TODO add soul time tooltip line
        }
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = (int) (screenWidth * entityPos.pos.x);
        int y = (int) (screenHeight * (1 - entityPos.pos.y));
        
        int uiColor = ActionsOverlayGui.getPowerUiColor(PowerClassification.STAND);
        int[] rgb = ClientUtil.rgbInt(uiColor);
        int color1 = (0xF0 << 24) + ClientUtil.fromRgbInt(rgb[0] / 5, rgb[1] / 5, rgb[2] / 5);
        int color2 = (0x50 << 24) + uiColor;
        int color3 = (color2 & 0xFEFEFE) >> 1 | color2 & 0xFF000000;
        
        CustomTooltipRender.drawHoveringText(matrixStack, tooltip, 
                x, y, screenWidth, screenHeight, -1, 
                color1, color2, color3, 
                mc.font, false);
    }
    
    
    
    private UUID serverId;
    private boolean isLoggedIn = false;
    
    public void setServerId(ServerIdPacket packet) {
        serverId = packet.serverId;
    }
    
    @Nullable
    public UUID getServerId() {
        return isLoggedIn ? serverId : null;
    }
    
    @SubscribeEvent
    public void clientLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
        isLoggedIn = true;
        ClientModSettings.getSettingsReadOnly().broadcasted.broadcastToServer();
    }
    
    @SubscribeEvent
    public void clientLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PhotosCache.onLogOut(serverId);
        isLoggedIn = false;
    }
    
    
    private boolean setScreenNextTick = false;
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if (event.getOriginalMessage().equals("//recording")) {
            event.setCanceled(true);
            mc.gui.getChat().clearMessages(false);
            setScreenNextTick = true;
        }
    }
    
    private void tickAfterChat() {
        if (setScreenNextTick) {
            mc.setScreen(new DummyScreen());
            setScreenNextTick = false;
        }
    }
    
    private static class DummyScreen extends Screen {

        protected DummyScreen() {
            super(StringTextComponent.EMPTY);
        }
        
        @Override
        public boolean isPauseScreen() {
            return false;
        }
        
    }
}
