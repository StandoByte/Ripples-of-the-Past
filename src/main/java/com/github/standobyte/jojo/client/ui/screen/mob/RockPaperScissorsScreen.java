package com.github.standobyte.jojo.client.ui.screen.mob;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSGamePickPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class RockPaperScissorsScreen extends Screen {
    public static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/rock_paper_scissors.png");
    private static final int WIDTH = 173;
    private static final int HEIGHT = 122;

    private final RockPaperScissorsGame game;
    @Nullable
    private Pick pickMouseOver;
    @Nullable
    private Pick pickClicked;

    public RockPaperScissorsScreen(RockPaperScissorsGame game, Entity player1, Entity player2) {
        super(new TranslationTextComponent("jojo.screen.versus", player1.getDisplayName(), player2.getDisplayName()));
        this.game = game;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (game == null) {
            this.onClose();
            return false;
        }
        if (pickMouseOver != null) {
            pickClicked = pickMouseOver;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (game == null) {
            this.onClose();
            return false;
        }
        if (pickClicked != null && pickMouseOver == pickClicked) {
            game.makeAPick(ClientUtil.getClientPlayer(), pickMouseOver);
            PacketManager.sendToServer(new ClRPSGamePickPacket(pickMouseOver));
            pickClicked = null;
            return true;
        }
        pickClicked = null;

        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (game == null) {
            this.onClose();
            return;
        }
        int windowX = (width - WIDTH) / 2;
        int windowY = (height - HEIGHT) / 2;
        renderScreen(matrixStack, windowX, windowY);
        renderElements(matrixStack, windowX, windowY, mouseX, mouseY);
    }

    private void renderScreen(MatrixStack matrixStack, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, windowX, windowY, 0, 0, WIDTH, HEIGHT);
        RenderSystem.enableRescaleNormal();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderElements(MatrixStack matrixStack, int windowX, int windowY, int mouseX, int mouseY) {
        int nonTieRound = game.player1.getPreviousPicks().size();
        int y = 17;
        for (int i = 0; i < nonTieRound; i++) {
            Pick pick1 = game.player1.getPreviousPicks().get(i);
            blit(matrixStack, windowX + 66, windowY + y, getIconTexX(pick1), 122, 16, 16);
            Pick pick2 = game.player2.getPreviousPicks().get(i);
            blit(matrixStack, windowX + 101, windowY + y, getIconTexX(pick2), 122, 16, 16);
            y += 18;
        }
        if (game.player1.getCurrentPick() == null) {
            pickMouseOver = pickMouseOver(windowX, windowY, mouseX, mouseY, nonTieRound);
            blit(matrixStack, windowX + 7, windowY + 16 + nonTieRound * 18, getButtonTexX(Pick.ROCK), 122, 18, 18);
            blit(matrixStack, windowX + 25, windowY + 16 + nonTieRound * 18, getButtonTexX(Pick.PAPER), 122, 18, 18);
            blit(matrixStack, windowX + 43, windowY + 16 + nonTieRound * 18, getButtonTexX(Pick.SCISSORS), 122, 18, 18);

            blit(matrixStack, windowX + 8, windowY + 17 + nonTieRound * 18, getIconTexX(Pick.ROCK), 122, 16, 16);
            blit(matrixStack, windowX + 26, windowY + 17 + nonTieRound * 18, getIconTexX(Pick.PAPER), 122, 16, 16);
            blit(matrixStack, windowX + 44, windowY + 17 + nonTieRound * 18, getIconTexX(Pick.SCISSORS), 122, 16, 16);
        }
        else {
            blit(matrixStack, windowX + 66, windowY + 17 + nonTieRound * 18, getIconTexX(game.player1.getCurrentPick()), 122, 18, 18);
        }
        Pick opponentPick = game.player2.getCurrentPick();
        blit(matrixStack, windowX + 121, windowY + 4 + nonTieRound * 18, 173, 0, 24, 26);
        blit(matrixStack, windowX + 128, windowY + 5 + nonTieRound * 18, opponentPick != null ? getIconTexX(opponentPick) : 102, 122, 16, 16);

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
//        font.draw(matrixStack, title, windowX + 8, windowY + 6, 0x404040);
        minecraft.font.drawShadow(matrixStack, new StringTextComponent(game.player1.getScore() + " - " + game.player2.getScore()), windowX + 8, windowY + 108, 0xFFFFFF);
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    }
    
    @Nullable
    private Pick pickMouseOver(int windowX, int windowY, int mouseX, int mouseY, int nonTieRound) {
        int x0 = windowX + 7;
        int y0 = windowY + 16 + nonTieRound * 18;
        if (mouseY >= y0 && mouseY < y0 + 18) {
            if (mouseX >= x0) {
                if (mouseX < x0 + 18) {
                    return Pick.ROCK;
                }
                if (mouseX < x0 + 36) {
                    return Pick.PAPER;
                }
                if (mouseX < x0 + 54) {
                    return Pick.SCISSORS;
                }
            }
        }
        return null;
    }
    
    private int getButtonTexX(Pick pick) {
        if (pickClicked != null) {
            return pick == pickClicked ? 18 : 0;
        }
        return pick == pickMouseOver ? 36 : 0;
    }
    
    private int getIconTexX(Pick pick) {
        if (pick == null) return 118;
        switch (pick) {
        case ROCK:
            return 54;
        case PAPER:
            return 70;
        case SCISSORS:
            return 86;
        }
        return 102;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
