package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class GoldExperienceRevertLifeform extends StandAction {
    public static final double MARKER_DISTANCE = 64;

    public GoldExperienceRevertLifeform(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public Action<IStandPower> getVisibleAction(IStandPower power, ActionTarget target) {
        Action<IStandPower> action = super.getVisibleAction(power, target);
        if (action == this && 
                !StandEffectsTracker.getEffectsOfType(power, ModStandEffects.GE_CREATED_LIFEFORM.get(), MARKER_DISTANCE)
                .findAny().isPresent()) {
            action = null;
        }
        return action;
    }
    
//    @Override
//    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
//        if (user.level.isClientSide() && getChosenEntityType(ClientUtil.getClientPlayer()) == null) {
//            return ActionConditionResult.NEGATIVE;
//        }
//        
//        return ActionConditionResult.POSITIVE;
//    }
//    
//    @Override
//    public void clWriteExtraData(PacketBuffer buf) {
//        NetworkUtil.writeOptionally(buf, 
//                getChosenEntityType(ClientUtil.getClientPlayer()), 
//                type -> buf.writeRegistryId(type));
//    }
//    
//    @Nullable
//    private static EntityType<?> getChosenEntityType(PlayerEntity player) {
//        return player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve()
//                .map(playerData -> playerData.getGEChosenLifeformType()).orElse(null);
//    }
//    
    
    @Override
    public void clWriteExtraData(PacketBuffer buf) {
        clWriteTargetedStandEffect(buf, ModStandEffects.GE_CREATED_LIFEFORM.get(), MARKER_DISTANCE);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        if (!world.isClientSide() && extraInput != null) {
            readTargetedStandEffect(extraInput, power, ModStandEffects.GE_CREATED_LIFEFORM.get())
            .ifPresent(effect -> {
                effect.remove();
            });
        }
    }
    
    
//    @Override
//    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
//        EntityType<?> chosenEntityType = getChosenEntityType(ClientUtil.getClientPlayer());
//        if (chosenEntityType != null) {
//            return new TranslationTextComponent(key + ".param", chosenEntityType.getDescription());
//        }
//        else {
//            return super.getTranslatedName(power, key);
//        }
//    }
}
