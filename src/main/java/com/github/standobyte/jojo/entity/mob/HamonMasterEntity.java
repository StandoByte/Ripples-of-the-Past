package com.github.standobyte.jojo.entity.mob;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.INPC;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class HamonMasterEntity extends MobEntity implements INPC, IMobPowerUser, IEntityAdditionalSpawnData {
    private final INonStandPower hamonPower = new NonStandPower(this);
    @Deprecated
    private boolean reAddBaseHamon; // TODO remove in later versions
    
    public HamonMasterEntity(EntityType<? extends HamonMasterEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean removeWhenFarAway(double distanceFromPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            getPower().getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                HamonUtil.interactWithHamonTeacher(level, player, this, hamon);
            });
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
                        // FIXME !!!!!!!!!!!!!!!!!! sfx
                        HamonUtil.emitHamonSparkParticles(level, ClientUtil.getClientPlayer(), position(), 0.1F);
                        break;
                    }
                }
            }
        }
    }

    // FIXME !!!!!! (hamon) npc liquid walking
    @Override
    public boolean canStandOnFluid(Fluid fluid) {
        return hamonPower.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> 
        hamon.isSkillLearned(ModHamonSkills.LIQUID_WALKING.get()))
                .orElse(false);
    }

    @Override
    protected void lavaHurt() {}
    
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, 
            @Nullable ILivingEntityData additionalData, @Nullable CompoundNBT nbt) {
        addMasterHamon(hamonPower);
        setPersistenceRequired();
        
        return super.finalizeSpawn(world, difficulty, reason, additionalData, nbt);
    }
    
    @Deprecated
    private void restoreHamon() {
        if (reAddBaseHamon && !level.isClientSide()) {
            addMasterHamon(hamonPower);
            reAddBaseHamon = false;
        }
    }
    
    @Override
    public boolean isInvulnerableTo(DamageSource pDamageSource) {
        return super.isInvulnerableTo(pDamageSource) || pDamageSource.getMsgId().startsWith("hamon");
    }
    
    public void addMasterHamon(INonStandPower power) {
        hamonPower.givePower(ModPowers.HAMON.get());
        HamonData hamon = hamonPower.getTypeSpecificData(ModPowers.HAMON.get()).get();
        hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
        hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
        hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
        for (AbstractHamonSkill skill : JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues()) {
            if (skill.isBaseSkill()) {
                hamon.addHamonSkill(this, skill, false, false);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("HamonPower", hamonPower.writeNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HamonPower", MCUtil.getNbtId(CompoundNBT.class))) {
            hamonPower.readNBT(nbt.getCompound("HamonPower"));
        }
        reAddBaseHamon = true;
    }

    @Deprecated
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        restoreHamon();
    }

    @Deprecated
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Deprecated
    @Override
    public void readSpawnData(PacketBuffer additionalData) {}

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
