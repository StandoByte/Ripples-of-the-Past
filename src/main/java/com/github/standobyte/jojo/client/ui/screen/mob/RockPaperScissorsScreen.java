package com.github.standobyte.jojo.client.ui.screen.mob;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.RPSCheat;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSGameInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSPickThoughtsPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class RockPaperScissorsScreen extends ChatScreen {
    public static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/rock_paper_scissors.png");
    private static final int WIDTH = 173;
    private static final int HEIGHT = 129;

    public final RockPaperScissorsGame game;
    private int nonTieRound;
    @Nullable
    private Pick pickMouseOver;
    private boolean sendThoughts = false;
    @Nullable
    private Pick pickClicked;
    private Button cheatButton;
    private boolean cheatedThisRound = false;
    @Nullable
    private PowerClassification cheatPower;
    private static final int DEFAULT_CHEAT_BUTTON_Y = 10;

    public RockPaperScissorsScreen(RockPaperScissorsGame game) {
        super("");
        this.game = game;
    }

    @Override
    protected void init() {
        super.init();
        cheatButton = new Button((width - WIDTH) / 2 + WIDTH - 25, (height - HEIGHT) / 2 + DEFAULT_CHEAT_BUTTON_Y, 20, 20, StringTextComponent.EMPTY, button -> {
            if (cheatPower != null) {
                PlayerEntity playerEntity = minecraft.player;
                RPSCheat cheat = game.getCheat(playerEntity, cheatPower);
                if (cheat != null) {
                    cheatedThisRound = true;
                    cheat.cheat(game, game.player1, playerEntity.level);
                }
                PacketManager.sendToServer(ClRPSGameInputPacket.cheat(cheatPower));
            }
        });
        addButton(cheatButton);
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
            game.makeAPick(ClientUtil.getClientPlayer(), pickMouseOver, false);
            PacketManager.sendToServer(ClRPSGameInputPacket.pick(pickMouseOver));
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
        if (game.checkNewRound()) {
            cheatedThisRound = false;
        }
        int windowX = (width - WIDTH) / 2;
        int windowY = (height - HEIGHT) / 2;
        renderScreen(matrixStack, windowX, windowY);
        renderElements(matrixStack, windowX, windowY, mouseX, mouseY);
        super.render(matrixStack, mouseX, mouseY, partialTick);
        renderCheatIcon(matrixStack, windowX, windowY, mouseX, mouseY);
        renderTooltips(matrixStack, windowX, windowY, mouseX, mouseY);
    }

    @SuppressWarnings("deprecation")
    private void renderScreen(MatrixStack matrixStack, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, windowX, windowY, 0, 0, WIDTH, HEIGHT);
        RenderSystem.enableRescaleNormal();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @SuppressWarnings("deprecation")
    private void renderElements(MatrixStack matrixStack, int windowX, int windowY, int mouseX, int mouseY) {
        nonTieRound = game.player1.getPreviousPicks().size();
        int y = 26;
        for (int i = 0; i < nonTieRound; i++) {
            Pick pick1 = game.player1.getPreviousPicks().get(i);
            blit(matrixStack, windowX + 66, windowY + y, getIconTexX(pick1), HEIGHT, 16, 16);
            Pick pick2 = game.player2.getPreviousPicks().get(i);
            blit(matrixStack, windowX + 101, windowY + y, getIconTexX(pick2), HEIGHT + 16, 16, 16);
            if (pick1.beats(pick2)) {
                blit(matrixStack, windowX + 84, windowY + y, 15, HEIGHT + 18, 15, 15);
            }
            else if (pick2.beats(pick1)) {
                blit(matrixStack, windowX + 84, windowY + y, 0, HEIGHT + 18, 15, 15);
            }
            y += 18;
        }
        if (game.player1.getCurrentPick() == null) {
            Pick prevPickMouseOver = pickMouseOver;
            pickMouseOver = pickMouseOver(windowX, windowY, mouseX, mouseY, nonTieRound);
            if (game.player1.canOpponentReadThoughts && pickMouseOver != prevPickMouseOver) {
                sendThoughts = true;
            }
            blit(matrixStack, windowX + 7, windowY + 25 + nonTieRound * 18, getButtonTexX(Pick.ROCK), HEIGHT, 18, 18);
            blit(matrixStack, windowX + 25, windowY + 25 + nonTieRound * 18, getButtonTexX(Pick.PAPER), HEIGHT, 18, 18);
            blit(matrixStack, windowX + 43, windowY + 25 + nonTieRound * 18, getButtonTexX(Pick.SCISSORS), HEIGHT, 18, 18);

            blit(matrixStack, windowX + 8, windowY + 26 + nonTieRound * 18, getIconTexX(Pick.ROCK), HEIGHT, 16, 16);
            blit(matrixStack, windowX + 26, windowY + 26 + nonTieRound * 18, getIconTexX(Pick.PAPER), HEIGHT, 16, 16);
            blit(matrixStack, windowX + 44, windowY + 26 + nonTieRound * 18, getIconTexX(Pick.SCISSORS), HEIGHT, 16, 16);
        }
        else {
            blit(matrixStack, windowX + 66, windowY + 26 + nonTieRound * 18, getIconTexX(game.player1.getCurrentPick()), HEIGHT, 16, 16);
        }
        Pick opponentPick = game.player2.getCurrentPick();
        if (opponentPick != null) {
            blit(matrixStack, windowX + 101, windowY + 26 + nonTieRound * 18, getIconTexX(opponentPick), HEIGHT + 16, 16, 16);
        }
        blit(matrixStack, windowX + 121, windowY + 11 + nonTieRound * 18, 173, 0, 24, 26);
        Pick opponentPickThoughts = opponentPick != null ? opponentPick : game.player2.getPickThoughts();
        blit(matrixStack, windowX + 128, windowY + 12 + nonTieRound * 18, opponentPickThoughts != null ? getIconTexX(opponentPickThoughts) : 102, HEIGHT + 16, 16, 16);

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        minecraft.font.drawShadow(matrixStack, new StringTextComponent(game.player1.getScore() + " - " + game.player2.getScore()), windowX + 78, windowY + 117, 0xFFFFFF);
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    }

    private void renderCheatIcon(MatrixStack matrixStack, int windowX, int windowY, int mouseX, int mouseY) {
        cheatPower = PowerClassification.STAND;
        RPSCheat cheat = game.getCheat(minecraft.player, cheatPower);
        if (cheat == null) {
            cheatPower = PowerClassification.NON_STAND;
            cheat = game.getCheat(minecraft.player, cheatPower);
        }
        cheatButton.active = !cheatedThisRound && cheat != null;
        cheatButton.visible = cheat != null;
        if (cheat != null) {
            IPower<?, ?> cheatPowerCap = IPower.getPowerOptional(minecraft.player, cheatPower).resolve().get();
            cheatButton.y = (height - HEIGHT) / 2 + DEFAULT_CHEAT_BUTTON_Y + nonTieRound * 18;
            minecraft.getTextureManager().bind(cheatPowerCap.clGetPowerTypeIcon());
            blit(matrixStack, cheatButton.x + 2, cheatButton.y + 2, 0, 0, 16, 16, 16, 16);
            if (cheatButton.isMouseOver(mouseX, mouseY)) {
                renderTooltip(matrixStack, minecraft.font.split(new TranslationTextComponent(
                        "jojo.rps.cheat." + cheatPowerCap.getType().getRegistryName().toString().replace(":", ".")), 150), mouseX, mouseY);
            }
        }
    }

    private void renderTooltips(MatrixStack matrixStack, int windowX, int windowY, int mouseX, int mouseY) {
        if (game.player1.getCurrentPick() == null && pickMouseOver != null) {
            renderTooltip(matrixStack, new TranslationTextComponent("jojo.rps." + pickMouseOver.name().toLowerCase()), mouseX, mouseY);
        }
    }
    
    @Nullable
    private Pick pickMouseOver(int windowX, int windowY, int mouseX, int mouseY, int nonTieRound) {
        int x0 = windowX + 7;
        int y0 = windowY + 25 + nonTieRound * 18;
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
    public void tick() {
        super.tick();
        if (sendThoughts) {
            PacketManager.sendToServer(new ClRPSPickThoughtsPacket(pickClicked != null ? pickClicked : pickMouseOver));
        }
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        if (p_231046_1_ == 256) {
            PacketManager.sendToServer(ClRPSGameInputPacket.quitGame());
            this.minecraft.setScreen(null);
        } else if (p_231046_1_ == 257 || p_231046_1_ == 335) {
            String s = this.input.getValue().trim();
            if (!s.isEmpty()) {
                this.sendMessage(s);
            }

            this.input.setValue("");
            this.minecraft.gui.getChat().resetChatScroll();
            return true;
        }

        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }
    
}
