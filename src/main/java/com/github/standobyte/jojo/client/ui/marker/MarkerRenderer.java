package com.github.standobyte.jojo.client.ui.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

public abstract class MarkerRenderer {
    private final ResourceLocation iconTexture;
    private final Action<?> iconAction;
    private final List<MarkerInstance> positions = new ArrayList<>();
    protected final Minecraft mc;
    
    @Deprecated
    /**
     * @deprecated use {@link MarkerRenderer#MarkerRenderer(ResourceLocation, Action, Minecraft)}
     */
    public MarkerRenderer(int color, ResourceLocation iconTexture, Minecraft mc) {
        this(iconTexture, mc);
    }
    
    public MarkerRenderer(ResourceLocation iconTexture, Minecraft mc) {
        this(iconTexture, null, mc);
    }
    
    public MarkerRenderer(ResourceLocation defaultIconTexture, Action<?> iconAction, Minecraft mc) {
        this.iconTexture = defaultIconTexture;
        this.iconAction = iconAction;
        this.mc = mc;
    }

    protected void render(MatrixStack matrixStack, ActiveRenderInfo camera, float partialTick) {
        if (shouldRender()) {
            positions.clear();
            updatePositions(positions, partialTick);
            if (!positions.isEmpty()) {
                matrixStack.pushPose();
                matrixStack.mulPose(camera.rotation());
                
                float[] rgb = ClientUtil.rgb(getColor());
                positions.forEach(marker -> {
                    renderAt(matrixStack, marker, camera, partialTick, rgb);
                });
                
                matrixStack.popPose();
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void renderAt(MatrixStack matrixStack, MarkerInstance marker, ActiveRenderInfo camera, float partialTick, float[] rgb) {
        matrixStack.pushPose();
        Vector3d diff = marker.pos.subtract(camera.getPosition())
                .yRot(camera.getYRot() * MathUtil.DEG_TO_RAD)
                .xRot(camera.getXRot() * MathUtil.DEG_TO_RAD);
        
        double distance = diff.length();
        if (distance > 256) return;
        
        float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);
        
        matrixStack.translate(diff.x, diff.y, diff.z);
        matrixStack.scale(-scale, -scale, 1);
        matrixStack.scale(0.8f, 0.8f, 0.8f);
        
        matrixStack.pushPose();
        matrixStack.translate(-8, -28, 0);
        renderIcon(matrixStack, marker, partialTick);
        matrixStack.popPose();
        
        mc.getTextureManager().bind(ClientUtil.ADDITIONAL_UI);
        RenderSystem.color4f(rgb[0], rgb[1], rgb[2], 1.0F);
        AbstractGui.blit(matrixStack, -16, -32, 0, 0, 32, 32, 256, 256);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (marker.outlined) {
            AbstractGui.blit(matrixStack, -16, -32, 32, 0, 64, 32, 256, 256);
        }

        matrixStack.pushPose();
        matrixStack.translate(-8, -28, 0);
        RenderSystem.disableDepthTest();
        renderIconOnBorder(matrixStack, marker, partialTick);
        RenderSystem.enableDepthTest();
        matrixStack.popPose();

        matrixStack.popPose();
    }
    
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {
        ResourceLocation icon = getIcon();
        if (icon != null) {
            mc.getTextureManager().bind(icon);
            AbstractGui.blit(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
        }
    }
    
    protected void renderIconOnBorder(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {}
    
    protected abstract boolean shouldRender();
    protected abstract void updatePositions(List<MarkerInstance> list, float partialTick);
    
    protected static void fillWithStandEffectTargets(List<MarkerInstance> list, float partialTick, 
            StandEffectType<?> standEffect, double range, Minecraft mc, boolean highlightLookedAt) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            List<StandEffectInstance> targets = StandEffectsTracker.getEffectsOfType(stand, 
                    standEffect, range).collect(Collectors.toList());
            Optional<StandEffectInstance> outlined = highlightLookedAt ? StandEffectsTracker.getTargetLookedAt(targets.stream(), mc.player) : Optional.empty();
            targets.forEach(effect -> {
                Entity target = effect.getTarget();
                if (target != null) {
                    list.add(new MarkerInstance(
                            target.getPosition(partialTick).add(0, target.getBbHeight() * 1.1, 0), 
                            highlightLookedAt && outlined.map(outlinedEffect -> effect == outlinedEffect).orElse(false),
                            Optional.of(effect)));
                }
            });
        });
    }
    
    protected int getColor() {
        return IStandPower.getStandPowerOptional(mc.player).map(stand -> StandSkinsManager.getUiColor(stand)).orElse(0xFFFFFF);
    }
    
    protected ResourceLocation getIcon() {
        if (iconAction != null) {
            return IPower.getPowerOptional(mc.player, iconAction.getPowerClassification())
                    .map(power -> getIconFromAction(iconAction, power))
                    .orElse(iconTexture);
        }
        return iconTexture;
    }
    
    private <P extends IPower<P, ?>> ResourceLocation getIconFromAction(Action<P> action, IPower<?, ?> power) {
        return action.getIconTexture((P) power);
    }

    @EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
    public static class Handler {
        private static Collection<MarkerRenderer> RENDERERS = new ArrayList<>();
        
        public static void addRenderer(MarkerRenderer markerRenderer) {
            RENDERERS.add(markerRenderer);
        }

        @SubscribeEvent
        public static void renderMarkers(RenderWorldLastEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.options.hideGui) {
                RenderSystem.disableDepthTest();
                if (mc.options.graphicsMode == GraphicsFanciness.FABULOUS) { // it just works
                    RenderSystem.enableTexture();
                }

                MatrixStack matrixStack = event.getMatrixStack();
                RENDERERS.forEach(marker -> marker.render(matrixStack, mc.gameRenderer.getMainCamera(), event.getPartialTicks()));
                
                RenderSystem.enableDepthTest();
            }
        }
    }
    
    
    
    protected static class MarkerInstance {
        protected Vector3d pos;
        protected boolean outlined;
        protected final Optional<StandEffectInstance> standEffect;
        
        public MarkerInstance(Vector3d pos, boolean outlined) {
            this(pos, outlined, Optional.empty());
        }
        
        public MarkerInstance(Vector3d pos, boolean outlined, Optional<StandEffectInstance> standEffect) {
            this.pos = pos;
            this.outlined = outlined;
            this.standEffect = standEffect;
        }
    }
}
