package com.github.standobyte.jojo.mechanics.speechbubble.client;

import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.mechanics.speechbubble.CharacterRecentlySaidSpeechBubbles;
import com.github.standobyte.jojo.mechanics.speechbubble.SpeechBubble;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class RenderSpeechBubbles {
    public static final ResourceLocation[] SPEECH = new ResourceLocation[] {
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_1.png"),
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_2.png"),
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_3.png"),
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_4.png")
    };
    public static final ResourceLocation[] SPEECH_OFFSCREEN = new ResourceLocation[] {
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_1_offscreen.png"),
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_2_offscreen.png"),
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_3_offscreen.png"),
            JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_4_offscreen.png")
    };
    public static final ResourceLocation SHOUT = JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_shout.png");
    public static final ResourceLocation THOUGHT = JojoMod.resLoc("textures/speech_bubble/bubbles/speech_bubble_thought.png");

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        float partialTick = event.getPartialTicks();
        MatrixStack matrixStack = event.getMatrixStack();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();

        matrixStack.pushPose();
        matrixStack.mulPose(camera.rotation());
        
        for (Int2ObjectMap.Entry<CharacterRecentlySaidSpeechBubbles> entry : ClientSpeechBubblesStorage.perEntityId.int2ObjectEntrySet()) {
            Entity entity = mc.level.getEntity(entry.getIntKey());
            if (entity == null || entity == mc.cameraEntity && mc.options.getCameraType().isFirstPerson()) continue;
            
            CharacterRecentlySaidSpeechBubbles speechBubbles = entry.getValue();
            if (speechBubbles != null) {
                for (int i = 0; i < speechBubbles.speechBubbles.length; i++) {
                    SpeechBubble speechBubble = speechBubbles.speechBubbles[i];
                    if (speechBubble != null) {
                        // matrix stack stuff from markers
                        Vector3d entityPos = entity.getEyePosition(partialTick);
                        Vector3d diff = entityPos.subtract(camera.getPosition())
                                .yRot(camera.getYRot() * MathUtil.DEG_TO_RAD)
                                .xRot(camera.getXRot() * MathUtil.DEG_TO_RAD);
                        double distance = diff.length();
                        if (distance > 256) continue;
                        float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);
                        boolean onScreen = true;

                        matrixStack.pushPose();
                        matrixStack.translate(diff.x, diff.y, diff.z);
                        matrixStack.scale(-scale, -scale, 1);
                        
                        double aaa = MathHelper.clampedLerp(-50, -75, Math.max(distance * -0.2 + 1, 0));
                        switch (i) {
                            case 0: { matrixStack.translate(-0.53 * aaa, 1.0 * aaa, 0); break; }
                            case 1: { matrixStack.translate(-0.49 * aaa, 0.25 * aaa, 0); break; }
                            case 2: { matrixStack.translate(0.48 * aaa, 0.97 * aaa, 0); break; }
                            case 3: { matrixStack.translate(0.51 * aaa, 0.29 * aaa, 0); break; }
                        }
                        
                        ResourceLocation speechBubbleBg;
                        switch (speechBubble.type) {
                            case REGULAR: {
                                speechBubbleBg = (onScreen ? SPEECH : SPEECH_OFFSCREEN)[i];
                                break;
                            }
                            case SHOUT: {
                                speechBubbleBg = SHOUT;
                                break;
                            }
                            case THOUGHT: {
                                speechBubbleBg = THOUGHT;
                                break;
                            }
                            default: throw new AssertionError();
                        }
                        mc.textureManager.bind(speechBubbleBg);
                        
                        float alpha = speechBubble.fadeOut.getAlpha(partialTick);
                        RenderSystem.color4f(1, 1, 1, alpha);
                        RenderSystem.disableDepthTest();
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        AbstractGui.blit(matrixStack, -20, 10, 0, 0, 40, 40, 40, 40);
                        
                        matrixStack.scale(0.5f, 0.5f, 0.5f);
                        List<IReorderingProcessor> text = mc.font.split(speechBubble.text, 32);
                        float y = 62 - text.size() * mc.font.lineHeight * 0.5f;
                        int color = (int) (255f * alpha) << 24;
                        for (IReorderingProcessor line : text) {
                            float x = -mc.font.width(line) / 2;

                            RenderSystem.enableAlphaTest();
                            IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
                            mc.font.drawInBatch(line, x, y, color, 
                                    false, matrixStack.last().pose(), buffer, 
                                    true /* this parameter NEEDS to be true to use the render type with no depth test */, 
                                    0, ClientUtil.MAX_MODEL_LIGHT);
                            buffer.endBatch();
                            
                            //mc.font.draw(matrixStack, line, x, y, color);
                            y += mc.font.lineHeight;
                        }
                        
                        matrixStack.popPose();
                    }
                }
            }
        }
        matrixStack.popPose();
    }
    
    @SubscribeEvent
    public static void tick(ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isPaused()) {
                if (mc.level == null) {
                    ClientSpeechBubblesStorage.perEntityId.clear();
                }
                else {
                    ObjectIterator<Int2ObjectMap.Entry<CharacterRecentlySaidSpeechBubbles>> iter = ClientSpeechBubblesStorage.perEntityId.int2ObjectEntrySet().iterator();
                    while (iter.hasNext()) {
                        Int2ObjectMap.Entry<CharacterRecentlySaidSpeechBubbles> entry = iter.next();
                        Entity entity = mc.level.getEntity(entry.getIntKey());
                        if (entity == null) {
                            iter.remove();
                        }
                        else {
                            CharacterRecentlySaidSpeechBubbles obj = entry.getValue();
                            obj.tick(entity);
                        }
                    }
                }
            }
        }
    }
}
