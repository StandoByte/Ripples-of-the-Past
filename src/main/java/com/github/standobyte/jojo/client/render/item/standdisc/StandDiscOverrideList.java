package com.github.standobyte.jojo.client.render.item.standdisc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class StandDiscOverrideList extends ItemOverrideList {
    private final Map<ResourceLocation, IBakedModel> cache = new HashMap<>();
    private final ItemOverrideList wrappedOverrides;
    
    public StandDiscOverrideList(ItemOverrideList wrappedOverrides) {
        this.wrappedOverrides = wrappedOverrides;
    }
    
    @Override
    public IBakedModel resolve(IBakedModel model, ItemStack item, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        StandInstance discStand = StandDiscItem.getStandFromStack(item);
        if (discStand != null) {
            StandType<?> standType = discStand.getType();
            ItemModelMesher itemModelShaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
            IBakedModel standSpecificModel = cache.computeIfAbsent(standType.getRegistryName(), standId -> itemModelShaper.getModelManager().getModel(makeStandSpecificModelPath(standType)));
            if (standSpecificModel != null && !isMissingModel(standSpecificModel, itemModelShaper)) {
                model = standSpecificModel;
            }
        }
        
        return wrappedOverrides.resolve(model, item, world, entity);
    }
    
    public static ResourceLocation makeStandSpecificModelPath(StandType<?> standType) {
        ResourceLocation id = standType.getRegistryName();
        return new ResourceLocation(id.getNamespace(), "item/stand_disc_" + id.getPath());
    }
    
    private static boolean isMissingModel(IBakedModel model, ItemModelMesher itemModelShaper) {
//        return model == itemModelShaper.getModelManager().getMissingModel(); // you'd think that should work
        return model instanceof SimpleBakedModel && (((SimpleBakedModel) model).getParticleIcon() instanceof MissingTextureSprite);
    }

}
