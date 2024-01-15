package com.github.standobyte.jojo.jei;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.crafting.StandUserRecipe;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JeiJojoPlugin implements IModPlugin {
    private final ResourceLocation id = new ResourceLocation(JojoMod.MOD_ID, "jei_plugin");
    
    public JeiJojoPlugin() {}

    @Override
    public ResourceLocation getPluginUid() {
        return id;
    }
    
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.STAND_DISC.get(), (item, context) -> {
            StandInstance stand = StandDiscItem.getStandFromStack(item);
            if (stand == null) {
                return IIngredientSubtypeInterpreter.NONE;
            }
            return stand.getType().getRegistryName().toString();
        });
    }
    
    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(StandUserRecipe.class, JeiStandUserRecipeExtension::new);
    }
    
}
