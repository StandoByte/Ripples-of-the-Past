package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonAbandonButtonPacket;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonAbandonTabGui extends HamonTabGui {
    private HamonTabGui previousTab;
    private final List<IReorderingProcessor> descLines;
    private HamonScreenButton yesButton;
    private HamonScreenButton noButton;

    HamonAbandonTabGui(Minecraft minecraft, HamonScreen screen, int index, String title) {
        super(minecraft, screen, index, title, -1, -1);
        int textWidth = HamonScreen.WINDOW_WIDTH - 30;
        descLines = minecraft.font.split(new TranslationTextComponent("hamon.abandon.tab.desc"), textWidth);
        setPrevTab(screen.selectedTab);
    }
    
    void setPrevTab(HamonTabGui tab) {
        this.previousTab = tab;
    }
    
    @Override
    void addButtons() {
        screen.addButton(yesButton = new HamonScreenButton(screen.windowPosX() + 13, screen.windowPosY() + 192, 100, 20, new TranslationTextComponent("gui.yes"), button -> {
            PacketManager.sendToServer(new ClHamonAbandonButtonPacket());
            minecraft.setScreen(null);
            minecraft.mouseHandler.grabMouse();
        }));
        screen.addButton(noButton = new HamonScreenButton(screen.windowPosX() + 117, screen.windowPosY() + 192, 100, 20, new TranslationTextComponent("gui.no"), button -> {
            if (previousTab == null) previousTab = screen.selectableTabs[0];
            screen.selectTab(previousTab);
        }));
    }

    @Override
    List<HamonScreenButton> getButtons() {
        return ImmutableList.of(yesButton, noButton);
    }

    @Override
    protected void drawActualContents(HamonScreen screen, MatrixStack matrixStack, int mouseX, int mouseY) {}

    @Override
    protected void drawText(MatrixStack matrixStack) {
        int textX = intScrollX + 5 + (HamonScreen.WINDOW_WIDTH - 30) / 2;
        int textY = intScrollY + 2;
        for (int i = 0; i < descLines.size(); i++) {
            textY += 9;
            ClientUtil.drawCenteredString(matrixStack, minecraft.font, descLines.get(i), textX, textY, 0xFFFFFF);
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
