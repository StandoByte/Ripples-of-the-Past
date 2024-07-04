package com.github.standobyte.jojo.client.ui.screen;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyConflictContext;

public class WasdAllowingScreen extends Screen {
    /* 
     * TODO
     * sprint
     * shift toggle
     * sprint toggle
     * mouse wheel scroll
     */

    public WasdAllowingScreen(ITextComponent pTitle) {
        super(pTitle);
        saveHeldKeyBinds();
        heldKeyBinds.forEach(keybind -> keybind.setDown(true));
    }
    
    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return false;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void setFocused(@Nullable IGuiEventListener pListener) {
    } // otherwise space will press focused buttons instead of jumping
    
    protected void doSetFocused(@Nullable IGuiEventListener pListener) {
        super.setFocused(pListener);
    }
    
    
    protected Collection<KeyBinding> heldKeyBinds;
    private void saveHeldKeyBinds() {
        Collection<KeyBinding> allKeyBindings = ClientReflection.getKeyBindingsMap().values();
        heldKeyBinds = allKeyBindings.stream().filter(KeyBinding::isDown).collect(Collectors.toList());
    }
    
    @Override
    protected void init() {
        super.init();
    }
    
    
    
    @Override
    public void tick() {
        passEvents = acceptsKeyInput();
    }
    
    public void tickInput(Minecraft mc, ClientPlayerEntity player, MovementInput input) {
        if (!KeyConflictContext.IN_GAME.isActive() && input instanceof MovementInputFromOptions && acceptsKeyInput()) {
            boolean isMovingSlowly = player.isMovingSlowly();
            
            input.up = isDownNoConflictContext(mc.options.keyUp);
            input.down = isDownNoConflictContext(mc.options.keyDown);
            input.left = isDownNoConflictContext(mc.options.keyLeft);
            input.right = isDownNoConflictContext(mc.options.keyRight);
            input.jumping = isDownNoConflictContext(mc.options.keyJump);
            input.shiftKeyDown = isDownNoConflictContext(mc.options.keyShift);
            
            input.forwardImpulse = input.up == input.down ? 0.0F : (input.up ? 1.0F : -1.0F);
            input.leftImpulse = input.left == input.right ? 0.0F : (input.left ? 1.0F : -1.0F);
            if (isMovingSlowly) {
                input.leftImpulse *= 0.3F;
                input.forwardImpulse *= 0.3F;
            }
        }
    }
    
    public void clickKey(Minecraft mc, int key, int scanCode, int action, int modifiers, KeyBindingMap keyBindingMap) {
        if (action == GLFW.GLFW_RELEASE || !acceptsKeyInput()) return;
        
        InputMappings.Input inputmappings$input = InputMappings.getKey(key, scanCode);
        
        for (KeyBinding keybinding : keyBindingMap.lookupAll(inputmappings$input)) {
            if (keybinding != null) {
                clickIfKeyIs(keybinding, mc.options.keyTogglePerspective);
            }
        }
    }
    
    private void clickIfKeyIs(KeyBinding keyPressed, KeyBinding keyNeeded) {
        if (keyPressed == keyNeeded && keyPressed.getKeyModifier().isActive(null) && !keyPressed.getKeyConflictContext().isActive()) {
            ClientReflection.setClickCount(keyPressed, ClientReflection.getClickCount(keyPressed) + 1);
        }
    }
    
    /**
     * Some keybinds only work when there's no screen opened, so they need to bypass the check
     * {@link net.minecraft.client.GameSettings#setForgeKeybindProperties}
     */
    private static boolean isDownNoConflictContext(KeyBinding keyBinding) {
        return keyBinding.getKeyModifier().isActive(null) && ClientReflection.isDownFieldOnly(keyBinding);
    }
    
    public boolean acceptsKeyInput() {
        return true;
    }
}
