package com.github.standobyte.jojo.client.render.item;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientSetup;
import com.github.standobyte.jojo.client.render.item.generic.CustomModelItemISTER;
import com.github.standobyte.jojo.client.render.item.generic.ItemISTERModelWrapper;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.common.util.Constants;

public class CustomIconItem {
    public static final Supplier<Item> DUMMY_ITEM = ModItems.METEORIC_SCRAP;

    
    public static final ItemStack makeIconItem(RegularIcon icon) {
        ItemStack iconItem = new ItemStack(DUMMY_ITEM.get());
        iconItem.getOrCreateTag().putInt("Icon", icon.overrideValue);
        return iconItem;
    }
    
    public static final ItemStack makeIconItem(CustomModelIcon icon) {
        ItemStack iconItem = new ItemStack(DUMMY_ITEM.get());
        CompoundNBT nbt = iconItem.getOrCreateTag();
        nbt.putInt("CustomModel", icon.ordinal() + 1);
        return iconItem;
    }
    
    public static enum RegularIcon {
        KATAKANA_GO(1),
        CRIMSON_BUBBLE(2),
        VAMPIRISM_FREEZE(3),
        STAR_PLATINUM_BARRAGE(4),
        RESOLVE_FULL(5),
        RESOLVE_EFFECT(6),
        TIME_STOP(7),
        SP_TRANSLUCENT(8),
        SP(9),
        SOUL_CLOUD(10),
        RPS_ICONS(11),
        MR_FIREBALL(12),
        STEVE_MUSCLE(13),
        AWAKEN(14),
        WIND_MODE(15),
        HEAT_MODE(16),
        LIGHT_MODE(17),
        PILLAR_MAN_PUNCH(18),
        VAMPIRISM_PUNCH(19),
        HAMON_PUNCH(20),
        PILLAR_MAN_EXPLODE(21);
        
        private final int overrideValue;
        
        private RegularIcon(int overrideValue) {
            this.overrideValue = overrideValue;
        }
    }
    
    public static enum CustomModelIcon {
        MOD_LOGO(
//                new ResourceLocation(JojoMod.MOD_ID, "mod_logo"),
                () -> () -> new CustomModelItemISTER<>(
                        new ResourceLocation(JojoMod.MOD_ID, "mod_logo"), 
                        new ResourceLocation(JojoMod.MOD_ID, "textures/mod_logo_model.png"), 
                        DUMMY_ITEM, 
                        ModLogoModel::new));
        
//        private final ResourceLocation vanillaModelTransforms;
        private final Supplier<Callable<ItemStackTileEntityRenderer>> isterSupplier;
        private Supplier<ItemStackTileEntityRenderer> ister;
        
        private CustomModelIcon(
//                ResourceLocation modelWithTransforms, 
                Supplier<Callable<ItemStackTileEntityRenderer>> ister) {
//            this.vanillaModelTransforms = modelWithTransforms;
            this.isterSupplier = ister;
        }
    }

    
    
    public static void registerModelOverride() {
        ItemModelsProperties.register(DUMMY_ITEM.get(), 
                new ResourceLocation(JojoMod.MOD_ID, "icon"), 
                (itemStack, clientWorld, livingEntity) -> {
                    return itemStack.getOrCreateTag().getInt("Icon");
                });
    }
    
    public static void onModelBake(Map<ResourceLocation, IBakedModel> modelRegistry) {
        ClientSetup.registerCustomBakedModel(DUMMY_ITEM.get().getRegistryName(), modelRegistry, 
                model -> new ItemISTERModelWrapper(model));
    }
    
    
    
    public static class DummyIconItemISTER extends ItemStackTileEntityRenderer {
        
        public DummyIconItemISTER() {
            for (CustomModelIcon icon : CustomModelIcon.values()) {
                try {
                    ItemStackTileEntityRenderer ister = icon.isterSupplier.get().call();
                    icon.ister = () -> ister;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // TODO display transform
        @Override
        public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
                IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
            if (itemStack.hasTag()) {
                CompoundNBT nbt = itemStack.getTag();
                if (nbt.contains("CustomModel", Constants.NBT.TAG_INT)) {
                    int modelOrdinal = nbt.getInt("CustomModel") - 1;
                    CustomModelIcon values[] = CustomModelIcon.values();
                    if (modelOrdinal >= 0 && modelOrdinal < values.length) {
                        CustomModelIcon model = values[modelOrdinal];
                        model.ister.get().renderByItem(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
                        return;
                    }
                }
            }
            IBakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null);
            CustomModelItemISTER.renderItemNormally(matrixStack, itemStack, 
                    transformType, renderTypeBuffer, light, overlay, itemModel);
        }
    }
    
    protected static class ModLogoModel extends Model {
        private ModelRenderer root;

        public ModLogoModel() {
            super(RenderType::entityCutoutNoCull);
        }

        @Override
        public void renderToBuffer(MatrixStack pMatrixStack, 
                IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay,
                float pRed, float pGreen, float pBlue, float pAlpha) {
            if (root != null) {
                pMatrixStack.pushPose();
                pMatrixStack.scale(0.75f, 0.75f, 0.75f);
                pMatrixStack.translate(0, 0.5f, 0);
                Matrix3f lighting = pMatrixStack.last().normal();
                lighting.mul(Vector3f.YP.rotationDegrees(-45));
                lighting.mul(Vector3f.XP.rotationDegrees(-45));
                lighting.mul(Vector3f.ZP.rotationDegrees(45));
                root.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
                pMatrixStack.popPose();
            }
        }
    }
    
}
