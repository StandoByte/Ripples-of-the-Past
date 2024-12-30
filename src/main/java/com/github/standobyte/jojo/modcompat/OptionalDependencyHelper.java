package com.github.standobyte.jojo.modcompat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public class OptionalDependencyHelper {
    
    private static IVampirismModIntegration vampirism;
    
    public static IVampirismModIntegration vampirism() {
        return vampirism;
    }
    
    
    public static void init() {
        vampirism = initModHandlingInterface("vampirism", 
                "com.github.standobyte.jojo.modcompat.mod.vampirism.VampirismModIntergration", 
                IVampirismModIntegration.Dummy::new, 
                "Vampirism mod");
        
        Object expandabilityEventHandler = createIfModPresent("expandability", 
                "com.github.standobyte.jojo.modcompat.mod.expandability.ExpandabilityEventHandler", 
                "Expandability lib");
        if (expandabilityEventHandler != null) {
            MinecraftForge.EVENT_BUS.register(expandabilityEventHandler);
        }
    }
    
    public static <I> I initModHandlingInterface(String modId, 
            String modPresentClassName,
            Supplier<? extends I> modAbsent,
            String loggingModName) {
        if (ModList.get().isLoaded(modId)) {
            try {
                Class<? extends I> animatorClass = (Class<? extends I>) Class.forName(modPresentClassName);
                Constructor<? extends I> constructor = animatorClass.getConstructor();
                I instance = constructor.newInstance();
                JojoMod.getLogger().debug("{}: {} compatibility initialized.", JojoMod.MOD_ID, loggingModName);
                return instance;
            } catch (ClassNotFoundException e) {
                JojoMod.getLogger().error(e);
            } catch (NoSuchMethodException e) {
                JojoMod.getLogger().error(e);
            } catch (SecurityException e) {
                JojoMod.getLogger().error(e);
            } catch (InstantiationException e) {
                JojoMod.getLogger().error(e);
            } catch (IllegalAccessException e) {
                JojoMod.getLogger().error(e);
            } catch (IllegalArgumentException e) {
                JojoMod.getLogger().error(e);
            } catch (InvocationTargetException e) {
                JojoMod.getLogger().error(e.getCause());
            }
        }
        else {
            JojoMod.getLogger().debug("{}: {} not found.", JojoMod.MOD_ID, loggingModName);
        }
        
        return modAbsent.get();
    }
    
    public static Object createIfModPresent(String modId, 
            String modPresentClassName,
            String loggingModName) {
        if (ModList.get().isLoaded(modId)) {
            try {
                Class<?> objClass = Class.forName(modPresentClassName);
                Constructor<?> constructor = objClass.getConstructor();
                Object instance = constructor.newInstance();
                JojoMod.getLogger().debug("{}: {} compatibility initialized.", JojoMod.MOD_ID, loggingModName);
                return instance;
            } catch (ClassNotFoundException e) {
                JojoMod.getLogger().error(e);
            } catch (NoSuchMethodException e) {
                JojoMod.getLogger().error(e);
            } catch (SecurityException e) {
                JojoMod.getLogger().error(e);
            } catch (InstantiationException e) {
                JojoMod.getLogger().error(e);
            } catch (IllegalAccessException e) {
                JojoMod.getLogger().error(e);
            } catch (IllegalArgumentException e) {
                JojoMod.getLogger().error(e);
            } catch (InvocationTargetException e) {
                JojoMod.getLogger().error(e.getCause());
            }
        }
        else {
            JojoMod.getLogger().debug("{}: {} not found.", JojoMod.MOD_ID, loggingModName);
        }
        return null;
    }
}
