package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.entity.Entity;

public class StandControlMouseHelper extends MouseHelper {
    private final Minecraft minecraft;
    private final MouseSmoother smoothTurnX = new MouseSmoother();
    private final MouseSmoother smoothTurnY = new MouseSmoother();
    private double lastMouseEventTime = Double.MIN_VALUE;

    public StandControlMouseHelper(Minecraft minecraft) {
        super(minecraft);
        this.minecraft = minecraft;
    }
    
    public static void overrideVanillaMouseHelper(Minecraft mc) {
        MouseHelper cursed = new StandControlMouseHelper(mc);
        ClientReflection.setMouseHandler(mc, cursed);
    }
    
    @Override
    public void turnPlayer() {
        double time = NativeUtil.getTime();
        double timeDelta = time - lastMouseEventTime;
        lastMouseEventTime = time;
        
        if (isMouseGrabbed() && minecraft.isWindowActive() && minecraft.player != null) {
            StandEntity standManual = ControllerStand.getInstance().getManuallyControlledStand();
            if (standManual != null && standManual.isAlive()) {
                turnStand(standManual, timeDelta);
                return;
            }
        }
        
        super.turnPlayer();
    }
    
    private void turnStand(Entity standEntity, double timeDelta) {
        if (isMouseGrabbed() && minecraft.isWindowActive()) {
            double accumDX = getXVelocity();
            double accumDY = getYVelocity();
            
            double sensitivity = minecraft.options.sensitivity * 0.6 + 0.2;
            double velocity = sensitivity * sensitivity * sensitivity * 8.0D;
            double velocityX;
            double velocityY;
            if (minecraft.options.smoothCamera) {
                velocityX = smoothTurnX.getNewDeltaValue(accumDX * velocity, timeDelta * velocity);
                velocityY = smoothTurnY.getNewDeltaValue(accumDY * velocity, timeDelta * velocity);
            } else {
                smoothTurnX.reset();
                smoothTurnY.reset();
                velocityX = accumDX * velocity;
                velocityY = accumDY * velocity;
            }
            
            resetMouseDelta();
            double yInvert = minecraft.options.invertYMouse ? -1 : 1;

            minecraft.getTutorial().onMouse(velocityX, velocityY);
            standEntity.turn(velocityX, velocityY * yInvert);

        } else {
            resetMouseDelta();
        }
    }
    
    private void resetMouseDelta() {
        if (minecraft.isWindowActive()) {
            minecraft.setWindowActive(false);
            super.turnPlayer();
            minecraft.setWindowActive(true);
        } else {
            super.turnPlayer();
        }
    }

}
