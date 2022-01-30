package com.github.standobyte.jojo.item;

import java.util.Random;
import java.util.UUID;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.damage.ModDamageSources;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.entity.EntityType;
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
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class TommyGunItem extends Item {

    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public TommyGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(World world, LivingEntity entity, ItemStack stack, int remainingTicks) {
        JojoMod.LOGGER.debug(remainingTicks);
        int ammo = getAmmo(stack);
        int t = remainingTicks % 12;
        boolean shotTick = t == 0 || t == 2 || t == 4 || t == 5 || t == 7 || t == 9 || t == 11;
        if (remainingTicks <= 1) {
            entity.releaseUsingItem();
            return;
        }
        if (!world.isClientSide()) {
            JojoMod.LOGGER.debug(remainingTicks + ", " + getUseDuration(stack) + ", " + ammo + ", " + josephVoiceLine(entity));
            if (remainingTicks == getUseDuration(stack) && ammo == MAX_AMMO && josephVoiceLine(entity)) {
                JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_SCREAM_SHOOTING.get());
            }
            if (ammo > 0) {
                if (shotTick) {
                    TommyGunBulletEntity bullet = new TommyGunBulletEntity(entity, world);
                    bullet.shootFromRotation(entity, 20F, 0); // FIXME (!!!!) bullets seem to fly to another direction on such high velocity
                    world.addFreshEntity(bullet);
                    consumeAmmo(stack);
                    JojoMod.LOGGER.debug("pew");
                    entity.playSound(ModSounds.TOMMY_GUN_SHOT.get(), 1.0F, 1.0F);
                    // FIXME (!!) some sort of particle
                }
            }
            else {
                JojoMod.LOGGER.debug("no ammo");
                entity.playSound(ModSounds.TOMMY_GUN_NO_AMMO.get(), 1.0F, 1.0F);
                entity.releaseUsingItem();
            }
        }
        if (ammo > 0 && shotTick) {
            boolean rightSide = entity.getType() == EntityType.PLAYER ? world.isClientSide() : !world.isClientSide();
            if (rightSide) {
                float recoil = 1F + Math.min((1F - (float) remainingTicks / (float) getUseDuration(stack)) * 6F, 3F);
                Random random = entity.getRandom();
                entity.yRot += (random.nextFloat() - 0.5F) * 0.5F * recoil;
                entity.xRot += -random.nextFloat() * 1F * recoil;
            }
        }
    }
    
    private static final int MAX_AMMO = 50;
    private int getAmmo(ItemStack gun) {
        // FIXME (!!!) nbt ammo tag
        return 50;
    }
    
    private void consumeAmmo(ItemStack gun) {
        // FIXME (!!!) nbt ammo tag
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int remainingTicks) {
        if (remainingTicks <= 1 && josephVoiceLine(entity)) {
            JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_WAR_DECLARATION.get());
        }
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return reload(stack, player, world) ? ActionResult.success(stack) : ActionResult.fail(stack);
        }
        else {
            player.startUsingItem(hand);
            return ActionResult.consume(stack);
        }
    }
    
    private boolean reload(ItemStack stack, LivingEntity entity, World world) {
        // FIXME (!!!) reload (consume ammo, set cooldown (2 ticks per round))
        // FIXME (!!!) nbt ammo tag
        return false;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 85;
    }

    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        return INonStandPower.getNonStandPowerOptional(user).map(power -> 
        power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).map(hamon -> {
            if (hamon.getTechnique() == Technique.JOSEPH) {
                if (!user.level.isClientSide()) {
                    if (power.consumeEnergy(200) && ModDamageSources.dealHamonDamage(target, 0.15F, user, null)) {
                        target.invulnerableTime = 0;
                        hamon.hamonPointsFromAction(HamonStat.STRENGTH, 200);
                        JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_SHOOT.get());
                        return true;
                    }
                    return false;
                }
                return true;
            }
            return false;
        }).orElse(false)).orElse(false);
    }
    
    private boolean josephVoiceLine(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getTypeSpecificData(ModNonStandPowers.HAMON.get())
                        .map(hamon -> hamon.getTechnique() == Technique.JOSEPH)
                        .orElse(false))
                .orElse(false);
    }
    
    public void initAttributeModifiers() {
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(UUID.fromString("9b14156e-7ba3-446a-b18b-4c81a7d47a8b"), 
                "Weapon modifier", 0.5, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
                "Weapon modifier", -3.2, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
        if (slot == EquipmentSlotType.MAINHAND) {
            return attributeModifiers;
        }
        return super.getAttributeModifiers(slot, stack);
    }
}
