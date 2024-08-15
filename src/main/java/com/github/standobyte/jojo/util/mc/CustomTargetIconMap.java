package com.github.standobyte.jojo.util.mc;

import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.item.CustomIconMapRender;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.common.util.Constants;

public class CustomTargetIconMap {
    
    @Nullable
    public static ItemStack createMap(ServerWorld serverWorld, Structure<?> structure, BlockPos traderBlockPos, 
            OptionalInt customColor, String structureName, ResourceLocation iconPath) {
        BlockPos blockpos = serverWorld.findNearestMapFeature(structure, traderBlockPos, 100, true);
        if (blockpos == null) return null;
        
        ItemStack mapItem = FilledMapItem.create(serverWorld, blockpos.getX(), blockpos.getZ(), (byte)2, true, true);
        FilledMapItem.renderBiomePreviewMap(serverWorld, mapItem);
        MapData.addTargetDecoration(mapItem, blockpos, "+", MapDecoration.Type.TARGET_POINT);
        customColor.ifPresent(color -> {
            CompoundNBT compoundnbt1 = mapItem.getOrCreateTagElement("display");
            compoundnbt1.putInt("MapColor", color);
        });
        mapItem.setHoverName(new TranslationTextComponent("filled_map." + structure.getFeatureName().toLowerCase(Locale.ROOT)));
        
        mapItem.getTag().putString("JojoStructure", structureName); // no fucking clue why the advancement criteria doesn't work with the custom item name
        
        CompoundNBT customIconNBT = new CompoundNBT();
        customIconNBT.putString("path", iconPath.toString());
        customIconNBT.putDouble("x", blockpos.getX());
        customIconNBT.putDouble("z", blockpos.getZ());
        customIconNBT.putDouble("rot", 180);
        customIconNBT.putString("iconPath", iconPath.toString());
        mapItem.addTagElement("JojoIcon", customIconNBT);
        
        return mapItem;
    }
    
    public static void mixinMakeIconDecoration(PlayerEntity player, ItemStack mapStack, IMapDataMixin mapData) {
        CompoundNBT nbt = mapStack.getTag();
        String decorationKey = "jojotargeticon";
        if (nbt != null) {
            MCUtil.nbtGetCompoundOptional(nbt, "JojoIcon").ifPresent(iconNBT -> {
                World pLevel = player.level;
                double pLevelX = iconNBT.getDouble("x");
                double pLevelZ = iconNBT.getDouble("z");
                double pRotation = iconNBT.getDouble("rot");
                MapDecoration.Type pType = MapDecoration.Type.MANSION;
                ITextComponent hackToSendData = iconNBT.contains("iconPath", Constants.NBT.TAG_STRING)
                        ? new StringTextComponent(CustomIconMapDecoration.ICON_PATH_PREFIX + iconNBT.getString("iconPath")) : null;
                
                int x = mapData.x();
                int z = mapData.z();
                byte scale = mapData.scale();
                RegistryKey<World> dimension = mapData.dimension();
                Map<String, MapDecoration> decorations = mapData.decorations();
                
                int i = 1 << scale;
                float f = (float)(pLevelX - (double)x) / (float)i;
                float f1 = (float)(pLevelZ - (double)z) / (float)i;
                byte b0 = (byte)((int)((double)(f * 2.0F) + 0.5D));
                byte b1 = (byte)((int)((double)(f1 * 2.0F) + 0.5D));
                byte b2;
                if (f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F) {
                    pRotation = pRotation + (pRotation < 0.0D ? -8.0D : 8.0D);
                    b2 = (byte)((int)(pRotation * 16.0D / 360.0D));
                    if (dimension == World.NETHER && pLevel != null) {
                        int l = (int)(pLevel.getLevelData().getDayTime() / 10L);
                        b2 = (byte)(l * l * 34187121 + l * 121 >> 15 & 15);
                    }
                } else {
                    decorations.remove(decorationKey);
                    return;
                }
                
                decorations.put(decorationKey, new MapDecoration(pType, b0, b1, b2, hackToSendData));
            });
        }
    }
    
    
    public static class CustomIconMapDecoration extends MapDecoration {
        private static final String ICON_PATH_PREFIX = "jojoicon_";
        public final String data;

        public static void replaceWithCustomIcons(MapDecoration[] icons) {
            boolean hasExplorerMapTarget = false;
            for (int i = 0; i < icons.length; i++) {
                MapDecoration originalIcon = icons[i];
                if (originalIcon.getName() instanceof StringTextComponent) {
                    String data = ((StringTextComponent) originalIcon.getName()).getString();
                    if (data.startsWith(ICON_PATH_PREFIX)) {
                        String iconPath = data.substring(ICON_PATH_PREFIX.length());
                        icons[i] = new CustomIconMapDecoration(originalIcon.getType(), 
                                originalIcon.getX(), originalIcon.getY(), originalIcon.getRot(), 
                                iconPath);
                        hasExplorerMapTarget = true;
                    }
                }
            }
            
            if (hasExplorerMapTarget) { // removes the TARGET_POINT triangle icon
                for (int i = 0; i < icons.length; i++) {
                    MapDecoration originalIcon = icons[i];
                    if (originalIcon.getType() == MapDecoration.Type.TARGET_POINT) {
                        icons[i] = new DummyMapDecoration(originalIcon.getType(), 
                                originalIcon.getX(), originalIcon.getY(), originalIcon.getRot(), 
                                originalIcon.getName());
                    }
                }
            }
        }

        public CustomIconMapDecoration(Type pType, byte pX, byte pY, byte pRot, String data) {
            super(pType, pX, pY, pRot, null);
            this.data = data;
        }
        
        @Override
        public boolean render(int index) {
            CustomIconMapRender.customIconRender(this, new ResourceLocation(data), index);
            return true;
        }
    }
    
    private static class DummyMapDecoration extends MapDecoration {

        public DummyMapDecoration(Type pType, byte pX, byte pY, byte pRot, ITextComponent pName) {
            super(pType, pX, pY, pRot, pName);
        }
        
        @Override
        public boolean render(int index) {
            return true;
        }
        
    }
    
    
    public static interface IMapDataMixin {
        Map<String, MapDecoration> decorations();
        int x();
        int z();
        byte scale();
        RegistryKey<World> dimension();
    }
}
