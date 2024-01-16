package com.github.standobyte.jojo.item;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.item.cassette.TrackSourceDye;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CassetteBlankItem extends Item {

    public CassetteBlankItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        boolean hasDyes = Arrays.stream(DyeColor.values())
                .anyMatch(dye -> {
                    TrackSourceDye source = new TrackSourceDye(dye);
                    return WalkmanSoundHandler.CassetteTracksSided.getTracks(source)
                            .findAny().isPresent();
                });
        String key = hasDyes ? "item.jojo.cassette_blank.hint.has_dyes" : "item.jojo.cassette_blank.hint";
        tooltip.add(new TranslationTextComponent(key).withStyle(TextFormatting.GRAY));
        tooltip.add(ClientUtil.donoItemTooltip("Кхъ"));
    }

}
