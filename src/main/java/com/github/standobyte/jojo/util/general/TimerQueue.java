package com.github.standobyte.jojo.util.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.lang3.mutable.MutableInt;

public class TimerQueue {
    private final Collection<MutableInt> timers;
    
    public TimerQueue(boolean large) {
        this.timers = large ? new LinkedList<>() : new ArrayList<>();
    }
    
    public void add(int timeTicks) {
        timers.add(new MutableInt(timeTicks));
    }
    
    public void tick(Runnable onOver) {
        Iterator<MutableInt> iter = timers.iterator();
        if (!timers.isEmpty()) {
            MutableInt timer = iter.next();
            if (timer.intValue() > 0) timer.decrement();
            if (timer.intValue() <= 0) {
                iter.remove();
                onOver.run();
            }
        }
    }
}
