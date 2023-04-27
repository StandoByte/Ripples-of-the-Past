package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.container.WalkmanItemContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainers {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, JojoMod.MOD_ID);
    
    
    public static final RegistryObject<ContainerType<WalkmanItemContainer>> WALKMAN = CONTAINERS.register("walkman", 
           () -> IForgeContainerType.create(WalkmanItemContainer::new));

}
