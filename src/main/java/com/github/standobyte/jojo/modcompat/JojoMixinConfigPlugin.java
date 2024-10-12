package com.github.standobyte.jojo.modcompat;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;

public class JojoMixinConfigPlugin implements IMixinConfigPlugin {

    private static final int PACKAGE_NAME_LENGTH = "com.github.standobyte.jojo.mixin.".length();
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        switch (mixinClassName.substring(PACKAGE_NAME_LENGTH)) {
        case "EntityLiquidWalkingMixin":
            return !isModPresent("expandability");
        case "LivingEntityArmorBreakMixin":
            if (isModPresent("arclight")) {
                System.out.println("RotP: Minor incompatibility issue with Arclight - Stand barrages and similar moves will reduce armor durability a lot faster than they should.");
                return false;
            }
        default:
            break;
        }
        return true;
    }
    
    private static boolean isModPresent(String modId) {
        LoadingModList loadingModList = FMLLoader.getLoadingModList();
        return loadingModList == null || loadingModList.getModFileById(modId) != null;
    }
    
    
    
    
    
    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

}
