package com.github.standobyte.jojo.client.render.entity.model.animnew.molang;

import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;

// TODO update the RotP-Addon-Example build.gradle
public class MolangInterpreter {
    private static MochaEngine<?> mochaInstance;
    
    public static void init() {
        if (mochaInstance == null) {
            mochaInstance = MochaEngine.createStandard();
            mochaInstance.scope().set("query", JavaObjectBinding.of(AnimContext.class, AnimContext._INSTANCE, null));
        }
    }
    
    public static MochaEngine<?> get() {
        return mochaInstance;
    }
}
