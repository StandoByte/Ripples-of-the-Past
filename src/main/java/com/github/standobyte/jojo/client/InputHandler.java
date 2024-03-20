package com.github.standobyte.jojo.client;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSLASH;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.QuickAccess.QuickAccessKeyConflictContext;
import com.github.standobyte.jojo.client.ui.screen.hudlayout.HudLayoutEditingScreen;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.entity.itemprojectile.ItemProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClDoubleShiftPressPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonInteractAskTeacherPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonInteractTeachPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHasInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHeldActionTargetPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnStandDashPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.layout.ActionsLayout;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.settings.KeyBindingMap;
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
    
    public RayTraceResult mouseTarget;

    private static final String MAIN_CATEGORY = new String("key.categories." + JojoMod.MOD_ID);
    private KeyBinding toggleStand;
    private KeyBinding standRemoteControl;
    public KeyBinding hamonSkillsWindow;

    private static final String HUD_CATEGORY = new String("key.categories." + JojoMod.MOD_ID + ".hud");
    public KeyBinding nonStandMode;
    public KeyBinding standMode;
    private KeyBinding scrollMode;
    public KeyBinding editHotbars;
    public KeyBinding actionQuickAccess;
    public KeyBinding disableHotbars;
    
    private KeyBinding attackHotbar;
    private KeyBinding abilityHotbar;
    private KeyBinding scrollAttack;
    private KeyBinding scrollAbility;
    private KeyBinding lockAttackHotbar;
    private KeyBinding lockAbilityHotbar;

    private static final String HUD_ALTERNATIVE_CATEGORY = new String("key.categories." + JojoMod.MOD_ID + ".hud.alternative");
    @Nullable
    private KeyBinding[] attackSlots;
    @Nullable
    private KeyBinding deselectAttack;
    @Nullable
    private KeyBinding[] abilitySlots;
    @Nullable
    private KeyBinding deselectAbility;
    
    // it works because the actual map is static, not sure if that's intended or just an implementation detail
    // but hey, i'll take it
    private KeyBindingMap keyBindingMap = new KeyBindingMap();
    
    private int leftClickBlockDelay;
    private int quickAccessMmbDelay;
    
    public boolean hasInput;
    
    private boolean canLeap;
    
    private DoubleShiftDetector doubleShift = new DoubleShiftDetector();

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
        ClientRegistry.registerKeyBinding(editHotbars = new KeyBinding(JojoMod.MOD_ID + ".key.edit_hud", GLFW_KEY_BACKSLASH, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(actionQuickAccess = new KeyBinding(JojoMod.MOD_ID + ".key.quick_access", 
                QuickAccessKeyConflictContext.INSTANCE, InputMappings.Type.MOUSE, GLFW_MOUSE_BUTTON_MIDDLE, HUD_CATEGORY));
        
        ClientRegistry.registerKeyBinding(attackHotbar = new KeyBinding(JojoMod.MOD_ID + ".key.attack_hotbar", GLFW_KEY_V, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(abilityHotbar = new KeyBinding(JojoMod.MOD_ID + ".key.ability_hotbar", GLFW_KEY_B, HUD_CATEGORY));
        ClientRegistry.registerKeyBinding(disableHotbars = new KeyBinding(JojoMod.MOD_ID + ".key.disable_hotbars", GLFW_KEY_LEFT_ALT, HUD_CATEGORY));
        
        ClientRegistry.registerKeyBinding(scrollMode = new KeyBinding(JojoMod.MOD_ID + ".key.scroll_mode", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        ClientRegistry.registerKeyBinding(scrollAttack = new KeyBinding(JojoMod.MOD_ID + ".key.scroll_attack", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        ClientRegistry.registerKeyBinding(scrollAbility = new KeyBinding(JojoMod.MOD_ID + ".key.scroll_ability", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        ClientRegistry.registerKeyBinding(lockAttackHotbar = new KeyBinding(JojoMod.MOD_ID + ".key.lock_attack", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        ClientRegistry.registerKeyBinding(lockAbilityHotbar = new KeyBinding(JojoMod.MOD_ID + ".key.lock_ability", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        
        if (JojoModConfig.CLIENT.actionSlotHotkeys.get()) {
            attackSlots = new KeyBinding[9];
            abilitySlots = new KeyBinding[9];
            for (int i = 0; i < 9; i++) {
                ClientRegistry.registerKeyBinding(attackSlots[i] = new KeyBinding(JojoMod.MOD_ID + ".key.attack_" + (i + 1), GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
                ClientRegistry.registerKeyBinding(abilitySlots[i] = new KeyBinding(JojoMod.MOD_ID + ".key.ability_" + (i + 1), GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
            }
            ClientRegistry.registerKeyBinding(deselectAttack = new KeyBinding(JojoMod.MOD_ID + ".key.deselect_attack", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
            ClientRegistry.registerKeyBinding(deselectAbility = new KeyBinding(JojoMod.MOD_ID + ".key.deselect_ability", GLFW_KEY_UNKNOWN, HUD_ALTERNATIVE_CATEGORY));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseScroll(MouseScrollEvent event) {
        if (standPower == null || nonStandPower == null || actionsOverlay == null) {
            return;
        }

        if (actionsOverlay.isActive() && !mc.player.isSpectator()) {
            boolean scrollAttack = attackHotbar.isDown() || areControlsLockedForHotbar(ActionsLayout.Hotbar.LEFT_CLICK);
            boolean scrollAbility = abilityHotbar.isDown() || areControlsLockedForHotbar(ActionsLayout.Hotbar.RIGHT_CLICK);
            if (scrollAttack || scrollAbility) {
                if (scrollAttack) {
                    actionsOverlay.scrollAction(ActionsLayout.Hotbar.LEFT_CLICK, event.getScrollDelta() > 0.0D);
                }
                if (scrollAbility) {
                    actionsOverlay.scrollAction(ActionsLayout.Hotbar.RIGHT_CLICK, event.getScrollDelta() > 0.0D);
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
                boolean chooseAttack = attackHotbar.isDown() || areControlsLockedForHotbar(ActionsLayout.Hotbar.LEFT_CLICK);
                boolean chooseAbility = abilityHotbar.isDown() || areControlsLockedForHotbar(ActionsLayout.Hotbar.RIGHT_CLICK);
                actionsOverlay.setHotbarButtonsDows(chooseAttack, chooseAbility);
                actionsOverlay.setHotbarsEnabled(!disableHotbars.isDown());
                if (chooseAttack || chooseAbility) {
                    for (int i = 0; i < 9; i++) {
                        if (mc.options.keyHotbarSlots[i].consumeClick()) {
                            if (chooseAttack) {
                                actionsOverlay.selectAction(ActionsLayout.Hotbar.LEFT_CLICK, i);
                            }
                            if (chooseAbility) {
                                actionsOverlay.selectAction(ActionsLayout.Hotbar.RIGHT_CLICK, i);
                            }
                        }
                    }
                }
                else {
                    if (attackSlots != null) {
                        for (int i = 0; i < 9; i++) {
                            if (attackSlots[i].consumeClick()) {
                                actionsOverlay.selectAction(ActionsLayout.Hotbar.LEFT_CLICK, i);
                            }
                        }
                    }
                    if (abilitySlots != null) {
                        for (int i = 0; i < 9; i++) {
                            if (abilitySlots[i].consumeClick()) {
                                actionsOverlay.selectAction(ActionsLayout.Hotbar.RIGHT_CLICK, i);
                            }
                        }
                    }
                }
                
                if (scrollAttack.consumeClick()) {
                    actionsOverlay.scrollAction(ActionsLayout.Hotbar.LEFT_CLICK, mc.player.isShiftKeyDown());
                }
                
                if (scrollAbility.consumeClick()) {
                    actionsOverlay.scrollAction(ActionsLayout.Hotbar.RIGHT_CLICK, mc.player.isShiftKeyDown());
                }
                
                if (lockAttackHotbar.consumeClick()) {
                    switchLockedHotbarControls(ActionsLayout.Hotbar.LEFT_CLICK);
                }
                
                if (lockAbilityHotbar.consumeClick()) {
                    switchLockedHotbarControls(ActionsLayout.Hotbar.RIGHT_CLICK);
                }
                
                if (actionQuickAccess.isDown() && quickAccessMmbDelay <= 0
                        && !mc.player.isSpectator() && !disableHotbars.isDown()) {
                    HudClickResult result = handleMouseClickPowerHud(ActionKey.QUICK_ACCESS, actionQuickAccess);
                    if (result.vanillaInput == HudClickResult.Behavior.CANCEL) {
                        KeyBinding keybinding = keyBindingMap.lookupActive(actionQuickAccess.getKey());
                        if (keybinding != null) {
                            while (keybinding.consumeClick());
                        }
                    }
                    if (result.handSwing == HudClickResult.Behavior.FORCE) {
                        mc.player.swing(Hand.MAIN_HAND);
                    }
                    quickAccessMmbDelay = 4;
                }
            }
            
            if (mc.options.keyJump.isDown()) {
                SoulController.getInstance().skipAscension();
            }
            tickEffects();
            clickWithBusyHands();
        }
        else {
            boolean targetChanged = pickMouseTarget();
            
            if (leftClickBlockDelay > 0) {
                leftClickBlockDelay--;
            }
            if (quickAccessMmbDelay > 0) {
                quickAccessMmbDelay--;
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
                if (standPower.hasPower() && !standPower.isActive()) {
                    actionsOverlay.onStandSummon();
                }
//                else {
//                    actionsOverlay.onStandUnsummon();
//                }
                PacketManager.sendToServer(new ClToggleStandSummonPacket());
            }
            
            if (standRemoteControl.consumeClick()) {
                PacketManager.sendToServer(new ClToggleStandManualControlPacket());
            }
            
            if (hamonSkillsWindow.consumeClick()) {
                if (nonStandPower.hasPower() && nonStandPower.getType() == ModPowers.HAMON.get()) {
                    boolean taughtHamon = false;
                    if (mouseTarget instanceof EntityRayTraceResult) {
                        Entity mouseTargetEntity = ((EntityRayTraceResult) mouseTarget).getEntity();
                        taughtHamon = nonStandPower.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                            if (mouseTargetEntity instanceof PlayerEntity) {
                                return hamon.interactWithNewLearner((PlayerEntity) mouseTargetEntity);
                            }
                            return false;
                        }).orElse(false);
                        if (taughtHamon) {
                            PacketManager.sendToServer(new ClHamonInteractTeachPacket(mouseTargetEntity.getId()));
                        }
                    }
                    if (!taughtHamon) {
                        ClientUtil.openHamonTeacherUi();
                    }
                }
                else {
                    boolean askedForHamonTraining = false;
                    if (mouseTarget instanceof EntityRayTraceResult) {
                        Entity mouseTargetEntity = ((EntityRayTraceResult) mouseTarget).getEntity();
                        askedForHamonTraining = HamonUtil.interactWithHamonTeacher(mc.level, mc.player, (LivingEntity) mouseTargetEntity);
                        if (askedForHamonTraining) {
                            PacketManager.sendToServer(new ClHamonInteractAskTeacherPacket(mouseTargetEntity.getId()));
                        }
                    }
                    if (!askedForHamonTraining) {
                        ITextComponent message;
                        if (nonStandPower.getTypeSpecificData(ModPowers.VAMPIRISM.get())
                                .map(vampirism -> vampirism.isVampireHamonUser()).orElse(false)) {
                            message = new TranslationTextComponent("jojo.chat.message.no_hamon_vampire");
                        }
                        else if (nonStandPower.hadPowerBefore(ModPowers.HAMON.get())) {
                            message = new TranslationTextComponent("jojo.chat.message.no_hamon_abandoned");
                        }
                        else {
                            message = new TranslationTextComponent("jojo.chat.message.no_hamon");
                        }
                        mc.gui.handleChat(ChatType.GAME_INFO, message, Util.NIL_UUID);
                    }
                }
            }
            
            if (editHotbars.consumeClick() && (standPower.hasPower() || nonStandPower.hasPower())) {
                HudLayoutEditingScreen screen = new HudLayoutEditingScreen();
                mc.setScreen(screen);
            }
            
            if (!mc.options.keyAttack.isDown()) {
                leftClickBlockDelay = 0;
            }
            if (!actionQuickAccess.isDown()) {
                quickAccessMmbDelay = 0;
            }
            
            checkHeldActionAndTarget(standPower, targetChanged);
            checkHeldActionAndTarget(nonStandPower, targetChanged);
            
            if (targetChanged) {
                ClientEventHandler.onMouseTargetChanged(mouseTarget);
            }
        }
    }
    
    
    public void switchLockedHotbarControls(ActionsLayout.Hotbar hotbar) {
        setLockedHotbarControls(hotbar, !areControlsLockedForHotbar(hotbar));
    }
    
    private void setLockedHotbarControls(ActionsLayout.Hotbar hotbar, boolean value) {
        switch (hotbar) {
        case LEFT_CLICK:
            lockedAttacksHotbar = value;
            if (value) lockedAbilitiesHotbar = false;
            break;
        case RIGHT_CLICK:
            if (value) lockedAttacksHotbar = false;
            lockedAbilitiesHotbar = value;
            break;
        }
    }
    
    private boolean lockedAttacksHotbar;
    private boolean lockedAbilitiesHotbar;
    public boolean areControlsLockedForHotbar(ActionsLayout.Hotbar hotbar) {
        if (hotbar == null) return false;
        switch (hotbar) {
        case LEFT_CLICK:
            return lockedAttacksHotbar;
        case RIGHT_CLICK:
            return lockedAbilitiesHotbar;
        }
        return false;
    }
    
    
    private static final Random RANDOM = new Random();
    public void setRandomStandSkin() {
        if (standPower.hasPower()) {
            ResourceLocation standId = standPower.getType().getRegistryName();
            List<StandSkin> allSkins = StandSkinsManager.getInstance().getStandSkinsView(standId);
            int i = RANDOM.nextInt(allSkins.size());
            Optional<StandSkin> standSkin = Optional.of(allSkins.get(i));
            PacketManager.sendToServer(new ClSetStandSkinPacket(standSkin.map(skin -> skin.resLoc), standId));
        }
    }
    
    private boolean pickMouseTarget() {
        RayTraceResult target = mc.hitResult;
        if (actionsOverlay != null && actionsOverlay.getCurrentPower() != null) {
            IPower<?, ?> power = actionsOverlay.getCurrentPower();
            if (power.hasPower()) {
                target = power.clientHitResult(mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player, target);
            }
        }
        
        if (target != null && !MCUtil.rayTraceTargetEquals(target, mouseTarget)) {
            this.mouseTarget = target;
            return true;
        }
        
        return false;
    }
    
    private final Map<IPower<?, ?>, ActionKey> heldKeys = new HashMap<>();
    
    public enum ActionKey {
        ATTACK(ActionsLayout.Hotbar.LEFT_CLICK) {
            @Override
            protected KeyBinding getKey(Minecraft mc, InputHandler modInput) { return mc.options.keyAttack; }
        },
        ABILITY(ActionsLayout.Hotbar.RIGHT_CLICK) {
            @Override
            protected KeyBinding getKey(Minecraft mc, InputHandler modInput) { return mc.options.keyUse; }
        },
        QUICK_ACCESS(null) {
            @Override
            protected KeyBinding getKey(Minecraft mc, InputHandler modInput) { return modInput.actionQuickAccess; }
        };
        
        private final ActionsLayout.Hotbar hotbar;
        
        private ActionKey(ActionsLayout.Hotbar hotbar) {
            this.hotbar = hotbar;
        }
        
        protected abstract KeyBinding getKey(Minecraft mc, InputHandler modInput);
        
        @Nullable
        public ActionsLayout.Hotbar getHotbar() {
            return hotbar;
        }
    }
    
    private void checkHeldActionAndTarget(IPower<?, ?> power, boolean targetChanged) {
        boolean keyHeld;
        if (heldKeys.containsKey(power)) {
            keyHeld = heldKeys.get(power).getKey(mc, this).isDown();
            if (!keyHeld) {
                heldKeys.remove(power);
            }
        }
        else {
            keyHeld = mc.options.keyAttack.isDown() || mc.options.keyUse.isDown() || mc.options.keyPickItem.isDown();
        }
        
        if (!keyHeld && power.getHeldAction() != null) {
            stopHeldAction(power, power.getPowerClassification() == actionsOverlay.getCurrentMode());
        }
        
        if (power.isTargetUpdateTick() && targetChanged) {
            PacketManager.sendToServer(ClHeldActionTargetPacket.withRayTraceResult(power.getPowerClassification(), mouseTarget));
        }
    }
    
    public void stopHeldAction(IPower<?, ?> power, boolean shouldFire) {
        if (power.getHeldAction() != null) {
            power.stopHeldAction(shouldFire);
            PacketManager.sendToServer(new ClStopHeldActionPacket(power.getPowerClassification(), shouldFire));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelClickInput(ClickInputEvent event) {
        if (SoulController.getInstance().isCameraEntityPlayerSoul()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
        else if (nonStandPower != null) {
            nonStandPower.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (hamon.isMeditating()) {
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            });
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void modActionClick(ClickInputEvent event) {
        doubleShift.reset();
        
        if (mc.player.isSpectator() || event.getHand() == Hand.OFF_HAND || disableHotbars.isDown()) {
            return;
        }

        ActionKey key;
        KeyBinding keyBinding;
        if (event.isAttack()) {
            key = ActionKey.ATTACK;
            keyBinding = mc.options.keyAttack;
        }
        else if (event.isUseItem()) {
            key = ActionKey.ABILITY;
            keyBinding = mc.options.keyUse;
        }
        else {
            return;
        }
        
        HudClickResult clickResult = handleMouseClickPowerHud(key, keyBinding);
        if (clickResult.vanillaInput == HudClickResult.Behavior.CANCEL) {
            event.setCanceled(true);
        }
        if (clickResult.handSwing == HudClickResult.Behavior.CANCEL) {
            event.setSwingHand(false);
        }
    }
    
    private void clickWithBusyHands() {
        if (ClientUtil.arePlayerHandsBusy()) {
            while (mc.options.keyAttack.consumeClick()) {
                handleMouseClickPowerHud(ActionKey.ATTACK, mc.options.keyAttack);
            }
            while (mc.options.keyUse.consumeClick()) {
                handleMouseClickPowerHud(ActionKey.ABILITY, mc.options.keyUse);
            }
        }
    }
    
    private <P extends IPower<P, ?>> HudClickResult handleMouseClickPowerHud(ActionKey key, KeyBinding keyBinding) {
        HudClickResult result = new HudClickResult();
        if (!actionsOverlay.areHotbarsEnabled() || mc.player.isSpectator()) {
            return result;
        }

        P power = (P) actionsOverlay.getCurrentPower();

        ActionsLayout.Hotbar actionType = key.getHotbar();
        boolean actionClick = false;
        if (power != null) {
            if (key == ActionKey.QUICK_ACCESS) {
                actionClick = actionsOverlay.isQuickAccessActive();
            }
            else {
                actionClick = !actionsOverlay.noActionSelected(actionType);
            }
        }
        
        if (!actionClick) {
            // cancel vanilla click
            if (shouldVanillaInputStun()) {
                result.cancelVanillaInput();
            }
            return result;
        }
        
        if (key == ActionKey.ATTACK && leftClickBlockDelay > 0) {
            result.cancelHandSwing();
            result.cancelVanillaInput();
            return result;
        }
        
        if (power != null) {
            boolean leftClickedBlock = key == ActionKey.ATTACK && mc.hitResult.getType() == Type.BLOCK;
            boolean sneak = mc.player.isShiftKeyDown();
            boolean shiftActionVar = useShiftActionVariant(mc);
            
            Pair<Action<P>, Boolean> click = null;
            if (key == ActionKey.QUICK_ACCESS) {
                click = actionsOverlay.onQuickAccessClick(power, shiftActionVar, sneak);
            }
            else if (!(leftClickedBlock && leftClickBlockDelay > 0)) {
                click = actionsOverlay.onClick(power, key.getHotbar(), shiftActionVar, sneak);
            }
            if (click != null && click.getRight()) {
                Action<P> action = click.getLeft();
                if (action != null) {
                    result.handSwing = action.getHoldDurationMax(power) <= 0 && action.swingHand() ? HudClickResult.Behavior.FORCE : HudClickResult.Behavior.CANCEL;
                    if (action.cancelsVanillaClick()) result.cancelVanillaInput();
                }
                if (action.getHoldDurationMax(power) > 0) {
                    heldKeys.put(power, key);
                }
                if (leftClickedBlock && leftClickBlockDelay <= 0) {
                    leftClickBlockDelay = 4;
                }
            }
            else {
                if (heldKeys.get(power) == key) {
                    result.cancelHandSwing();
                    result.cancelVanillaInput();
                }
                else if (shouldVanillaInputStun()) {
                    result.cancelHandSwing();
                }
            }
        }
        
        return result;
    }
    
    public static boolean useShiftActionVariant(Minecraft mc) {
        return mc.player.isShiftKeyDown();
    }
    
    public static boolean renderShiftVarInScreenUI(Minecraft mc, int key, int scanCode) {
        return mc.options.keyShift.matches(key, scanCode);
    }
    
    private static class HudClickResult {
        private Behavior vanillaInput = Behavior.PASS;
        private Behavior handSwing = Behavior.PASS;
        
        public void cancelVanillaInput() {
            vanillaInput = Behavior.CANCEL;
        }
        
        public void cancelHandSwing() {
            handSwing = Behavior.CANCEL;
        }
        
        public enum Behavior {
            CANCEL,
            PASS,
            FORCE
        }
    }
    
    private boolean shouldVanillaInputStun() {
        return ModStatusEffects.isStunned(mc.player) || !mc.player.canUpdate();
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void fixArrowPunchKick(ClickInputEvent event) {
        if (event.isAttack() && mc.hitResult.getType() == Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mc.hitResult).getEntity();
            if (entity == mc.player || entity instanceof ItemProjectileEntity) {
                event.setCanceled(true); // prevents kick for "Attempting to attack an invalid entity"
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void invertMovementInput(InputUpdateEvent event) {
        if (GeneralUtil.orElseFalse(INonStandPower.getNonStandPowerOptional(event.getPlayer()).resolve().flatMap(
                power -> power.getTypeSpecificData(ModPowers.HAMON.get())), hamon -> {
                    if (hamon.isMeditating()) {
                        MovementInput input = event.getMovementInput();
                        if (hamon.getMeditationTicks() >= 40) {
                            boolean hasInput = input.up || input.down || input.left || input.right || input.jumping;
                            if (hasInput) {
                                PacketManager.sendToServer(new ClHamonMeditationPacket(false));
                            }
                        }
                        input.up = false;
                        input.down = false;
                        input.left = false;
                        input.right = false;
                        input.jumping = false;
                        input.forwardImpulse = 0;
                        input.leftImpulse = 0;
                        return true;
                    }
                    return false;
                })) {
            return;
        }
        
        if (event.getPlayer().hasEffect(ModStatusEffects.MISSHAPEN_LEGS.get())) {
            MovementInput input = event.getMovementInput();
            input.forwardImpulse *= -1;
            input.leftImpulse *= -1;
            
            boolean tmp = input.down;
            input.down = input.up;
            input.up = tmp;
            
            tmp = input.left;
            input.left = input.right;
            input.right = tmp;
            
            tmp = input.jumping;
            input.jumping = input.shiftKeyDown;
            input.shiftKeyDown = tmp;
        }
    }
    
    private boolean wasStunned = false;
    private double prevSensitivity = 0.5;
    private static final double ZERO_SENSITIVITY = -1.0 / 3.0;
    @SubscribeEvent
    public void setMouseSensitivity(ClientTickEvent event) {
        if (mc.player == null) {
            if (mc.options.sensitivity <= ZERO_SENSITIVITY) {
                mc.options.sensitivity = prevSensitivity;
            }
            return;
        }
        
        if (ModStatusEffects.isStunned(mc.player)) {
            if (!wasStunned) {
                prevSensitivity = mc.options.sensitivity;
                wasStunned = true;
            }
            mc.options.sensitivity = ZERO_SENSITIVITY;
            return;
        }
        else if (wasStunned) {
            mc.options.sensitivity = prevSensitivity;
            wasStunned = false;
        }
        
        boolean invert = mc.player.hasEffect(ModStatusEffects.MISSHAPEN_FACE.get());
        if (invert ^ mc.options.sensitivity < 0) {
            mc.options.sensitivity = -mc.options.sensitivity + ZERO_SENSITIVITY * 2;
        }
    }
    
    private boolean mouseButtonsSwapped = false;
    private InputMappings.Input lmbKey;
    private InputMappings.Input rmbKey;
    
    public void mouseButtonsInvertTick() {
        if (!mouseButtonsSwapped) {
            lmbKey = mc.options.keyAttack.getKey();
            rmbKey = mc.options.keyUse.getKey();
            mouseButtonsSwapped = true;
        }
        
        if (mc.options.keyAttack.getKey() != rmbKey || mc.options.keyUse.getKey() != lmbKey) {
            mc.options.keyAttack.setKey(rmbKey);
            mc.options.keyUse.setKey(lmbKey);
            KeyBinding.resetMapping();
        }
    }
    
    public void mouseButtonsInvertEnd() {
        if (mouseButtonsSwapped) {
            mc.options.keyAttack.setKey(lmbKey);
            mc.options.keyUse.setKey(rmbKey);
            mouseButtonsSwapped = false;
            KeyBinding.resetMapping();
        }
    }
    
    private void tickEffects() {
        if (mc.player != null && mc.player.hasEffect(ModStatusEffects.MISSHAPEN_ARMS.get())) {
            mouseButtonsInvertTick();
        }
        else {
            mouseButtonsInvertEnd();
        }
    }
    
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInputUpdate(InputUpdateEvent event) {
        MovementInput input = event.getMovementInput();
        
        boolean hasInput = input.up || input.down || input.left || input.right || input.jumping || input.shiftKeyDown;
        if (this.hasInput != hasInput) {
            PacketManager.sendToServer(new ClHasInputPacket(hasInput));
            this.hasInput = hasInput;
        }
        
//        if (hasInput) {
            boolean slowedDown = slowDownFromHeldAction(mc.player, input, standPower);
            slowedDown = slowDownFromHeldAction(mc.player, input, nonStandPower) || slowedDown;
            slowedDown = slowDownFromStandEntity(mc.player, input) || slowedDown;
            slowedDown = slowDownFromContinuousAction(mc.player, input) || slowedDown;
            slowedDown = actionsOverlay.isPlayerOutOfBreath() && slowDown(mc.player, input, 0.8F) || slowedDown;

            canLeap = false;
//            if (!mc.player.isPassenger()) {
                IPower<?, ?> power = actionsOverlay.getCurrentPower();
                if (power != null) {
                    if (power.canLeap() && !slowedDown) {
                        Entity playerVehicle = mc.player.getVehicle();
                        // FIXME let passengers other than the controlling player leap
                        boolean onGround = false;
                        if (playerVehicle != null && playerVehicle.getType() != ModEntityTypes.ROAD_ROLLER.get()) {
                            onGround = playerVehicle.isOnGround()
                                    || playerVehicle.getType() == ModEntityTypes.LEAVES_GLIDER.get()
                                    && MCUtil.collide(playerVehicle, new Vector3d(0, -1, 0)).y > -1;
                        }
                        onGround |= mc.player.isOnGround();
                        // TODO wall leap
                        boolean atWall = false && mc.player.horizontalCollision;
                        
                        boolean groundLeap = onGround && (mc.player.isPassenger() || input.shiftKeyDown) && input.jumping;
                        // FIXME wall leap without pressing shift
                        boolean wallLeap = false;
//                                atWall && !groundLeap && input.jumping &&
//                                (!leapNeedsShiift || input.shiftKeyDown || false);
                        
                        if (groundLeap || wallLeap) {
                            float leapStrength = power.leapStrength();
                            if (leapStrength > 0) {
                                if (!mc.player.isPassenger()) {
                                    input.shiftKeyDown = false;
                                }
                                input.jumping = false;
                                
                                Entity entity = playerVehicle != null ? playerVehicle : mc.player;
                                PacketManager.sendToServer(new ClOnLeapPacket(power.getPowerClassification()));
                                if (groundLeap) {
                                    MCUtil.leap(entity, leapStrength);
                                }
                                else if (wallLeap) {
                                    wallLeap(mc.player, input, leapStrength);
                                }
                            }
                        }
//                        if (onGround && power.getPowerClassification() == PowerClassification.STAND) {
//                            leftDash.inputUpdate(input.left, input.right || input.down, mc.player);
//                            rightDash.inputUpdate(input.right, input.left || input.down, mc.player);
//                            backDash.inputUpdate(input.down, input.left || input.right, mc.player);
//                        }
                        canLeap = onGround || atWall;
                    }
                }
//            }
//        }
        
        Entity vehicle = mc.player.getVehicle();
        if (vehicle instanceof LeavesGliderEntity) {
            ((LeavesGliderEntity) vehicle).setInput(input.left, input.right);
        }
        
        int shiftPress = doubleShift.inputUpdate(input);
        boolean pressedDoubleShift = shiftPress == 2;
        if (pressedDoubleShift && ClDoubleShiftPressPacket.Handler.sendOnPress(mc.player)) {
            mc.player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setDoubleShiftPress());
            PacketManager.sendToServer(new ClDoubleShiftPressPacket());
        }
    }
    
    public boolean canPlayerLeap() {
        return canLeap;
    }
    
    private boolean slowDownFromStandEntity(PlayerEntity player, MovementInput input) {
        if (standPower == null) return false;
        IStandManifestation stand = standPower.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) stand;
            float speed = standEntity.getUserWalkSpeed();
            return slowDown(player, input, speed);
        }
        return false;
    }
    
    private boolean slowDownFromHeldAction(PlayerEntity player, MovementInput input, IPower<?, ?> power) {
        if (power == null) return false;
        Action<?> heldAction = power.getHeldAction();
        if (heldAction != null) {
            float speed = heldAction.getHeldWalkSpeed();
            return slowDown(player, input, speed);
        }
        return false;
    }
    
    private boolean slowDownFromContinuousAction(PlayerEntity player, MovementInput input) {
        Optional<ContinuousActionInstance<?, ?>> action = player.getCapability(PlayerUtilCapProvider.CAPABILITY)
                .resolve().flatMap(cap -> cap.getContinuousAction());
        if (action.isPresent()) {
            float speed = action.get().getWalkSpeed();
            return slowDown(player, input, speed);
        }
        return false;
    }
    
    private boolean slowDown(PlayerEntity player, MovementInput input, float speed) {
        if (speed < 1.0F) {
            input.leftImpulse *= speed;
            input.forwardImpulse *= speed;
            player.setSprinting(false);
            KeyBinding.set(mc.options.keySprint.getKey(), false);
            if (speed == 0) {
                input.jumping = false;
            }
            return true;
        }
        return false;
    }
    
    private void wallLeap(ClientPlayerEntity player, MovementInput input, float strength) {
        player.hasImpulse = true;
        Vector3d inputVec = new Vector3d(player.xxa, 0, player.zza)
                .yRot((-player.yRot) * MathUtil.DEG_TO_RAD);
        Vector3d collide = MCUtil.collide(player, inputVec);
        Vector3d leap = collide.subtract(inputVec).normalize().scale(strength);
        float leapYRot = (float) -MathHelper.atan2(leap.x, leap.z);
        leap = leap.yRot(leapYRot)
                .xRot(-MathHelper.clamp(player.xRot, -82.5F, -30F) * MathUtil.DEG_TO_RAD)
                .yRot(-leapYRot);
        player.setDeltaMovement(leap.x, leap.y * 0.5, leap.z);
    }
    
    private final DashTrigger leftDash = new DashTrigger(-90F);
    private final DashTrigger rightDash = new DashTrigger(90F);
    private final DashTrigger backDash = new DashTrigger(180F);
    
    private void dash(ClientPlayerEntity player, float yRot) {
        PacketManager.sendToServer(new ClOnStandDashPacket());
        player.setOnGround(false);
        player.hasImpulse = true;
        Vector3d dash = Vector3d.directionFromRotation(0, player.yRot + yRot).scale(0.5).add(0, 0.2, 0);
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
        
        private void inputUpdate(boolean keyPress, boolean anotherKeyPress, ClientPlayerEntity player) {
            if (anotherKeyPress) {
                triggerTime = 0;
                return;
            }
            if (triggerTime > 0) {
                triggerTime--;
            }
            if (keyPress) {
                if (triggerTime > 0 && triggerGap) {
                    dash(player, yRot);
                }
                triggerTime = 7;
            }
            triggerGap = !keyPress;
        }
    }
    
    
    
    private class DoubleShiftDetector {
        private MovementInput prevTickInput = null;
        private int shiftPresses = 0;
        private int triggerTime = 0;
        private boolean triggerGap = false;
        
        private int inputUpdate(MovementInput playerInput) {
            boolean isShiftPressed = playerInput.shiftKeyDown;
            boolean trigger = false;
            
            if (shiftPresses > 0 && (playerInput.jumping || !checkInputUpdate(prevTickInput, playerInput))) {
                reset();
            }
            
            if (triggerTime > 0) {
                if (--triggerTime == 0) {
                    reset();
                }
            }
            
            if (isShiftPressed) {
                trigger = triggerGap && (shiftPresses == 0 || triggerTime > 0);
                if (trigger) {
                    triggerTime = 7;
                    if (shiftPresses++ == 0) {
                        saveInputState(playerInput);
                    }
                }
            }
            
            triggerGap = !isShiftPressed;
            
            return trigger ? shiftPresses : 0;
        }
        
        private boolean checkInputUpdate(@Nullable MovementInput prevTick, MovementInput thisTick) {
            if (prevTick == null) {
                saveInputState(thisTick);
                return true;
            }
            
            return  prevTick.up == thisTick.up &&
                    prevTick.down == thisTick.down &&
                    prevTick.left == thisTick.left &&
                    prevTick.right == thisTick.right;
        }
        
        private void saveInputState(MovementInput input) {
            this.prevTickInput = new MovementInput();
            this.prevTickInput.up = input.up;
            this.prevTickInput.down = input.down;
            this.prevTickInput.left = input.left;
            this.prevTickInput.right = input.right;
        }
        
        private void reset() {
            shiftPresses = 0;
            triggerTime = 0;
        }
    }
    
    
    
    public enum MouseButton {
        LEFT,
        RIGHT,
        MIDDLE;
        
        public static MouseButton getButtonFromId(int id) {
            if (id >= 0 && id < MouseButton.values().length) {
                return MouseButton.values()[id];
            }
            return null;
        }
    }
    
    private static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
        map.put(GLFW.GLFW_KEY_UP,    Direction2D.UP);
        map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
        map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
    });
    
    @Nullable
    public static Direction2D getArrowKey(int keyCode) {
        return ARROW_KEYS.get(keyCode);
    }
}
