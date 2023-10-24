package com.github.standobyte.jojo.client.ui.screen.standskin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.render.entity.standskin.StandSkin;
import com.github.standobyte.jojo.client.render.entity.standskin.StandSkinsManager;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;

public class StandSkinsScreen extends Screen {
    private static final ResourceLocation TEXTURE_MAIN_WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins.png");
    private static final ResourceLocation TEXTURE_BG = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins_bg.png");
    
    private static final int WINDOW_WIDTH = 195;
    private static final int WINDOW_HEIGHT = 180;
    public static final int WINDOW_INSIDE_X = 7;
    public static final int WINDOW_INSIDE_WIDTH = 165;
    public static final int WINDOW_INSIDE_Y = 20;
    public static final int WINDOW_INSIDE_HEIGHT = 153;
    
    private static ResourceLocation latestStand = null;
    private static int latestScroll;
    
    private IStandPower standCap;
    private List<SkinView> skins;
    private int tickCount = 0;
    private int scroll;
    
    @Nullable
    private SkinFullView skinFullView;
    
    private StandSkinsScreen() {
        super(StringTextComponent.EMPTY);
    }
    
    public static void openScreen() {
        IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(playerStand -> {
            if (playerStand.hasPower()) {
                StandSkinsScreen screen = new StandSkinsScreen();
                screen.setStandCap(playerStand);
                Minecraft.getInstance().setScreen(screen);
            }
        });
    }
    
    private void setStandCap(IStandPower standCap) {
        this.standCap = standCap;
        this.skins = Streams.mapWithIndex(StandSkinsManager.getInstance()
                .getStandSkinsView(standCap.getType().getRegistryName())
                .stream(), (skin, index) -> {
                    int x = 3 + (int) (index % 3) * (SkinView.width + 3);
                    int y = 3 + (int) (index / 3) * (SkinView.height + 3);
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
        renderWindow(matrixStack);
        matrixStack.pushPose();
        matrixStack.translate(getWindowX() + WINDOW_INSIDE_X, getWindowY() + WINDOW_INSIDE_Y, 0);
        renderContents(matrixStack, mouseX, mouseY, partialTick);
        matrixStack.popPose();
        
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
        RenderSystem.enableDepthTest();
        RenderSystem.translatef(getWindowX() + 4, getWindowY() + 4, 750.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(matrixStack, 4680, 2260, -4680, -2260, -0x1000000);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, -750.0F);
        RenderSystem.depthFunc(518);
        fill(matrixStack, WINDOW_WIDTH - 8, WINDOW_HEIGHT - 8, 0, 0, -0x1000000);
        RenderSystem.depthFunc(515);
        minecraft.getTextureManager().bind(TEXTURE_BG);
        int l = scroll % 16;
        for (int i1 = -1; i1 <= 11; ++i1) {
            for (int j1 = -1; j1 <= 11; ++j1) {
                blit(matrixStack, 5 + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
        RenderSystem.popMatrix();
    }
    
    private void renderWindow(MatrixStack matrixStack) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
        blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    private void renderContents(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        float ticks = tickCount + partialTick;
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        if (skinFullView != null) {
            skinFullView.render(matrixStack, mouseX, mouseY, ticks, buffer);
        }
        else {
            Optional<SkinView> hoveredSkin = getSkinAt(mouseX, mouseY);
            for (SkinView skin : skins) {
                skin.render(matrixStack, mouseX, mouseY, ticks, buffer, 
                        hoveredSkin.map(hovered -> skin == hovered).orElse(false));
            }
        }
        buffer.endBatch();
    }
    
    private Optional<SkinView> getSkinAt(int mouseX, int mouseY) {
        if (skinFullView != null) return Optional.empty();
        
        int x = mouseX - (getWindowX() + WINDOW_INSIDE_X);
        int y = mouseY - (getWindowY() + WINDOW_INSIDE_Y);
        return skins.stream().filter(skin -> 
        x > skin.x && x <= skin.x + SkinView.width && 
        y > skin.y && y <= skin.y + SkinView.height)
                .findFirst();
    }
    
    private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
    private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }
        
        Optional<SkinView> hoveredBox = getSkinAt((int) mouseX, (int) mouseY);
        switch (mouseButton) {
        case GLFW.GLFW_MOUSE_BUTTON_1:
            if (skinFullView == null && hoveredBox.isPresent()) {
                StandSkin skin = hoveredBox.get().skin;
                PacketManager.sendToServer(new ClSetStandSkinPacket(Optional.of(skin.resLoc), skin.standTypeId));
                return true;
            }
            break;
        case GLFW.GLFW_MOUSE_BUTTON_2:
            if (skinFullView == null) {
                return hoveredBox.map(skinBox -> {
                    setFullViewSkin(skinBox.skin);
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
    public void onClose() {
        if (skinFullView != null) {
            setFullViewSkin(null);
        }
        else {
            super.onClose();
        }
    }
    
    
    private void setFullViewSkin(@Nullable StandSkin skin) {
        if (skin != null) {
            skinFullView = new SkinFullView(skin);
        }
        else {
            skinFullView = null;
        }
    }
    
    
    private class SkinView {
        public final StandSkin skin;
        public final int x;
        public final int y;
        public static final int width = 51;
        public static final int height = 72;
        
        public SkinView(StandSkin skin, int x, int y) {
            this.skin = skin;
            this.x = x;
            this.y = y;
        }
        
        @SuppressWarnings("deprecation")
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, 
                float ticks, IRenderTypeBuffer buffer, boolean isHovered) {
            minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
//            blit(matrixStack, x, y, 98, 182, width, height);
            
            StandType<?> standType = standCap.getType();
            if (standType instanceof EntityStandType) {
                matrixStack.pushPose();
                matrixStack.translate(x + width / 2, y + height / 2, 100);
                matrixStack.translate(0.5F, 0, 0);
                
                // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! lighting is weird
                matrixStack.scale(-25, 25, 25);
                matrixStack.translate(0, -0.35F, 0);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));

                // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! SP hair isn't animated (decouple model methods from entity instance as much as possible)
                StandEntityRenderer<?, ?> renderer = (StandEntityRenderer<?, ?>) Minecraft.getInstance()
                        .getEntityRenderDispatcher().renderers.get(((EntityStandType<?>) standType).getEntityType());
                RenderSystem.runAsFancy(() -> {
                    renderer.renderIdleWithSkin(matrixStack, skin, buffer, ticks);
                });
                matrixStack.popPose();
            }

            RenderSystem.enableBlend();
            minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
            if (isSkinSelected(skin)) {
                blit(matrixStack, x + width - 18, y + 2, 0, 192, 16, 16);
            }
            if (isHovered) {
                float[] color = ClientUtil.rgb(skin.color);
                RenderSystem.color3f(color[0], color[1], color[2]);
                blit(matrixStack, x - 2, y - 2, 32, 180, width + 4, height + 4);
                RenderSystem.color3f(1, 1, 1);
            }
        }
    }
    
    
    private class SkinFullView {
        public final StandSkin skin;
        public float yRot = 0;
        
        public SkinFullView(StandSkin skin) {
            this.skin = skin;
        }
        
        @SuppressWarnings("deprecation")
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, 
                float ticks, IRenderTypeBuffer buffer) {
//            minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
            
            StandType<?> standType = standCap.getType();
            if (standType instanceof EntityStandType) {
                matrixStack.pushPose();
                matrixStack.translate(82.5F, 0, 100);

                matrixStack.scale(-55, 55, 55);
                matrixStack.translate(0, 1.02F, 0);
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F + yRot * 180F));

                StandEntityRenderer<?, ?> renderer = (StandEntityRenderer<?, ?>) Minecraft.getInstance()
                        .getEntityRenderDispatcher().renderers.get(((EntityStandType<?>) standType).getEntityType());
                RenderSystem.runAsFancy(() -> {
                    renderer.renderIdleWithSkin(matrixStack, skin, buffer, ticks);
                });
                matrixStack.popPose();
            }

            RenderSystem.enableBlend();
        }
    }
}
