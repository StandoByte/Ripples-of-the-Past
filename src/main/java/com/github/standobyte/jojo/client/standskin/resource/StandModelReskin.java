package com.github.standobyte.jojo.client.standskin.resource;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;

import net.minecraft.util.ResourceLocation;

public class StandModelReskin extends ResourceReskin<StandEntityModel<?>> {

    @Override
    protected StandEntityModel<?> createValueLazy(ResourceLocation path) {
        return null;
    }

}
