package com.github.standobyte.jojo.util.general;

/**
 * ffs Java, stop making me write the default case, 
 * the whole point of an enum is to know all of its instances in compile time
 */
public class ThatsSimplyNotPossible extends RuntimeException {
    private static final long serialVersionUID = -8332770966390314007L;
    
    public ThatsSimplyNotPossible(Enum<?> enumSwitch) {
        super("Unhandled element " + enumSwitch.toString() + 
                " of class " + enumSwitch.getClass().getSimpleName() + 
                " in a switch statement");
    }
    
    public ThatsSimplyNotPossible() {
        super("Unhandled enum element in a switch statement");
    }
}
