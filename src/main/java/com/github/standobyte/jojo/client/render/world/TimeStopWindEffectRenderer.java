package com.github.standobyte.jojo.client.render.world;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientTimeStopHandler;
import com.github.standobyte.jojo.util.general.PerlinNoise;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class TimeStopWindEffectRenderer {
    private static final List<AirFlowLine> airFlowLines = new ArrayList<>();
    private static final Random random = new Random();
    private static final PerlinNoise noise = new PerlinNoise(random.nextLong());

    // todo: a client config to modify this
    private static final int BASE_LINE_COUNT = 120;
    private static final int LINES_PER_CHUNK = 40;
    private static final int MAX_LINE_COUNT = 1500;
    private static final float FADE_SPEED = 0.02F;

    private static float transitionProgress = 0.0F;
    private static boolean wasTimeStopped = false;
    private static boolean isFadingOut = false;
    private static float fixedNoiseTick = 0;

    private static void generateLines() {
        airFlowLines.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int renderDistanceChunks = mc.options.renderDistance;
        int lineCount = Math.min(BASE_LINE_COUNT + renderDistanceChunks * LINES_PER_CHUNK, MAX_LINE_COUNT);
        float renderRadius = renderDistanceChunks * 16.0F;

        Vector3d playerPos = mc.player.position();
        for (int i = 0; i < lineCount; i++) {
            Vector3d start = new Vector3d(
                    playerPos.x + (random.nextDouble() - 0.5) * renderRadius * 2,
                    playerPos.y + (random.nextDouble() - 0.5) * renderRadius,
                    playerPos.z + (random.nextDouble() - 0.5) * renderRadius * 2
            );
            Vector3d end = start.add(
                    (random.nextDouble() - 0.5) * 12,
                    (random.nextDouble() - 0.5) * 6,
                    (random.nextDouble() - 0.5) * 12
            );
            airFlowLines.add(new AirFlowLine(start, end));
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (Minecraft.getInstance().player == null) return;

        ClientTimeStopHandler tsFields = ClientTimeStopHandler.getInstance();
        boolean isTimeStopped = tsFields.isTimeStopped();
        boolean timeStopWindEffect = ClientModSettings.getSettingsReadOnly().timeStopWindEffect;

        if (isTimeStopped && !wasTimeStopped) {
            if (timeStopWindEffect) {
                generateLines();
                isFadingOut = false;
                transitionProgress = 0.0F;
                fixedNoiseTick = Minecraft.getInstance().player.tickCount + event.getPartialTicks();
            }
        }
        else if (!isTimeStopped && wasTimeStopped && !airFlowLines.isEmpty()) {
            isFadingOut = true;
        }
        wasTimeStopped = isTimeStopped;

        if (!airFlowLines.isEmpty()) {
            if (isFadingOut) {
                transitionProgress -= FADE_SPEED;
                if (transitionProgress <= 0) {
                    airFlowLines.clear();
                    isFadingOut = false;
                    return;
                }
            }
            else if (isTimeStopped) {
                transitionProgress = Math.min(1.0F, transitionProgress + FADE_SPEED);
            }
            renderEffect(event, transitionProgress);
        }
    }

    private static void renderEffect(RenderWorldLastEvent event, float currentAlpha) {
        Minecraft mc = Minecraft.getInstance();
        MatrixStack matrixStack = event.getMatrixStack();
        Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        matrixStack.pushPose();
        matrixStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        IVertexBuilder buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lightning());
        Matrix4f matrix = matrixStack.last().pose();

        for (AirFlowLine line : airFlowLines) {
            renderAirFlowRibbon(buffer, matrix, line, cameraPos, currentAlpha);
        }

        mc.renderBuffers().bufferSource().endBatch(RenderType.lightning());

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        matrixStack.popPose();
    }

    private static void renderAirFlowRibbon(IVertexBuilder buffer, Matrix4f matrix, AirFlowLine line, Vector3d cameraPos, float globalAlpha) {
        int segments = 15;
        float noiseFactor = 0.3f;
        float width = 0.075f;

        Vector3d prevPos = null;
        Vector3d prevSide = null;

        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            Vector3d currentPos = line.getPoint(t);

            float nX = noise.noise(currentPos.x * 0.4, currentPos.y * 0.4, fixedNoiseTick * 0.05 + line.noiseOffset) * noiseFactor;
            float nY = noise.noise(currentPos.y * 0.4, currentPos.z * 0.4, fixedNoiseTick * 0.05 + line.noiseOffset) * noiseFactor;
            float nZ = noise.noise(currentPos.z * 0.4, currentPos.x * 0.4, fixedNoiseTick * 0.05 + line.noiseOffset) * noiseFactor;
            currentPos = currentPos.add(nX, nY, nZ);

            Vector3d dirToNext = (i == segments) ? line.diff.normalize() : line.getPoint(Math.min(1.0f, t + 1.0f / segments)).subtract(currentPos).normalize();
            Vector3d toCamera = cameraPos.subtract(currentPos).normalize();
            Vector3d side = dirToNext.cross(toCamera).normalize().scale(width);

            if (prevPos != null) {
                float segmentAlpha = (float) (Math.sin(t * Math.PI) * 0.4F) * globalAlpha;

                buffer.vertex(matrix, (float) (prevPos.x - prevSide.x), (float) (prevPos.y - prevSide.y), (float) (prevPos.z - prevSide.z)).color(0.8F, 0.9F, 1.0F, segmentAlpha).endVertex();
                buffer.vertex(matrix, (float) (prevPos.x + prevSide.x), (float) (prevPos.y + prevSide.y), (float) (prevPos.z + prevSide.z)).color(0.8F, 0.9F, 1.0F, segmentAlpha).endVertex();
                buffer.vertex(matrix, (float) (currentPos.x + side.x), (float) (currentPos.y + side.y), (float) (currentPos.z + side.z)).color(0.8F, 0.9F, 1.0F, segmentAlpha).endVertex();
                buffer.vertex(matrix, (float) (currentPos.x - side.x), (float) (currentPos.y - side.y), (float) (currentPos.z - side.z)).color(0.8F, 0.9F, 1.0F, segmentAlpha).endVertex();
            }
            prevPos = currentPos;
            prevSide = side;
        }
    }

    private static class AirFlowLine {
        final Vector3d start;
        final Vector3d end;
        final Vector3d diff;
        final float noiseOffset;

        AirFlowLine(Vector3d start, Vector3d end) {
            this.start = start;
            this.end = end;
            this.diff = end.subtract(start);
            this.noiseOffset = random.nextFloat() * 100;
        }

        Vector3d getPoint(float t) {
            return start.add(diff.scale(t));
        }
    }
}