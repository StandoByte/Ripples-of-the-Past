package com.github.standobyte.jojo.action;

import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

public interface ITrainableAction<P extends IPower<P, ?>> {
    boolean isTrained();
    default float getMaxTrainingPoints(IStandPower power) {
        return 1F;
    }
    void onTrainingPoints(IStandPower power, float points);
    void onMaxTraining(IStandPower power);
    void onProgressionSkip(IStandPower power);
}
