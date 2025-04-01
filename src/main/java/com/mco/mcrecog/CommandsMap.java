package com.mco.mcrecog;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.misc.MultiMap;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.HudClickResult;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClClickActionPacket2;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.JsonModUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Hand;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class CommandsMap extends ReloadListener<JsonObject> {
    private final Gson gson;
    public MultiMap<String, VoiceCommand> allCommands = new MultiMap<>();
    
    public CommandsMap(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected JsonObject prepare(IResourceManager pResourceManager, IProfiler pProfiler) {
        JsonObject json = new JsonObject();
        
        for (String namespace : pResourceManager.getNamespaces()) {
            
            try {
                for(IResource resource : pResourceManager.getResources(new ResourceLocation(namespace, "rotp_vc.json"))) {

                    try (
                            InputStream inputstream = resource.getInputStream();
                            Reader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
                            ) {
                        JsonObject readJson = JSONUtils.fromJson(this.gson, reader, JsonObject.class);
                        if (readJson != null) {
                            JsonModUtil.merge(json, readJson);
                        }

                        pProfiler.pop();
                    } catch (RuntimeException e) {
                        JojoMod.LOGGER.error(e);
                    }

                    pProfiler.pop();
                }
             } catch (IOException e) {
             }
        }
        return json;
    }

    @Override
    protected void apply(JsonObject pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
        allCommands.clear();
        for (Map.Entry<String, JsonElement> powerTypeEntry : pObject.entrySet()) {
            ResourceLocation powerTypeId = new ResourceLocation(powerTypeEntry.getKey());
            if (powerTypeEntry.getValue().isJsonObject()) {
                JsonObject commandsMap = powerTypeEntry.getValue().getAsJsonObject();
                for (Map.Entry<String, JsonElement> commandActionEntry : commandsMap.entrySet()) {
                    String commandActionStr = commandActionEntry.getKey();
                    JsonElement elem = commandActionEntry.getValue();
                    JsonArray commands;
                    if (elem.isJsonArray()) {
                        commands = elem.getAsJsonArray();
                    } else {
                        commands = new JsonArray();
                        commands.add(elem);
                    }
                    for (JsonElement commandJson : commands) {
                        String command = commandJson.getAsString().replace(" ", "").toLowerCase();
                        VoiceCommand commandAction = null;
                        if ("summon".equals(commandActionStr)) {
                            commandAction = VoiceCommand.summonStand(powerTypeId);
                        }
                        else {
                            commandAction = VoiceCommand.useAbility(powerTypeId, new ResourceLocation(commandActionStr));
                        }
                        if (commandAction != null) {
                            allCommands.map(command, commandAction);
                        }
                    }
                }
            }
        }
    }
    
    public boolean onVoiceCommand(String commandRaw, IStandPower stand, INonStandPower power) {
        dontStopHeld = false;
        boolean result = false;
        PlayerEntity player = ClientUtil.getClientPlayer();
        if (player != null) {
            String commandStr = commandRaw.replace(" ", "").toLowerCase();
            
            List<VoiceCommand> commands = allCommands.get(commandStr.toLowerCase());
            if (commands != null) {
                if (triggerCommands(commands, stand, power)) {
                    result = true;
                }
            }
            else {
                for (Map.Entry<String, List<VoiceCommand>> commandsEntry : allCommands.entrySet()) {
                    if (commandStr.startsWith(commandsEntry.getKey()) && triggerCommands(commandsEntry.getValue(), stand, power)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }
    
    public static boolean triggerCommands(List<VoiceCommand> commands, IStandPower stand, INonStandPower power) {
        boolean result = false;
        for (VoiceCommand command : commands) {
            if (command.trigger(stand, power)) {
                result = true;
            }
        }
        return result;
    }

    
    public static class VoiceCommand {
        public final ResourceLocation powerTypeId;
        
        public final ResourceLocation abilityId;
        public final boolean isStandSummon;
        
        public VoiceCommand(ResourceLocation powerTypeId, ResourceLocation abilityId,  boolean isStandSummon) {
            this.powerTypeId = powerTypeId;
            this.abilityId = abilityId;
            this.isStandSummon = isStandSummon;
        }
        
        public static VoiceCommand useAbility(ResourceLocation powerTypeId, ResourceLocation abilityId) {
            return new VoiceCommand(powerTypeId, abilityId, false);
        }
        
        public static VoiceCommand summonStand(ResourceLocation standTypeId) {
            return new VoiceCommand(standTypeId, null, true);
        }
        
        
        public boolean trigger(IStandPower standPower, INonStandPower nonStandPower) {
            boolean result = false;
            if (standPower != null && standPower.hasPower() && standPower.getType().getRegistryName().equals(powerTypeId)) {
                if (isStandSummon) {
                    if (standPower.hasPower() && !standPower.isActive()) {
                        ActionsOverlayGui.getInstance().onStandSummon();
                    }
                    PacketManager.sendToServer(new ClToggleStandSummonPacket());
                    result = true;
                }
                if (abilityId != null) {
                    Action<?> ability = JojoCustomRegistries.ACTIONS.getValue(abilityId);
                    if (ability != null) JojoMod.LOGGER.debug(ability.getRegistryName());
                    if (ability != null && clickAbility(ability, standPower)) {
                        result = true;
                    }
                }
            }
            
            if (nonStandPower != null && nonStandPower.hasPower() && nonStandPower.getType().getRegistryName().equals(powerTypeId)) {
                if (abilityId != null) {
                    Action<?> ability = JojoCustomRegistries.ACTIONS.getValue(abilityId);
                    if (ability != null && clickAbility(ability, nonStandPower)) {
                        result = true;
                    }
                }
            }
            return result;
        }
    }
    
    public static boolean dontStopHeld = false;
    
    public static <P extends IPower<P, ?>> boolean clickAbility(Action<?> ability, P power) {
        if (ability == null) return false;
        Minecraft mc = Minecraft.getInstance();

        if (power != null) {
            ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
            
            boolean sneak = InputHandler.useShiftActionVariant(mc);
            Action<P> action = (Action<P>) ability;
            action = ActionsOverlayGui.resolveVisibleActionInSlot(action, false, power, hud.getMouseTarget());
            
            ActionTarget mouseTarget = hud.getMouseTarget();
            ClClickActionPacket2 packet = new ClClickActionPacket2(
                    power.getPowerClassification(), action, mouseTarget, sneak);
            PacketManager.sendToServer(packet);
            action.clWriteExtraData(hud._extraInputBuf);
            
            boolean actionWentOff = ClClickActionPacket2.Handler.clickAction(power, action, sneak, mouseTarget, hud._extraInputBuf);
            
            hud._extraInputBuf.clear();
            
            if (action != null) {
                if (action.withUserPunch()) {
                    InputHandler.getInstance().mcPlayerAttack();
                }
                if (actionWentOff && (InputHandler.actionSwingsHand(action, power) == HudClickResult.Behavior.FORCE)) {
                    mc.player.swing(Hand.MAIN_HAND);
                }
                if (power.getHeldAction() == ability) {
                    dontStopHeld = true;
                }
            }
            
            // TODO NSP actions
            
            // TODO test the inputs more
            // TODO disable the Tmp chat activation
            
            // TODO make the installation instruction
            // TODO send to the beta testers
            // TODO make the update post
        }
        
        return true;
    }
    
}
