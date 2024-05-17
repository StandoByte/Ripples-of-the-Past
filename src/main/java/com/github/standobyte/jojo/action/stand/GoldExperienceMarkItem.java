package com.github.standobyte.jojo.action.stand;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class GoldExperienceMarkItem extends StandAction {

    public GoldExperienceMarkItem(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide()) {
            ItemStack heldItem = user.getItemInHand(Hand.OFF_HAND);
            if (!heldItem.isEmpty() && user instanceof ServerPlayerEntity) {
                ItemStack markedStack;
                if (heldItem.getCount() == 1) {
                    markedStack = heldItem;
                    user.setItemInHand(Hand.OFF_HAND, user.getItemInHand(Hand.MAIN_HAND));
                    user.setItemInHand(Hand.MAIN_HAND, heldItem);
                }
                else {
                    markedStack = heldItem.split(1);
                    if (user.getItemInHand(Hand.MAIN_HAND).isEmpty()) {
                        user.setItemInHand(Hand.MAIN_HAND, markedStack);
                    }
                    else {
                        MCUtil.giveItemTo(user, markedStack, true);
                    }
                }
                user.stopUsingItem();
                TrackerItemStack itemTracker = TrackerItemStack.setTracked(markedStack, (ServerPlayerEntity) user);
                if (itemTracker != null) {
                    GEItemMarkEffect effect = new GEItemMarkEffect(itemTracker.getTrackerId());
                    effect.withStand(power);
                    power.getContinuousEffects().addEffect(effect);
                }
            }
        }
    }
    
    
    
    public static Optional<TrackerItemStack> getTargetedMarkedItem(IStandPower power, LivingEntity player) {
        List<Pair<GEItemMarkEffect, Vector3d>> targets = getTargets(power, player);
        
        Vector3d lookAngle = player.getLookAngle();
        Vector3d eyePos = player.getEyePosition(1.0F);
        Optional<GEItemMarkEffect> outlined = targets.stream().max(Comparator.comparingDouble(
                e -> lookAngle.dot(e.getRight().subtract(eyePos).normalize())))
                .map(pair -> pair.getLeft());
        
        return outlined.map(effect -> effect.getItemTracker());
    }
    
    public static List<Pair<GEItemMarkEffect, Vector3d>> getTargets(IStandPower stand, LivingEntity player) {
        double rangeSq = GoldExperienceRevertLifeform.MARKER_DISTANCE * GoldExperienceRevertLifeform.MARKER_DISTANCE;
        List<Pair<GEItemMarkEffect, Vector3d>> targets = stand.getContinuousEffects()
                .getEffects()
                .filter(effect -> effect.effectType == ModStandEffects.GE_ITEM_MARK.get())
                .map(effect -> (GEItemMarkEffect) effect)
                .filter(effect -> effect.getItemTracker() != null && effect.getItemTracker().getAtEntity(player.level) != player)
                .map(effect -> Pair.of(effect, effect.getItemTracker().markerPos(player.level, ClientUtil.getPartialTick())))
                .filter(entry -> entry.getRight() != null && entry.getRight().distanceToSqr(player.position()) < rangeSq)
                .collect(Collectors.toList());
        return targets;
    }
    
}
