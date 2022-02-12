package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.util.ResourceLocation;

public class HueShiftShaders {
    public static final ResourceLocation[] SHADERS_HUE_SHIFT;
    static {
        SHADERS_HUE_SHIFT = new ResourceLocation[546];
        initShaders();
    }
    private static int i = 0;
    
    public static void initShaders() {
        i = 0;
        for (int shift = 0; shift < 12; shift++) {
            shaderByName(shift, "");
            for (int desat = 1; desat <= 6; desat++) {
                shaderByName(shift, "_desat" + String.valueOf(desat));
            }
            for (int stretch = 1; stretch <= 6; stretch++) {
                shaderByName(shift, "_exclude" + String.valueOf(stretch));
            }
        }
        int n = i;
        for (int j = 0; j < n; j++) {
            addShader(String.format(SHADERS_HUE_SHIFT[j].getPath().replace("shaders/post/", "").replace(".json", "") + "_split.json"));
        }
    }
    
    private static void shaderByName(int shift, String postfix) {
        String name = "hueshift" + String.valueOf(shift) + "%s" + postfix + ".json";
        boolean hasNonFlipped = shift != 0 && shift != 1 && shift != 11;
        if (hasNonFlipped) {
            addShader(String.format(name, ""));
        }
        addShader(String.format(name, "_flip"));
    }
    
    private static void addShader(String name) {
        SHADERS_HUE_SHIFT[i] = new ResourceLocation(JojoMod.MOD_ID, "shaders/post/" + name);
        i++;
    }
}
