package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.MolotovEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class MolotovItem extends Item {

    public MolotovItem(Properties pProperties) {
        super(pProperties);

        DispenserBlock.registerBehavior(this, new IDispenseItemBehavior() {
            public ItemStack dispense(IBlockSource blockSource, ItemStack item) {
                return new ProjectileDispenseBehavior() {
                    
                    @Override
                    protected ProjectileEntity getProjectile(World pLevel, IPosition pPosition, ItemStack pStack) {
                        return new MolotovEntity(pLevel, pPosition.x(), pPosition.y(), pPosition.z());
                    }
                    
                    @Override
                    protected float getUncertainty() {
                        return super.getUncertainty() * 0.5F;
                    }
                    
                    @Override
                    protected float getPower() {
                        return super.getPower() * 1.25F;
                    }
                }.dispense(blockSource, item);
            }
        });
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        boolean hasFire = player.isOnFire() || IStandPower.getStandPowerOptional(player).resolve()
                .map(stand -> stand.getType() == ModStands.MAGICIANS_RED.getStandType()).orElse(false);
        ObjectWrapper<ItemStack> flintAndSteelWr = new ObjectWrapper<>(ItemStack.EMPTY);
        ItemStack fireCharge = ItemStack.EMPTY;
        if (!hasFire) {
            BlockPos center = player.blockPosition().above();
            for (int x = -2; x <= 2 && !hasFire; x++) {
                for (int y = -2; y <= 2 && !hasFire; y++) {
                    for (int z = -2; z <= 2 && !hasFire; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState blockState = world.getBlockState(pos);
                        FluidState fluidState = world.getFluidState(pos);
                        if (
                                blockState.getBlock() instanceof AbstractFireBlock || 
                                    ((blockState.getBlock() instanceof AbstractFurnaceBlock || blockState.getBlock() instanceof CampfireBlock
                                            || blockState.getBlock() instanceof TorchBlock && blockState.getBlock() != Blocks.REDSTONE_TORCH)
                                     && (!blockState.hasProperty(BlockStateProperties.LIT) || blockState.getValue(BlockStateProperties.LIT))) || 
                                fluidState.getType() instanceof LavaFluid || 
                                fluidState.is(FluidTags.LAVA)) {
                            hasFire = true;
                        }
                    }
                }
            }
        }
        if (!hasFire) {
            flintAndSteelWr.set(MCUtil.findInInventory(player.inventory, 
                    stack -> !stack.isEmpty() && stack.getItem() instanceof FlintAndSteelItem));
            if (!flintAndSteelWr.get().isEmpty()) {
                hasFire = true;
                if (!player.isSilent()) {
                    player.playSound(SoundEvents.FLINTANDSTEEL_USE, 1, random.nextFloat() * 0.4F + 0.8F);
                }
            }
        }
        if (!hasFire) {
            fireCharge = MCUtil.findInInventory(player.inventory, 
                    stack -> !stack.isEmpty() && stack.getItem() instanceof FireChargeItem);
            if (!fireCharge.isEmpty()) {
                hasFire = true;
                if (!player.isSilent()) {
                    player.playSound(SoundEvents.FIRECHARGE_USE, 1, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                }
            }
        }
        if (!hasFire) {
            if (!world.isClientSide()) {
                player.displayClientMessage(new TranslationTextComponent("jojo.message.action_condition.molotov_fire"), true);
            }
            return ActionResult.fail(heldItem);
        }
        
        if (!world.isClientSide) {
            MolotovEntity molotovEntity = new MolotovEntity(world, player);
            molotovEntity.setItem(heldItem);
            molotovEntity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 0.75F, 1.0F);
            world.addFreshEntity(molotovEntity);
            if (!player.abilities.instabuild) {
                heldItem.shrink(1);
                ItemStack flintAndSteel = flintAndSteelWr.get();
                if (!flintAndSteel.isEmpty()) {
                    flintAndSteel.hurtAndBreak(1, player, entity -> {
                        if (!entity.isSilent()) {
                            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                                    SoundEvents.ITEM_BREAK, entity.getSoundSource(), 0.8F, 0.8F + world.random.nextFloat() * 0.4F);
                            MCUtil.spawnItemParticles(player, flintAndSteel, 5);
                        }
                    });
                }
                else if (!fireCharge.isEmpty()) {
                    fireCharge.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        
        if (!player.isSilent()) {
            player.playSound(ModSounds.MOLOTOV_THROW.get(), 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        }
        
        return ActionResult.sidedSuccess(heldItem, world.isClientSide());
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("ArchLunatic"));
    }

}
