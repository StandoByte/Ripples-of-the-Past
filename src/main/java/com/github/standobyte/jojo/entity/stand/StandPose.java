package com.github.standobyte.jojo.entity.stand;

public class StandPose {
    private final String name;
    public final boolean armsObstructView;
    
    public StandPose(String name, boolean armsObstructView) {
        this.name = name;
        this.armsObstructView = armsObstructView;
    }
    
    public StandPose(String name) {
        this(name, false);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public static final StandPose IDLE = new StandPose("IDLE");
    public static final StandPose SUMMON = new StandPose("SUMMON");
    public static final StandPose BLOCK = new StandPose("BLOCK");
    public static final StandPose LIGHT_ATTACK = new StandPose("LIGHT_ATTACK");
    public static final StandPose HEAVY_ATTACK = new StandPose("HEAVY_ATTACK");
    public static final StandPose HEAVY_ATTACK_FINISHER = new StandPose("HEAVY_ATTACK_FINISHER");
    public static final StandPose RANGED_ATTACK = new StandPose("RANGED_ATTACK");
    public static final StandPose BARRAGE = new StandPose("BARRAGE");
}
