package com.github.standobyte.jojo.power;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.ModCommonRegistries;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class ActionHotbarData<P extends IPower<P, ?>> {
    private List<Action<P>> allActions = new ArrayList<>();
    private ActionHotbarLayout<P> userSetLayout = new ActionHotbarLayout<>(new ArrayList<>());
    private List<Action<P>> enabledActions = new ArrayList<>();

    
    
    public Collection<Action<P>> getAll() {
        return Collections.unmodifiableList(allActions);
    }
    
    public List<Action<P>> getEnabled() {
        return enabledActions;
    }
    
    public void setLayout(ActionHotbarLayout<P> layout) {
        userSetLayout = layout;
        updateEnabledActions();
    }
    
    public ActionHotbarLayout<P> getLayout() {
        return userSetLayout;
    }
    
    public void editLayout(Consumer<ActionHotbarLayout<P>> edit) {
        edit.accept(userSetLayout);
        updateEnabledActions();
    }
    
    void resetLayout() {
        userSetLayout = new ActionHotbarLayout<P>(allActions);
        updateEnabledActions();
        userSetLayout.wasEdited = false;
    }
    
    public List<ActionSwitch<P>> getLayoutView() {
        return Collections.unmodifiableList(userSetLayout.actionEnableSwitch);
    }
    
    public void addExtraAction(Action<P> action) {
        if (allActions.add(action)) {
            editLayout(layout -> {
                layout.actionEnableSwitch.add(new ActionSwitch<>(action));
            });
        }
    }
    
    public void removeAction(Action<P> action) {
        if (allActions.remove(action)) {
            editLayout(layout -> {
                layout.actionEnableSwitch.removeIf(actionSwitch -> actionSwitch.getAction() == action);
            });
        }
    }
    
    void onNewPowerType(@Nullable Action<P>[] newTypeActions) {
        this.allActions = newTypeActions != null ? new ArrayList<>(Arrays.asList(newTypeActions)) : new ArrayList<>();
    }
    
    public void updateEnabledActions() {
        userSetLayout.wasEdited = true;
        this.enabledActions = userSetLayout.actionEnableSwitch.stream()
                .filter(actionSwitch -> actionSwitch.isEnabled)
                .map(actionSwitch -> actionSwitch.action)
                .collect(Collectors.toList());
    }
    
    Optional<ListNBT> layoutNBT() {
        return userSetLayout.wasEdited ? Optional.of(userSetLayout.toNBT()) : Optional.empty();
    }
    
    void updateLayoutFromNBT(ListNBT nbt) {
        userSetLayout.fromNBT(nbt);
        updateEnabledActions();
    }

    public void keepLayout(ActionHotbarData<P> oldLayout) {
        this.userSetLayout = oldLayout.userSetLayout;
        this.enabledActions = oldLayout.enabledActions;
    }
    

    public static class ActionHotbarLayout<P extends IPower<P, ?>> {
        private List<ActionSwitch<P>> actionEnableSwitch;
        private boolean wasEdited = false;
        
        private ActionHotbarLayout(List<Action<P>> newTypeActions) {
            actionEnableSwitch = newTypeActions.stream().map(ActionSwitch::new)
                    .collect(Collectors.toList());
        }
        
        public void swapActions(ActionSwitch<?> first, ActionSwitch<?> second) {
            int firstIndex = actionEnableSwitch.indexOf(first);
            if (firstIndex < 0) return;
            int secondIndex = actionEnableSwitch.indexOf(second);
            if (secondIndex < 0) return;
            Collections.swap(actionEnableSwitch, firstIndex, secondIndex);
        }

        
        
        private ListNBT toNBT() {
            ListNBT nbt = new ListNBT();
            actionEnableSwitch.forEach(actionSwitch -> {
                CompoundNBT actionNBT = new CompoundNBT();
                actionNBT.put("Action", StringNBT.valueOf(actionSwitch.action.getRegistryName().toString()));
                actionNBT.putBoolean("Enabled", actionSwitch.isEnabled);
                nbt.add(actionNBT);
            });
            return nbt;
        }

        private void fromNBT(ListNBT nbt) {
            actionEnableSwitch.clear();
            nbt.forEach(actionNBT -> {
                if (actionNBT instanceof CompoundNBT) {
                    CompoundNBT actionCNBT = (CompoundNBT) actionNBT;
                    String actionName = actionCNBT.getString("Action");
                    if (!"".equals(actionName)) {
                        ResourceLocation actionId = new ResourceLocation(actionName);
                        if (ModCommonRegistries.ACTIONS.getRegistry().containsKey(actionId)) {
                            Action<P> action = (Action<P>) ModCommonRegistries.ACTIONS.getRegistry().getValue(actionId);
                            actionEnableSwitch.add(new ActionSwitch<P>(action, actionCNBT.getBoolean("Enabled")));
                        }
                    }
                }
            });
        }
        
        
        
        private static final int ARBITRARY_ACTIONS_LIMIT = 16;
        public void toBuf(PacketBuffer buf) {
            int size = actionEnableSwitch.size();
            if (size > ARBITRARY_ACTIONS_LIMIT) {
                JojoMod.getLogger().warn("Tried to send a layout of more than {} actions", ARBITRARY_ACTIONS_LIMIT);
                size = ARBITRARY_ACTIONS_LIMIT;
            }
            buf.writeVarInt(size);
            
            short enabled = 0;
            for (int i = 0; i < size; i++) {
                ActionSwitch<P> action = actionEnableSwitch.get(i);
                buf.writeRegistryId(action.getAction());
                enabled <<= 1;
                enabled |= action.isEnabled() ? 1 : 0;
            }
            
            buf.writeShort(enabled);
        }
        
        public static <P extends IPower<P, ?>> ActionHotbarLayout<P> fromBuf(PacketBuffer buf) {
            ActionHotbarLayout<P> layout = new ActionHotbarLayout<>(Collections.emptyList());
            int count = Math.min(buf.readVarInt(), ARBITRARY_ACTIONS_LIMIT);
            layout.wasEdited = true;
            
            for (int i = 0; i < count; i++) {
                layout.actionEnableSwitch.add(new ActionSwitch<P>((Action<P>) buf.readRegistryIdSafe(Action.class)));
            }
            
            short enableFlags = buf.readShort();
            for (int i = count - 1; i >= 0; i--) {
                layout.actionEnableSwitch.get(i).isEnabled = (enableFlags & 1) > 0;
                enableFlags >>= 1;
            }
            
            return layout;
        }
    }
    
    
    
    public static class ActionSwitch<P extends IPower<P, ?>> {
        private final Action<P> action;
        private boolean isEnabled = true;
        
        private ActionSwitch(Action<P> action) {
            this(action, true);
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
        
        public void setIsEnabled(boolean enable) {
            this.isEnabled = enable;
        }
    }
}
