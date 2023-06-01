package com.github.standobyte.jojo.client.render.armor.model;

import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;

public class GlovesModel<T extends LivingEntity> extends PlayerModel<T> {

    public GlovesModel(float inflate, boolean slim) {
        super(inflate, slim);
    }

}
