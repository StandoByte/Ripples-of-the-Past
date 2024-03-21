package com.github.standobyte.jojo.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.client.ui.screen.mob.RockPaperScissorsScreen;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.general.MathUtil.Matrix4ZYX;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class ClientUtil {
    public static final ResourceLocation ADDITIONAL_UI = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/additional.png");
    public static final int MAX_MODEL_LIGHT = LightTexture.pack(15, 15);
    static boolean canSeeStands;
    static boolean canHearStands;

    public static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static World getClientWorld() {
        return Minecraft.getInstance().level;
    }
    
    public static boolean isLocalServer() {
        return Minecraft.getInstance().isLocalServer();
    }
    
    public static boolean isShiftPressed() {
        return Screen.hasShiftDown();
    }
    
    public static boolean isDestroyingBlock() {
        return Minecraft.getInstance().gameMode.isDestroying();
    }
    
    public static boolean arePlayerHandsBusy() {
        return Minecraft.getInstance().player.isHandsBusy();
    }

    public static Entity getEntityById(int entityId) {
        return Minecraft.getInstance().level.getEntity(entityId);
    }
    
    public static Entity getCrosshairPickEntity() {
        return Minecraft.getInstance().crosshairPickEntity;
    }
    
    public static Entity getCameraEntity() {
        return Minecraft.getInstance().cameraEntity;
    }
    
    public static Vector3d getCameraPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }
    
    public static Vector3f getCameraLook() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector();
    }
    
    public static float getPartialTick() {
        return ClientEventHandler.getInstance().getPartialTick();
    }
    
    public static String getCurrentLanguageCode() {
        return Minecraft.getInstance().getLanguageManager().getSelected().getCode();
    }
    
    public static boolean isInSinglePlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.hasSingleplayerServer() && !mc.getSingleplayerServer().isPublished();
    }
    
    public static boolean useActionShiftVar(PlayerEntity player) {
        return player.isShiftKeyDown();
    }
    
    public static boolean canSeeStands() {
        return canSeeStands;
    }
    
    public static boolean canHearStands() {
        return canHearStands;
    }
    
    private static IStandPower playerStandCached;
    public static IStandPower getStandPowerClCached() {
        return playerStandCached;
    }
    
    public static void updatePowersCapCache() {
        playerStandCached = IStandPower.getPlayerStandPower(Minecraft.getInstance().player);
    }
    
    public static void setCameraEntityPreventShaderSwitch(Minecraft mc, Entity entity) {
        mc.setCameraEntity(entity);
        if (mc.gameRenderer.currentEffect() == null) {
            ShaderEffectApplier.getInstance().updateCurrentShader();
        }
    }
    
    public static void openScreen(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }
    
    public static void openRockPaperScissorsScreen(RockPaperScissorsGame game) {
        Minecraft.getInstance().setScreen(new RockPaperScissorsScreen(game));
    }
    
    public static void closeRockPaperScissorsScreen(RockPaperScissorsGame game) {
        if (Minecraft.getInstance().screen instanceof RockPaperScissorsScreen) {
            RockPaperScissorsScreen screen = (RockPaperScissorsScreen) Minecraft.getInstance().screen;
            if (screen.game == game) {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    public static void openHamonTeacherUi() {
        Minecraft.getInstance().setScreen(new HamonScreen());
    }
    
    public static void setThirdPerson() {
        GameSettings options = Minecraft.getInstance().options;
        if (options.getCameraType() == PointOfView.FIRST_PERSON) {
            options.setCameraType(PointOfView.THIRD_PERSON_FRONT);
        }
    }
    
    public static boolean resourceExists(ResourceLocation location) {
        try {
            return Minecraft.getInstance().getResourceManager().getResource(location) != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static void drawRightAlignedString(MatrixStack matrixStack, FontRenderer font, String line, float x, float y, int color) {
        font.drawShadow(matrixStack, line, x - font.width(line), y, color);
    }

    public static void drawRightAlignedString(MatrixStack matrixStack, FontRenderer font, ITextComponent line, float x, float y, int color) {
        drawRightAlignedString(matrixStack, font, line.getVisualOrderText(), x, y, color);
    }

    public static void drawRightAlignedString(MatrixStack matrixStack, FontRenderer font, IReorderingProcessor line, float x, float y, int color) {
        font.drawShadow(matrixStack, line, x - font.width(line), y, color);
    }

    public static void drawCenteredString(MatrixStack matrixStack, FontRenderer font, IReorderingProcessor line, float x, float y, int color) {
        font.drawShadow(matrixStack, line, x - font.width(line) / 2, y, color);
    }

    public static void drawCenteredStringNoShadow(MatrixStack matrixStack, FontRenderer font, ITextComponent line, float x, float y, int color) {
        font.draw(matrixStack, line, x - font.width(line) / 2, y, color);
    }
    
    public static void drawLines(MatrixStack matrixStack, FontRenderer font, List<IReorderingProcessor> lines, 
            float x, float y, float lineGap, int color, boolean shadow, boolean backdrop) {
        for (int i = 0; i < lines.size(); i++) {
            IReorderingProcessor line = lines.get(i);
            float lineX = x;
            float lineY = y + i * (font.lineHeight + lineGap);
            
            if (backdrop) {
                ClientUtil.drawBackdrop(matrixStack, (int) lineX, (int) lineY, font.width(line), 1.0F);
            }
            
            if (shadow) {
                font.drawShadow(matrixStack, line, lineX, lineY, color);
            }
            else {
                font.draw(matrixStack, line, lineX, lineY, color);
            }
        }
    }
    
    public static void drawTooltipRectangle(MatrixStack matrixStack, int x, int y, int width, int height) {
        drawTooltipRectangle(matrixStack, x, y, width, height, 
                GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, GuiUtils.DEFAULT_BORDER_COLOR_END, 400);
    }

    @SuppressWarnings("deprecation")
    public static void drawTooltipRectangle(MatrixStack matrixStack, int x, int y, int width, int height, 
            int backgroundColor, int borderColorStart, int borderColorEnd, int zLevel) {
        RenderSystem.disableRescaleNormal();
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        matrixStack.pushPose();
        Matrix4f mat = matrixStack.last().pose();
        
        drawGradientRect(mat, zLevel, x - 3, y - 4, x + width + 3, y - 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 3, y + height + 3, x + width + 3, y + height + 4, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 3, y - 3, x + width + 3, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 4, y - 3, x - 3, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x + width + 3, y - 3, x + width + 4, y + height + 3, backgroundColor, backgroundColor);
        drawGradientRect(mat, zLevel, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(mat, zLevel, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, borderColorStart, borderColorEnd);
        drawGradientRect(mat, zLevel, x - 3, y - 3, x + width + 3, y - 3 + 1, borderColorStart, borderColorStart);
        drawGradientRect(mat, zLevel, x - 3, y + height + 2, x + width + 3, y + height + 3, borderColorEnd, borderColorEnd);

        matrixStack.popPose();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableRescaleNormal();
    }
    
    private static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
        float startBlue  = (float)(startColor       & 255) / 255.0F;
        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
        float endBlue    = (float)(endColor         & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.vertex(mat, right,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(mat,  left,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        buffer.vertex(mat,  left, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        buffer.vertex(mat, right, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
        tessellator.end();
    }
    
    public static void fillSingleRect(double x, double y, double width, double height, int red, int green, int blue, int alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        fillRect(bufferBuilder, x, y, width, height, red, green, blue, alpha);
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
    
    public static void fillRect(BufferBuilder bufferBuilder, double x, double y, double width, double height, int red, int green, int blue, int alpha) {
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(x + 0 , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + 0 , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(x + width , y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().end();
    }
    
    private static final int[] RED_PIXEL =   new int[] { 255, 0, 0, 63 };
    private static final int[] GREEN_PIXEL = new int[] { 0, 255, 0, 63 };
    public static void pixelCheckOverlay(BiPredicate<Integer, Integer> pixelCheck) {
        MainWindow window = Minecraft.getInstance().getWindow();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuilder();
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] color = pixelCheck.test(x, y) ? GREEN_PIXEL : RED_PIXEL;
                bufferBuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(x,   y,   0.0D).color(color[0], color[1], color[2], color[3]).endVertex();
                bufferBuilder.vertex(x,   y+1, 0.0D).color(color[0], color[1], color[2], color[3]).endVertex();
                bufferBuilder.vertex(x+1, y+1, 0.0D).color(color[0], color[1], color[2], color[3]).endVertex();
                bufferBuilder.vertex(x+1, y,   0.0D).color(color[0], color[1], color[2], color[3]).endVertex();
                Tessellator.getInstance().end();
            }
        }
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
    
    public static void drawBackdrop(MatrixStack matrixStack, int x, int y, int width, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        int backdropColor = mc.options.getBackgroundColor(0.0F);
        if (backdropColor != 0) {
            AbstractGui.fill(matrixStack, x - 2, y - 2, x + width + 2, y + mc.font.lineHeight + 2, 
                    ColorHelper.PackedColor.multiply(backdropColor, addAlpha(0xFFFFFF, alpha)));
        }
    }
    
    private static int latestScissorX;
    private static int latestScissorY;
    private static int latestScissorWidth;
    private static int latestScissorHeight;
    public static void enableGlScissor(float x, float y, float width, float height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft mc = Minecraft.getInstance();
        float guiScale = mc.getWindow().calculateScale(mc.options.guiScale, mc.isEnforceUnicode());
        y = mc.getWindow().getGuiScaledHeight() - y - height;
        
        latestScissorX =        (int) (guiScale * x);
        latestScissorY =        (int) (guiScale * y);
        latestScissorWidth =    (int) (guiScale * width);
        latestScissorHeight =   (int) (guiScale * height);
        GL11.glScissor(latestScissorX, latestScissorY, latestScissorWidth, latestScissorHeight);
    }
    
    public static void disableGlScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    
    public static void reenableGlScissor() {
        GL11.glScissor(latestScissorX, latestScissorY, latestScissorWidth, latestScissorHeight);
    }
    
    public static String getShortenedTranslationKey(String originalKey) {
        String shortenedKey = originalKey + ".shortened";
        return I18n.exists(shortenedKey) ? shortenedKey : originalKey;
    }
    
    public static void setOverlayMessage(ITextComponent message) {
        Minecraft.getInstance().gui.handleChat(ChatType.GAME_INFO, message, Util.NIL_UUID);
    }
    
    public static Style textColor(int color) {
        return Style.EMPTY.withColor(Color.fromRgb(color));
    }
    
    public static int getFoliageColor(BlockState blockState, @Nullable IBlockDisplayReader world, BlockPos blockPos) {
        return Minecraft.getInstance().getBlockColors().getColor(blockState, world, blockPos, 0);
    }
    
    public static void playSoundAtClient(SoundEvent sound, SoundCategory category, BlockPos soundPos, float volume, float pitch) {
        ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (renderInfo.isInitialized()) {
            Vector3d clientPos = renderInfo.getPosition();
            Vector3d soundDir = Vector3d.atCenterOf(soundPos).subtract(clientPos);
            double dist = soundDir.length();
            if (dist > 0) {
                clientPos = clientPos.add(soundDir.scale(2 / dist));
            }
            ClientUtil.getClientWorld().playLocalSound(clientPos.x, clientPos.y, clientPos.z, 
                    sound, category, volume, pitch, false);
        }
    }
    
    public static void playMusic(SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSound(
                sound.getLocation(), 
                SoundCategory.RECORDS, 
                volume, pitch, false, 0, ISound.AttenuationType.NONE, 
                0, 0, 0, true));
    }
    
    public static boolean decreasedParticlesSetting() {
        return Minecraft.getInstance().options.particles == ParticleStatus.DECREASED;
    }
    
    public static float[] rgb(int color) {
        int[] rgbInt = rgbInt(color);
        return new float[] {
                (float) rgbInt[0] / 255F,
                (float) rgbInt[1] / 255F,
                (float) rgbInt[2] / 255F
        };
    }
    
    public static int[] rgbInt(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return new int[] {red, green, blue};
    }
    
    public static int fromRgb(float r, float g, float b) {
        return ((int) (r * 255) << 16) + ((int) (g * 255) << 8) + (int) (b * 255);
    }
    
    public static int fromRgbInt(int r, int g, int b) {
        return (r << 16) + (g << 8) + b;
    }
    
    public static int discColor(int color) {
        return (((0xFFFFFF - color) & 0xFEFEFE) >> 1) + color;
    }
    
    public static int addAlpha(int color, float alpha) {
        return color | ((int) (255F * alpha)) << 24 & -0x1000000;
    }
    
    public static void vertex(MatrixStack.Entry matrixEntry, IVertexBuilder vertexBuilder, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha, 
            float x, float y, float z, float texU, float texV) {
        vertexBuilder
        .vertex(matrixEntry.pose(), x, y, z)
        .color(red, green, blue, alpha)
        .uv(texU, texV)
        .overlayCoords(packedOverlay)
        .uv2(packedLight)
        .normal(matrixEntry.normal(), 0.0F, 1.0F, 0.0F)
        .endVertex();
    }    
    
    public static void vertex(Matrix4f matrix, Matrix3f normals, IVertexBuilder vertexBuilder, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha, 
            float offsetX, float offsetY, float offsetZ, 
            float texU, float texV, 
            float normalX, float normalY, float normalZ) {
        vertexBuilder
        .vertex(matrix, offsetX, offsetY, offsetZ)
        .color(red, green, blue, alpha)
        .uv(texU, texV)
        .overlayCoords(packedOverlay)
        .uv2(packedLight)
        .normal(normals, normalX, normalZ, normalY)
        .endVertex();
    }

    public static float getHighlightAlpha(float ticks, float cycleTicks, float maxAlphaTicks, float minAlpha, float maxAlpha) {
        ticks %= cycleTicks;
        float coeff = maxAlpha / maxAlphaTicks;
        float alpha = ticks <= cycleTicks / 2 ? coeff * ticks : coeff * (cycleTicks - ticks);
        return Math.min(alpha, maxAlpha - minAlpha) + minAlpha;
    }
    
    public static void setMousePos(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        MainWindow window = mc.getWindow();
        
        double xPos = mouseX * window.getScreenWidth()  / window.getGuiScaledWidth();
        double yPos = mouseY * window.getScreenHeight() / window.getGuiScaledHeight();
        
        MouseHelper mouseHandler = mc.mouseHandler;
        ClientReflection.setXPos(mouseHandler, xPos);
        ClientReflection.setYPos(mouseHandler, yPos);
        InputMappings.grabOrReleaseMouse(window.getWindow(), GLFW.GLFW_CURSOR_NORMAL, xPos, yPos);
    }
    
    public static DefaultPlayerSkinType getPlayerDefaultSkinType(AbstractClientPlayerEntity player) {
        ResourceLocation skinLocation = player.getSkinTextureLocation();
        if (DefaultPlayerSkinType.STEVE.skinTex.equals(skinLocation)) return DefaultPlayerSkinType.STEVE;
        if (DefaultPlayerSkinType.ALEX .skinTex.equals(skinLocation)) return DefaultPlayerSkinType.ALEX;
        return DefaultPlayerSkinType.NONE;
    }
    
    public static enum DefaultPlayerSkinType {
        STEVE(new ResourceLocation("textures/entity/steve.png")),
        ALEX(new ResourceLocation("textures/entity/alex.png")),
        NONE(null);
        
        private final ResourceLocation skinTex;
        private DefaultPlayerSkinType(ResourceLocation skinTex) {
            this.skinTex = skinTex;
        }
    }
    
    public static void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public static void setRotationAngleDegrees(ModelRenderer modelRenderer, float x, float y, float z) {
        setRotationAngle(modelRenderer, x * MathUtil.DEG_TO_RAD, y * MathUtil.DEG_TO_RAD, z * MathUtil.DEG_TO_RAD);
    }
    
    public static void rotateAngles(ModelRenderer modelRenderer, float xRotSecond) {
        Vector3f angles = rotateAngles(modelRenderer.xRot, modelRenderer.yRot, modelRenderer.zRot, xRotSecond);
        modelRenderer.xRot = angles.x();
        modelRenderer.yRot = angles.y();
        modelRenderer.zRot = angles.z();
    }
    
    public static Vector3f rotateAngles(float xRot, float yRot, float zRot, float xRotSecond) {
        Quaternion quat = MathUtil.quaternionZYX(xRot, yRot, zRot, false);
        Quaternion q2 = Vector3f.XP.rotation(xRotSecond);
        q2.mul(quat);
        Matrix4ZYX rotMatrix = new Matrix4ZYX(q2);
        Vector3f rotVec = rotMatrix.rotationVec();
        return rotVec;
    }
    
    public static void clearCubes(ModelRenderer modelRenderer) {
        ClientReflection.setCubes(modelRenderer, new ObjectArrayList<>());
    }
    
    public static void editLatestCube(ModelRenderer modelRenderer, Consumer<ModelRenderer.ModelBox> edit) {
        List<ModelRenderer.ModelBox> cubes = ClientReflection.getCubes(modelRenderer);
        if (cubes.isEmpty()) return;
        ModelRenderer.ModelBox box = cubes.get(cubes.size() - 1);
        edit.accept(box);
    }
    
    public static void setFaceUv(ModelRenderer.ModelBox cube, Direction faceDir, float u0, float v0, float u1, float v1, Model model) {
        if (faceDir.getAxis() == Direction.Axis.Y) {
            faceDir = faceDir.getOpposite();
        }
        Vector3f faceNormal = faceDir.step();
        Optional<ModelRenderer.TexturedQuad> faceOptional = Arrays.stream(ClientReflection.getPolygons(cube))
                .filter(quad -> quad.normal.equals(faceNormal)).findFirst();
        if (faceOptional.isPresent()) {
            u0 /= model.texWidth;
            v0 /= model.texHeight;
            u1 /= model.texWidth;
            v1 /= model.texHeight;
            ModelRenderer.TexturedQuad face = faceOptional.get();
            if (face.vertices[0].u < face.vertices[1].u) {
                float swap = u0;
                u0 = u1;
                u1 = swap;
            }
            if (face.vertices[0].v > face.vertices[2].v) {
                float swap = v0;
                v0 = v1;
                v1 = swap;
            }
            face.vertices[0] = face.vertices[0].remap(u1, v0);
            face.vertices[1] = face.vertices[1].remap(u0, v0);
            face.vertices[2] = face.vertices[2].remap(u0, v1);
            face.vertices[3] = face.vertices[3].remap(u1, v1);
        }
    }
    
    public static ModelRenderer getArm(BipedModel<?> model, HandSide side) {
        return side == HandSide.LEFT ? model.leftArm : model.rightArm;
    }
    
    public static ModelRenderer getArmOuter(PlayerModel<?> model, HandSide side) {
        return side == HandSide.LEFT ? model.leftSleeve : model.rightSleeve;
    }
    
    public static void setupForFirstPersonRender(PlayerModel<AbstractClientPlayerEntity> model, AbstractClientPlayerEntity player) {
        model.rightArmPose = BipedModel.ArmPose.EMPTY;
        model.leftArmPose = BipedModel.ArmPose.EMPTY;
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        PlayerAnimationHandler.getPlayerAnimator().setupLayerFirstPersonRender(model);
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }
    
    public static void addItemReferenceQuote(List<ITextComponent> tooltip, Item item) {
        tooltip.add(new StringTextComponent(" "));
        ResourceLocation itemId = item.getRegistryName();
        tooltip.add(new TranslationTextComponent("item." + itemId.getNamespace() + "." + itemId.getPath() + ".reference_quote").withStyle(TextFormatting.ITALIC, TextFormatting.DARK_GRAY));
    }
    
    public static ITextComponent donoItemTooltip(String donoUsername) {
        return new TranslationTextComponent("item.jojo.dono_tooltip", donoUsername).withStyle(TextFormatting.DARK_GRAY);
    }
    
    
    public static PosOnScreen posOnScreen(Vector3d posInWorld, ActiveRenderInfo camera, MatrixStack matrixStack, Matrix4f projection) {
        Vector3d cameraPos = camera.getPosition();
        Vector3d vecToEntity = posInWorld.subtract(cameraPos);
        
        Matrix4f projectionMatrix = projection.copy();
        Matrix4f viewMatrix = matrixStack.last().pose();
        projectionMatrix.multiply(viewMatrix);
        Vector3f clip = MathUtil.multiplyPoint(projectionMatrix, vecToEntity);
        
        Vector2f posOnScreen = new Vector2f(clip.x() * 0.5F + 0.5F, clip.y() * 0.5F + 0.5F);
        boolean isOnScreen = MathHelper.abs(clip.x()) < 1 && MathHelper.abs(clip.y()) < 1 && clip.z() < 1;
        return new PosOnScreen(posOnScreen, isOnScreen);
    }
    
    public static class PosOnScreen {
        public static final PosOnScreen SCREEN_CENTER = new PosOnScreen(new Vector2f(0.5F, 0.5F), true);
        
        public final Vector2f pos;
        public final boolean isOnScreen;
        
        private PosOnScreen(Vector2f pos, boolean isOnScreen) {
            this.pos = pos;
            this.isOnScreen = isOnScreen;
        }
    }
    
    
    public static Button.ITooltip buttonMessageTooltip(Screen screen) {
        return (Button button, MatrixStack matrixStack, int x, int y) -> {
            screen.renderTooltip(matrixStack, button.getMessage(), x, y);
        };
    }
}
