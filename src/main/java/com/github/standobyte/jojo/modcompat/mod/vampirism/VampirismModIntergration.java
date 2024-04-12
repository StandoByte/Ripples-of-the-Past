package com.github.standobyte.jojo.modcompat.mod.vampirism;

import com.github.standobyte.jojo.modcompat.IVampirismModIntegration;

import de.teamlapen.vampirism.util.Helper;
import net.minecraft.entity.LivingEntity;

public class VampirismModIntergration implements IVampirismModIntegration {

    @Override
    public boolean isEntityVampire(LivingEntity entity) {
        return Helper.isVampire(entity);
    }

}
