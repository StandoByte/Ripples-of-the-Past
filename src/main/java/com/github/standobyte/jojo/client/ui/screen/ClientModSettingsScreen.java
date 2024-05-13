package com.github.standobyte.jojo.client.ui.screen;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.HudNamesRender;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.PositionConfig;
import com.github.standobyte.jojo.client.ui.screen.widgets.ImageVanillaButton;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;

public class ClientModSettingsScreen extends SettingsScreen {
    protected final ClientModSettings settings;
    protected final ClientModSettings.Settings settingsValues;

    public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings) {
        this(lastScreen, settings, new TranslationTextComponent("jojo.options.client.title"));
    }

    public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings, ITextComponent title) {
        super(lastScreen, lastScreen.getMinecraft().options, title);
        this.settings = settings;
        this.settingsValues = ClientModSettings.getSettingsReadOnly();
    }

    @Override
    protected void init() {
        addButtons();
    }

    protected void addButtons() {
        int i = 0;
        
        addButton(new Button(calcButtonX(i), calcButtonY(i++), 150, 20, 
                new TranslationTextComponent("jojo.options.client.hud"), 
                button -> minecraft.setScreen(new HudSettings(this, settings, button.getMessage()))));
        
        addButton(new Button(calcButtonX(i), calcButtonY(i++), 150, 20, 
                new TranslationTextComponent("jojo.options.client.stand"), 
                button -> minecraft.setScreen(new StandSettings(this, settings, button.getMessage()))));
        
        addButton(new Button(calcButtonX(i), calcButtonY(i++), 150, 20, 
                new TranslationTextComponent("jojo.options.client.hamon"), 
                button -> minecraft.setScreen(new HamonSettings(this, settings, button.getMessage()))));
        
        addButton(new Button(calcButtonX(i), calcButtonY(i++), 150, 20, 
                new TranslationTextComponent("jojo.options.client.misc"), 
                button -> minecraft.setScreen(new MiscSettings(this, settings, button.getMessage()))));
        
        
        addBackButton(DialogTexts.GUI_DONE, i);
    }
    
    protected void addBackButton(ITextComponent text, int buttonsAdded) {
        buttonsAdded += 2;
        if (buttonsAdded % 2 == 1) {
            ++buttonsAdded;
        }

        addButton(new Button(
                this.width / 2 - 100, 
                calcButtonY(buttonsAdded), 
                200, 20, 
                text, button -> minecraft.setScreen(lastScreen)));
    }
    
    
    
    public static class HudSettings extends ClientModSettingsScreen {

        public HudSettings(Screen lastScreen, ClientModSettings settings, ITextComponent title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            EnumSetting<PositionConfig> barsPosition = new EnumSetting<PositionConfig>(settings, 
                    new TranslationTextComponent("jojo.config.client.barsPosition"), 
                    new TranslationTextComponent("jojo.config.client.barsPosition.tooltip"), 
                    PositionConfig.class) {
                @Override public PositionConfig get() { return settingsValues.barsPosition; }
                @Override public void set(PositionConfig value) { settingsValues.barsPosition = value; }
            };
            addButton(barsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            
            EnumSetting<PositionConfig> hotbarsPosition = new EnumSetting<PositionConfig>(settings, 
                    new TranslationTextComponent("jojo.config.client.hotbarsPosition"), 
                    new TranslationTextComponent("jojo.config.client.hotbarsPosition.tooltip"), 
                    PositionConfig.class) {
                @Override public PositionConfig get() { return settingsValues.hotbarsPosition; }
                @Override public void set(PositionConfig value) { settingsValues.hotbarsPosition = value; }
            };
            addButton(hotbarsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            
            EnumSetting<HudNamesRender> hudNamesRender = new EnumSetting<HudNamesRender>(settings, 
                    new TranslationTextComponent("jojo.config.client.hudNamesRender"), 
                    new TranslationTextComponent("jojo.config.client.hudNamesRender.tooltip"), 
                    HudNamesRender.class) {
                @Override public HudNamesRender get() { return settingsValues.hudNamesRender; }
                @Override public void set(HudNamesRender value) { settingsValues.hudNamesRender = value; }
            };
            addButton(hudNamesRender.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            
            BooleanSetting hudHotbarsFold = new BooleanSetting(settings, 
                    new TranslationTextComponent("jojo.config.client.hudHotbarsFold"), 
                    new TranslationTextComponent("jojo.config.client.hudHotbarsFold.tooltip")
                    ) {
                @Override public boolean get() { return settingsValues.hudHotbarsFold; }
                @Override public void set(boolean value) { 
                    settingsValues.hudHotbarsFold = value;
                    if (minecraft.player != null) {
                        for (PowerClassification power : PowerClassification.values()) {
                            IPower.getPowerOptional(minecraft.player, power).ifPresent(IPower::clUpdateHud);
                        }
                    }
                }
            };
            addButton(hudHotbarsFold.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            
            BooleanSetting showLockedSlots = new BooleanSetting(settings, 
                    new TranslationTextComponent("jojo.config.client.showLockedSlots"), 
                    new TranslationTextComponent("jojo.config.client.showLockedSlots.tooltip")
                    ) {
                @Override public boolean get() { return settingsValues.showLockedSlots; }
                @Override public void set(boolean value) {
                    settingsValues.showLockedSlots = value;
                    if (minecraft.player != null) {
                        for (PowerClassification power : PowerClassification.values()) {
                            IPower.getPowerOptional(minecraft.player, power).ifPresent(IPower::clUpdateHud);
                        }
                    }
                }
            };
            addButton(showLockedSlots.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            addBackButton(DialogTexts.GUI_BACK, i);
        }
        
    }
    
    public static class StandSettings extends ClientModSettingsScreen {

        public StandSettings(Screen lastScreen, ClientModSettings settings, ITextComponent title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            BooleanSetting resolveShaders = new BooleanSetting(settings, 
                    new TranslationTextComponent("jojo.config.client.resolveShaders"), 
                    new TranslationTextComponent("jojo.config.client.resolveShaders.tooltip")
                    ) {
                @Override public boolean get() { return settingsValues.resolveShaders; }
                @Override public void set(boolean value) { 
                    settingsValues.resolveShaders = value;
                    if (!value) {
                        ShaderEffectApplier.getInstance().stopResolveShader();
                    }
                }
            };
            addButton(resolveShaders.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            
            BooleanSetting timeStopAnimation = new BooleanSetting(settings, 
                    new TranslationTextComponent("jojo.config.client.timeStopAnimation"), 
                    new TranslationTextComponent("jojo.config.client.timeStopAnimation.tooltip")
                    ) {
                @Override public boolean get() { return settingsValues.timeStopAnimation; }
                @Override public void set(boolean value) { settingsValues.timeStopAnimation = value; }
            };
            addButton(timeStopAnimation.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            addBackButton(DialogTexts.GUI_BACK, i);
        }
        
    }
    
    public static class HamonSettings extends ClientModSettingsScreen {

        public HamonSettings(Screen lastScreen, ClientModSettings settings, ITextComponent title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            // TODO
            
            addBackButton(DialogTexts.GUI_BACK, i);
        }
        
    }
    
    public static class MiscSettings extends ClientModSettingsScreen {

        public MiscSettings(Screen lastScreen, ClientModSettings settings, ITextComponent title) {
            super(lastScreen, settings, title);
        }
        
        @Override
        protected void addButtons() {
            int i = 0;
            
            BooleanSetting characterVoiceLines = new BooleanSetting(settings, 
                    new TranslationTextComponent("jojo.config.client.characterVoiceLines"), 
                    new TranslationTextComponent("jojo.config.client.characterVoiceLines.tooltip")
                    ) {
                @Override public boolean get() { return settingsValues.characterVoiceLines; }
                @Override public void set(boolean value) { settingsValues.characterVoiceLines = value; }
            };
            addButton(characterVoiceLines.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            
            BooleanSetting menacingParticles = new BooleanSetting(settings, 
                    new TranslationTextComponent("jojo.config.client.menacingParticles"), 
                    new TranslationTextComponent("jojo.config.client.menacingParticles.tooltip")
                    ) {
                @Override public boolean get() { return settingsValues.menacingParticles; }
                @Override public void set(boolean value) { settingsValues.menacingParticles = value; }
            };
            addButton(menacingParticles.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this));
            
            addBackButton(DialogTexts.GUI_BACK, i);
        }
        
    }
    
    
    
    protected int calcButtonX(int i) {
        return this.width / 2 - 155 + i % 2 * 160;
    }
    
    protected int calcButtonY(int i) {
        return this.height / 6 + 24 * (i >> 1);
    }

    @Override
    public void removed() {
        settings.save();
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, font, title, width / 2, 20, 0xFFFFFF);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
    
    
    
    protected static abstract class BooleanSetting {
        private final ClientModSettings settings;
        private final ITextComponent name;
        private final ITextComponent tooltip;
        
        public BooleanSetting(ClientModSettings settings, ITextComponent name, @Nullable ITextComponent tooltip) {
            this.settings = settings;
            this.name = name;
            this.tooltip = tooltip;
        }
        
        public abstract boolean get();
        public abstract void set(boolean value);
        
        public Button createButton(int x, int y, int width, int height, Screen screen) {
            return new Button(
                    x, y, width, height,
                    DialogTexts.optionStatus(name, get()), 
                    button -> {
                        settings.editSettings(s -> {
                            set(!get());
                            button.setMessage(DialogTexts.optionStatus(name, get()));
                        });
                    },
                    (button, matrixStack, mouseX, mouseY) -> {
                        if (tooltip != null) {
                            screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY);
                        }
                    });
        }
    }
    
    protected static abstract class EnumSetting<T extends Enum<T>> {
        private final ClientModSettings settings;
        private final ITextComponent name;
        private final ITextComponent tooltip;
        private final Class<T> enumClass;
        
        public EnumSetting(ClientModSettings settings, ITextComponent name, @Nullable ITextComponent tooltip, Class<T> enumClass) {
            this.settings = settings;
            this.name = name;
            this.enumClass = enumClass;
            this.tooltip = tooltip;
        }
        
        public abstract T get();
        public abstract void set(T value);
        
        public Button createButton(int x, int y, int width, int height, Screen screen) {
            return new Button(
                    x, y, width, height,
                    new TranslationTextComponent("options.generic_value", name, getValueMessage(get())), 
                    button -> {
                        settings.editSettings(s -> {
                            T[] values = enumClass.getEnumConstants();
                            T val = get();
                            T nextVal = values[(val.ordinal() + 1) % values.length];
                            set(nextVal);
                            button.setMessage(new TranslationTextComponent("options.generic_value", name, getValueMessage(nextVal)));
                        });
                    },
                    (button, matrixStack, mouseX, mouseY) -> {
                        if (tooltip != null) {
                            screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY);
                        }
                    });
        }
        
        private ITextComponent getValueMessage(T value) {
            return new StringTextComponent(value.name()); // tmp
        }
    }
    
    
    
    public static Button addSettingsButton(Screen optionsScreen, List<Widget> otherModdedButtons) {
        final int minY = optionsScreen.height / 6 + 48 - 6;
        final int maxY = minY + 72;
        
        final int minX1 = 0;
        final int maxX1 = optionsScreen.width / 2 - 155 - 20 - 5;
        final int minX2 = optionsScreen.width / 2 + 160;
        final int maxX2 = optionsScreen.width - 20;
        
        final int minX3 = optionsScreen.width / 2 - 155;
        final int maxX3 = minX3 + 290;
        final int y3 = maxY + 24;
        
        int[] buttonPos = null;
        
        // try placing the button to the right side
        for (int x = minX2; x <= maxX2 && buttonPos == null; x += 25) {
            int y = maxY;
            if (ModList.get().isLoaded("essential")) y -= 24; // for fuck's sake
            for (; y >= minY && buttonPos == null; y -= 24) {
                buttonPos = noOverlapPos(otherModdedButtons, x, y);
            }
        }
        // ...or to the left side
        if (buttonPos == null) {
            for (int x = maxX1; x >= minX1 && buttonPos == null; x -= 25) {
                for (int y = maxY; y >= minY && buttonPos == null; y -= 24) {
                    buttonPos = noOverlapPos(otherModdedButtons, x, y);
                }
            }
        }
        // ...or below the vanilla options
        if (buttonPos == null) {
            for (int x = minX3; x <= maxX3 && buttonPos == null; x += 29) {
                buttonPos = noOverlapPos(otherModdedButtons, x, y3);
            }
        }
        // ...how many new buttons are there?? fuck it, just put it at the "Done" button
        if (buttonPos == null) {
            buttonPos = new int[] { optionsScreen.width / 2 + 110, optionsScreen.height / 6 + 168 };
        }
        
        ITextComponent tooltip = new TranslationTextComponent("jojo.options.client.title");
        return new ImageVanillaButton(buttonPos[0], buttonPos[1], 20, 20, 
                80, 128, 
                ClientUtil.ADDITIONAL_UI, 256, 256,
                button -> {
                    optionsScreen.getMinecraft().setScreen(new ClientModSettingsScreen(optionsScreen, ClientModSettings.getInstance()));
                },
                (button, matrixStack, mouseX, mouseY) -> {
                    optionsScreen.renderTooltip(matrixStack, tooltip, mouseX, mouseY);
                },
                tooltip);
    }
    
    @Nullable
    private static int[] noOverlapPos(List<Widget> buttonsList, int x, int y) {
        int x2 = x + 20;
        int y2 = y + 20;
        return buttonsList.stream().anyMatch(button -> {
            return button.x < x2 && button.x + button.getWidth() > x && button.y < y2 && button.y + button.getHeight() > y;
        }) ? null : new int[] { x, y };
    }
}
