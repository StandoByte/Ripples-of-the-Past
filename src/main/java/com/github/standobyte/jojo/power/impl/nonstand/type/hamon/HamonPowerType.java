package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;
import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SnakeMufflerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRunAwayPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion.CustomExplosionType;
import com.github.standobyte.jojo.util.mc.damage.explosion.HamonBlastExplosion;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TripWireBlock;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class HamonPowerType extends NonStandPowerType<HamonData> {

    public HamonPowerType(int color, HamonAction[] startingAttacks, HamonAction[] startingAbilities) {
        super(color, startingAttacks, startingAbilities, startingAbilities[0], HamonData::new);
    }
    
    @Override
    public boolean keepOnDeath(INonStandPower power) {
        return JojoModConfig.getCommonConfigInstance(false).keepHamonOnDeath.get();
    }
    
    @Override
    public void onClear(INonStandPower power) {
        power.getTypeSpecificData(this).ifPresent(hamon -> hamon.setBreathingLevel(0));
    }

    @Override
    public float getTargetResolveMultiplier(INonStandPower power, IStandPower attackingStand) {
        return 2;
    }

    // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!! (breath stability) move all hasEnergy and consumeEnergy calls into actions?
    @Override
    public boolean hasEnergy(INonStandPower power, float amount) {
        return amount == 0 || power.getEnergy() > 0 || power.getTypeSpecificData(this).get().getBreathStability() > 0;
    }

    @Override
    public boolean consumeEnergy(INonStandPower power, float amount) {
        return power.getTypeSpecificData(this).get().getHamonEnergyUsageEfficiency(amount, true) > 0;
    }
    
    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return power.getTypeSpecificData(this).get().isSkillLearned(ModHamonSkills.JUMP.get());
    }
    
    @Override
    public void onLeap(INonStandPower power) {
        power.getTypeSpecificData(this).get().hamonPointsFromAction(HamonStat.CONTROL, getLeapEnergyCost());
//        createHamonSparkParticles(power.getUser().level, null, power.getUser().position(), getLeapStrength(power) * 0.15F);
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        HamonData hamon = power.getTypeSpecificData(this).get();
        return hamon.isSkillLearned(ModHamonSkills.AFTERIMAGES.get()) ? 2F : 1.25F;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 100;
    }
    
    @Override
    public float getLeapEnergyCost() {
        return 100;
    }
    
    @Override
    public float getMaxEnergy(INonStandPower power) {
        return power.getTypeSpecificData(this).get().getMaxEnergy();
    }

    @Override
    public float tickEnergy(INonStandPower power) {
        return power.getTypeSpecificData(this).get().tickEnergy();
    }

    @Override
    public float getStaminaRegenFactor(INonStandPower power, IStandPower standPower) {
        return 1F + power.getTypeSpecificData(this).get().getBreathingLevel() * 0.01F;
    }

    @Override
    public void tickUser(LivingEntity user, INonStandPower power) {
        HamonData hamon = power.getTypeSpecificData(this).get();
        World world = user.level;
        hamon.tick();
        if (!world.isClientSide()) {
            if (user instanceof PlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) user;
                if (hamon.isSkillLearned(ModHamonSkills.ROPE_TRAP.get())) {
                    if (player.isOnGround() && player.isShiftKeyDown()) {
                        BlockPos pos = player.blockPosition();
                        if (player.level.isEmptyBlock(pos)) {
                            PlayerInventory inventory = player.inventory;
                            for (int i = 8; i >= 0; i--) {
                                ItemStack stack = inventory.items.get(i);
                                if (!stack.isEmpty() && stack.getItem() == Items.STRING) {
                                    BlockItemUseContext ctx = new BlockItemUseContext(player, Hand.OFF_HAND, stack, 
                                            new BlockRayTraceResult(Vector3d.atCenterOf(pos), Direction.UP, pos, false));
                                    BlockState state = Blocks.TRIPWIRE.getStateForPlacement(ctx);
                                    if (state != null && !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(
                                            world.dimension(), world, pos.below()), Direction.UP) && world.setBlock(pos, state, 3) && !player.abilities.instabuild) {
                                        stack.shrink(1);
                                    }
                                    break;
                                }
                            }
                        }
                    }
//                    if (player.fishing != null) {
//                        ItemStack mainHandItem = player.getMainHandItem();
//                        if (mainHandItem.getItem() instanceof FishingRodItem) {
//                            Entity hooked = player.fishing.getHookedIn();
//                            if (hooked != null) {
//                                float energyCost = 30;
//                                if (power.consumeEnergy(energyCost)) {
//                                    ModDamageSources.dealHamonDamage(hooked, 0.0125F, player.fishing, player);
//                                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, energyCost);
//                                }
//                                else {
//                                    player.fishing.retrieve(mainHandItem);
//                                }
//                            }
//                        }
//                    }
                }
            }
        }
        
        if (!world.isClientSide() || user.is(ClientUtil.getClientPlayer())) {
            if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                if (user.isSprinting()) {
                    Vector3d vecBehind = Vector3d.directionFromRotation(0, 180F + user.yRot).scale(8D);
                    AxisAlignedBB aabb = new AxisAlignedBB(user.position().subtract(0, 2D, 0), user.position().add(vecBehind.x, 2D, vecBehind.z));
                    List<LivingEntity> entitiesBehind = world.getEntitiesOfClass(LivingEntity.class, aabb, entity -> entity != user)
                            .stream().filter(entity -> !(entity instanceof StandEntity)).collect(Collectors.toList());
                    if (!entitiesBehind.isEmpty()) {
                        if (world.isClientSide()) {
                            if (user instanceof ClientPlayerEntity && ((ClientPlayerEntity) user).sprintTime == 0) {
                                PacketManager.sendToServer(new ClRunAwayPacket());
                            }
                        }
                        else {
                            EffectInstance speed = user.getEffect(Effects.MOVEMENT_SPEED);
                            int speedAmplifier = speed != null && speed.getDuration() > 100 ? speed.getAmplifier() + 2 : 1;
                            user.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 100, speedAmplifier, false, false, true));
                        }
                    }
                }
            }
            if (user instanceof PlayerEntity) {
                hamon.tickExercises((PlayerEntity) user);
            }
        }
    }
    
    @Override
    public void onNewDay(LivingEntity user, INonStandPower power, long prevDay, long day) {
        if (user instanceof PlayerEntity) {
            HamonData hamon = power.getTypeSpecificData(this).get();
            hamon.breathingTrainingDay((PlayerEntity) user);
        }
    }

    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return newType == ModPowers.VAMPIRISM.get();
    }
    
    public static boolean ropeTrap(LivingEntity user, BlockPos pos, BlockState blockState, World world, INonStandPower power, HamonData hamon) {
        if (hamon.isSkillLearned(ModHamonSkills.ROPE_TRAP.get())) {
            createChargedCobweb(user, pos, blockState, world, 64, null, power, 
                    40 + (int) ((float) (160 * hamon.getHamonStrengthLevel()) / (float) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(STRING_CHARGE_COST, false)), 
                    0.02F * hamon.getHamonDamageMultiplier() * hamon.getActionEfficiency(STRING_CHARGE_COST, false), hamon);
            return true;
        }
        return false;
    }
    
    private static final Map<BooleanProperty, Direction> PROPERTY_TO_DIRECTION = ImmutableMap.of(
            TripWireBlock.EAST, Direction.EAST, 
            TripWireBlock.SOUTH, Direction.SOUTH, 
            TripWireBlock.WEST, Direction.WEST, 
            TripWireBlock.NORTH, Direction.NORTH);
    private static final float STRING_CHARGE_COST = 10;
    private static void createChargedCobweb(LivingEntity user, BlockPos pos, BlockState blockState, World world, 
            int range, @Nullable Direction from, INonStandPower power, int chargeTicks, float charge, HamonData hamon) {
        if (range > 0 && blockState.getBlock() == Blocks.TRIPWIRE && power.consumeEnergy(STRING_CHARGE_COST)) {
            hamon.hamonPointsFromAction(HamonStat.CONTROL, STRING_CHARGE_COST / 2);
            Map<Property<?>, Comparable<?>> values = blockState.getValues();
            List<Direction> directions = new ArrayList<Direction>();
            for (Map.Entry<BooleanProperty, Direction> entry: PROPERTY_TO_DIRECTION.entrySet()) {
                if (entry.getValue() != from && (Boolean) values.get(entry.getKey())) {
                    directions.add(entry.getValue());
                }
            }
            world.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            HamonBlockChargeEntity chargeEntity = new HamonBlockChargeEntity(world, pos);
            chargeEntity.setCharge(charge, chargeTicks, user, STRING_CHARGE_COST / 2);
            world.addFreshEntity(chargeEntity);
            for (Direction direction : directions) {
                BlockPos nextPos = pos.relative(direction);
                createChargedCobweb(user, nextPos, world.getBlockState(nextPos), world, 
                        range - 1, direction.getOpposite(), power, chargeTicks, charge, hamon);
            }
        }
    }
    
    public static void updateCheatDeathEffect(LivingEntity user) {
        user.addEffect(new EffectInstance(ModEffects.CHEAT_DEATH.get(), 120000, 0, false, false, true));
    }
    
    public static boolean snakeMuffler(LivingEntity target, DamageSource dmgSource, float dmgAmount) {
        if (!target.level.isClientSide() && target.canUpdate() && target.isOnGround()) {
            Entity attacker = dmgSource.getEntity();
            if (attacker != null && dmgSource.getDirectEntity() == attacker && attacker instanceof LivingEntity
                    && target instanceof PlayerEntity && target.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get()) {
                LivingEntity livingAttacker = (LivingEntity) attacker;
                PlayerEntity playerTarget = (PlayerEntity) target;
                if (!playerTarget.getCooldowns().isOnCooldown(ModItems.SATIPOROJA_SCARF.get())) {
                    INonStandPower power = INonStandPower.getPlayerNonStandPower(playerTarget);
                    float energyCost = 500F;
                    if (power.hasEnergy(energyCost)) {
                        if (power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                            if (hamon.isSkillLearned(ModHamonSkills.SNAKE_MUFFLER.get())) {
                                playerTarget.getCooldowns().addCooldown(ModItems.SATIPOROJA_SCARF.get(), 80);
                                float efficiency = hamon.getActionEfficiency(energyCost, false);
                                if (efficiency == 1 || efficiency >= dmgAmount / target.getMaxHealth()) {
                                    JojoModUtil.sayVoiceLine(target, ModSounds.LISA_LISA_SNAKE_MUFFLER.get());
                                    power.consumeEnergy(energyCost);
                                    DamageUtil.dealHamonDamage(attacker, 0.75F, target, null);
                                    livingAttacker.addEffect(new EffectInstance(Effects.GLOWING, 200));
                                    SnakeMufflerEntity snakeMuffler = new SnakeMufflerEntity(target.level, target);
                                    snakeMuffler.setEntityToJumpOver(attacker);
                                    target.level.addFreshEntity(snakeMuffler);
                                    snakeMuffler.attachToBlockPos(target.blockPosition());
                                    return true;
                                }
                            }
                            return false;
                        }).orElse(false)) return true;
                    }
                }
            }
        }
        return false;
    }
    

    @Nullable
    public static Set<AbstractHamonSkill> nearbyTeachersSkills(LivingEntity learner) {
        Set<AbstractHamonSkill> skills = new HashSet<>();
        if (MCUtil.entitiesAround(LivingEntity.class, learner, 3D, false, 
                entity -> INonStandPower.getNonStandPowerOptional(entity).map(power -> 
                power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                    hamon.getLearnedSkills().forEach(skill -> {
                        if (skill.requiresTeacher()) {
                            skills.add(skill);
                        }
                    });
                    return true;
                }).orElse(false)).orElse(false)).isEmpty()) {
            return null;
        }
        return skills;
    }
    
    public static void interactWithHamonTeacher(World world, PlayerEntity player, LivingEntity teacher, HamonData teacherHamon) {
        INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
            Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
            if (!hamonOptional.isPresent() && !world.isClientSide()) {
                if (teacher instanceof PlayerEntity) {
                    teacherHamon.addNewPlayerLearner(player);
                }
                else {
                    startLearningHamon(world, player, power, teacher, teacherHamon);
                }
            }
            hamonOptional.ifPresent(hamon -> {
                if (world.isClientSide()) {
                    ClientUtil.openHamonTeacherUi();
                }
                else {
                    if (player.abilities.instabuild) {
                        hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
                        hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
                        hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
                        hamon.tcsa(false);
                    }
                }
            });
        });
    }
    
    public static void startLearningHamon(World world, PlayerEntity player, INonStandPower playerPower, LivingEntity teacher, HamonData teacherHamon) {
        if (playerPower.canGetPower(ModPowers.HAMON.get()) && teacherHamon.characterIs(ModHamonSkills.CHARACTER_ZEPPELI.get())) {
            JojoModUtil.sayVoiceLine(teacher, ModSounds.ZEPPELI_FORCE_BREATH.get());
            teacher.swing(Hand.MAIN_HAND, true);
            if (player.getRandom().nextFloat() <= 0.01F) {
                player.hurt(DamageSource.GENERIC, 4.0F);
                player.setAirSupply(0);
                return;
            }
            else {
                player.hurt(DamageSource.GENERIC, 0.1F);
            }
        } 
        if (playerPower.givePower(ModPowers.HAMON.get())) {
            playerPower.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (player.abilities.instabuild) {
                    hamon.setBreathingLevel(HamonData.MAX_BREATHING_LEVEL);
                    hamon.setHamonStatPoints(HamonStat.STRENGTH, HamonData.MAX_HAMON_POINTS, true, true);
                    hamon.setHamonStatPoints(HamonStat.CONTROL, HamonData.MAX_HAMON_POINTS, true, true);
                    hamon.tcsa(false);
                }
                player.sendMessage(new TranslationTextComponent("jojo.chat.message.learnt_hamon"), Util.NIL_UUID);
                PlayerUtilCap utilCap = player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseThrow(() -> new IllegalStateException());
                utilCap.sendNotification(OneTimeNotification.HAMON_WINDOW, 
                        new TranslationTextComponent("jojo.chat.message.hamon_window_hint", new KeybindTextComponent("jojo.key.hamon_skills_window")));
            });
        }
        else {
            player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.cant_learn_hamon"), true);
        }
        return;
    }
    

    
    public static void cancelCactusDamage(LivingAttackEvent event) {
        if (event.getSource() == DamageSource.CACTUS || event.getSource() == DamageSource.SWEET_BERRY_BUSH) {
            LivingEntity entity = event.getEntityLiving();
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    if (power.consumeEnergy(event.getAmount() * 0.5F)) {
                        HamonPowerType.emitHamonSparkParticles(entity.level, null, entity.getX(), entity.getY(0.5), entity.getZ(), 0.1F);
                        event.setCanceled(true);
                    }
                });
            });
        }
    }

    public static void hamonPerksOnDeath(LivingEntity dead) {
        if (JojoModConfig.getCommonConfigInstance(false).keepHamonOnDeath.get() && !dead.level.getLevelData().isHardcore()) return;
        INonStandPower.getNonStandPowerOptional(dead).ifPresent(power -> {
            power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                if (hamon.isSkillLearned(ModHamonSkills.CRIMSON_BUBBLE.get())) {
                    CrimsonBubbleEntity bubble = new CrimsonBubbleEntity(dead.level);
                    ItemStack heldItem = dead.getMainHandItem();
                    if (!heldItem.isEmpty()) {
                        dead.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        ItemEntity item = new ItemEntity(dead.level, dead.getX(), dead.getEyeY() - 0.3D, dead.getZ(), heldItem);
                        item.setPickUpDelay(2);
                        dead.level.addFreshEntity(item);
                        bubble.putItem(item);
                    }
                    dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), 
                            ModSounds.CAESAR_LAST_HAMON.get(), dead.getSoundSource(), 1.0F, 1.0F);
                    bubble.moveTo(dead.getX(), dead.getEyeY(), dead.getZ(), dead.yRot, dead.xRot);
                    bubble.setHamonPoints(hamon.getHamonStrengthPoints(), hamon.getHamonControlPoints());
                    dead.level.addFreshEntity(bubble);
                }
                else if (hamon.isSkillLearned(ModHamonSkills.DEEP_PASS.get())) {
                    PlayerEntity closestHamonUser = MCUtil.entitiesAround(PlayerEntity.class, dead, 8, false, player -> 
                    INonStandPower.getNonStandPowerOptional(player).map(pwr -> pwr.getType() == ModPowers.HAMON.get()).orElse(false))
                            .stream()
                            .min(Comparator.comparingDouble(player -> player.distanceToSqr(dead)))
                            .orElse(null);
                    if (closestHamonUser != null) {
                        dead.level.playSound(null, dead.getX(), dead.getY(), dead.getZ(), 
                                ModSounds.ZEPPELI_DEEP_PASS.get(), dead.getSoundSource(), 1.0F, 1.0F);
                        HamonData receiverHamon = INonStandPower.getPlayerNonStandPower(closestHamonUser).getTypeSpecificData(ModPowers.HAMON.get()).get();
                        if (receiverHamon.characterIs(ModHamonSkills.CHARACTER_JONATHAN.get())) {
                            JojoModUtil.sayVoiceLine(closestHamonUser, ModSounds.JONATHAN_DEEP_PASS_REACTION.get());
                        }
                        receiverHamon.setHamonStatPoints(HamonStat.STRENGTH, 
                                receiverHamon.getHamonStrengthPoints() + hamon.getHamonStrengthPoints(), true, false);
                        receiverHamon.setHamonStatPoints(HamonStat.CONTROL, 
                                receiverHamon.getHamonControlPoints() + hamon.getHamonControlPoints(), true, false);
                        if (closestHamonUser instanceof ServerPlayerEntity) {
                            ModCriteriaTriggers.LAST_HAMON.get().trigger((ServerPlayerEntity) closestHamonUser, dead);
                        }
                        createHamonSparkParticlesEmitter(closestHamonUser, 1.0F);
                    }
                }
            });
        });
    }
    
    public static void hamonExplosion(World world, @Nullable Entity source, @Nullable Entity hamonUser, 
            Vector3d position, float radius, float damage) {
        HamonBlastExplosion hamonBlast = new HamonBlastExplosion(world, source, null, 
                position.x, position.y, position.z, radius);
        hamonBlast.setHamonDamage(damage);
        CustomExplosion.explodePreCreated(hamonBlast, world, CustomExplosionType.HAMON);
    }
    
    
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, 
            double x, double y, double z, float intensity, @Nullable SoundEvent hamonSound) {
        if (intensity > 0) {
            intensity = Math.min(intensity, 4F);
            int count = Math.max((int) (intensity * 16.5F), 1);
            if (!world.isClientSide()) {
                ((ServerWorld) world).sendParticles(ModParticles.HAMON_SPARK.get(), x, y, z, count, 0.05, 0.05, 0.05, 0.25);
            }
            else if (clientHandled == ClientUtil.getClientPlayer()) {
                ClientUtil.createHamonSparkParticles(x, y, z, count);
            }
            if (hamonSound != null) {
                world.playSound(clientHandled, x, y, z, hamonSound, 
                        SoundCategory.AMBIENT, intensity * 2, 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
            }
        }
    }
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, double x, double y, double z, float intensity) {
        emitHamonSparkParticles(world, clientHandled, x, y, z, intensity, ModSounds.HAMON_SPARK.get());
    }
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, Vector3d vec, float intensity) {
        emitHamonSparkParticles(world, clientHandled, vec.x, vec.y, vec.z, intensity);
    }
    
    public static void emitHamonSparkParticles(World world, @Nullable PlayerEntity clientHandled, Vector3d vec, float intensity, @Nullable SoundEvent hamonSound) {
        emitHamonSparkParticles(world, clientHandled, vec.x, vec.y, vec.z, intensity, hamonSound);
    }
    
    
    public static void createHamonSparkParticlesEmitter(Entity entity, float intensity) {
        createHamonSparkParticlesEmitter(entity, intensity, 1, ModParticles.HAMON_SPARK.get());
    }
    
    public static void createHamonSparkParticlesEmitter(Entity entity, float intensity, float soundVolumeMultiplier, IParticleData hamonParticle) {
        if (intensity > 0) {
            intensity = Math.min(intensity, 4F);
            World world = entity.level;
            if (!world.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonParticlesPacket(entity.getId(), intensity, soundVolumeMultiplier, 
                        hamonParticle != ModParticles.HAMON_SPARK.get() ? hamonParticle : null), entity);
            }
            else {
                float volume = intensity * 2 * soundVolumeMultiplier;
                for (int i = (int) (intensity * 9.5F); i >= 0; i--) {
                    ClientUtil.createParticlesEmitter(entity, hamonParticle, Math.max(1, (int) (intensity * 9.5) - i));
                    if (i % 2 == 0 && i < 4) {
                        ClientTickingSoundsHelper.playHamonSparksSound(entity, Math.min(volume, 1.0F), 1.0F + (world.random.nextFloat() - 0.5F) * 0.15F);
                    }
                }
            }
        }
    }
}
