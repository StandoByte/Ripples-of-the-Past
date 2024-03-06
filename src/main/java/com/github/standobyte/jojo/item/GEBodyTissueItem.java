package com.github.standobyte.jojo.item;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.stand.GoldExperienceHeal;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GEBodyTissueItem extends Item {

    public GEBodyTissueItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack item = player.getItemInHand(hand);
        ActionConditionResult canUse = GoldExperienceHeal.canHeal(player, player, true, GoldExperienceHeal.MAX_REGEN_LVL - 1);
        if (canUse.isPositive()) {
            if (!world.isClientSide()) {
                Optional<LivingEntity> userCreator = getGEUser((ServerWorld) world, item);
                if (userCreator == null) { // item was created by other means, does not have data about the user
                    healAfterDelay(player, null);
                }
                else { // in this case only heal if the user is still in the world and has Gold Experience
                    userCreator.flatMap(e -> IStandPower.getStandPowerOptional(e).resolve()).ifPresent(userPower -> {
                        if (ModStandsInit.GOLD_EXPERIENCE_HEALING_ITEM.get().isUnlocked(userPower)) {
                            healAfterDelay(player, userPower);
                        }
                    });
                }
                
                if (!player.abilities.instabuild) {
                    item.shrink(1);
                }
            }
            return ActionResult.consume(item);
        }
        else {
            if (!world.isClientSide()) {
                ((ServerPlayerEntity) player).sendMessage(canUse.getWarning(), ChatType.GAME_INFO, Util.NIL_UUID);
            }
            return ActionResult.fail(item);
        }
    }
    
    private static void healAfterDelay(LivingEntity player, @Nullable IStandPower geUserPower) {
        GoldExperienceHeal.giveGEHealEffect(player, geUserPower, 100);
    }
    
    public static void onCreated(IStandPower userPower, ItemStack item) {
        if (userPower != null) {
            LivingEntity user = userPower.getUser();
            if (user != null) {
                item.getOrCreateTag().putUUID("GEUser", user.getUUID());
            }
        }
    }
    
    @Nullable
    private static Optional<LivingEntity> getGEUser(ServerWorld world, ItemStack item) {
        if (item.hasTag() && item.getTag().hasUUID("GEUser")) {
            UUID id = item.getTag().getUUID("GEUser");
            Entity entity = world.getEntity(id);
            if (entity instanceof LivingEntity) {
                return Optional.of((LivingEntity) entity);
            }
            else {
                return Optional.empty();
            }
        }
        
        return null;
    }
}
