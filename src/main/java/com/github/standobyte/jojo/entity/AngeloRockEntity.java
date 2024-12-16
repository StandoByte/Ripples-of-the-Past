package com.github.standobyte.jojo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.capability.chunk.ChunkCap.PrevBlockInfo;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.stand.ModStands;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.TimerQueue;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

// TODO (angelo) if the Crazy D user dies and this is in the process of being made, break this
public class AngeloRockEntity extends Entity implements IEntityAdditionalSpawnData {
    protected static final DataParameter<Optional<BlockPos>> DATA_ATTACH_POS_ID = EntityDataManager.defineId(AngeloRockEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    protected static final DataParameter<Boolean> CREATION_COMPLETE = EntityDataManager.defineId(AngeloRockEntity.class, DataSerializers.BOOLEAN);
    protected static final DataParameter<Float> DAMAGE = EntityDataManager.defineId(AngeloRockEntity.class, DataSerializers.FLOAT);
    private static final int CREATION_ANIM_LEN = 40;
    private int creationAnimTicks;
    private Map<BlockPos, PrevBlockInfo> angeloRockBlocks = new HashMap<>();
    private boolean startedSound;
    private EntityOwnerResolver angeloEntity = new EntityOwnerResolver();
    /* fuck it, sure, yeah */ private MobEntity mob;
    private boolean useMobHurtSound;
    private TimerQueue responseSoundTimer = new TimerQueue(false);
    
    
    public AngeloRockEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
    }
    
    public static AngeloRockEntity turnIntoRock(World world, Entity entity, Vector3d rockPos, float yRot, 
            PrevBlockInfo... angeloRockBlocks) {
        if (world.isClientSide()) {
            return null;
        }
        
        AngeloRockEntity angeloRock = new AngeloRockEntity(ModEntityTypes.ANGELO_ROCK.get(), world);
        angeloRock.yRot = yRot;
        angeloRock.setPos(rockPos.x, rockPos.y, rockPos.z);
        if (entity instanceof MobEntity) {
            angeloRock.mob = (MobEntity) entity;
            angeloRock.useMobHurtSound = CommonReflection.getAmbientSound(angeloRock.mob) == null;
        }
        
        angeloRock.creationAnimTicks = CREATION_ANIM_LEN;
        if (entity instanceof LivingEntity) {
            angeloRock.angeloEntity.setOwner(entity);
        }
        
        if (angeloRockBlocks != null) {
            for (PrevBlockInfo block : angeloRockBlocks) {
                if (block != null) {
                    angeloRock.angeloRockBlocks.put(block.pos, block);
                }
            }
        }
        
        world.addFreshEntity(angeloRock);
        return angeloRock;
    }
    
    @Override
    public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
        if (creationAnimTicks > 0) {
            return ActionResultType.FAIL;
        }
        if (level.isClientSide()) {
            return ActionResultType.SUCCESS;
        }
        else {
            if (mob != null) {
                mob.setPos(getX(), getY(), getZ());
            }
            SoundEvent voiceline = IStandPower.getStandPowerOptional(pPlayer).resolve().map(power -> {
                StandType<?> stand = power.getType();
                if (stand == ModStands.CRAZY_DIAMOND.getStandType()) {
                    return ModSounds.JOSUKE_YO_ANGELO.get();
                }
                if (stand != null && stand.getRegistryName().getPath().contains("echoes")) {
                    return ModSounds.KOICHI_YO_ANGELO.get();
                }
                return null;
            }).orElse(null);
            if (voiceline != null && JojoModUtil.sayVoiceLine(pPlayer, voiceline, null, 1, 1, 0, true)) {
                responseSoundTimer.add(30);
            }
            else {
                playMobResponseSound();
            }
            return ActionResultType.CONSUME;
        }
    }
    
    public void playMobResponseSound() {
        if (mob != null) {
            if (useMobHurtSound) {
                CommonReflection.playHurtSound(mob, DamageSource.GENERIC);
            }
            else {
                mob.playAmbientSound();
            }
        }
    }
    
    private void tickResponseTimers() {
        if (!level.isClientSide()) {
            responseSoundTimer.tick(this::playMobResponseSound);
        }
    }
    
    
    @Override
    public boolean hurt(DamageSource dmgSource, float dmgAmount) {
        if (level.isClientSide()) return false;
        
        if ("player".equals(dmgSource.getMsgId()) && dmgSource.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) dmgSource.getEntity();
            if (attacker instanceof PlayerEntity && ((PlayerEntity) attacker).abilities.instabuild) {
                dropMode = DropMode.NONE;
                entityData.set(DAMAGE, Float.MAX_VALUE);
                cancelPlayerHitSound = true;
                return true;
            }
            
            ItemStack item = attacker.getMainHandItem();
            if (!item.isEmpty() && item.getItem() instanceof PickaxeItem) {
                Collection<BlockState> blocks = new ArrayList<>();
                blocks.add(getLowerBlock());
                blocks.add(getUpperBlock());
                dmgAmount = (float) blocks.stream().mapToDouble(item::getDestroySpeed).average().getAsDouble();
                int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, item);
                if (i > 0) {
                    dmgAmount += (float)(i * i + 1);
                }
                if (blocks.stream().noneMatch(block -> !block.requiresCorrectToolForDrops() || item.isCorrectToolForDrops(block))) {
                    dmgAmount *= 0.3f;
                }
                
                dmgAmount = Math.max(dmgAmount, 1);
                BlockState randomBlock = blocks.stream().skip(random.nextInt(blocks.size())).findFirst().get();
                SoundType blockSound = randomBlock.getSoundType();
                level.playSound(null, getX(), getY(0.5), getZ(), blockSound.getHitSound(), 
                        getSoundSource(), (blockSound.getVolume() + 1.0F) / 8.0F, blockSound.getPitch() * 0.5F);
                
//                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, item) > 0) {
//                    dropMode = DropMode.SILK_TOUCH;
//                }
                
                entityData.set(DAMAGE, entityData.get(DAMAGE) + dmgAmount);
                if (isBroken()) {
                    item.hurt(1, random, attacker instanceof ServerPlayerEntity ? (ServerPlayerEntity) attacker : null);
                }
                
                cancelPlayerHitSound = true;
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean cancelPlayerHitSound = false;
    
    private void onDamageApplied() {
        if (isBroken()) {
            breakRock();
        }
    }
    
    private boolean isBroken() {
        return entityData.get(DAMAGE) >= 40;
    }
    
    private DropMode dropMode = DropMode.BLOCKS;
    
    private enum DropMode {
        BLOCKS,
        NONE,
        SILK_TOUCH
    }
    
    public void breakRock() {
        if (!level.isClientSide()) {
            angeloRockBlocks.values().forEach(block -> {
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(level, block.pos, block.state, Optional.empty(), block.drops);
                
                if (dropMode == DropMode.BLOCKS && !block.drops.isEmpty()) {
                    Vector3d pos = Vector3d.atCenterOf(block.pos);
                    for (ItemStack item : block.drops) {
                        ItemEntity itemEntity = new ItemEntity(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, item);
                        itemEntity.setDefaultPickUpDelay();
                        if (captureDrops() != null) {
                            captureDrops().add(itemEntity);
                        }
                        else {
                            level.addFreshEntity(itemEntity);
                        }
                    }
                }
            });
            // TODO angelo rock silk touch
            if (dropMode == DropMode.SILK_TOUCH) {
                
            }
            remove();
        }
        else {
            clBreakBlockVisuals(getUpperBlock(), blockPosition());
            clBreakBlockVisuals(getLowerBlock(), blockPosition().above());
        }
    }
    
    private void clBreakBlockVisuals(BlockState blockState, BlockPos blockPos) {
        CustomParticlesHelper.addBlockBreakParticles(blockPos, blockState);
        SoundType soundType = blockState.getSoundType();
        SoundEvent sound = soundType.getBreakSound();
        level.playLocalSound(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                sound, getSoundSource(), (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
    }
    
    public float getDamageRatio() {
        return entityData.get(DAMAGE) / (20 * angeloRockBlocks.size());
    }
    
    
    @Override
    public boolean canBeCollidedWith() {
        return isAlive();
    }
    
    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
        this.entityData.define(CREATION_COMPLETE, false);
        this.entityData.define(DAMAGE, 0f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {
        BlockPos blockpos = this.getAttachPosition();
        if (blockpos != null) {
            pCompound.putInt("APX", blockpos.getX());
            pCompound.putInt("APY", blockpos.getY());
            pCompound.putInt("APZ", blockpos.getZ());
        }
        pCompound.putBoolean("Created", entityData.get(CREATION_COMPLETE));
        pCompound.putInt("CreationAnim", creationAnimTicks);
        pCompound.putFloat("RockDamage", entityData.get(DAMAGE));
        
        if (!angeloRockBlocks.isEmpty()) {
            ListNBT blocksNbt = new ListNBT();
            for (PrevBlockInfo block : angeloRockBlocks.values()) {
                blocksNbt.add(block.toNBT());
            }
            pCompound.put("RockBlocks", blocksNbt);
        }
        
        if (mob != null) {
            String s = mob.getEncodeId();
            if (s != null) {
                CompoundNBT mobNBT = new CompoundNBT();
                mobNBT.putString("id", s);
                mob.saveWithoutId(mobNBT);
                mobNBT.remove("Passengers");
                pCompound.put("AngeloMob", mobNBT);
                pCompound.putBoolean("NoAmbient", useMobHurtSound);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {
        if (pCompound.contains("APX")) {
            int i = pCompound.getInt("APX");
            int j = pCompound.getInt("APY");
            int k = pCompound.getInt("APZ");
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(i, j, k)));
        } else {
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
        }
        entityData.set(CREATION_COMPLETE, pCompound.getBoolean("Created"));
        this.creationAnimTicks = pCompound.getInt("CreationAnim");
        entityData.set(DAMAGE, pCompound.getFloat("RockDamage"));
        
        MCUtil.getNbtElement(pCompound, "RockBlocks", ListNBT.class).ifPresent(blocksNbt -> {
            if (blocksNbt.getElementType() != Constants.NBT.TAG_COMPOUND) return;
            blocksNbt.forEach(blockNbt -> {
                PrevBlockInfo block = PrevBlockInfo.fromNBT((CompoundNBT) blockNbt);
                if (block != null) {
                    angeloRockBlocks.put(block.pos, block);
                }
            });
        });
        
        Entity mobEntity = MCUtil.nbtGetCompoundOptional(pCompound, "AngeloMob").flatMap(mobNBT -> {
            try {
                return EntityType.create(mobNBT, level);
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }).orElse(null);
        this.mob = mobEntity instanceof MobEntity ? (MobEntity) mobEntity : null;
        this.useMobHurtSound = pCompound.getBoolean("NoAmbient");
    }
    
    @Override
    public void tick() {
        super.tick();
        cancelPlayerHitSound = false;
        
        if (!level.isClientSide()) {
            tickResponseTimers();
        }
        
        if (creationAnimTicks > 0) {
            if (level.isClientSide()) {
                if (ClientUtil.canSeeStands()) {
                    CrazyDiamondHeal.addParticlesAround(this);
                }
                if (ClientUtil.canHearStands() && !this.isSilent()) {
                    if (!startedSound) {
                        ClientTickingSoundsHelper.playStoppableEntitySound(this, 
                                ModSounds.CRAZY_DIAMOND_FIX_LOOP.get(), 1, 1, true, entity -> entity.creationAnimTicks > 0);
                        startedSound = true;
                    }
                    if (creationAnimTicks == 1) {
                        level.playSound(ClientUtil.getClientPlayer(), getX(), getY(), getZ(), 
                                ModSounds.CRAZY_DIAMOND_FIX_ENDED.get(), getSoundSource(), 1, 1);
                    }
                }
            }
            --creationAnimTicks;
        }
        
        LivingEntity angeloEntity = this.angeloEntity.getEntityLiving(level);
        if (angeloEntity != null && !entityData.get(CREATION_COMPLETE)) {
            angeloEntity.hurtTime = 0;
            angeloEntity.deathTime = 0;
            angeloEntity.animationPosition = 0;
            angeloEntity.animationSpeed = 0;
            angeloEntity.animationSpeedOld = 0;
            angeloEntity.addEffect(new EffectInstance(ModStatusEffects.IMMOBILIZE.get(), 10, 0, false, false, true));
            angeloEntity.setPosAndOldPos(getX(), getY(), getZ());
            // TODO (angelo) lock player camera rotation
            angeloEntity.yRot = this.yRot;
            angeloEntity.yRot = this.yRot;
            angeloEntity.xRotO = this.xRot;
            angeloEntity.xRotO = this.xRot;
            angeloEntity.setPose(Pose.STANDING);
            if (creationAnimTicks <= 0) {
                if (!level.isClientSide()) {
                    if (angeloEntity instanceof IPlayerPossess) {
                        angeloEntity.removeAllEffects();
                        ((IPlayerPossess) angeloEntity).jojoPossessEntity(this, false, null);
                    }
                    else {
                        angeloEntity.remove();
                    }
                }
                entityData.set(CREATION_COMPLETE, true);
                this.angeloEntity.setOwner(null);
            }
        }
        
        BlockPos attachPos = getAttachPosition();
        if (attachPos == null && !this.level.isClientSide) {
            attachPos = this.blockPosition();
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(attachPos));
        }

        if (this.isPassenger()) {
            attachPos = null;
            float f = this.getVehicle().yRot;
            this.yRot = f;
        } else if (!this.level.isClientSide) {
            Optional<BlockPos> moveWithPiston = moveWithPiston(attachPos);
            if (!moveWithPiston.isPresent()) moveWithPiston = moveWithPiston(attachPos.above());
            if (moveWithPiston.isPresent()) {
                this.entityData.set(DATA_ATTACH_POS_ID, moveWithPiston);
            }
        }

        if (attachPos != null) {
            setPosAndOldPos(attachPos.getX() + 0.5, attachPos.getY(), attachPos.getZ() + 0.5);
//            if (isAddedToWorld() && level instanceof ServerWorld) {
//                ((ServerWorld)level).updateChunkPos(this); // Forge - Process chunk registration after moving.
//            }
            setBoundingBox(new AxisAlignedBB(
                    getX() - 0.5, 
                    getY(), 
                    getZ() - 0.5, 
                    getX() + 0.5, 
                    getY() + getBbHeight(), 
                    getZ() + 0.5));
        }
    }
    
    @Nullable
    private Optional<BlockPos> moveWithPiston(BlockPos pistonBlockPos) {
        Direction pistonHeadDir = null;
        BlockState blockState = level.getBlockState(pistonBlockPos);
        if (!blockState.isAir(this.level, pistonBlockPos) && (blockState.is(Blocks.MOVING_PISTON) || blockState.is(Blocks.PISTON_HEAD))) {
            pistonHeadDir = blockState.getValue(BlockStateProperties.FACING);
        }
        if (pistonHeadDir != null) {
            BlockPos attachPos = getAttachPosition();
            if (attachPos != null) {
                BlockPos newPos = attachPos.relative(pistonHeadDir);
                if (this.level.isEmptyBlock(newPos) && this.level.isEmptyBlock(newPos.above())) {
                    return Optional.of(newPos);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public void setPos(double pX, double pY, double pZ) {
        super.setPos(pX, pY, pZ);
        if (this.entityData != null && this.tickCount != 0) {
            Optional<BlockPos> optional = this.entityData.get(DATA_ATTACH_POS_ID);
//            if (this.isAddedToWorld() && this.level instanceof ServerWorld) {
//                ((ServerWorld) level).updateChunkPos(this); // Forge - Process chunk registration after moving.
//            }
            Optional<BlockPos> optional1 = Optional.of(new BlockPos(pX, pY, pZ));
            if (!optional1.equals(optional)) {
                this.entityData.set(DATA_ATTACH_POS_ID, optional1);
                this.hasImpulse = true;
            }

        }
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> pKey) {
        if (DATA_ATTACH_POS_ID.equals(pKey)) {
            if (level.isClientSide && !isPassenger()) {
                BlockPos blockpos = getAttachPosition();
                if (blockpos != null) {
                    setPosAndOldPos(blockpos.getX() + 0.5, blockpos.getY(), blockpos.getZ() + 0.5);
                }
            }
        }
        else if (DAMAGE.equals(pKey)) {
            onDamageApplied();
        }

        super.onSyncedDataUpdated(pKey);
    }

    @Nullable
    public BlockPos getAttachPosition() {
        return this.entityData.get(DATA_ATTACH_POS_ID).orElse(null);
    }

    public void setAttachPosition(@Nullable BlockPos pPos) {
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(pPos));
    }
    
    public float getCreationAnimProgress(float partialTick) {
        return creationAnimTicks == 0 ? 1 : (CREATION_ANIM_LEN - creationAnimTicks + partialTick) / CREATION_ANIM_LEN;
    }
    
    
    public BlockState getUpperBlock() {
        return getBlock(blockPosition().above());
    }
    
    public BlockState getLowerBlock() {
        return getBlock(blockPosition());
    }
    
    public BlockState getBlock(BlockPos pos) {
        PrevBlockInfo block = angeloRockBlocks.get(pos);
        return block != null ? block.state : Blocks.STONE.defaultBlockState();
    }
    
    
    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        NetworkUtil.writeOptionally(buffer, angeloEntity.getEntityLiving(level), entity -> buffer.writeInt(entity.getId()));
        buffer.writeVarInt(creationAnimTicks);
        NetworkUtil.writeCollection(buffer, angeloRockBlocks.values(), PrevBlockInfo::toBuf, false);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        Entity angeloEntity = NetworkUtil.readOptional(additionalData, buf -> ClientUtil.getEntityById(buf.readInt())).orElse(null);
        if (angeloEntity instanceof LivingEntity) {
            this.angeloEntity.setOwner(angeloEntity);
        }
        creationAnimTicks = additionalData.readVarInt();
        angeloRockBlocks.clear();
        NetworkUtil.readCollection(additionalData, PrevBlockInfo::fromBuf).forEach(block -> angeloRockBlocks.put(block.pos, block));
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
