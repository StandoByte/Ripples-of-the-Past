package com.github.standobyte.jojo.power.layout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPower;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ActionHotbarLayout<P extends IPower<P, ?>> { // TODO remove the generic parameter
    private final Map<Action<P>, ActionSwitch<P>> _actions = new LinkedHashMap<>();
    private List<ActionSwitch<P>> hotbarOrder = new ArrayList<>();
    private List<Action<P>> enabledActionsCache = new ArrayList<>();
    
    
    
    public List<Action<P>> getEnabled() {
        return enabledActionsCache;
    }
    
    public void editLayout(Runnable edit) {
        edit.run();
        updateCache();
    }
    
    private void updateCache() {
        this.enabledActionsCache = hotbarOrder.stream()
                .filter(entry -> entry.isEnabled())
                .map(ActionSwitch::getAction)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }
    
    public List<ActionSwitch<P>> getLayoutView() {
        return ImmutableList.copyOf(hotbarOrder);
    }
    
    void initActions(@Nullable Action<P>[] newTypeActions) {
        _actions.clear();
        if (newTypeActions != null) {
            for (Action<P> action : newTypeActions) {
                _actions.put(action, new ActionSwitch<>(action));
            }
        }
        // resetLayout() is called afterwards
    }
    
    void resetLayout() {
        hotbarOrder = new ArrayList<>(_actions.values());
        editLayout(() -> hotbarOrder.forEach(actionSwitch -> actionSwitch.reset()));
    }
    
    void copySwitches(ActionHotbarLayout<P> source) {
        editLayout(() -> {
            _actions.forEach((action, actionSwitch) -> {
                if (source._actions.containsKey(action)) {
                    actionSwitch.copyFrom(source._actions.get(action));
                }
            });
        });
    }
    
    public void addExtraAction(Action<P> action) {
        if (!_actions.containsKey(action)) {
            editLayout(() -> {
                ActionSwitch<P> actionSwitch = new ActionSwitch<>(action);
                _actions.put(action, actionSwitch);
                hotbarOrder.add(actionSwitch);
            });
        }
    }
    
    public void removeAction(Action<P> action) {
        if (_actions.containsKey(action)) {
            editLayout(() -> {
                ActionSwitch<P> actionSwitch = _actions.remove(action);;
                hotbarOrder.remove(actionSwitch);
            });
        }
    }
    
    public void setIsEnabled(Action<?> action, boolean isEnabled) {
        if (_actions.containsKey(action)) {
            ActionSwitch<P> actionSwitch = _actions.get(action);
            if (actionSwitch.isEnabled() != isEnabled) {
                editLayout(() -> actionSwitch.setIsEnabled(isEnabled));
            }
        }
    }
    
    public void swapActionsOrder(Action<?> first, Action<?> second) {
        int firstIndex = getOrderPlaceOf(first);
        if (firstIndex < 0) return;
        int secondIndex = getOrderPlaceOf(second);
        if (secondIndex < 0) return;
        editLayout(() -> Collections.swap(hotbarOrder, firstIndex, secondIndex));
    }
    
    private int getOrderPlaceOf(Action<?> action) {
        if (_actions.containsKey(action)) {
            ActionSwitch<?> actionSwitch = _actions.get(action);
            if (actionSwitch != null) {
                return hotbarOrder.indexOf(actionSwitch);
            }
        }
        return -1;
    }
    
    
    
    public static final int ARBITRARY_ACTIONS_LIMIT = 16;
    public void toBuf(PacketBuffer buf) {
        List<ActionSwitch<P>> switches = getLayoutView();
        int size = switches.size();
        if (size > ARBITRARY_ACTIONS_LIMIT) {
            JojoMod.getLogger().warn("Tried to send a layout of more than {} actions", ARBITRARY_ACTIONS_LIMIT);
            size = ARBITRARY_ACTIONS_LIMIT;
        }
        buf.writeVarInt(size);
        
        short enabled = 0;
        for (int i = 0; i < size; i++) {
            ActionSwitch<P> action = switches.get(i);
            buf.writeRegistryId(action.getAction());
            enabled <<= 1;
            enabled |= action.isEnabled() ? 1 : 0;
        }
        
        buf.writeShort(enabled);
    }
    
    public void setFromBuf(PacketBuffer buf) {
        int count = Math.min(buf.readVarInt(), ARBITRARY_ACTIONS_LIMIT);
        List<ActionSwitch<?>> switches = new ArrayList<>(count);
        
        for (int i = 0; i < count; i++) {
            switches.add(new ActionSwitch<P>((Action<P>) buf.readRegistryIdSafe(Action.class)));
        }
        
        short enableFlags = buf.readShort();
        for (int i = count - 1; i >= 0; i--) {
            switches.get(i).isEnabled = (enableFlags & 1) > 0;
            enableFlags >>= 1;
        }
        
        editLayout(() -> {
            hotbarOrder.clear();
            
            for (ActionSwitch<?> switchFromNetwork : switches) {
                Action<?> action = switchFromNetwork.getAction();
                
                if (action != null) {
                    ActionSwitch<P> switchLocal = _actions.computeIfAbsent((Action<P>) action, ActionSwitch::new);
                    switchLocal.copyFrom(switchFromNetwork);
                    hotbarOrder.add(switchLocal);
                }
            }
            
            // shouldn't happen, but just in case
            if (fillInMissingActions()) {
                JojoMod.getLogger().warn("Action layout wasn't full!");
            }
        });
    }
    
    private boolean fillInMissingActions() {
        boolean wereMissing = false;
        for (ActionSwitch<P> notAdded : _actions.values()) {
            if (!hotbarOrder.contains(notAdded)) {
                hotbarOrder.add(notAdded);
                wereMissing = true;
            }
        }
        return wereMissing;
    }
    
    public boolean containsAction(Action<P> action) {
        return _actions.containsKey(action);
    }
    
    
    
    Optional<ListNBT> toNBT() {
        ListNBT nbt = new ListNBT();
        hotbarOrder.forEach(actionSwitch -> {
            CompoundNBT actionNBT = new CompoundNBT();
            actionNBT.put("Action", StringNBT.valueOf(actionSwitch.action.getRegistryName().toString()));
            actionNBT.putBoolean("Enabled", actionSwitch.isEnabled);
            nbt.add(actionNBT);
        });
        return Optional.of(nbt);
    }
    
    void fromNBT(ListNBT nbt) {
        editLayout(() -> {
            hotbarOrder.clear();
            
            for (INBT actionNBT : nbt) {
                if (actionNBT instanceof CompoundNBT) {
                    CompoundNBT actionCNBT = (CompoundNBT) actionNBT;
                    String actionName = actionCNBT.getString("Action");
                    if (!actionName.isEmpty()) {
                        ResourceLocation actionId = new ResourceLocation(actionName);
                        if (JojoCustomRegistries.ACTIONS.getRegistry().containsKey(actionId)) {
                            Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(actionId);
                            
                            if (action != null) {
                                ActionSwitch<P> switchLocal = _actions.computeIfAbsent((Action<P>) action, ActionSwitch::new);
                                switchLocal.fromNBT(actionCNBT);
                                hotbarOrder.add(switchLocal);
                            }
                        }
                    }
                }
            }
            
            fillInMissingActions();
        });
    }
    
    
    
    public static class JsonSerialization implements JsonSerializer<ActionHotbarLayout<?>>, JsonDeserializer<ActionHotbarLayout<?>> {
        public static final JsonSerialization INSTANCE = new JsonSerialization();
        
        protected JsonSerialization() {}

        @Override
        public ActionHotbarLayout<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            ActionHotbarLayout<?> layout = new ActionHotbarLayout<>();
            
            JsonObject jsonObj = json.getAsJsonObject();
            JsonArray actions = jsonObj.get("actions").getAsJsonArray();
            for (JsonElement actionName : actions) {
                Action<?> action = Action.JsonSerialization.INSTANCE.deserialize(actionName, Action.class, context);
                if (action != null) {
                    // FIXME initialize this beforehand (with the power type instance), 
                    // these entries must not be serialized nor deserialized
                    addActionBase(layout, action);
                }
            }
            
            JsonObject layoutJson = jsonObj.get("hotbarOrder").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : layoutJson.entrySet()) {
                Action<?> action = Action.JsonSerialization.INSTANCE.fromStringId(entry.getKey());
                if (action != null && layout._actions.containsKey(action)) {
                    ActionSwitch<?> actionSwitch = layout._actions.get(action);
                    actionSwitch.isEnabled = entry.getValue().getAsBoolean();
                    addToLayout(layout, actionSwitch);
                }
            }
            
            layout.fillInMissingActions();
            layout.updateCache();
            return layout;
        }
        
        private static <P extends IPower<P, ?>> void addActionBase(ActionHotbarLayout<P> layout, Action<?> action) {
            Action<P> actionP = (Action<P>) action;
            layout._actions.put(actionP, new ActionSwitch<>(actionP));
        }
        
        private static <P extends IPower<P, ?>> void addToLayout(ActionHotbarLayout<P> layout, ActionSwitch<?> actionSwitch) {
            layout.hotbarOrder.add((ActionSwitch<P>) actionSwitch);
        }

        @Override
        public JsonElement serialize(ActionHotbarLayout<?> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            
            JsonArray actions = new JsonArray();
            for (ActionSwitch<?> actionSwitch : src._actions.values()) {
                actions.add(actionSwitch.action.getRegistryName().toString());
            }
            json.add("actions", actions);
            
            JsonObject actionsOrder = new JsonObject();
            for (ActionSwitch<?> actionSwitch : src.hotbarOrder) {
                actionsOrder.addProperty(actionSwitch.action.getRegistryName().toString(), actionSwitch.isEnabled);
            }
            json.add("hotbarOrder", actionsOrder);
            
            return json;
        }
    }
    
    
    
    public static class ActionSwitch<P extends IPower<P, ?>> {
        private final Action<P> action;
        private boolean isEnabled = true;
        
        private ActionSwitch(Action<P> action) {
            this(action, action.enabledInHudDefault());
        }
        
        private ActionSwitch(Action<P> action, boolean isEnabled) {
            this.action = action;
            this.isEnabled = isEnabled;
        }
        
        public Action<P> getAction() {
            return action;
        }
        
        public boolean isEnabled() {
            return isEnabled;
        }
        
        private void setIsEnabled(boolean enable) {
            this.isEnabled = enable;
        }
        
        private void reset() {
            this.isEnabled = action.enabledInHudDefault();
        }
        
        private void copyFrom(ActionSwitch<?> actionSwitch) {
            this.isEnabled = actionSwitch.isEnabled;
        }
        
        void fromNBT(CompoundNBT nbt) {
            this.isEnabled = nbt.contains("Enabled") ? nbt.getBoolean("Enabled") : true;
        }
    }
}
