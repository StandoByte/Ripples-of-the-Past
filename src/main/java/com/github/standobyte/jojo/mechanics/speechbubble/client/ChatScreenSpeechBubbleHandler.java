package com.github.standobyte.jojo.mechanics.speechbubble.client;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.port_bridge.jojo.client.ui.screen_widgets.IconButton;
import com.github.standobyte.jojo.port_bridge.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.port_bridge.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ChatScreenSpeechBubbleHandler {
    public static boolean isTypingCommand;
    
    public static final Map<ClientChatMode, GuiIcon> MODE_ICONS = Util.make(new EnumMap<>(ClientChatMode.class), map -> {
        map.put(ClientChatMode.VANILLA, new GuiIcon(JojoMod.resLoc("textures/speech_bubble/buttons/chat_mode_vanilla.png"), 16, 16));
        map.put(ClientChatMode.SPEECH, new GuiIcon(JojoMod.resLoc("textures/speech_bubble/buttons/chat_mode_speech.png"), 16, 16));
        map.put(ClientChatMode.SPEECH_MENACING, new GuiIcon(JojoMod.resLoc("textures/speech_bubble/buttons/chat_mode_speech_gogogo.png"), 16, 16));
        // maybe later
        //map.put(ClientChatMode.SHOUT, new GuiIcon(JojoMod.resLoc("textures/speech_bubble/buttons/chat_mode_shout.png"), 16, 16));
        //map.put(ClientChatMode.THOUGHT, new GuiIcon(JojoMod.resLoc("textures/speech_bubble/buttons/chat_mode_thought.png"), 16, 16));
    });
    public static final GuiIcon CHECKMARK = new GuiIcon(JojoMod.resLoc("textures/speech_bubble/buttons/checkmark.png"), 9, 8);

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void addButton(/*ScreenEvent.Init.Post*/InitGuiEvent.Post event) {
        Screen screen = event.getGui();
        //Screen screen = event.getScreen();
        //Player player = Minecraft.getInstance().player;
        if (chatHasSpeechBubbles(screen)) {
            int x = 0;
            int y = screen.height - 40;

            for (ClientChatMode mode : ClientChatMode.values()) {
                GuiIcon icon = MODE_ICONS.get(mode);
                if (icon != null) {
                    String key = "jojo_ripples.chat." + mode.name().toLowerCase();
                    ITextComponent buttonName = new TranslationTextComponent(key);
                    MultiLineScreenTooltip tooltip;
                    if (mode == ClientChatMode.VANILLA) {
                        tooltip = new MultiLineScreenTooltip(buttonName);
                    }
                    else {
                        tooltip = new MultiLineScreenTooltip(buttonName, new TranslationTextComponent(key + ".desc"));
                    }
                    
                    // FIXME (1.21.1) this shit sets focus on the button, not allowing the player to type in the chat field
                    Button chatModeButton = new IconButton(
                            x, y, 20, 20,
                            icon,
                            b -> {
                                ClientChatMode.setChatMode(mode);
                            },
                            tooltip.bred()) {

                        @Override
                        public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
                            this.visible = !isTypingCommand;
                            super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
                        }
                        
                        @Override
                        public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
                            //public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                            super.renderButton(poseStack, mouseX, mouseY, partialTick);
                            if (mode == ClientChatMode.curChatMode) {
                                ChatScreenSpeechBubbleHandler.CHECKMARK.render(poseStack, this.x + 11, this.y + 13);
                                //CHECKMARK.render(guiGraphics.pose(), this.getX() + 11, this.getY() + 13);
                            }
                        }
                    };
                    event.addWidget(chatModeButton);
                    //event.addListener(chatModeButton);
                    
                    x += 20;
                }
            }
            ClientChatMode.onOpenedChat();
        }
    }
    
    public static boolean chatHasSpeechBubbles(Screen screen) {
        return screen instanceof ChatScreen && !(screen instanceof /* InBedChatScreen */ SleepInMultiplayerScreen);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChatClosed(GuiOpenEvent event) {
        if (!chatHasSpeechBubbles(event.getGui())) {
            ClientChatMode.onClosedChat();
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            Screen screen = mc.screen;
            boolean isTypingCommand = false;
            if (screen instanceof ChatScreen) {
                ChatScreen chatScreen = (ChatScreen) screen;
                isTypingCommand = chatScreen.input.getValue().startsWith("/");
                if (chatHasSpeechBubbles(chatScreen) && ChatScreenSpeechBubbleHandler.isTypingCommand != isTypingCommand) {
                    ClientChatMode.onChangedTypingCommand(isTypingCommand);
                }
                ChatScreenSpeechBubbleHandler.isTypingCommand = isTypingCommand;
            }
        }
    }
}
