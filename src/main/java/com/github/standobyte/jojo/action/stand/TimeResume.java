package com.github.standobyte.jojo.action.stand;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.TimeUtil;

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
    
    private static final int TICKS_FIRST_CLICK = TimeStopInstance.TIME_RESUME_SOUND_TICKS;
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
    public StandAction getVisibleAction(IStandPower power) {
        LivingEntity user = power.getUser();
        if (user != null && TimeUtil.isTimeStopped(user.level, user.blockPosition()) 
                && TimeResume.userTimeStopInstance(user.level, user, null)) {
            return this;
        }
        return null;
    }
}
