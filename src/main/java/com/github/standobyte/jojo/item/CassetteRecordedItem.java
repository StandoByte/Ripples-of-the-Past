package com.github.standobyte.jojo.item;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.item.cassette.CassetteCap;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCap.TrackSourceList;
import com.github.standobyte.jojo.capability.item.cassette.CassetteCapProvider;
import com.github.standobyte.jojo.capability.item.cassette.TrackSourceDye;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
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
        // TODO нахуй удаляю ебаные capability у предметов, хуйня без задач
        // спасибо NeoForge, что они избавятся от этого говна
        // может, это я долбоеб и нихуя не понял, как ими пользоваться
        // но все равно лучше не ебать себе мозги и просто блять засунуть все данные в tag и все
        return new CassetteCapProvider(stack, nbt);
    }
    
    public static LazyOptional<CassetteCap> getCapability(ItemStack stack) {
        return !stack.isEmpty() ? stack.getCapability(CassetteCapProvider.CAPABILITY) : LazyOptional.empty();
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        
        CompoundNBT cassetteNBT = getCapability(stack).map(CassetteCap::toNBT).orElse(null); 
        // why the fuck does it have default values when i put the item from creative to my inventory
        // this is so fucking stupid
        // this is yet another minor stupid fucking thing i lose my brain cells over
        // doesn't happen that often, but i fucking hate it every time
        // серьезно, я в рот это ебал, какого хуя эта хуета не работает так, как от нее ожидается
        // с хуя ли он мне блять дает какой-то новый стак, в котором нихуя нет
        // пиздец ебаный
        
        // когда создается SSetSlotPacket, в стаке нет ни capabilities, ни capNbt
        // просто блять нет, null, пусто
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
        TrackSourceList trackSources = cassette != null ? cassette.getTracks() : TrackSourceList.BROKEN_CASSETTE;
        if (trackSources.isBroken()) {
            tooltip.add(new TranslationTextComponent("jojo.cassette.bad_recording").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
        }
        else {
            WalkmanSoundHandler.CassetteTracksSided.fromSourceList(trackSources).forEach((side, tracks) -> {
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
    
    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (group != ItemGroup.TAB_SEARCH && this.allowdedIn(group)) {
//            boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT; // nope
            boolean isClientSide = true;
            if (isClientSide) {
                for (DyeColor dye : DyeColor.values()) {
                    TrackSourceDye source = new TrackSourceDye(dye);
                    if (WalkmanSoundHandler.CassetteTracksSided.getTracks(source)
                            .findAny().isPresent()) {
                        ItemStack cassette = new ItemStack(this);
                        CassetteRecordedItem.getCapability(cassette).ifPresent(cap -> {
                            cap.setDye(dye);
                            cap.recordTracks(Collections.singletonList(source));
                        });
                        items.add(cassette);
                    }
                }
            }
        }
    }
    
}
