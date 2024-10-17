package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.potion.BleedingEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class KnifeItem extends Item {
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public KnifeItem(Properties properties) {
        super(properties);
        
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 2.0D, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();

        DispenserBlock.registerBehavior(this, new ProjectileDispenseBehavior() {
            @Override
            protected ProjectileEntity getProjectile(World world, IPosition position, ItemStack stack) {
                KnifeEntity knife = new KnifeEntity(world, position.x(), position.y(), position.z());
                knife.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
                return knife;
            }
        });
    }

    public static final int MAX_KNIVES_THROW = 8;
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        int knivesToThrow = !player.isShiftKeyDown() ? Math.min(handStack.getCount(), MAX_KNIVES_THROW) : 1;
        if (!world.isClientSide()) {
            ItemStack headStack = player.getItemBySlot(EquipmentSlotType.HEAD);
            if (handStack.getCount() == 1 && headStack.getItem() instanceof StoneMaskItem && BleedingEffect.applyStoneMask(player, headStack)) {
                player.hurt(DamageSource.playerAttack(player), 1.0F);
                return ActionResult.consume(handStack);
            }
            
            for (int i = 0; i < knivesToThrow; i++) {
                KnifeEntity knifeEntity = new KnifeEntity(world, player);
                knifeEntity.setTimeStopFlightTicks(5);
                knifeEntity.shootFromRotation(player, 1.5F, i == 0 ? 1.0F : 16.0F);
                world.addFreshEntity(knifeEntity);
            }
            
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    knivesToThrow == 1 ? ModSounds.KNIFE_THROW.get() : ModSounds.KNIVES_THROW.get(), 
                            SoundCategory.PLAYERS, 0 * 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            
            int cooldown = knivesToThrow * 3;
            player.getCooldowns().addCooldown(this, cooldown);
            if (!player.abilities.instabuild) {
                handStack.shrink(knivesToThrow);
            }
            
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.getStandManifestation() instanceof StandEntity) {
                    ((StandEntity) power.getStandManifestation()).onKnivesThrow(world, player, handStack, knivesToThrow);
                }
            });
        }
        return ActionResult.success(handStack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (stack.getCount() > 1) {
            return super.getDestroySpeed(stack, state);
        }
        Block block = state.getBlock();
        if (block == Blocks.COBWEB) {
            return 15.0F;
        } else {
            Material material = state.getMaterial();
            return material != Material.PLANT && material != Material.REPLACEABLE_PLANT && material != Material.CORAL && !state.is(BlockTags.LEAVES) && material != Material.VEGETABLE ? 1.0F : 1.5F;
        }
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockIn) {
        return blockIn.getBlock() == Blocks.COBWEB;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        if (slot == EquipmentSlotType.MAINHAND && stack.getCount() == 1) {
            return attributeModifiers;
        }
        return super.getAttributeModifiers(slot, stack);
    }
    
    
    // axes are in shambles rn
    @Override
    public ActionResultType useOn(ItemUseContext pContext) {
        World world = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState block = blockstate.getToolModifiedState(world, blockpos, pContext.getPlayer(), pContext.getItemInHand(), ToolType.AXE);
        if (block != null) {
            PlayerEntity playerentity = pContext.getPlayer();
            world.playSound(playerentity, blockpos, SoundEvents.AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide) {
                world.setBlock(blockpos, block, 11);
//                if (playerentity != null) {
//                    pContext.getItemInHand().hurtAndBreak(1, playerentity, (p_220040_1_) -> {
//                        p_220040_1_.broadcastBreakEvent(pContext.getHand());
//                    });
//                }
            }

            return ActionResultType.sidedSuccess(world.isClientSide);
        } else {
            return ActionResultType.PASS;
        }
    }
    
    // this would actually nerf shears though so i don't feel like going through with this suggestion
//    @Override
//    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity entity, Hand hand) {
//        if (entity.level.isClientSide) return ActionResultType.PASS;
//        if (entity instanceof IForgeShearable) {
//            IForgeShearable target = (IForgeShearable)entity;
//            BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
//            if (target.isShearable(stack, entity.level, pos)) {
//                List<ItemStack> drops = target.onSheared(playerIn, stack, entity.level, pos,
//                        EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack));
//                Random rand = new Random();
//                drops.forEach(d -> {
//                    ItemEntity ent = entity.spawnAtLocation(d, 1.0F);
//                    ent.setDeltaMovement(ent.getDeltaMovement().add(
//                            (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F), 
//                            (double)(rand.nextFloat() * 0.05F), 
//                            (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
//                });
////                stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(hand));
//            }
//            return ActionResultType.SUCCESS;
//        }
//        return ActionResultType.PASS;
//    }
}
