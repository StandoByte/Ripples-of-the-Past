package com.github.standobyte.jojo.client;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.entity.itemprojectile.ItemProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonStartMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHeldActionTargetPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.Util;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class InputHandler {
    private static InputHandler instance = null;

    private Minecraft mc;
    private ActionsOverlayGui actionsOverlay;
    private IStandPower standPower;
    private INonStandPower nonStandPower;

    private static final String MAIN_CATEGORY = new String("key.categories." + JojoMod.MOD_ID);
    private KeyBinding toggleStand;
    private KeyBinding standRemoteControl;
    public KeyBinding hamonSkillsWindow;

    private static final String HUD_CATEGORY = new String("key.categories." + JojoMod.MOD_ID + ".hud");
    public KeyBinding nonStandMode;
    public KeyBinding standMode;
    private KeyBinding scrollMode;
    
    private KeyBinding attackHotbar;
    private KeyBinding abilityHotbar;
    private KeyBinding scrollAttack;
    private KeyBinding scrollAbility;

    private static final String HUD_SLOTS_CATEGORY = new String("key.categories." + JojoMod.MOD_ID + ".hud.slots");
    @Nullable
    private KeyBinding[] attackSlots;
    @Nullable
    private KeyBinding deselectAttack;
    @Nullable
    private KeyBinding[] abilitySlots;
    @Nullable
    private KeyBinding deselectAbility;
    
    private int leftClickBlockDelay;

    private InputHandler(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new InputHandler(mc);
            instance.registerKeyBindings();
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static InputHandler getInstance() {
        return instance;
    }
    
    public void setActionsOverlay(ActionsOverlayGui instance) {
        this.actionsOverlay = instance;
    }
    
    public void registerKeyBindings() {
        ClientRegistry.registerKeyBinding(toggleStand = new KeyBinding(JojoMod.MOD_ID + ".key.toggle_stand", GLFW_KEY_M, MAIN_CATEGORY));
        ClientRegistry.registerKeyBinding(standRemoteControl = new KeyBinding(JojoMod.MOD_ID + ".key.stand_remote_control", GLFW_KEY_O, MAIN_CATEGORY));
        ClientRegistry.registerKeyBinding(hamonSkillsWindow = new KeyBinding(JojoMod.MOD_ID + ".key.hamon_skills_window", GLFW_KEY_H, MAIN_CATEGORY));
        
        ClientRegistry.registerKeyBinding(nonStandMode = new KeyBinding(JojoMod.MOD_ID + ".key.non_stand_mode", GLFW_KEY_J, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(standMode = new KeyBinding(JojoMod.MOD_ID + ".key.stand_mode", GLFW_KEY_K, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(scrollMode = new KeyBinding(JojoMod.MOD_ID + ".key.scroll_mode", GLFW_KEY_UNKNOWN, HUD_CATEGORY));
        
        ClientRegistry.registerKeyBinding(attackHotbar = new KeyBinding(JojoMod.MOD_ID + ".key.attack_hotbar", GLFW_KEY_V, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(abilityHotbar = new KeyBinding(JojoMod.MOD_ID + ".key.ability_hotbar", GLFW_KEY_B, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(scrollAttack = new KeyBinding(JojoMod.MOD_ID + ".key.scroll_attack", GLFW_KEY_UNKNOWN, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(scrollAbility = new KeyBinding(JojoMod.MOD_ID + ".key.scroll_ability", GLFW_KEY_UNKNOWN, HUD_CATEGORY));
        
        if (JojoModConfig.CLIENT.slotHotkeys.get()) {
            attackSlots = new KeyBinding[9];
            abilitySlots = new KeyBinding[9];
            for (int i = 0; i < 9; i++) {
                ClientRegistry.registerKeyBinding(attackSlots[i] = new KeyBinding(JojoMod.MOD_ID + ".key.attack_" + (i + 1), GLFW_KEY_UNKNOWN, HUD_SLOTS_CATEGORY));
                ClientRegistry.registerKeyBinding(abilitySlots[i] = new KeyBinding(JojoMod.MOD_ID + ".key.ability_" + (i + 1), GLFW_KEY_UNKNOWN, HUD_SLOTS_CATEGORY));
            }
            ClientRegistry.registerKeyBinding(deselectAttack = new KeyBinding(JojoMod.MOD_ID + ".key.deselect_attack", GLFW_KEY_UNKNOWN, HUD_SLOTS_CATEGORY));
            ClientRegistry.registerKeyBinding(deselectAbility = new KeyBinding(JojoMod.MOD_ID + ".key.deselect_ability", GLFW_KEY_UNKNOWN, HUD_SLOTS_CATEGORY));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseScroll(MouseScrollEvent event) {
        if (standPower == null || nonStandPower == null || actionsOverlay == null) {
            return;
        }

        if (actionsOverlay.isActive() && !mc.player.isSpectator()) {
            boolean scrollAttack = attackHotbar.isDown();
            boolean scrollAbility = abilityHotbar.isDown();
            if (scrollAttack || scrollAbility) {
                if (scrollAttack) {
                    actionsOverlay.scrollAction(ActionType.ATTACK, event.getScrollDelta() > 0.0D);
                }
                if (scrollAbility) {
                    actionsOverlay.scrollAction(ActionType.ABILITY, event.getScrollDelta() > 0.0D);
                }
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public void handleKeyBindings(ClientTickEvent event) {
        if (mc.overlay != null || (mc.screen != null && !mc.screen.passEvents) || mc.level == null || standPower == null || nonStandPower == null || actionsOverlay == null || mc.player.isSpectator()) {
            return;
        }
        
        if (event.phase == TickEvent.Phase.START) {
            if (actionsOverlay.isActive()) {
                boolean chooseAttack = attackHotbar.isDown();
                boolean chooseAbility = abilityHotbar.isDown();
                actionsOverlay.setHotbarButtonsDows(chooseAttack, chooseAbility);
                if (chooseAttack || chooseAbility) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.options.keyHotbarSlots[i].consumeClick()) {
                            if (chooseAttack) {
                                // FIXME (!!) (controls) doesn't seem to work for key 2 while walking (???)
                                actionsOverlay.selectAction(ActionType.ATTACK, i);
                            }
                            if (chooseAbility) {
                                actionsOverlay.selectAction(ActionType.ABILITY, i);
                            }
                        }
                    }
                }
                else {
                    // FIXME (!!) (controls) behaves weird when mapped to 1-9
                    if (attackSlots != null) {
                        for (int i = 0; i < 9; i++) {
                            if (attackSlots[i].consumeClick()) {
                                actionsOverlay.selectAction(ActionType.ATTACK, i);
                            }
                        }
                    }
                    if (abilitySlots != null) {
                        for (int i = 0; i < 9; i++) {
                            if (abilitySlots[i].consumeClick()) {
                                actionsOverlay.selectAction(ActionType.ABILITY, i);
                            }
                        }
                    }
                }
                
                if (scrollAttack.consumeClick()) {
                    actionsOverlay.scrollAction(ActionType.ATTACK, mc.player.isShiftKeyDown());
                }
                
                if (scrollAbility.consumeClick()) {
                    actionsOverlay.scrollAction(ActionType.ABILITY, mc.player.isShiftKeyDown());
                }

                while (mc.options.keyPickItem.consumeClick()) {
                    handleMouseClickPowerHud(null, ActionKey.STAND_BLOCK);
                }
            }
        }
        else {
            if (leftClickBlockDelay > 0) {
                leftClickBlockDelay--;
            }
            
            if (standMode.consumeClick()) {
                actionsOverlay.setMode(PowerClassification.STAND);
            }
            
            if (nonStandMode.consumeClick()) {
                actionsOverlay.setMode(PowerClassification.NON_STAND);
            }
            
            if (scrollMode.consumeClick()) {
                actionsOverlay.scrollMode();
            }
            

            if (toggleStand.consumeClick()) {
                if (!standPower.isActive()) {
                    actionsOverlay.onStandSummon();
                }
                else {
                    actionsOverlay.onStandUnsummon();
                }
                PacketManager.sendToServer(new ClToggleStandSummonPacket());
            }
            
            if (standRemoteControl.consumeClick()) {
                PacketManager.sendToServer(new ClToggleStandManualControlPacket());
            }
            
            if (hamonSkillsWindow.consumeClick()) {
                if (nonStandPower.hasPower() && nonStandPower.getType() == ModNonStandPowers.HAMON.get()) {
                    if (mc.player.isShiftKeyDown()) {
                        PacketManager.sendToServer(new ClHamonStartMeditationPacket());
                    }
                    else {
                        ClientUtil.openHamonTeacherUi();
                    }
                }
                else {
                    mc.player.sendMessage(new TranslationTextComponent(
                            nonStandPower.getTypeSpecificData(ModNonStandPowers.VAMPIRISM.get())
                            .map(vampirism -> vampirism.isVampireHamonUser()).orElse(false) ? 
                                    "jojo.chat.message.no_hamon_vampire"
                                    : "jojo.chat.message.no_hamon"), Util.NIL_UUID);
                }
            }
            
            if (!mc.options.keyAttack.isDown()) {
                leftClickBlockDelay = 0;
            }
            
            checkHeldAction(standPower);
            checkHeldAction(nonStandPower);
        }
    }
    
    private final Map<IPower<?, ?>, ActionKey> heldKeys = new HashMap<>();
    
    private enum ActionKey {
        ATTACK(mc -> mc.options.keyAttack),
        ABILITY(mc -> mc.options.keyUse),
        STAND_BLOCK(mc -> mc.options.keyPickItem);
        
        private Function<Minecraft, KeyBinding> key;
        
        private ActionKey(Function<Minecraft, KeyBinding> key) {
            this.key = key;
        }
        
        private KeyBinding getKey(Minecraft mc) {
            return key.apply(mc);
        }
    }
    
    private void checkHeldAction(IPower<?, ?> power) {
        Action<?> heldAction = power.getHeldAction();
        if (heldAction != null) {
            boolean keyHeld;
            if (heldKeys.containsKey(power)) {
                keyHeld = heldKeys.get(power).getKey(mc).isDown();
            }
            else {
                keyHeld = mc.options.keyAttack.isDown() || mc.options.keyUse.isDown() || mc.options.keyPickItem.isDown();
            }
            if (!keyHeld) {
                stopHeldAction(power, power.getPowerClassification() == actionsOverlay.getCurrentMode());
            }
            else {
                PacketManager.sendToServer(ClHeldActionTargetPacket.withRayTraceResult(power.getPowerClassification(), mc.hitResult));
            }
        }
    }
    
    public void stopHeldAction(IPower<?, ?> power, boolean shouldFire) {
        if (power.getHeldAction() != null) {
            power.stopHeldAction(shouldFire);
            heldKeys.remove(power);
            PacketManager.sendToServer(new ClStopHeldActionPacket(power.getPowerClassification(), shouldFire));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void modActionClick(ClickInputEvent event) {
        if (mc.player.isSpectator() || event.getHand() == Hand.OFF_HAND) {
            return;
        }

        ActionKey key;
        if (event.isAttack()) {
            key = ActionKey.ATTACK;
        }
        else if (event.isUseItem()) {
            key = ActionKey.ABILITY;
        }
        else if (event.isPickBlock()) {
            key = ActionKey.STAND_BLOCK;
        }
        else {
            return;
        }
        
        handleMouseClickPowerHud(event, key);
    }
    
    private void handleMouseClickPowerHud(@Nullable ClickInputEvent event, ActionKey key) {
        if (mc.player.isSpectator()) {
            return;
        }
        
        ActionType actionType = key == ActionKey.ATTACK ? ActionType.ATTACK : ActionType.ABILITY;

        IPower<?, ?> power = actionsOverlay.getCurrentPower();
        if (key == ActionKey.STAND_BLOCK && power.getAbilities().isEmpty() || actionsOverlay.noActionSelected(actionType)) {
            if (mc.player.hasEffect(ModEffects.STUN.get()) && event != null) {
                event.setCanceled(true);
            }
            return;
        }

        boolean leftClickedBlock = actionType == ActionType.ATTACK && mc.hitResult.getType() == Type.BLOCK;
        if (leftClickedBlock && leftClickBlockDelay > 0 || power.getHeldAction() != null) {
            if (event != null) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
            return;
        }
        boolean shift = mc.player.isShiftKeyDown();
        
        boolean click;
        if (key == ActionKey.STAND_BLOCK) {
            click = actionsOverlay.onClick(actionType, shift, 0);
        }
        else {
            click = actionsOverlay.onClick(actionType, shift);
        }
        if (click) {
            Action<?> action = key != ActionKey.STAND_BLOCK ? actionsOverlay.getSelectedAction(actionType) : power.getAction(actionType, 0, shift);
            if (event != null) {
                event.setSwingHand(action.swingHand());
                event.setCanceled(action.cancelsVanillaClick());
            }
            else if (action.swingHand()) {
                mc.player.swing(Hand.MAIN_HAND);
            }
            if (actionsOverlay.isSelectedActionHeld(actionType)) {
                heldKeys.put(power, key);
            }
        }
        if (leftClickedBlock) {
            leftClickBlockDelay = 4;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClickInput(ClickInputEvent event) {
        if (event.isAttack() && mc.hitResult.getType() == Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mc.hitResult).getEntity();
            if (entity == mc.player || entity instanceof ItemProjectileEntity) {
                event.setCanceled(true); // prevents kick for "Attempting to attack an invalid entity"
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInputUpdate(InputUpdateEvent event) {
        MovementInput input = event.getMovementInput();
        if (standPower != null && nonStandPower != null) {
            boolean actionSlowedDown = slowDownFromHeldAction(mc.player, input, standPower);
            actionSlowedDown = slowDownFromHeldAction(mc.player, input, nonStandPower) || actionSlowedDown;
            boolean standSlowedDown = slowDownFromStandEntity(mc.player, input);
            
            if (!mc.player.isPassenger()) {
                IPower<?, ?> power = actionsOverlay.getCurrentPower();
                if (power != null) {
                    if (power.canLeap() && !actionSlowedDown && !standSlowedDown && input.shiftKeyDown && input.jumping) {
                        float leapStrength = power.leapStrength();
                        if (leapStrength > 0) {
                            input.shiftKeyDown = false;
                            input.jumping = false;
                            PacketManager.sendToServer(new ClOnLeapPacket(power.getPowerClassification()));
                            leap(mc.player, input, leapStrength);
                        }
                    }
                }
                return;
            }
        }
        
        Entity vehicle = mc.player.getVehicle();
        if (vehicle instanceof LeavesGliderEntity) {
            ((LeavesGliderEntity) vehicle).setInput(input.left, input.right);
        }
    }
    
    private boolean slowDownFromStandEntity(PlayerEntity player, MovementInput input) {
        IStandManifestation stand = standPower.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) stand;
            float slowDown = standEntity.getUserMovementFactor();
            if (slowDown < 1.0F) {
                input.leftImpulse *= slowDown;
                input.forwardImpulse *= slowDown;
                player.setSprinting(false);
                KeyBinding.set(mc.options.keySprint.getKey(), false);
                return true;
            }
        }
        return false;
    }
    
    private boolean slowDownFromHeldAction(PlayerEntity player, MovementInput input, IPower<?, ?> power) {
        Action<?> heldAction = power.getHeldAction();
        if (heldAction != null && heldAction.getHeldSlowDownFactor() < 1.0F) {
            input.leftImpulse *= heldAction.getHeldSlowDownFactor();
            input.forwardImpulse *= heldAction.getHeldSlowDownFactor();
            player.setSprinting(false);
            KeyBinding.set(mc.options.keySprint.getKey(), false);
            return true;
        }
        return false;
    }
    
    private void leap(ClientPlayerEntity player, MovementInput input, float strength) {
        input.shiftKeyDown = false;
        input.jumping = false;
        player.setOnGround(false);
        player.hasImpulse = true;
        Vector3d leap = Vector3d.directionFromRotation(Math.min(player.xRot, -30F), player.yRot).scale(strength);
        player.setDeltaMovement(leap.x, leap.y * 0.5, leap.z);
    }

    private final DashTrigger leftDash = new DashTrigger(-90F);
    private final DashTrigger rightDash = new DashTrigger(90F);
    private final DashTrigger backDash = new DashTrigger(180F);
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void dash(InputUpdateEvent event) {
        if (mc.player.isOnGround()) {
            // FIXME (!!) (dash) more dash checks (summoned stand, stand is following player, stand task, leap cd, stun, on ground, maybe some sprint checks, etc.)
            MovementInput input = event.getMovementInput();
            leftDash.tick(input.left, mc.player);
            rightDash.tick(input.right, mc.player);
            backDash.tick(input.down, mc.player);
        }
    }
    
    private void dash(ClientPlayerEntity player, float yRot) {
        // FIXME (!!) (dash) set leap cd
        player.setOnGround(false);
        player.hasImpulse = true;
        Vector3d dash = Vector3d.directionFromRotation(0, player.yRot + yRot).scale(0.5).add(0, 0.2, 0);
        // FIXME (!!) (dash) different speeds with the same distance
        player.setDeltaMovement(player.getDeltaMovement().add(dash));
    }
    
    
    
    public void updatePowersCache() {
        standPower = IStandPower.getPlayerStandPower(mc.player);
        nonStandPower = INonStandPower.getPlayerNonStandPower(mc.player);
        heldKeys.clear();
    }
    
    
    
    private class DashTrigger {
        private final float yRot;
        private int triggerTime;
        private boolean triggerGap;
        
        private DashTrigger(float yRot) {
            this.yRot = yRot;
        }
        
        private void tick(boolean input, ClientPlayerEntity player) {
            if (triggerTime > 0) {
                triggerTime--;
            }
            if (input) {
                if (triggerTime <= 0) {
                    triggerTime = 7;
                }
                else if (triggerGap) {
                    dash(player, yRot);
                    triggerTime = 0;
                }
            }
            triggerGap = !input;
        }
    }
}
