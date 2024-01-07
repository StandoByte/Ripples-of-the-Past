package com.github.standobyte.jojo.action.stand;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class TimeResume extends StandAction {

    public TimeResume(StandAction.Builder builder) {
        super(builder);
    }
    
    public static boolean userTimeStopInstance(World world, LivingEntity user, @Nullable Consumer<TimeStopInstance> invoke) {
        return world.getCapability(WorldUtilCapProvider.CAPABILITY)
                .map(cap -> cap.getTimeStopHandler().userStoppedTime(user).map(instance -> {
                    if (invoke != null) {
                        invoke.accept(instance);
                    }
                    return true;
                }).orElse(false)).orElse(false);
    }
    
    private static final int TICKS_FIRST_CLICK = TimeStopInstance.TIME_RESUME_SOUND_TICKS + 1;
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            userTimeStopInstance(world, user, instance -> 
            instance.setTicksLeft(!instance.wereTicksManuallySet() && instance.getTicksLeft() > TICKS_FIRST_CLICK ? TICKS_FIRST_CLICK : 0));
        }
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return true;
    }
    
    @Nullable
    @Override
    public Action<IStandPower> getVisibleAction(IStandPower power, ActionTarget target) {
        LivingEntity user = power.getUser();
        if (user != null) {
            if (TimeStopHandler.isTimeStopped(user.level, user.blockPosition())) {
                if (TimeResume.userTimeStopInstance(user.level, user, null)) {
                    return this;
                }
                else {
                    return null;
                }
            }
            else {
                return super.getVisibleAction(power, target);
            }
        }
        return this;
    }
    
    @Nullable
    @Override
    protected Action<IStandPower> replaceAction(IStandPower power, ActionTarget target) {
        return getInstantTSAction() != null ? getInstantTSAction() : null;
    }
    
    @Nullable
    public Action<IStandPower> getInstantTSAction() {
        return blink;
    }
    
    private Action<IStandPower> blink;
    void setInstantTSAction(Action<IStandPower> blink) {
        this.blink = blink;
    }
}
