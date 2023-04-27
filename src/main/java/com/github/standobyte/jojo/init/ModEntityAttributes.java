package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntityAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, JojoMod.MOD_ID);

    public static final RegistryObject<Attribute> STAND_DURABILITY = ATTRIBUTES.register("stand_durability", 
            () -> new RangedAttribute("attribute.name.generic.max_health", 0.0, 0.0, 1024.0).setSyncable(true));

    public static final RegistryObject<Attribute> STAND_PRECISION = ATTRIBUTES.register("stand_precision", 
            () -> new RangedAttribute("attribute.name.generic.max_health", 0.0, 0.0, 160.0).setSyncable(true));
}
