package com.github.standobyte.jojo.power;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ActionsFullLayoutPacket;
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
    
    public void resetLayout() {
        for (ActionType hotbar : ActionType.values()) {
            actions.get(hotbar).resetLayout();
        }
        mmbActionCurrent = mmbActionStarting;
    }
    
    void onPowerSet(IPowerType<P, ?> type) {
        for (ActionType actionType : ActionType.values()) {
            actions.get(actionType).initActions(type != null ? 
                    type.getDefaultActions(actionType) : null);
        }
        mmbActionStarting = type != null ? type.getDefaultQuickAccess() : null;
        resetLayout();
    }
    
    void keepLayoutOnClone(ActionsLayout<P> oldLayout) {
        for (ActionType type : ActionType.values()) {
            actions.get(type).keepLayoutOnClone(oldLayout.getHotbar(type));
        }
        this.mmbActionCurrent = oldLayout.mmbActionCurrent;
    }
    
    void syncWithUser(ServerPlayerEntity player, PowerClassification powerClassification) {
        for (ActionType type : ActionType.values()) {
            ActionHotbarLayout<P> hotbar = actions.get(type);
            if (hotbar.wasEdited()) {
                PacketManager.sendToClient(ActionsFullLayoutPacket.withLayout(
                        powerClassification, type, hotbar), player);
            }
        }
        if (mmbActionCurrent != mmbActionStarting) {
            PacketManager.sendToClient(ActionsFullLayoutPacket.quickAccessAction(
                    powerClassification, mmbActionCurrent), player);
        }
    }
    
    
    
    CompoundNBT toNBT() {
        CompoundNBT layoutNBT = new CompoundNBT();
        actions.get(ActionType.ATTACK).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AttacksLayout", hotbarNBT));
        actions.get(ActionType.ABILITY).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AbilitiesLayout", hotbarNBT));
        if (mmbActionCurrent != mmbActionStarting) {
            layoutNBT.putString("QuickAccess", mmbActionCurrent != null ? mmbActionCurrent.getRegistryName().toString() : "");
        }
        return layoutNBT;
    }
    
    void fromNBT(CompoundNBT nbt) {
        actionLayoutFromNBT(nbt, "AttacksLayout", ActionType.ATTACK);
        actionLayoutFromNBT(nbt, "AbilitiesLayout", ActionType.ABILITY);
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
    }
    
    private void actionLayoutFromNBT(CompoundNBT nbt, String key, ActionType hotbar) {
        if (nbt.contains(key, MCUtil.getNbtId(ListNBT.class))) {
            actions.get(hotbar).fromNBT(nbt.getList(key, MCUtil.getNbtId(CompoundNBT.class)));
        }
        else {
            actions.get(hotbar).resetLayout();
        }
    }
}
