package com.github.standobyte.jojo.item;

import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ClackersItem extends Item {
    public static final int TICKS_MAX_POWER = 20;
    
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public ClackersItem(Properties properties) {
        super(properties);
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 6.0, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        INonStandPower power = INonStandPower.getPlayerNonStandPower(player);
        if (power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.isSkillLearned(ModHamonSkills.CLACKER_VOLLEY.get())) {
                return true;
            }
            return false;
        }).orElse(false)) {
            player.startUsingItem(hand);
            return ActionResult.pass(stack);
        }
        else {
            playClackSound(world, player);
            ding(world, player);
            return ActionResult.fail(stack);
        }
    }
    
    private void ding(World world, PlayerEntity player) {
    }

    private static final float CHARGE_TICK_COST = 5;
    private static final float UPKEEP_TICK_COST = CHARGE_TICK_COST / 5;
    @Override
    public void onUseTick(World world, LivingEntity entity, ItemStack stack, int remainingTicks) {
        int ticksUsed = getUseDuration(stack) - remainingTicks;
        int ticksMaxPower = TICKS_MAX_POWER;
        if (clackersTexVariant(ticksUsed, ticksMaxPower) > 0) {
            //playClackSound(world, entity);
            if (ticksUsed >= ticksMaxPower / 2 && !world.isClientSide()) {
                Vector3d sparkVec = entity.getLookAngle().scale(0.75)
                        .add(entity.getX(), entity.getY(0.6), entity.getZ());
//                HamonUtil.emitHamonSparkParticles(world, entity instanceof PlayerEntity ? (PlayerEntity) entity : null, 
//                        sparkVec, ticksUsed >= ticksMaxPower ? 0.25F : 0.1F);
            }
        }
        if (!world.isClientSide()) {
            if (!INonStandPower.getNonStandPowerOptional(entity).map(power -> 
            power.consumeEnergy(ticksUsed <= ticksMaxPower ? CHARGE_TICK_COST : UPKEEP_TICK_COST)).orElse(false)) {
                entity.releaseUsingItem();
                return;
            }
            if (ticksUsed == ticksMaxPower) {
                JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_CLACKER_VOLLEY.get());
            }
        }
    }
    
    public static int clackersTexVariant(int ticksUsed, int ticksMax) {
        if (ticksUsed < ticksMax / 2) {
            return ticksUsed % 20 == 10 ? 1 : 0;
        }
        if (ticksUsed < ticksMax) {
            return ticksUsed % 8 == 4 ? 1 : 0;
        }
        return 2 + ticksUsed % 2;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, World world, LivingEntity entity, int ticksLeft) {
        int ticksUsed = getUseDuration(itemStack) - ticksLeft;
        float power = (float) Math.min(ticksUsed, TICKS_MAX_POWER) / (float) TICKS_MAX_POWER;
        if (power > 0) {
            if (power < 0.15) {
                playClackSound(world, entity);
                if (!world.isClientSide()) {
                    entity.hurt(entity instanceof PlayerEntity ? DamageSource.playerAttack((PlayerEntity) entity) : DamageSource.mobAttack(entity), 1.0F);
                    JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_OH_NO.get());
                }
            }
            else if (!world.isClientSide() && power > 0.5) {
                ClackersEntity clackers = new ClackersEntity(world, entity);
                float projectileSpeed = power == 1.0F ? 3 : power * 2;
                float hamonDmg = projectileSpeed * 0.5F;
                clackers.setHamonDamage(hamonDmg);
                clackers.setHamonEnergySpent(Math.min(ticksUsed, TICKS_MAX_POWER) * CHARGE_TICK_COST + Math.max(ticksUsed - TICKS_MAX_POWER, 0) * UPKEEP_TICK_COST);
                clackers.shootFromRotation(entity, projectileSpeed, 0.5F);
                world.addFreshEntity(clackers);
            }
        }
        if (power > 0.5 && !(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild)) {
            itemStack.shrink(1);
        }
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        return INonStandPower.getNonStandPowerOptional(user).map(power -> 
        power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.isSkillLearned(ModHamonSkills.CLACKER_VOLLEY.get())) {
                if (!user.level.isClientSide()) {
                    if (power.consumeEnergy(200) && DamageUtil.dealHamonDamage(target, 0.15F, user, null)) {
                        target.invulnerableTime = 0;
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, 200);
                        return true;
                    }
                    return false;
                }
                return true;
            }
            return false;
        }).orElse(false)).orElse(false);
    }

    public static void playClackSound(World world, LivingEntity entity) {
        world.playSound(entity instanceof PlayerEntity ? (PlayerEntity) entity : null, entity.getX(), entity.getY(), entity.getZ(), 
                ModSounds.CLACKERS.get(), entity.getSoundSource(), 0.5F, 1.0F + (random.nextFloat() - 0.5F) * 0.1F);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.MAINHAND) {
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

}
