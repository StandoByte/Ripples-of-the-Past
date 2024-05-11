package com.github.standobyte.jojo.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class TommyGunItem extends Item {

    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    
    public TommyGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(World world, LivingEntity entity, ItemStack stack, int remainingTicks) {
        int ammo = getAmmo(stack);
        int t = remainingTicks % 12;
        boolean shotTick = t == 0 || t == 2 || t == 4 || t == 5 || t == 7 || t == 9 || t == 11;
        if (remainingTicks <= 1) {
            entity.releaseUsingItem();
            return;
        }
        if (!world.isClientSide()) {
            if (remainingTicks == getUseDuration(stack) - 12 && ammo == MAX_AMMO - 7 && josephVoiceLine(entity)) {
                JojoModUtil.sayVoiceLine(entity, ModSounds.JOSEPH_SCREAM_SHOOTING.get());
            }
            if (ammo > 0) {
                if (shotTick) {
                    TommyGunBulletEntity bullet = new TommyGunBulletEntity(entity, world);
                    bullet.shootFromRotation(entity, 20F, 0);
                    world.addFreshEntity(bullet);
                    consumeAmmo(stack, 1);
                    // TODO gun shot visual effect
                }
            }
            else {
                entity.releaseUsingItem();
            }
        }
        if (ammo > 0) {
            if (shotTick) {
                Random random = entity.getRandom();
                entity.playSound(ModSounds.TOMMY_GUN_SHOT.get(), 1.0F, 1.0F + (random.nextFloat() - 0.5F) * 0.3F);
                if (entity.getType() == EntityType.PLAYER ? world.isClientSide() : !world.isClientSide()) {
                    float recoil = 1F + Math.min((1F - (float) remainingTicks / (float) getUseDuration(stack)) * 6F, 3F);
                    entity.yRot += (random.nextFloat() - 0.5F) * 0.3F * recoil;
                    entity.xRot += -random.nextFloat() * 0.75F * recoil;
                }
            }
        }
        else {
            entity.playSound(ModSounds.TOMMY_GUN_NO_AMMO.get(), 1.0F, 1.0F);
        }
    }
    
    private static final int MAX_AMMO = 50;
    public static int getAmmo(ItemStack gun) {
        return gun.getOrCreateTag().getInt("Ammo");
    }
    
    public static boolean consumeAmmo(ItemStack gun, int amount) {
        int ammo = getAmmo(gun);
        if (ammo < 0) {
            gun.getTag().putInt("Ammo", 0);
            return false;
        }
        if (ammo > 0) {
            gun.getTag().putInt("Ammo", Math.max(ammo - amount, 0));
            return true;
        }
        return false;
    }
    
    private boolean reload(ItemStack stack, LivingEntity entity, World world) {
        int ammoToLoad = MAX_AMMO - getAmmo(stack);
        if (ammoToLoad > 0) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                ammoToLoad = consumeAmmo(player, ammoToLoad);
                if (!world.isClientSide()) {
                    player.getCooldowns().addCooldown(this, ammoToLoad * 2);
                }
            }
            if (ammoToLoad > 0) {
                if (!world.isClientSide()) {
                    stack.getTag().putInt("Ammo", getAmmo(stack) + ammoToLoad);
                }
                return true;
            }
        }
        return false;
    }

    private static final int BULLETS_PER_GUNPOWDER = 8;
    private int consumeAmmo(PlayerEntity player, int ammoToLoad) {
        if (!player.abilities.instabuild) {
            List<ItemStack> ironNuggets = new ArrayList<>();
            int ironNuggetsCount = 0;
            List<ItemStack> gunpowder = new ArrayList<>();
            int gunpowderCount = 0;
            
            for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
                ItemStack inventoryStack = player.inventory.getItem(i);
                if (inventoryStack.getItem() == Items.IRON_NUGGET) {
                    ironNuggets.add(inventoryStack);
                    ironNuggetsCount += inventoryStack.getCount();
                }
                else if (inventoryStack.getItem() == Items.GUNPOWDER) {
                    gunpowder.add(inventoryStack);
                    gunpowderCount += inventoryStack.getCount();
                }
            }
            
            ammoToLoad = MathUtil.min(ironNuggetsCount, gunpowderCount * BULLETS_PER_GUNPOWDER, ammoToLoad);
            ironNuggetsCount = ammoToLoad;
            gunpowderCount = MathHelper.ceil((float) ammoToLoad / BULLETS_PER_GUNPOWDER);

            for (ItemStack ironNuggetsStack : ironNuggets) {
                int consumed = Math.min(ironNuggetsStack.getCount(), ironNuggetsCount);
                if (!player.level.isClientSide()) {
                    ironNuggetsStack.shrink(consumed);
                }
                ironNuggetsCount -= consumed;
                if (ironNuggetsCount == 0) break;
            }
            for (ItemStack gunpowderStack : gunpowder) {
                int consumed = Math.min(gunpowderStack.getCount(), gunpowderCount);
                if (!player.level.isClientSide()) {
                    gunpowderStack.shrink(consumed);
                }
                gunpowderCount -= consumed;
                if (gunpowderCount == 0) break;
            }
        }
        return ammoToLoad;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getAmmo(stack) < MAX_AMMO;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - ((double) getAmmo(stack) / (double) MAX_AMMO);
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putInt("Ammo", MAX_AMMO);
            items.add(stack);
        }
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
            return reload(stack, player, world) ? ActionResult.consume(stack) : ActionResult.fail(stack);
        }
        else {
            player.startUsingItem(hand);
            return ActionResult.consume(stack);
        }
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
        power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                if (!user.level.isClientSide()) {
                    if (power.consumeEnergy(200) && DamageUtil.dealHamonDamage(target, 0.15F, user, null)) {
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
                .map(power -> power.getTypeSpecificData(ModPowers.HAMON.get())
                        .map(hamon -> hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get()))
                        .orElse(false))
                .orElse(false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
        if (slot == EquipmentSlotType.MAINHAND) {
            if (attributeModifiers == null) {
                Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(UUID.fromString("9b14156e-7ba3-446a-b18b-4c81a7d47a8b"), 
                        "Weapon modifier", 0.5, AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, 
                        "Weapon modifier", -2, AttributeModifier.Operation.ADDITION));
                attributeModifiers = builder.build();
            }
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("item.jojo.tommy_gun.reload_prompt", 
                new KeybindTextComponent("key.sneak"), new KeybindTextComponent("key.use")).withStyle(TextFormatting.GRAY));
        
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("KingKKrill"));
    }
}
