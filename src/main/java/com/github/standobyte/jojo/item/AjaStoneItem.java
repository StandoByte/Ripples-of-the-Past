package com.github.standobyte.jojo.item;

import java.util.Optional;

import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.damaging.LightBeamEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class AjaStoneItem extends Item {

    public AjaStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
//        if (player.isShiftKeyDown()) {
            INonStandPower power = INonStandPower.getPlayerNonStandPower(player);
            if (power.hasEnergy(getHamonChargeCost())) {
                Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
                if (hamonOptional.isPresent()) {
                    HamonData hamon = hamonOptional.get();
                    if (hamon.isSkillLearned(ModHamonSkills.AJA_STONE_KEEPER.get()) && power.consumeEnergy(getHamonChargeCost())) {
                        if (!world.isClientSide()) {
                            useStone(world, player, stack, 0.75F * hamon.getHamonDamageMultiplier() * hamon.getActionEfficiency(getHamonChargeCost(), false), true, false);
                            hamon.hamonPointsFromAction(HamonStat.STRENGTH, getHamonChargeCost());
                            JojoModUtil.sayVoiceLine(player, getHamonChargeVoiceLine());
                        }
                        Vector3d sparkVec = player.getLookAngle().scale(0.75)
                                .add(player.getX(), player.getY(0.6), player.getZ());
                        HamonUtil.emitHamonSparkParticles(world, player, sparkVec, 
                                hamon.getHamonDamageMultiplier() / HamonData.MAX_HAMON_STRENGTH_MULTIPLIER * 1.5F);
                        return ActionResult.success(stack);
                    }
                }
            }
//        }
        if (sufficientLight(world, player)) {
            player.startUsingItem(hand);
            return ActionResult.consume(stack);
        }
        return ActionResult.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        boolean perk = INonStandPower.getNonStandPowerOptional(entity).map(power -> power.getTypeSpecificData(ModPowers.HAMON.get()).map(
                hamon -> hamon.isSkillLearned(ModHamonSkills.AJA_STONE_KEEPER.get())).orElse(false)).orElse(false);
        useStone(world, entity, stack, 10F, perk, true);
        return stack;
    }

    protected void useStone(World world, LivingEntity entity, ItemStack itemStack, float damage, boolean perk, boolean checkLight) {
        if (checkLight && !sufficientLight(world, entity)) return;
        entity.playSound(ModSounds.AJA_STONE_BEAM.get(), Math.min(0.02F * damage, 1.0F), 1.0F + (random.nextFloat() - random.nextFloat()) * 0.1F);
        if (!world.isClientSide()) {
            LightBeamEntity beam = new LightBeamEntity(ModEntityTypes.AJA_STONE_BEAM.get(), entity, world);
            beam.shoot(damage, 16F + damage / 2F);
            world.addFreshEntity(beam);
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.getCooldowns().addCooldown(this, getCooldown());
            breakItem(world, player, itemStack, perk);
        }
        else {
            itemStack.shrink(1);
        }
    }

    @Override
    public void onUseTick(World world, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (world.isClientSide() && remainingTicks == getUseDuration(stack)) {
            ClientTickingSoundsHelper.playItemUseSound(entity, ModSounds.AJA_STONE_CHARGING.get(), 
                    0.25F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.05F, false, stack);
        }
    }
    
    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    protected int getCooldown() {
        return 100;
    }

    protected float getHamonChargeCost() {
        return 200;
    }

    protected SoundEvent getHamonChargeVoiceLine() {
        return ModSounds.LISA_LISA_AJA_STONE.get();
    }

    protected void breakItem(World world, PlayerEntity player, ItemStack itemStack, boolean perk) {
        if (!player.abilities.instabuild) {
            itemStack.shrink(1);
            if (!world.isClientSide() && random.nextInt(2) == 0) {
                player.addItem(perk && random.nextInt(200) == 0 ? new ItemStack(ModItems.SUPER_AJA_STONE.get()) : new ItemStack(Items.REDSTONE));
            }
        }
    }

    private static boolean sufficientLight(World world, LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        if (!world.isClientSide()) {
            return world.getMaxLocalRawBrightness(pos) > 9;
        }

        int time = (int) (world.getDayTime() % 24000);
        int light = world.dimension() != World.OVERWORLD || 
                world.isRainingAt(pos) || time > 12866 && time < 23135 ? world.getBrightness(LightType.BLOCK, pos) : world.getMaxLocalRawBrightness(pos);
        return light > 9;
    }
}
