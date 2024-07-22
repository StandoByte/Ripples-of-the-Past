package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PhotosCache;
import com.github.standobyte.jojo.client.polaroid.PhotosCache.PhotoHolder;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class PhotoItem extends Item {

    public PhotoItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("DevTicks")) {
            int textureTicks = tag.getInt("DevTicks");
            if (textureTicks > 0) {
                if (textureTicks == 1) {
                    tag.remove("DevTicks");
                }
                else {
                    tag.putInt("DevTicks", --textureTicks);
                }
            }
        }
        if (world.isClientSide()) {
            PhotosCache.getOrTryLoadPhoto(ClientUtil.getServerUUID(), getPhotoId(stack));
        }
    }
    
    
    
    public static long getPhotoId(ItemStack photoItem) {
        if (photoItem.hasTag()) {
            CompoundNBT tag = photoItem.getTag();
            if (tag.contains("PhotoId")) {
                return tag.getLong("PhotoId");
            }
        }
        
        return -1;
    }
    
    public static void setPhotoId(ItemStack photoItem, long id) {
        photoItem.getOrCreateTag().putLong("PhotoId", id);
    }
    
    
    
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }
    
    public static void setPhotoAnimTicks(ItemStack stack) {
        stack.getOrCreateTag().putInt("DevTicks", PHOTO_DEVELOPMENT_TICKS);
    }
    
    public static final int PHOTO_DEVELOPMENT_TICKS = 160;
    public static float getPhotoAlpha(ItemStack stack, float partialTick) {
        if (stack.hasTag() && stack.getTag().contains("DevTicks")) {
            float timeLeft = stack.getTag().getInt("DevTicks") - partialTick;
            return timeLeft > 120 ? 0 : 1 - timeLeft / 120;
        }
        
        return 1;
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (flag.isAdvanced()) {
            long photoId = getPhotoId(stack);
            if (photoId > -1) {
                PhotoHolder.Status status = PhotosCache.getCacheStatus(ClientUtil.getServerUUID(), photoId);
                tooltip.add(new StringTextComponent("Id: " + photoId).withStyle(TextFormatting.DARK_GRAY));
                tooltip.add(new StringTextComponent("Status: " + status.toString()).withStyle(TextFormatting.DARK_GRAY));
            }
        }
    }

}
