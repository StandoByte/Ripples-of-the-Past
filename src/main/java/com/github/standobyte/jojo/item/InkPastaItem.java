package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class InkPastaItem extends Item {

    public InkPastaItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return useWithHamon(world, player, hand).orElse(super.use(world, player, hand));
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity entity) {
        ItemStack item = super.finishUsingItem(pStack, pLevel, entity);
        if (entity instanceof PlayerEntity) {
            onEaten(entity);
            if (((PlayerEntity) entity).abilities.instabuild) {
                return item;
            }
        }
        return new ItemStack(Items.BOWL);
    }
    
    public static void onEaten(LivingEntity player) {
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setInkPastaVisuals());
    }
    
    public static Optional<ActionResult<ItemStack>> useWithHamon(World world, PlayerEntity player, Hand hand) {
        boolean shootPasta = INonStandPower.getNonStandPowerOptional(player).resolve()
                .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get())
                        .map(hamon -> {
                            return hamon.isSkillLearned(ModHamonSkills.THROWABLES_INFUSION.get()) && power.consumeEnergy(150);
                        })).orElse(false);
        
        if (shootPasta) {
            ItemStack pastaItem = player.getItemInHand(hand);
            
            JojoMod.LOGGER.debug("PEW");
            
            if (!player.abilities.instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BOWL));
            }
            
            return Optional.of(ActionResult.consume(pastaItem));
        }
        
        return Optional.empty();
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("Scorpivan"));
    }

}
