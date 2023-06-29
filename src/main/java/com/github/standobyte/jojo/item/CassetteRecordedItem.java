package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.item.cassette.CassetteCap;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCap.TrackList;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class CassetteRecordedItem extends Item {

    public CassetteRecordedItem(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new CassetteCapProvider(stack, nbt);
    }
    
    public static LazyOptional<CassetteCap> getCapability(ItemStack stack) {
        return !stack.isEmpty() ? stack.getCapability(CassetteCapProvider.CAPABILITY) : LazyOptional.empty();
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT nbt = super.getShareTag(stack);
        if (nbt == null) nbt = new CompoundNBT();
        
        CompoundNBT cassetteNBT = getCapability(stack).map(CassetteCap::toNBT).orElse(null);
        if (cassetteNBT != null) nbt.put("Cassette", cassetteNBT);
        
        return nbt;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        if (nbt != null) {
            super.readShareTag(stack, nbt);
            if (nbt.contains("Cassette", MCUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT cassetteNBT = nbt.getCompound("Cassette");
                if (cassetteNBT != null) getCapability(stack).ifPresent(cap -> cap.fromNBT(cassetteNBT));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CassetteCap cassette = CassetteRecordedItem.getCapability(stack).orElse(null);
        TrackList trackSources = cassette != null ? cassette.getTracks() : TrackList.BROKEN_CASSETTE;
        if (trackSources.isBroken()) {
            tooltip.add(new TranslationTextComponent("jojo.cassette.bad_recording").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
        }
        else {
            WalkmanSoundHandler.splitIntoSides(WalkmanSoundHandler.getTracksOnClient(trackSources)).forEach((side, tracks) -> {
                if (!tracks.isEmpty()) {
                    tooltip.add((new TranslationTextComponent("jojo.cassette." + side.name().toLowerCase())).withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                    tracks.forEach(track -> tooltip.add(track.getName().withStyle(TextFormatting.GRAY)));
                    tooltip.add(new StringTextComponent(" "));
                }
            });
            
            int generation = cassette.getGeneration();
            tooltip.add((new TranslationTextComponent("jojo.cassette.generation." + Math.min(generation, 2))).withStyle(TextFormatting.GRAY));
        }
        
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }
    
}
