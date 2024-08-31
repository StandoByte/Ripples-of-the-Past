package com.github.standobyte.jojo.client.ui.screen.vampirism;

import com.github.standobyte.jojo.client.ui.screen.JojoStuffScreen.VampirismTab;
import com.github.standobyte.jojo.client.ui.screen.controls.HudLayoutEditingScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

public class VampirismScreen {
    static HudLayoutEditingScreen.RightSideTabs RIGHT_SIDE_TABS;
    
    public static void clientInit() {
        HudLayoutEditingScreen.RightSideTabs.register(ModPowers.VAMPIRISM.get().getRegistryName(), 
                VampirismTab.values(), VampirismTab.CONTROLS, PowerClassification.NON_STAND);
    }
}
