package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonAbandonButtonPacket;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonAbandonTabGui extends HamonTabGui {
    private HamonTabGui previousTab;
    private final List<IReorderingProcessor> descLines;
    private final List<IReorderingProcessor> descLines2;
    
    HamonAbandonTabGui(Minecraft minecraft, HamonScreen screen, String title) {
        super(minecraft, screen, title, -1, -1);
        int textWidth = HamonScreen.WINDOW_WIDTH - 30;
        descLines = minecraft.font.split(new TranslationTextComponent("hamon.abandon.tab.desc"), textWidth);
        descLines2 = minecraft.font.split(new TranslationTextComponent("hamon.abandon.tab.desc2"), textWidth);
        setPrevTab(screen.selectedTab);
    }
    
    void setPrevTab(HamonTabGui tab) {
        this.previousTab = tab;
    }
    
    @Override
    public void addButtons() {
        addButton(new HamonScreenButton(screen.windowPosX() + 13, screen.windowPosY() + 96, 100, 20, new TranslationTextComponent("gui.yes"), button -> {
            PacketManager.sendToServer(new ClHamonAbandonButtonPacket());
            minecraft.setScreen(null);
            minecraft.mouseHandler.grabMouse();
        }));
        addButton(new HamonScreenButton(screen.windowPosX() + 117, screen.windowPosY() + 96, 100, 20, new TranslationTextComponent("gui.no"), button -> {
            if (previousTab == null) previousTab = screen.statsTab;
            screen.selectTab(previousTab);
        }));
    }

    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void drawText(MatrixStack matrixStack) {
        int textX = intScrollX + 5 + (WINDOW_WIDTH - 30) / 2;
        int textY = intScrollY + 2;
        for (IReorderingProcessor line : descLines) {
            textY += 9;
            ClientUtil.drawCenteredString(matrixStack, minecraft.font, line, textX, textY, 0xFFFFFF);
        }
        textY += 18;
        for (IReorderingProcessor line : descLines2) {
            textY += 9;
            ClientUtil.drawCenteredString(matrixStack, minecraft.font, line, textX, textY, 0xFFFFFF);
        }
    }

    @Override
    boolean mouseClicked(double mouseX, double mouseY, int mouseButton, boolean mouseInsideWindow) {
        return false;
    }

    @Override
    boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    @Override
    void updateTab() {}
}
