package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

public class GoldExperienceModel extends HumanoidStandModel<GoldExperienceEntity> {
    private ModelRenderer theThing;
    private ModelRenderer rightString;
    private ModelRenderer leftString;
    private ModelRenderer loincloth;
    private ModelRenderer leftPartLoincloth;
    private ModelRenderer rightPartLoincloth;

    public GoldExperienceModel() {
        super();
    }
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftPartLoincloth, rightPartLoincloth);
        oppositeHandside.put(leftString, rightString);
    }
}