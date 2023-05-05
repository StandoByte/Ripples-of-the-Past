package com.github.standobyte.jojo.client;

import java.util.HashSet;
import java.util.Set;

public class ClientTicking {
    private static final Set<ITicking> TICKING = new HashSet<>();
    
    public static void addTicking(ITicking ticking) {
        TICKING.add(ticking);
    }
    
    public static void tickAll() {
        TICKING.forEach(ITicking::tick);
    }
    
    
    
    public static interface ITicking {
        public void tick();
    }
}
