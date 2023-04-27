package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.item.walkman.WalkmanCapProvider;
import com.github.standobyte.jojo.capability.item.walkman.WalkmanSlotHandlerCap;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
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
import net.minecraft.nbt.FloatNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class WalkmanItem extends Item {

    public WalkmanItem(Properties properties) {
        super(properties);
    }

    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            CompoundNBT tag = stack.getOrCreateTag();
            if (!tag.contains("WalkmanId")) {
                tag.putInt("WalkmanId", SaveFileUtilCapProvider.getSaveFileCap(((ServerWorld) world).getServer()).incWalkmanId());
            }
        }
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(cap -> {
            if (cap instanceof WalkmanSlotHandlerCap && !world.isClientSide()) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (WalkmanSlotHandlerCap) cap, 
                        buf -> WalkmanItemContainer.writeAdditionalData(buf, stack));
            }
        });
        return ActionResult.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new WalkmanCapProvider(stack);
    }
    
    
    public static float getVolume(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Volume", MCUtil.getNbtId(FloatNBT.class))) {
            return stack.getTag().getFloat("Volume");
        }
        return 1.0F;
    }
    
    public static void setVolume(ItemStack stack, float volume) {
        stack.getOrCreateTag().putFloat("Volume", volume);
    }
    
    public static PlaybackMode getPlaybackMode(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("Loop") ? PlaybackMode.LOOP : PlaybackMode.STOP_AT_THE_END;
    }
    
    public static void setPlaybackMode(ItemStack stack, PlaybackMode mode) {
        stack.getOrCreateTag().putBoolean("Loop", mode == PlaybackMode.LOOP);
    }
    
    public static int getId(ItemStack stack) {
        return stack.getOrCreateTag().getInt("WalkmanId");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        Playlist playlist = WalkmanSoundHandler.getPlaylist(getId(stack));
        if (playlist != null && playlist.isPlaying()) {
            TrackInfo playingNow = playlist.getCurrentTrack();
            if (playingNow != null) {
                tooltip.add(new TranslationTextComponent("record.nowPlaying", playingNow.track.getName()).withStyle(TextFormatting.GRAY));
                tooltip.add(new StringTextComponent(" "));
            }
        }

        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }
    
    
    
    public static enum PlaybackMode {
        STOP_AT_THE_END { @Override public PlaybackMode getOpposite() { return LOOP; }},
        LOOP { @Override public PlaybackMode getOpposite() { return STOP_AT_THE_END; }};
        
        public abstract PlaybackMode getOpposite();
    }
}
