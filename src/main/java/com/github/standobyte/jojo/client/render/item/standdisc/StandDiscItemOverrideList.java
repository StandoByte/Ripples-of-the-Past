package com.github.standobyte.jojo.client.render.item.standdisc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

/*
 * Was made with the help of TheGreyGhost's Minecraft by Example repository
 * https://github.com/TheGreyGhost/MinecraftByExample/tree/master/src/main/java/minecraftbyexample/mbe15_item_dynamic_item_model
 */
// FIXME sometimes uses sprites of another item for some reason
public class StandDiscItemOverrideList extends ItemOverrideList {
    private static final String MODEL_FILE_PREFIX = "stand_disc_";
    private static final Map<StandType<?>, IBakedModel> standTypeOverrides = new HashMap<>();

    public StandDiscItemOverrideList() {
        super();
    }
    
    public static void loadOverrides() {
        for (StandType<?> standType : JojoCustomRegistries.STANDS.getRegistry().getValues()) {
            ResourceLocation modelPath = new ModelResourceLocation(new ResourceLocation(
                    standType.getRegistryName().getNamespace(), 
                    MODEL_FILE_PREFIX + standType.getRegistryName().getPath()), "inventory");
            ModelLoader.addSpecialModel(modelPath);
        }
        clearCache();
    }
    
    public static void bakeOverrides(ModelLoader modelBakery) {
        for (StandType<?> standType : JojoCustomRegistries.STANDS.getRegistry().getValues()) {
            ResourceLocation standId = standType.getRegistryName();
            String namespace = standId.getNamespace();
            String path = standId.getPath();
            if (ClientUtil.resourceExists(new ResourceLocation(namespace, "models/item/" + MODEL_FILE_PREFIX + path + ".json"))) {
                IBakedModel overridingModel = modelBakery.getBakedModel(new ResourceLocation(namespace, "item/" + MODEL_FILE_PREFIX + path), ModelRotation.X0_Y0, 
                        material -> modelBakery.getSpriteMap().getSprite(material));
                if (overridingModel != null) {
                    overridingModel = new StandDiscIconModel(overridingModel);
                    standTypeOverrides.put(standType, overridingModel);
                }
            }
            else {
                standTypeOverrides.remove(standType);
            }
        }
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
    private static final Map<StandType<?>, StandDiscFinalisedModel> CACHED_MODELS = new HashMap<>(); // allows for a null key
    
    @Override
    public IBakedModel resolve(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        StandType<?> standType = null;
        if (stack != null) {
            StandInstance stand = StandDiscItem.getStandFromStack(stack, true);
            if (stand != null) {
                standType = stand.getType();
            }
        }
        
        if (standType != null && standTypeOverrides.containsKey(standType)) {
            IBakedModel overridingModel = standTypeOverrides.get(standType);
            if (overridingModel != null) {
                originalModel = overridingModel;
            }
        }
        
        return getCacheModel(originalModel, standType);
    }
    
    private static StandDiscFinalisedModel getCacheModel(IBakedModel parentModel, @Nullable StandType<?> stand) {
        if (CACHED_MODELS.containsKey(stand)) {
            StandDiscFinalisedModel model = CACHED_MODELS.get(stand);
            if (model != null) {
                return model;
            }
        }
        StandDiscFinalisedModel model = new StandDiscFinalisedModel(parentModel, stand);
        CACHED_MODELS.put(stand, model);
        return model;
    }
    
    private static void clearCache() {
        CACHED_MODELS.clear();
    }
}
