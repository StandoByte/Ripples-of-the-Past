package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class GoldExperienceToothLifeform extends StandEntityActionModifier {
    
    public GoldExperienceToothLifeform(Builder builder) {
        super(builder);
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get().isUnlocked(power);
    }
    
    @Override
    public void clWriteExtraData(PacketBuffer buf) {
        NetworkUtil.writeOptionally(buf, 
                GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer()), 
                type -> buf.writeRegistryId(type));
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        boolean triggerEffect = task.getTicksLeft() <= 1;
        if (task.getAdditionalData().isEmpty(TriggeredFlag.class) && task.getTarget().getType() == TargetType.ENTITY) {
            
            
            if (triggerEffect) {
                task.getAdditionalData().push(TriggeredFlag.class, new TriggeredFlag());
            }
        }
    }
    
    @Override
    public IFormattableTextComponent getTranslatedName(IStandPower power, String key) {
        EntityType<?> chosenEntityType = GoldExperienceCreateLifeform.getChosenEntityType(ClientUtil.getClientPlayer());
        if (chosenEntityType != null) {
            return new TranslationTextComponent(key + ".param", chosenEntityType.getDescription());
        }
        else {
            return super.getTranslatedName(power, key);
        }
    }
}
