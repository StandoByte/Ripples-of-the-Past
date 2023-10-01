package com.github.standobyte.jojo.power.layout;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ActionsLayoutPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class ActionsLayout<P extends IPower<P, ?>> {
    private final Map<Hotbar, ActionHotbarLayout<P>> hotbars;
    
    private final Action<P> mmbActionStarting;
    private Action<P> mmbActionCurrent;
    private boolean mmbActionHudVisibility = true;
    
    public ActionsLayout(@Nullable Action<P>[] leftClickHotbar, @Nullable Action<P>[] rightClickHotbar, 
            @Nullable Action<P> defaultQuickAccess) {
        hotbars = Util.make(new EnumMap<>(Hotbar.class), map -> {
            for (Hotbar hotbar : Hotbar.values()) {
                map.put(hotbar, new ActionHotbarLayout<>());
            }
        });
        hotbars.get(Hotbar.LEFT_CLICK).initActions(leftClickHotbar);
        hotbars.get(Hotbar.RIGHT_CLICK).initActions(rightClickHotbar);
        
        mmbActionStarting = defaultQuickAccess;
        resetLayout();
    }
    
    public static <P extends IPower<P, ?>> ActionsLayout<P> emptyLayout() {
        return new ActionsLayout<>(null, null, null);
    }
    
    public void copySwitchesState(ActionsLayout<P> sourceLayout) {
        if (sourceLayout == null) return;
        
        resetLayout();
        for (Hotbar hotbar : Hotbar.values()) {
            hotbars.get(hotbar).copySwitches(sourceLayout.hotbars.get(hotbar));
        }
        mmbActionCurrent = sourceLayout.mmbActionCurrent;
        mmbActionHudVisibility = sourceLayout.mmbActionHudVisibility;
    }
    
    
    
    @Nullable
    public Action<P> getVisibleActionInSlot(Hotbar hotbar, int index, boolean shiftVariant, P power, ActionTarget target) {
        List<Action<P>> actions = getHotbar(hotbar).getEnabled();
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return resolveVisibleActionInSlot(actions.get(index), shiftVariant, power, target);
    }
    
    @Nullable
    public Action<P> resolveVisibleActionInSlot(Action<P> baseAction, boolean shiftVariant, P power, ActionTarget target) {
        if (baseAction == null) return null;
        baseAction = baseAction.getVisibleAction(power, target);
        Action<P> held = power.getHeldAction();
        if (baseAction == held) {
            return baseAction;
        }
        if (baseAction != null && baseAction.hasShiftVariation()) {
            Action<P> shiftVar = baseAction.getShiftVariationIfPresent().getVisibleAction(power, target);
            if (shiftVar != null && (shiftVariant || shiftVar == held)) {
                baseAction = shiftVar;
            }
        }
        return baseAction;
    }
    
    @Nullable
    public Action<P> getVisibleQuickAccessAction(boolean shiftVariant, P power, ActionTarget target) {
        return resolveVisibleActionInSlot(mmbActionCurrent, shiftVariant, power, target);
    }
    
    
    
    public ActionHotbarLayout<P> getHotbar(Hotbar actionType) {
        return hotbars.get(actionType);
    }
    
    public void setQuickAccessAction(@Nullable Action<P> action) {
        if (action == null || hasAction(action)) {
            this.mmbActionCurrent = action;
        }
    }
    
    public boolean isMmbActionHudVisible() {
        return mmbActionHudVisibility;
    }
    
    public void setMmbActionHudVisibility(boolean isVisible) {
        this.mmbActionHudVisibility = isVisible;
    }
    
    public void resetLayout() {
        for (Hotbar hotbar : Hotbar.values()) {
            hotbars.get(hotbar).resetLayout();
        }
        mmbActionCurrent = mmbActionStarting;
    }
    
    public boolean hasAction(Action<P> action) {
        return hotbars.values().stream().anyMatch(hotbar -> hotbar.containsAction(action));
    }
    
    
    
    public void syncWithUser(ServerPlayerEntity player, PowerClassification powerClassification, IPowerType<?, ?> powerType) {
        PacketManager.sendToClient(new ActionsLayoutPacket(powerClassification, powerType, this), player);
    }
    
    public void toBuf(PacketBuffer buffer) {
        for (Hotbar hotbar : Hotbar.values()) {
            buffer.writeEnum(hotbar);
            hotbars.get(hotbar).toBuf(buffer);
        }

        NetworkUtil.writeOptionally(buffer, mmbActionCurrent, action -> buffer.writeRegistryId(action));
        buffer.writeBoolean(mmbActionHudVisibility);
    }
    
    public static <P extends IPower<P, ?>> ActionsLayout<P> fromBuf(IPowerType<P, ?> powerType, PacketBuffer buffer) {
        ActionsLayout<P> layout = powerType.createDefaultLayout();
        
        for (int i = 0; i < Hotbar.values().length; i++) {
            Hotbar hotbar = buffer.readEnum(Hotbar.class);
            layout.getHotbar(hotbar).setFromBuf(buffer);
        }
        
        NetworkUtil.readOptional(buffer, () -> buffer.readRegistryIdSafe(Action.class)).ifPresent(quickAccess -> {
            layout.mmbActionCurrent = (Action<P>) quickAccess;
        });
        layout.mmbActionHudVisibility = buffer.readBoolean();
        
        return layout;
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT layoutNBT = new CompoundNBT();
        hotbars.get(Hotbar.LEFT_CLICK).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AttacksLayout", hotbarNBT));
        hotbars.get(Hotbar.RIGHT_CLICK).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AbilitiesLayout", hotbarNBT));
        
        if (mmbActionCurrent != mmbActionStarting) {
            layoutNBT.putString("QuickAccess", mmbActionCurrent != null ? mmbActionCurrent.getRegistryName().toString() : "");
        }
        layoutNBT.putBoolean("QuickAccessHUD", mmbActionHudVisibility);
        return layoutNBT;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        hotbarLayoutFromNBT(nbt, "AttacksLayout", Hotbar.LEFT_CLICK);
        hotbarLayoutFromNBT(nbt, "AbilitiesLayout", Hotbar.RIGHT_CLICK);
        
        if (nbt.contains("QuickAccess", MCUtil.getNbtId(StringNBT.class))) {
            String quickAccessName = nbt.getString("QuickAccess");
            if (!"".equals(quickAccessName)) {
                ResourceLocation actionId = new ResourceLocation(quickAccessName);
                if (JojoCustomRegistries.ACTIONS.getRegistry().containsKey(actionId)) {
                    Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(actionId);
                    if (action != null) {
                        mmbActionCurrent = (Action<P>) action;
                    }
                }
            }
        }
        mmbActionHudVisibility = nbt.contains("QuickAccessHUD") ? nbt.getBoolean("QuickAccessHUD") : true;
    }
    
    private void hotbarLayoutFromNBT(CompoundNBT nbt, String key, Hotbar hotbar) {
        if (nbt.contains(key, MCUtil.getNbtId(ListNBT.class))) {
            hotbars.get(hotbar).fromNBT(nbt.getList(key, MCUtil.getNbtId(CompoundNBT.class)));
        }
        else {
            hotbars.get(hotbar).resetLayout();
        }
    }
    
    
    public static enum Hotbar {
        LEFT_CLICK,
        RIGHT_CLICK
    }
}
