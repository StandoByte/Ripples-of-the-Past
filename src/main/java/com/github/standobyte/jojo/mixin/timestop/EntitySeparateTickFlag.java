package com.github.standobyte.jojo.mixin.timestop;

import java.util.LinkedList;
import java.util.Queue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.timestop.EntityTimeStop;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public class EntitySeparateTickFlag implements EntityTimeStop {
    private boolean jojo_ripples$isStoppedInTime = false;
    private Queue<Runnable> jojo_ripples$runOnTimeResume = new LinkedList<>();

    @Override
    public boolean jojo_ripples$isStoppedInTime() {
        return jojo_ripples$isStoppedInTime;
    }

    @Override
    public void jojo_ripples$setStoppedInTime(boolean flag) {
        this.jojo_ripples$isStoppedInTime = flag;
        if (!flag) {
            jojo_ripples$runOnTimeResume.forEach(Runnable::run);
            jojo_ripples$runOnTimeResume.clear();
        }
    }

    @Override
    public void jojo_ripples$queueOnTimeResume(Runnable action) {
        if (jojo_ripples$isStoppedInTime) {
            jojo_ripples$runOnTimeResume.add(action);
        }
        else {
            action.run();
        }
    }
    
    @Inject(method = "canUpdate()Z", at = @At("HEAD"), cancellable = true, remap = false)
    public void canUpdateCheckTSFlag(CallbackInfoReturnable<Boolean> ci) {
        if (jojo_ripples$isStoppedInTime) {
            ci.setReturnValue(false);
        }
    }

}
