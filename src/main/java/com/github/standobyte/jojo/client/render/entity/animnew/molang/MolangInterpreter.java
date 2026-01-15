package com.github.standobyte.jojo.client.render.entity.animnew.molang;

import team.unnamed.mocha.MochaEngine;

// TODO update the RotP-Addon-Example build.gradle
public class MolangInterpreter {
    private static MochaEngine<?> mochaInstance;
    
    public static void init() {
        if (mochaInstance == null) {
            mochaInstance = MochaEngineWithoutJavassist.createStandard();
            mochaInstance.scope().set(AnimMolangQuery.NAMESPACE, AnimMolangQuery.instance);
        }
    }
    
    public static MochaEngine<?> get() {
        return mochaInstance;
    }
}
