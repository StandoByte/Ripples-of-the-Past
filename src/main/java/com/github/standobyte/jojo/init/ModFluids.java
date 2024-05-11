package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.block.BoilingBloodFluid;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, JojoMod.MOD_ID);
    
    
    public static final RegistryObject<FlowingFluid> FLOWING_BOILING_BLOOD = FLUIDS.register("flowing_boiling_blood", 
            () -> new BoilingBloodFluid.Flowing());
    
    public static final RegistryObject<FlowingFluid> BOILING_BLOOD = FLUIDS.register("boiling_blood", 
            () -> new BoilingBloodFluid.Source());
    
}
