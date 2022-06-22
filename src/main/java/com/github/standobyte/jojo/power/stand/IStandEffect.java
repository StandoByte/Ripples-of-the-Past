package com.github.standobyte.jojo.power.stand;

// TODO use stand effect interface inside StandPower
public interface IStandEffect {
    void onStart();
    void tick();
    void onStop();

}
