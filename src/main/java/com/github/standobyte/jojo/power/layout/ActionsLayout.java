package com.github.standobyte.jojo.power.layout;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;

public class ActionsLayout<P extends IPower<P, ?>> {
    private final Map<Hotbar, ActionHotbarLayout<P>> hotbars;
    public final Action<P> mmbActionStarting;
    
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
    }
    
    
    
    @Nullable
    public Action<P> getBaseActionInSlot(Hotbar hotbar, int index) {
        List<Action<P>> actions = getHotbar(hotbar).getEnabled();
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return actions.get(index);
    }
    
    
    
    public ActionHotbarLayout<P> getHotbar(Hotbar actionType) {
        return hotbars.get(actionType);
    }
    
    public void resetLayout() {
        for (Hotbar hotbar : Hotbar.values()) {
            hotbars.get(hotbar).resetLayout();
        }
    }
    
    public boolean hasAction(Action<P> action) {
        return hotbars.values().stream().anyMatch(hotbar -> hotbar.containsAction(action));
    }
    
    public void addExtraAction(Action<P> action, Hotbar hotbar) {
        hotbars.get(hotbar).addExtraAction(action);
    }
    
    public void removeExtraAction(Action<P> action) {
        hotbars.values().forEach(hotbarLayout -> hotbarLayout.removeAction(action));
    }
    
    
    
    @Deprecated
    public void toBuf(PacketBuffer buffer) {
        for (Hotbar hotbar : Hotbar.values()) {
            buffer.writeEnum(hotbar);
            hotbars.get(hotbar).toBuf(buffer);
        }
    }

    @Deprecated
    public static <P extends IPower<P, ?>> ActionsLayout<P> fromBuf(IPowerType<P, ?> powerType, PacketBuffer buffer) {
        ActionsLayout<P> layout = powerType.createDefaultLayout();
        
        for (int i = 0; i < Hotbar.values().length; i++) {
            Hotbar hotbar = buffer.readEnum(Hotbar.class);
            layout.getHotbar(hotbar).setFromBuf(buffer);
        }
        
        return layout;
    }
    
    

    @Deprecated
    public CompoundNBT toNBT() {
        CompoundNBT layoutNBT = new CompoundNBT();
        hotbars.get(Hotbar.LEFT_CLICK).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AttacksLayout", hotbarNBT));
        hotbars.get(Hotbar.RIGHT_CLICK).toNBT().ifPresent(hotbarNBT -> layoutNBT.put("AbilitiesLayout", hotbarNBT));
        return layoutNBT;
    }

    @Deprecated
    public void fromNBT(CompoundNBT nbt) {
        hotbarLayoutFromNBT(nbt, "AttacksLayout", Hotbar.LEFT_CLICK);
        hotbarLayoutFromNBT(nbt, "AbilitiesLayout", Hotbar.RIGHT_CLICK);
    }

    @Deprecated
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
