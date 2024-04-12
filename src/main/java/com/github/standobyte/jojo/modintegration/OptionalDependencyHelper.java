package com.github.standobyte.jojo.modintegration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.modintegration.safe.IVampirismModIntegration;

import net.minecraftforge.fml.ModList;

public class OptionalDependencyHelper {
    
    private static IVampirismModIntegration vampirism;
    
    public static IVampirismModIntegration vampirism() {
        return vampirism;
    }
    
    
    public static void init() {
        vampirism = initModHandlingInterface("vampirism", 
                "com.github.standobyte.jojo.modintegration.vampirism.VampirismModIntergration", 
                IVampirismModIntegration.Dummy::new, 
                "Vampirism mod");
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
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
            }
        }
        else {
            JojoMod.getLogger().debug("{}: {} not found.", JojoMod.MOD_ID, loggingModName);
        }
        
        return modAbsent.get();
    }
}
