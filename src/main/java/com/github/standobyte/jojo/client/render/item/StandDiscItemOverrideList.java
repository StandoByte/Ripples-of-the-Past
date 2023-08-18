package com.github.standobyte.jojo.client.render.item;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/*
 * Was made with the help of TheGreyGhost's Minecraft by Example repository
 * https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe15_item_dynamic_item_model
 */
public class StandDiscItemOverrideList extends ItemOverrideList {

    public StandDiscItemOverrideList() {
        super();
    }

    /**
     *  getModelWithOverrides() is used to create/select a suitable IBakedModel based on the itemstack information.
     *  For vanilla, the ItemOverrideList contains a list of IBakedModels, each with a corresponding ItemOverride (predicate),
     *    read in from the item json, which matches a PropertyOverride on the item.  See mbe12 (ItemNBTAnimate) for example
     *  In this case, we extend ItemOverrideList to return a dynamically-generated series of BakedQuads, instead of relying on
     *    a fixed BakedModel.
     *  It's probably safest to return a new model or at least an immutable one, rather than modifying the
     *    originalModel passed in, in case the rendering is multithreaded (block rendering has this problem, for example).
     * @param originalModel
     * @param stack
     * @param world
     * @param entity
     * @return
     */
    @Override
    public IBakedModel resolve(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        StandType<?> standType = null;
        if (stack != null) {
            StandInstance stand = StandDiscItem.getStandFromStack(stack, true);
            if (stand != null) {
                standType = stand.getType();
            }
        }
        return StandDiscFinalisedModel.getModel(originalModel, standType);
    }
}
