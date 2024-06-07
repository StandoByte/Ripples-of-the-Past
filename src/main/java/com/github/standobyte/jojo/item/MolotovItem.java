package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.MolotovEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class MolotovItem extends Item {

    public MolotovItem(Properties pProperties) {
        super(pProperties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack item = player.getItemInHand(hand);
        world.playSound(null, 
                player.getX(), player.getY(), player.getZ(), 
                ModSounds.MOLOTOV_THROW.get(), SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        if (!world.isClientSide) {
            MolotovEntity molotovEntity = new MolotovEntity(world, player);
            molotovEntity.setItem(item);
            molotovEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(molotovEntity);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.abilities.instabuild) {
            item.shrink(1);
        }

        return ActionResult.sidedSuccess(item, world.isClientSide());
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("ArchLunatic"));
    }

}
