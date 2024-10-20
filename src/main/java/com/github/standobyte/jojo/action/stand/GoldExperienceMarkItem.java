package com.github.standobyte.jojo.action.stand;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GoldExperienceMarkItem extends StandAction {

    public GoldExperienceMarkItem(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack item = user.getItemInHand(Hand.OFF_HAND);
        if (item.isEmpty()) {
            return conditionMessage("ge_lifeform_material_only_item");
        }
        if (!GoldExperienceCreateLifeform.canGiveLifeTo(item)) {
            return conditionMessage("ge_lifeform_material_item");
        }
        
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide()) {
            ItemStack heldItem = user.getItemInHand(Hand.OFF_HAND);
            if (!heldItem.isEmpty() && user instanceof ServerPlayerEntity) {
                ItemStack markedStack;
                boolean give = false;
                if (heldItem.getCount() == 1) {
                    markedStack = heldItem;
                    ItemStack mainHandItem = user.getItemInHand(Hand.MAIN_HAND);
                    // keep the bow/crossbow in main hand
                    if (!mainHandItem.isEmpty() && 
                            mainHandItem.getItem() instanceof ShootableItem && ((ShootableItem) mainHandItem.getItem()).getAllSupportedProjectiles().test(heldItem)) {
                        user.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
                        give = true;
                    }
                    // swap items
                    else {
                        user.setItemInHand(Hand.OFF_HAND, mainHandItem);
                        user.setItemInHand(Hand.MAIN_HAND, heldItem);
                    }
                }
                else {
                    markedStack = heldItem.split(1);
                    if (user.getItemInHand(Hand.MAIN_HAND).isEmpty()) {
                        user.setItemInHand(Hand.MAIN_HAND, markedStack);
                    }
                    else {
                        give = true;
                    }
                }
                user.stopUsingItem();

                StandEffectsTracker standEffects = power.getContinuousEffects();
                standEffects.getEffects()
                .filter(effect -> effect.effectType == ModStandEffects.GE_ITEM_MARK.get())
                .forEach(standEffects::removeEffect);
                
                TrackerItemStack itemTracker = TrackerItemStack.setTracked(markedStack, (ServerPlayerEntity) user);
                if (itemTracker != null) {
                    itemTracker.setAtEntity(user.getId(), world, KnownItemState.ENTITY_HAS_ITEM);
                    
                    GEItemMarkEffect effect = new GEItemMarkEffect(itemTracker.getTrackerId());
                    effect.withStand(power);
                    standEffects.addEffect(effect);
                    
                    MCUtil.playSound(world, null, user, ModSounds.GOLD_EXPERIENCE_LIFE_ITEM.get(), 
                            user.getSoundSource(), 0.5f, 1.0f, StandUtil::playerCanHearStands);
                }
                
                // needs to be done after the tracker NBT has been set
                if (give) {
                    MCUtil.giveItemTo(user, markedStack, true);
                }
            }
        }
    }
    

    
    public static Optional<TrackerItemStack> getTargetedMarkedItem(IStandPower power, LivingEntity player) {
        return getTargetedEffect(getTargets(power, player), player).map(effect -> effect.getItemTracker(false));
    }
    
    public static Optional<GEItemMarkEffect> getTargetedEffect(List<Pair<GEItemMarkEffect, Vector3d>> targets, LivingEntity player) {
        // TODO return empty on specific conditions (holding a fitting item off-hand)
        
        Vector3d lookAngle = player.getLookAngle();
        Vector3d eyePos = player.getEyePosition(1.0F);
        Optional<GEItemMarkEffect> outlined = targets.stream()
                .map(e -> Pair.of(e, lookAngle.dot(e.getRight().subtract(eyePos).normalize())))
                .filter(withCos -> withCos.getValue() > 0.92388)
                .max(Comparator.comparingDouble(Pair::getValue))
                .map(Pair::getLeft)
                .map(Pair::getLeft);
        
        return outlined;
    }
    
    public static List<Pair<GEItemMarkEffect, Vector3d>> getTargets(IStandPower stand, LivingEntity player) {
        double range = ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().maxLifeformDistance;
        List<Pair<GEItemMarkEffect, Vector3d>> targets = stand.getContinuousEffects()
                .getEffects()
                .filter(effect -> effect.effectType == ModStandEffects.GE_ITEM_MARK.get())
                .map(effect -> (GEItemMarkEffect) effect)
                .filter(effect -> effect.getItemTracker(true) != null && effect.getItemTracker(false).getAtEntity(player.level) != player)
                .map(effect -> Pair.of(effect, effect.getItemTracker(false).markerPos(player.level, ClientUtil.getPartialTick())))
                .filter(entry -> entry.getRight() != null && entry.getRight().distanceToSqr(player.position()) < range * range)
                .collect(Collectors.toList());
//        return targets;
        // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! make sure only one marked item can exist at the same time
        return targets.size() > 1 ? Collections.singletonList(targets.get(targets.size() - 1)) : targets;
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        ItemStack item = power.getUser().getOffhandItem();
        if (!item.isEmpty() && GoldExperienceCreateLifeform.canGiveLifeTo(item)) {
            return new TranslationTextComponent(key + ".param", item.getDisplayName());
        }
        else {
            return super.getTranslatedName(power, key);
        }
    }
    
}
