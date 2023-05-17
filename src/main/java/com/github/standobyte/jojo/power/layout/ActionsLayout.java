package com.github.standobyte.jojo.power.layout;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ActionsLayoutPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class ActionsLayout<P extends IPower<P, ?>> {
    private Map<ActionType, ActionHotbarLayout<P>> actions = Util.make(new EnumMap<>(ActionType.class), map -> {
        for (ActionType actionType : ActionType.values()) {
            map.put(actionType, new ActionHotbarLayout<>());
        }
    });
    
    private Action<P> mmbActionStarting;
    private Action<P> mmbActionCurrent;
    private boolean mmbActionHudVisibility = true;
    
    public ActionHotbarLayout<P> getHotbar(ActionType actionType) {
        return actions.get(actionType);
    }
    
    @Nullable
    public Action<P> getQuickAccessAction() {
        return mmbActionCurrent;
    }
    
    public void setQuickAccessAction(Action<P> action) {
        this.mmbActionCurrent = action;
    }
    
    public boolean isMmbActionHudVisible() {
        return mmbActionHudVisibility;
    }
    
    public void setMmbActionHudVisibility(boolean isVisible) {
        this.mmbActionHudVisibility = isVisible;
    }
    
    public void resetLayout() {
        for (ActionType hotbar : ActionType.values()) {
            actions.get(hotbar).resetLayout();
        }
        mmbActionCurrent = mmbActionStarting;
    }
    
    public void onPowerSet(IPowerType<P, ?> type) {
        for (ActionType actionType : ActionType.values()) {
            actions.get(actionType).initActions(type != null ? 
                    type.getDefaultActions(actionType) : null);
        }
        mmbActionStarting = type != null ? type.getDefaultQuickAccess() : null;
        resetLayout();
    }
    
    public void keepLayoutOnClone(ActionsLayout<P> oldLayout) {
        for (ActionType type : ActionType.values()) {
            actions.get(type).keepLayoutOnClone(oldLayout.getHotbar(type));
        }
        
        this.mmbActionCurrent = oldLayout.mmbActionCurrent;
        this.mmbActionHudVisibility = oldLayout.mmbActionHudVisibility;
    }
    
    public void syncWithUser(ServerPlayerEntity player, PowerClassification powerClassification) {
        for (ActionType type : ActionType.values()) {
            ActionHotbarLayout<P> hotbar = actions.get(type);
            if (hotbar.wasEdited()) {
                PacketManager.sendToClient(ActionsLayoutPacket.withLayout(
                        powerClassification, type, hotbar), player);
            }
        }
        if (mmbActionCurrent != mmbActionStarting) {
            PacketManager.sendToClient(ActionsLayoutPacket.quickAccessAction(
                    powerClassification, mmbActionCurrent), player);
        }
        PacketManager.sendToClient(ActionsLayoutPacket.quickAccessRenderInHud(powerClassification, mmbActionHudVisibility), player);
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT layoutNBT = new CompoundNBT();
        actions.get(ActionType.ATTACK).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AttacksLayout", hotbarNBT));
        actions.get(ActionType.ABILITY).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AbilitiesLayout", hotbarNBT));
        
        if (mmbActionCurrent != mmbActionStarting) {
            layoutNBT.putString("QuickAccess", mmbActionCurrent != null ? mmbActionCurrent.getRegistryName().toString() : "");
        }
        layoutNBT.putBoolean("QuickAccessHUD", mmbActionHudVisibility);
        return layoutNBT;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        hotbarLayoutFromNBT(nbt, "AttacksLayout", ActionType.ATTACK);
        hotbarLayoutFromNBT(nbt, "AbilitiesLayout", ActionType.ABILITY);
        
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
    
    private void hotbarLayoutFromNBT(CompoundNBT nbt, String key, ActionType hotbar) {
        if (nbt.contains(key, MCUtil.getNbtId(ListNBT.class))) {
            actions.get(hotbar).fromNBT(nbt.getList(key, MCUtil.getNbtId(CompoundNBT.class)));
        }
        else {
            actions.get(hotbar).resetLayout();
        }
    }
}
