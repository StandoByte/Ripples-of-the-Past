package com.github.standobyte.jojo.client.render.entity.pose;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

@Deprecated
// This type needs to exist to not break previously made Stand addons (Stone Free and Weather Report)
// TODO: delete in v0.3
public class XRotationModelRenderer extends ModelRenderer {
    public XRotationModelRenderer(Model model) {
        super(model);
    }

    public XRotationModelRenderer(Model model, int xTexOffs, int yTexOffs) {
        super(model, xTexOffs, yTexOffs);
    }

    public XRotationModelRenderer(int xTexSize, int yTexSize, int xTexOffs, int yTexOffs) {
        super(xTexSize, yTexSize, xTexOffs, yTexOffs);
    }
}
