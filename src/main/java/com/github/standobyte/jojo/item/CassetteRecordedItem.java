package com.github.standobyte.jojo.item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.cassette.CassetteCap;
import com.github.standobyte.jojo.item.cassette.CassetteCap.TrackSourceList;
import com.github.standobyte.jojo.item.cassette.TrackSourceDye;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CassetteRecordedItem extends Item {

    public CassetteRecordedItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CassetteCap cassette = CassetteRecordedItem.getCassetteData(stack).orElse(null);
        TrackSourceList trackSources = cassette != null ? cassette.getTracks() : TrackSourceList.BROKEN_CASSETTE;
        if (trackSources.isBroken()) {
            tooltip.add(new TranslationTextComponent("jojo.cassette.bad_recording")
                    .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
        }
        else {
            WalkmanSoundHandler.CassetteTracksSided.fromSourceList(trackSources).forEach((side, tracks) -> {
                if (!tracks.isEmpty()) {
                    tooltip.add(new TranslationTextComponent("jojo.cassette." + side.name().toLowerCase())
                            .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                    tracks.forEach(track -> tooltip.add(track.getName().withStyle(TextFormatting.GRAY)));
                    tooltip.add(new StringTextComponent(" "));
                }
            });
            
            if (cassette.hasDyeCraftHint()) {
                DyeColor dye = cassette.getDye();
                if (dye != null) {
                    Item dyeItem = DyeItem.byColor(dye);
                    tooltip.add(new TranslationTextComponent("item.jojo.cassette.dye_hint", 
                            new TranslationTextComponent(ModItems.CASSETTE_BLANK.get().getDescriptionId()),
                            new TranslationTextComponent(dyeItem.getDescriptionId()))
                            .withStyle(TextFormatting.GRAY));
                }
            }
            
            int generation = cassette.getGeneration();
            tooltip.add(new TranslationTextComponent("jojo.cassette.generation." + Math.min(generation, 2))
                    .withStyle(TextFormatting.GRAY));
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
                        CassetteRecordedItem.editCassetteData(cassette, cap -> {
                            cap.setDye(dye);
                            cap.addDyeCraftHint();
                            cap.recordTracks(Collections.singletonList(source));
                        });
                        items.add(cassette);
                    }
                }
            }
        }
    }
    
    
    public static Optional<CassetteCap> getCassetteData(ItemStack recordedCassetteItem) {
        if (recordedCassetteItem.isEmpty()) return Optional.empty();
        return MCUtil.nbtGetCompoundOptional(recordedCassetteItem.getOrCreateTag(), "Cassette")
                .map(nbt -> {
                    CassetteCap data = new CassetteCap(recordedCassetteItem);
                    data.fromNBT(nbt);
                    return data;
                });
    }
    
    public static void editCassetteData(ItemStack recordedCassetteItem, Consumer<CassetteCap> action) {
        CassetteCap cassetteData = getCassetteData(recordedCassetteItem).orElse(new CassetteCap(recordedCassetteItem));
        action.accept(cassetteData);
        recordedCassetteItem.getOrCreateTag().put("Cassette", cassetteData.toNBT());
    }
    
}
