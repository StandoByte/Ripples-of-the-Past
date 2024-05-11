package com.github.standobyte.jojo.entity;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class MolotovEntity extends ProjectileItemEntity {

    public MolotovEntity(EntityType<? extends ProjectileItemEntity> type, World world) {
        super(type, world);
    }

    public MolotovEntity(LivingEntity pShooter, World pLevel) {
        super(ModEntityTypes.MOLOTOV.get(), pShooter, pLevel);
    }

    public MolotovEntity(double pX, double pY, double pZ, World pLevel) {
        super(ModEntityTypes.MOLOTOV.get(), pX, pY, pZ, pLevel);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOLOTOV.get();
    }

}
