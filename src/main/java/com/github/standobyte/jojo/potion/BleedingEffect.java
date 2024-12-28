package com.github.standobyte.jojo.potion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import com.github.standobyte.jojo.action.stand.effect.DriedBloodDrops;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.block.StoneMaskBlock;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.item.StoneMaskItem;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.tileentity.StoneMaskTileEntity;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class BleedingEffect extends Effect implements IApplicableEffect {
    private static final float HP_REDUCTION = 4;
    public static final UUID ATTRIBUTE_MODIFIER_ID = UUID.fromString("1588be77-b81b-4eb0-a745-a8912de51e72");
    
    public BleedingEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
        getAttributeModifiers().put(Attributes.MAX_HEALTH, new AttributeModifier(ATTRIBUTE_MODIFIER_ID, 
                this::getDescriptionId, -HP_REDUCTION, AttributeModifier.Operation.ADDITION));
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(entity, pAttributeMap, pAmplifier);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }
    
    public static void onAddedBleeding(LivingEntity entity, int pAmplifier) {
        if (!entity.level.isClientSide()) {
            IStandPower.getStandPowerOptional(entity).ifPresent(power -> {
                if (ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get().isUnlocked(power)) {
                    power.setCooldownTimer(ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get(), 0);
                }
            });
            
            entity.level.broadcastEntityEvent(entity, (byte) MCUtil.EntityEvents.HURT);
            
            Vector3d particlesPos = entity.getCapability(LivingUtilCapProvider.CAPABILITY).resolve().map(data -> data.bleedingParticlesPos)
                    .orElse(entity.getBoundingBox().getCenter());
            splashBlood(entity.level, particlesPos, pAmplifier + 1, HP_REDUCTION * (pAmplifier + 1), 
                    OptionalInt.of(pAmplifier), Optional.of(entity));
        }
    }
    
    public static int limitAmplifier(LivingEntity entity, int amplifier) {
        return Math.min(amplifier, Math.max(
                (int) (entity.getAttributeBaseValue(Attributes.MAX_HEALTH) / HP_REDUCTION) - 2, 
                (int) (getMaxHealthWithoutBleeding(entity) / HP_REDUCTION) - 2));
    }
    
    public static float getMaxHealthWithoutBleeding(LivingEntity entity) {
        return (float) MCUtil.calcValueWithoutModifiers(entity.getAttribute(Attributes.MAX_HEALTH), ATTRIBUTE_MODIFIER_ID);
    }
    
    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.canBleed(entity);
    }
    
    
    public static void setNextParticlesPos(LivingEntity entity, Vector3d pos) {
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(data -> data.bleedingParticlesPos = pos);
    }
    
    public static boolean splashBlood(World world, Vector3d splashPos, double radius, 
            float bleedAmount, OptionalInt bleedingEffectLvl, Optional<LivingEntity> ownerEntity) {
        if (world.isClientSide()) {
            return false;
        }

        AxisAlignedBB aabb = new AxisAlignedBB(splashPos.subtract(radius, radius, radius), splashPos.add(radius, radius, radius));
        List<Vector3d> particlePos = new ArrayList<>();
        List<LivingEntity> entitiesAround = world.getEntitiesOfClass(LivingEntity.class, aabb, 
                EntityPredicates.ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS)
                .and(entity -> {
                    return world.clip(new RayTraceContext(splashPos, entity.getBoundingBox().getCenter(), 
                          RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity))
                          .getType() == RayTraceResult.Type.MISS;
                }));
        for (LivingEntity entity : entitiesAround) {
            if (dropBloodOnEntity(ownerEntity, entity, bleedAmount)) {
                particlePos.add(entity.getEyePosition(1.0F));
            }
        }

        BlockPos blockPos = new BlockPos(splashPos);
        BlockPos.betweenClosedStream(blockPos.offset(-radius, -radius, -radius), blockPos.offset(radius, radius, radius))
        .filter(pos -> world.getBlockState(pos).getBlock() == ModBlocks.STONE_MASK.get())
        .forEach(pos -> {
            BlockState blockState = world.getBlockState(pos);
            world.playSound(null, pos, ModSounds.STONE_MASK_ACTIVATION.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            switch (blockState.getValue(HorizontalFaceBlock.FACE)) {
            case FLOOR:
                TileEntity tileEntity = world.getBlockEntity(pos);
                if (tileEntity instanceof StoneMaskTileEntity) {
                    ((StoneMaskTileEntity) tileEntity).activate();
                }
                particlePos.add(Vector3d.atBottomCenterOf(pos));
                break;
            default:
                Block.popResource(world, pos, StoneMaskBlock.getItemFromBlock(world, pos, blockState));
                world.removeBlock(pos, false);
                particlePos.add(Vector3d.atCenterOf(pos));
                break;
            }
        });

        if (!particlePos.isEmpty()) {
            int count = Math.min((int) (bleedAmount * 5), 50);
            particlePos.forEach(posTo -> {
                PacketManager.sendToTrackingChunk(new BloodParticlesPacket(splashPos, posTo, 0.375f, count, 
                        ownerEntity.map(Entity::getId).orElse(-1)), world.getChunkAt(blockPos));
            });
        }
        else {
            bleedingEffectLvl.ifPresent(effectLvl -> {
                float speed = (Math.min(effectLvl, 3) + 1) * 0.09375f;
                int count = 10 * (effectLvl + 1) * (effectLvl + 1);
                PacketManager.sendToTrackingChunk(new BloodParticlesPacket(splashPos, speed, count, 
                        ownerEntity.map(Entity::getId).orElse(-1)), world.getChunkAt(blockPos));
            });
        }
        
        return !particlePos.isEmpty();
    }
    
    private static boolean dropBloodOnEntity(Optional<LivingEntity> bleedingEntity, LivingEntity nearbyEntity, float bleedAmount) {
        boolean dropped = false;
        
        ItemStack headArmor = nearbyEntity.getItemBySlot(EquipmentSlotType.HEAD);
        if (headArmor.getItem() instanceof StoneMaskItem && applyStoneMask(nearbyEntity, headArmor)) {
            dropped = true;
        }

        dropped |= GeneralUtil.orElseFalse(bleedingEntity, entity -> {
            return nearbyEntity.getRandom().nextFloat() < bleedAmount / 5 && 
                    GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(entity), (IStandPower power) -> {
                        if (ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get().isUnlocked(power) && CDBloodCutterEntity.canHaveBloodDropsOn(nearbyEntity, power)) {
                            DriedBloodDrops bloodDrops = power.getContinuousEffects().getOrCreateEffect(ModStandEffects.DRIED_BLOOD_DROPS.get(), nearbyEntity);
                            return bloodDrops.tickCount > 0;
                        }
                        return false;
                    });
        });
        
        return dropped;
    }

    public static boolean applyStoneMask(LivingEntity entity, ItemStack headStack) {
        if (entity.level.getDifficulty() == Difficulty.PEACEFUL) {
            if (entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) entity).displayClientMessage(
                        new TranslationTextComponent("jojo.chat.message.stone_mask_peaceful"), true);
            }
            return false;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            return INonStandPower.getNonStandPowerOptional(player).map(power -> {
                //Prevents aja-stone mask to work on non pillar men
                Optional<PillarmanData> pillarmanOptional = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get());
                
                if (headStack.getItem() == ModItems.AJA_STONE_MASK.get()) {
                    if (!pillarmanOptional.isPresent()) {
                        if (entity instanceof ServerPlayerEntity) {
                            ModCriteriaTriggers.MASK_SUICIDE.get().trigger((ServerPlayerEntity) entity);
                        }
                        entity.hurt(DamageUtil.STONE_MASK, 1000);
                        return false;
                    } else {
                        PillarmanData pillarman = pillarmanOptional.get();
                        if (pillarmanOptional.get().getEvolutionStage() < 3) {
                            pillarman.setEvolutionStage(3);
                            //Gives a random Mode
                            switch (entity.getRandom().nextInt(3)) {
                            case 0:
                                pillarman.setMode(Mode.WIND);
                                break;
                            case 1:
                                pillarman.setMode(Mode.HEAT);
                                break;
                            case 2:
                                pillarman.setMode(Mode.LIGHT);
                                break;
                            }
                            applyMaskEffect(entity, headStack);
                            return true;
                        }
                    }
                }
                else /*if (headStack.getItem() == ModItems.STONE_MASK.get())*/ {
                    if (pillarmanOptional.isPresent()) {
                        PillarmanData pillarman = pillarmanOptional.get();
                        if (pillarman.getEvolutionStage() < 2) {
                            pillarman.setEvolutionStage(2);
                            applyMaskEffect(entity, headStack);
                            return true;
                        }
                    }
                    else if (power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).map(
                            vamp -> !vamp.isVampireAtFullPower()).orElse(false) || power.givePower(ModPowers.VAMPIRISM.get())) {
                        if (power.getType() == ModPowers.VAMPIRISM.get()) {
                            power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).get().setVampireFullPower(true);
                            applyMaskEffect(entity, headStack);
                            return true;
                        }
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }
    
    private static void applyMaskEffect(LivingEntity entity, ItemStack headStack) {
        entity.level.playSound(null, entity, ModSounds.STONE_MASK_ACTIVATION_ENTITY.get(), entity.getSoundSource(), 1.0F, 1.0F);
        StoneMaskItem.setActivatedArmorTexture(headStack); // TODO light beams on stone mask activation
        headStack.hurtAndBreak(1, entity, stack -> {});
    }
}
