package com.github.standobyte.jojo.entity.mob;

import java.util.Map;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class HamonMasterEntity extends MobEntity implements INPC, IMobPowerUser {
    private final INonStandPower hamonPower;
    private final HamonData hamon;
    
    public HamonMasterEntity(EntityType<? extends HamonMasterEntity> type, World world) {
        super(type, world);
        hamonPower = new NonStandPower(this);
        hamonPower.givePower(ModNonStandPowers.HAMON.get());
        hamon = hamonPower.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
        hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
        hamon.setHamonStatPoints(HamonSkill.HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
        hamon.setHamonStatPoints(HamonSkill.HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
        for (HamonSkill skill : HamonSkill.values()) {
            if (skill.getTechnique() == null) {
                hamon.learnHamonSkill(skill, false);
            }
        }
        hamonPower.setEnergy(hamonPower.getMaxEnergy());
        setPersistenceRequired();
    }

    @Override
    public boolean requiresCustomPersistence() { // if he somehow despawns again, i swear...
        return true;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getItem() == Items.NAME_TAG) {
            itemStack.interactLivingEntity(player, this, hand);
        }
        if (hand == Hand.MAIN_HAND) {
            HamonPowerType.interactWithHamonTeacher(level, player, this, 
                    getPower().getTypeSpecificData(ModNonStandPowers.HAMON.get()).get());
        }
        return super.mobInteract(player, hand);
    }
    
    @Override
    public INonStandPower getPower() {
        return hamonPower;
    }
    
    @Override
    public void tick() {
        super.tick();
        getPower().tick();
        if (level.isClientSide() && tickCount % 20 == 0) {
            if (!fluidHeight.isEmpty()) {
                for (Map.Entry<ITag<Fluid>, Double> entry : fluidHeight.object2DoubleEntrySet()) {
                    if (entry.getValue() > 0 && entry.getValue() < 0.4) {
                        HamonPowerType.createHamonSparkParticles(level, ClientUtil.getClientPlayer(), position(), 0.1F);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean canStandOnFluid(Fluid fluid) { // TODO Forge PR to add LivingStandOnFluidEvent or smth
        return hamon.isSkillLearned(HamonSkill.LAVA_WALKING) || hamon.isSkillLearned(HamonSkill.WATER_WALKING) && fluid.is(FluidTags.WATER);
    }

    @Override
    protected void lavaHurt() {}

    @Override
    protected void registerGoals() {} // TODO Hamon Master ai
    
    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ATTACK_SPEED, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.5D)
                .add(ForgeMod.SWIM_SPEED.get(), 2.0D);
    }
}
