package com.github.standobyte.jojo.client;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.AIR;
import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.FOOD;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.TimeHandler;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static ClientEventHandler instance = null;
    
    private Minecraft mc;

    private Timer clientTimer;
    private boolean canSeeInStoppedTime = true;
    private boolean canMoveInStoppedTime = true;
    private float partialTickStoppedAt;
    
    private double zoomModifier;
    public boolean isZooming;

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
        return mc.level != null && TimeHandler.isTimeStopped(mc.level, chunkPos);
    }
    
    public void setTimeStopClientState(int ticks, ChunkPos chunkPos, boolean canSee, boolean canMove) {
        if (ticks > 0) {
            canSeeInStoppedTime = canSee;
            canMoveInStoppedTime = canSee && canMove;
            partialTickStoppedAt = canMove ? mc.getFrameTime() : 0.0F;
        }
        else {
            canSeeInStoppedTime = true;
            canMoveInStoppedTime = true;
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }
    }
    
    public void updateCanMoveInStoppedTime(boolean canMove, ChunkPos chunkPos) {
        if (isTimeStopped(chunkPos)) {
            this.canMoveInStoppedTime = canMove;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public <T extends LivingEntity, M extends EntityModel<T>> void onRenderLiving(RenderLivingEvent.Pre<T, M> event) {
        if (isTimeStopped(event.getEntity().blockPosition())) {
            T entity = (T) event.getEntity();
            if (!entity.canUpdate() && event.getPartialRenderTick() != partialTickStoppedAt) {
                event.getRenderer().render(entity, MathHelper.lerp(partialTickStoppedAt, entity.yRotO, entity.yRot), partialTickStoppedAt, event.getMatrixStack(), event.getBuffers(), event.getLight());
                event.setCanceled(true);
            }
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
        if (event.phase == TickEvent.Phase.START) {
            ActionsOverlayGui.getInstance().tick();
        }
        if (mc.level != null && isTimeStopped(mc.player.blockPosition())) {
            if (event.phase == TickEvent.Phase.START) {
                if (!canSeeInStoppedTime) {
                    ClientReflection.pauseClient(mc);
                }
            }
            else {
                if (canSeeInStoppedTime && mc.gameRenderer.currentEffect() == null) {
                    mc.gameRenderer.loadEffect(new ResourceLocation("shaders/post/desaturate.json"));
                }
            }
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
                if (power.getType() == ModNonStandPowers.VAMPIRISM.get()) {
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderHand(RenderHandEvent event) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity instanceof LivingEntity) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                if (power.isActionOnCooldown(ModActions.HAMON_ZOOM_PUNCH.get())) {
                    event.setCanceled(true);
                }
                else if (ActionsOverlayGui.getInstance().getSelectedAction(ActionType.ATTACK) == ModActions.JONATHAN_OVERDRIVE_BARRAGE.get()) {
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
            });
        }
    }
}
