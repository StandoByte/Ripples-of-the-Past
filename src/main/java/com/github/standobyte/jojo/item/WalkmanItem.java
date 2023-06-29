package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.MultipleCapsProvider;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanCassetteSlotCap;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanCassetteSlotProvider;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanDataCap;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanDataCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.Playlist;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.TrackInfo;
import com.github.standobyte.jojo.container.WalkmanItemContainer;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class WalkmanItem extends Item {

    public WalkmanItem(Properties properties) {
        super(properties);
    }

    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            getWalkmanData(stack).ifPresent(data -> data.initId((ServerWorld) world));
            stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(cap -> {
                if (cap instanceof WalkmanCassetteSlotCap) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (WalkmanCassetteSlotCap) cap, 
                            buf -> WalkmanItemContainer.writeAdditionalData(buf, stack));
                }
            });
        }
        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new MultipleCapsProvider.Builder()
                .addSerializable(new WalkmanCassetteSlotProvider(stack), "CassetteSlot")
                .addSerializable(new WalkmanDataCapProvider(stack, nbt), "Walkman")
                .build();
    }
    
    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT nbt = super.getShareTag(stack);
        if (nbt == null) nbt = new CompoundNBT();
        
        CompoundNBT cassetteNBT = getWalkmanData(stack).map(WalkmanDataCap::toNBT).orElse(null);
        if (cassetteNBT != null) nbt.put("Walkman", cassetteNBT);
        
        return nbt;
    }
    
    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            super.readShareTag(stack, nbt);
            if (nbt.contains("Walkman", MCUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT cassetteNBT = nbt.getCompound("Walkman");
                if (cassetteNBT != null) getWalkmanData(stack).ifPresent(cap -> cap.fromNBT(cassetteNBT));
            }
        }
    }
    
    public static LazyOptional<WalkmanDataCap> getWalkmanData(ItemStack stack) {
        return !stack.isEmpty() ? stack.getCapability(WalkmanDataCapProvider.CAPABILITY) : LazyOptional.empty();
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (world != null) {
            getWalkmanData(stack).ifPresent(walkman -> {
                if (walkman.isIdInitialized()) {
                    Playlist playlist = WalkmanSoundHandler.getPlaylist(walkman.getId());
                    if (playlist != null && playlist.isPlaying()) {
                        TrackInfo playingNow = playlist.getCurrentTrack();
                        if (playingNow != null) {
                            tooltip.add(new TranslationTextComponent("record.nowPlaying", playingNow.track.getName()).withStyle(TextFormatting.GRAY));
                            tooltip.add(new StringTextComponent(" "));
                        }
                    }
                }
            });
        }

        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }
}
