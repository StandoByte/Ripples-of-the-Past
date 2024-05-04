package com.github.standobyte.jojo.client.ui.screen.standskin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.render.FlameModelRenderer;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.screen.JojoStuffScreen;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;

public class StandSkinsScreen extends Screen {
    public static final ResourceLocation TEXTURE_MAIN_WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins.png");
    private static final ResourceLocation TEXTURE_BG = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins_bg.png");
    
    private static final int WINDOW_WIDTH = 230;
    private static final int WINDOW_HEIGHT = 180;
    private static final int WINDOW_INSIDE_X = 7;
    private static final int WINDOW_INSIDE_WIDTH = 201;
    private static final int WINDOW_INSIDE_Y = 20;
    private static final int WINDOW_INSIDE_HEIGHT = 153;
    
    private static final int SKINS_IN_ROW = 3;
    
    private static ResourceLocation latestStand = null;
    private static int latestScroll;
    
    @Nullable private Screen prevScreen;
    private IStandPower standCap;
    private List<SkinView> skins;
    private int tickCount = 0;
    private int scroll;
    
    @Nullable
    private SkinFullView skinFullView;
    
    public StandSkinsScreen(IStandPower power) {
        super(StringTextComponent.EMPTY);
        setStandCap(power);
    }
    
    public static void openScreen(@Nullable Screen prevScreen) {
        IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(playerStand -> {
            if (playerStand.hasPower()) {
                StandSkinsScreen screen = new StandSkinsScreen(playerStand);
                screen.prevScreen = prevScreen;
                Minecraft.getInstance().setScreen(screen);
            }
        });
    }
    
    private void setStandCap(IStandPower standCap) {
        this.standCap = standCap;
        this.skins = Streams.mapWithIndex(StandSkinsManager.getInstance()
                .getStandSkinsView(standCap.getType().getRegistryName())
                .stream(), (skin, index) -> {
                    int x = 12 + (int) (index % SKINS_IN_ROW) * (SkinView.boxWidth + 12);
                    int y = 3 + (int) (index / SKINS_IN_ROW) * (SkinView.boxHeight + 3);
                    return new SkinView(skin, x, y);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public void tick() {
        tickCount++;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!standCap.hasPower()) {
            onClose();
            return;
        }
        
        renderBackground(matrixStack, 0);
        renderBgPattern(matrixStack);
        renderContents(mouseX, mouseY, partialTick);
        renderWindow(matrixStack);
        
        JojoStuffScreen.renderStandTabs(matrixStack, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), true, 
                mouseX, mouseY, this, JojoStuffScreen.StandTab.SKINS, standCap);
        
        for (Widget button : buttons) {
            button.render(matrixStack, mouseX, mouseY, partialTick);
        }
    }
    
    private boolean isSkinSelected(StandSkin skin) {
        StandInstance stand = standCap.getStandInstance().get();
        return stand.getSelectedSkin().equals(skin.getNonDefaultLocation());
    }
    
    @SuppressWarnings("deprecation")
    private void renderBgPattern(MatrixStack matrixStack) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(getWindowX() + 4, getWindowY() + 4, 0);
        minecraft.getTextureManager().bind(TEXTURE_BG);
        
        int x = getWindowX() + WINDOW_INSIDE_X;
        int y = getWindowY() + WINDOW_INSIDE_Y;
        ClientUtil.enableGlScissor(x, y, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT);
        int l = -scroll % 16;
        for (int i1 = -1; i1 <= 12; ++i1) {
            for (int j1 = -1; j1 <= 11; ++j1) {
                blit(matrixStack, 5 + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
        ClientUtil.disableGlScissor();
        
        RenderSystem.popMatrix();
    }
    
    private void renderWindow(MatrixStack matrixStack) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
        blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    @SuppressWarnings("deprecation")
    private void renderContents(int mouseX, int mouseY, float partialTick) {
        int x = getWindowX() + WINDOW_INSIDE_X;
        int y = getWindowY() + WINDOW_INSIDE_Y;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        MatrixStack matrixStack = new MatrixStack();
        float ticks = tickCount + partialTick;
        if (skinFullView != null) {
            skinFullView.render(matrixStack, mouseX, mouseY, ticks);
        }
        else {
            RenderSystem.translatef(0, -scroll, 0);
            ClientUtil.enableGlScissor(x, y, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT);
            // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! filter to only visible
            for (SkinView skin : skins) {
                skin.renderStand(matrixStack, mouseX, mouseY, ticks);
            }

            Optional<SkinView> hoveredSkin = getSkinAt(mouseX, mouseY);
            for (SkinView skin : skins) {
                skin.renderAdditional(matrixStack, mouseX, mouseY, ticks, 
                        hoveredSkin.map(hovered -> skin == hovered).orElse(false));
            }
            ClientUtil.disableGlScissor();
        }
        RenderSystem.popMatrix();
    }
    
    private Optional<SkinView> getSkinAt(int mouseX, int mouseY) {
        if (skinFullView != null) return Optional.empty();
        
        int x = mouseX - (getWindowX() + WINDOW_INSIDE_X);
        int y = mouseY - (getWindowY() + WINDOW_INSIDE_Y);
        if (
                x >= 0 && x <= WINDOW_INSIDE_WIDTH && 
                y >= 0 && y <= WINDOW_INSIDE_HEIGHT) {
            int yWithScroll = y + scroll;
            
            return skins.stream().filter(skin -> 
            x >           skin.x && x           <= skin.x + SkinView.boxWidth && 
            yWithScroll > skin.y && yWithScroll <= skin.y + SkinView.boxHeight)
                    .findFirst();
        }
        else {
            return Optional.empty();
        }
    }
    
    private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
    private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (JojoStuffScreen.mouseClick(mouseX, mouseY, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), 
                JojoStuffScreen.TabsEnumType.STAND)) {
            return true;
        }
        
        Optional<SkinView> hoveredBox = getSkinAt((int) mouseX, (int) mouseY);
        switch (mouseButton) {
        case GLFW.GLFW_MOUSE_BUTTON_1:
            if (skinFullView == null && hoveredBox.isPresent()) {
                selectSkin(hoveredBox.get().skin);
                return true;
            }
            break;
        case GLFW.GLFW_MOUSE_BUTTON_2:
            if (skinFullView == null) {
                return hoveredBox.map(skinBox -> {
                    setFullViewSkin(skinBox);
                    return true;
                }).orElse(false);
            }
            else {
                setFullViewSkin(null);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (super.mouseScrolled(mouseX, mouseY, scrollDelta)) {
            return true;
        }
        
        if (skinFullView != null) {
            skinFullView.yRot += scrollDelta * 0.05F;
            return true;
        }
        else {
            addScroll((int) (-scrollDelta * 10));
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY)) {
            return true;
        }
        
        if (skinFullView != null) {
            skinFullView.yRot -= dragX / WINDOW_INSIDE_WIDTH * 2;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (skinFullView != null) {
                selectSkin(skinFullView.skin);
                return true;
            }
            else {
                
            }
        }
        
        else {
            Direction2D arrowKey = InputHandler.getArrowKey(keyCode);
            if (arrowKey != null) {
                if (skinFullView != null) {
                    float yRot = skinFullView.yRot;
                    switch (arrowKey) {
                    case RIGHT:
                    case DOWN:
                        setFullViewSkin(skins.get((skinFullView.skinIndex + 1) % skins.size()));
                        break;
                    case LEFT:
                    case UP:
                        setFullViewSkin(skins.get((skinFullView.skinIndex - 1 + skins.size()) % skins.size()));
                        break;
                    }
                    skinFullView.yRot = yRot;
                    return true;
                }
                else {
                    switch (arrowKey) {
                    case RIGHT:
                        
                        break;
                    case DOWN:
                        
                        break;
                    case LEFT:
                        
                        break;
                    case UP:
                        
                        break;
                    }
                    return true;
                }
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void onClose() {
        if (skinFullView != null) {
            setFullViewSkin(null);
        }
        else if (prevScreen != null) {
            minecraft.setScreen(prevScreen);
        }
        else {
            super.onClose();
        }
    }
    
    private void selectSkin(StandSkin skin) {
        PacketManager.sendToServer(new ClSetStandSkinPacket(Optional.of(skin.resLoc), skin.standTypeId));
    }
    
    private void addScroll(int scroll) {
        this.scroll = MathHelper.clamp(this.scroll + scroll, 0, getMaxScroll());
    }
    
    public int getMaxScroll() {
        int rowsCount = (skins.size() - 1) / SKINS_IN_ROW + 1;
        return Math.max((SkinView.boxHeight + 4) * rowsCount - WINDOW_INSIDE_HEIGHT, 0);
    }
    
    
    private void setFullViewSkin(@Nullable SkinView skin) {
        if (skin != null) {
            skinFullView = new SkinFullView(skin.skin, skins.indexOf(skin));
        }
        else {
            skinFullView = null;
        }
    }
    
    private class SkinView {
        public final StandSkin skin;
        public final int x;
        public final int y;
        public static final int boxWidth = 51;
        public static final int boxHeight = 72;
        
        public SkinView(StandSkin skin, int x, int y) {
            this.skin = skin;
            this.x = x;
            this.y = y;
        }
        
        public void renderStand(MatrixStack matrixStack, int mouseX, int mouseY, float ticks) {
            minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
//            blit(matrixStack, x, y, 98, 182, width, height);
            
            StandType<?> standType = standCap.getType();
            if (standType instanceof EntityStandType) {
                // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! SP hair isn't animated (decouple model methods from entity instance as much as possible)
                renderStandModel(x + boxWidth / 2, y + boxHeight / 2 + 26.6667F, 25, 0, 
                        (EntityStandType<?>) standType, skin, ticks);
            }
        }
        
        @SuppressWarnings("deprecation")
        public void renderAdditional(MatrixStack matrixStack, int mouseX, int mouseY, 
                float ticks, boolean isHovered) {
            minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
            if (isSkinSelected(skin)) {
                blit(matrixStack, x + boxWidth - 18, y + 2, 0, 192, 16, 16);
            }
            if (isHovered) {
                float[] color = ClientUtil.rgb(skin.color);
                RenderSystem.enableBlend();
                RenderSystem.color4f(color[0], color[1], color[2], 1);
                blit(matrixStack, x - 2, y - 2, 
                        32, 180, boxWidth + 4, boxHeight + 4);
                RenderSystem.color4f(1, 1, 1, 1);
                RenderSystem.disableBlend();
            };
        }
    }
    
    
    private class SkinFullView {
        public final StandSkin skin;
        public final int skinIndex;
        public float yRot = 0;
        
        public SkinFullView(StandSkin skin, int skinIndex) {
            this.skin = skin;
            this.skinIndex = skinIndex;
        }
        
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float ticks) {
            ResourceLocation standIcon = JojoModUtil.makeTextureLocation("power", 
                    skinFullView.skin.standTypeId.getNamespace(), skinFullView.skin.standTypeId.getPath());
            standIcon = skinFullView.skin.getRemappedResPath(standIcon).or(standIcon);
            minecraft.getTextureManager().bind(standIcon);
            blit(matrixStack, 4, 4, 0, 0, 16, 16, 16, 16);
            
            StandType<?> standType = standCap.getType();
            if (standType instanceof EntityStandType) {
                renderStandModel(WINDOW_WIDTH / 2 - 15, 133.33F, 55, yRot * 180F, 
                        (EntityStandType<?>) standType, skin, ticks);    
            }
            
            if (isSkinSelected(skin)) {
                minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
                blit(matrixStack, WINDOW_INSIDE_WIDTH - 20, 4, 0, 192, 16, 16);
            }

            RenderSystem.enableBlend();
        }
    }

    @SuppressWarnings("deprecation")
    public static void renderStandModel(float posX, float posY, float scale, float yRot, 
            EntityStandType<?> standType, StandSkin standSkin, float ticks) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(posX, posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(0.0D, -1.4D, 0.0D);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(0.0F);
        Quaternion quaternion1 = Vector3f.YP.rotationDegrees(yRot);
        quaternion.mul(quaternion1);
        matrixStack.mulPose(quaternion);
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderermanager.overrideCameraOrientation(quaternion1);
        
        // rotate lighting
        matrixStack.last().normal().mul(Vector3f.YP.rotationDegrees(60));
        
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        EntityRendererManager entityRendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
        StandEntityRenderer<?, ?> renderer = (StandEntityRenderer<?, ?>) entityRendererManager.renderers.get(standType.getEntityType());
        FlameModelRenderer.renderingUI = true;
        RenderSystem.runAsFancy(() -> {
            renderer.renderIdleWithSkin(matrixStack, standSkin, buffer, ticks);
        });
        FlameModelRenderer.renderingUI = false;
        buffer.endBatch();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }
}
