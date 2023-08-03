package com.github.standobyte.jojo.client.ui.screen.hamon;

import static com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen.WINDOW_WIDTH;

import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

@Deprecated
public class TmpHamonWipTabGui extends HamonTabGui {
    private final List<IReorderingProcessor> descLines;
    HamonTabGui techniquesTab = null;
    
    TmpHamonWipTabGui(Minecraft minecraft, HamonScreen screen) {
        super(minecraft, screen, "", -1, -1);
        int textWidth = HamonScreen.WINDOW_WIDTH - 30;
        descLines = minecraft.font.split(new StringTextComponent(
                "This part of the Hamon training is undergoing some changes, as the mod's developer isn't yet satisfied with the mechanic. The skills are currently unfinished too.\n" + 
                "This will be addressed in the upcoming updates, where the updated techniques will be added one by one.\n" + 
                "Please don't throw rotten tomatoes at the developer."
                ), textWidth); // doesn't need an entry in the lang files really
    }
    
    @Override
    ITextComponent getTitle() {
       return StringTextComponent.EMPTY;
    }
    
    @Override
    public void addButtons() {
        addButton(new HamonScreenButton(screen.windowPosX() + 85, screen.windowPosY() + 126, 60, 20, new StringTextComponent("Next"), button -> {
            if (techniquesTab != null) {
                screen.selectTab(techniquesTab);
            }
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

    public static final void makeCompilerShoutAtMeWhenIDeleteThis() {}
}
