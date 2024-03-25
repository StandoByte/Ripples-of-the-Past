package com.github.standobyte.jojo.entity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.ai.GELifeformFollowOwnerGoal;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class GETransformationEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final DataParameter<Boolean> LIFE_FORM_SPAWNED = EntityDataManager.defineId(GETransformationEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IS_TURNING_BACK = EntityDataManager.defineId(GETransformationEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> REVERSE_SIGNAL = EntityDataManager.defineId(GETransformationEntity.class, DataSerializers.BOOLEAN);
    
    private GETransformationData source = new GETransformationData();
    private EntityOwnerResolver owner = new EntityOwnerResolver();
    
    private Entity target;
    
    private int duration;
    private float renderAsItemTime;
    public int actionCooldown;

    public GETransformationEntity(EntityType<?> type, World level) {
        super(type, level);
    }

    public GETransformationEntity(World pLevel) {
        this(ModEntityTypes.GE_LIFEFORM_TRANSFORMATION.get(), pLevel);
    }
    
    public GETransformationEntity withTransformationTarget(Entity entity) {
        this.target = entity;
        return this;
    }
    
    public GETransformationEntity withOwner(LivingEntity user) {
        this.owner.setOwner(user);
        return this;
    }
    
    public GETransformationEntity withDuration(int duration) {
        this.duration = duration;
        this.renderAsItemTime = Math.min((float) duration / 3, 20);
        return this;
    }
    
    public GETransformationData getTfSourceData() {
        return source;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public boolean isTurningBack() {
        return entityData.get(IS_TURNING_BACK);
    }
    
    public Entity getTransformationTarget() {
        return target;
    }
    
    @SuppressWarnings("deprecation")
    private void turnInto() {
        Entity entityToSummon = null;
        BlockState blockToPlace = null;
        if (isTurningBack()) {
            if (source.sourceEntity != null) {
                entityToSummon = source.sourceEntity;
            }
            else if (source.sourceBlockState != null) {
                blockToPlace = source.sourceBlockState;
            }
        }
        else {
            entityToSummon = target;
        }
        BlockPos blockPos = blockPosition();
        
        if (blockToPlace != null) {
            blockToPlace = Block.updateFromNeighbourShapes(blockToPlace, level, blockPos);
            BlockState existingBlock = level.getBlockState(blockPos);
            if (!((existingBlock.isAir(level, blockPos) || existingBlock.getMaterial().isReplaceable())
                    && blockToPlace.canSurvive(level, blockPos))) {
                if (!(blockToPlace.getBlock() instanceof AbstractFireBlock)) {
                    level.levelEvent(2001, blockPos, Block.getId(blockToPlace));
                }
                Block.dropResources(blockToPlace, level, blockPos, null, owner.getEntity(level), ItemStack.EMPTY);
                
                blockToPlace = null;
            }
        }
        
        if (entityToSummon != null) {
            entityToSummon.copyPosition(this);
            if (entityToSummon instanceof MobEntity) {
                ((MobEntity) entityToSummon).setPersistenceRequired();
            }
            else if (entityToSummon instanceof ItemEntity) {
                ((ItemEntity) entityToSummon).setNoPickUpDelay();
            }
            copyStatus(this, entityToSummon);
            level.addFreshEntity(entityToSummon);
            if (!isTurningBack()) {
                if (entityToSummon instanceof MobEntity) {
                    MobEntity mob = (MobEntity) entityToSummon;
                    mob.playAmbientSound();
                    if (source.aggroTarget != null) {
                        mob.goalSelector.addGoal(-1, new GELifeformFollowOwnerGoal(mob, source.aggroTarget, 1.0));
                    }
                }
            }
        }
        else if (blockToPlace != null) {
            Entity ownerEntity = owner.getEntity(level);
            if (!ForgeEventFactory.onBlockPlace(ownerEntity, BlockSnapshot.create(level.dimension(), level, blockPos.below()), Direction.UP)) {
                if (this.isOnFire()) {
                    blockToPlace.catchFire(level, blockPos, Direction.UP, null);
                }
                else {
                    level.setBlock(blockPos, blockToPlace, 3);
                }
            }
        }
        
        remove();
    }
    
    @Override
    public void tick() {
        if (!level.isClientSide()) {
            source.resolveNbtRead(level);
        }
        else if (ClientUtil.canHearStands()) {
            clTickSound();
        }
        
        if (tickCount >= duration) {
            if (!level.isClientSide()) {
                entityData.set(LIFE_FORM_SPAWNED, true);
                turnInto();
            }
            return;
        }
        else {
            if (isTurningBack() && source.sourceEntity == null && source.sourceBlockState != null) {
                int timeLeft = duration - tickCount;
                float timeAsBlock = getRenderAsItemTime();
                if (timeLeft - 1 <= timeAsBlock && timeLeft > timeAsBlock) {
                    BlockPos blockPos = blockPosition();
                    Vector3d pos = Vector3d.atBottomCenterOf(blockPos);
//                    BlockPos blockPosNew = new BlockPos(pos);
//                    if (!blockPosNew.equals(blockPos)) {
//                        BlockPos diff = blockPosNew.subtract(blockPos);
//                        pos = pos.subtract(diff.getX(), diff.getY(), diff.getZ());
//                        JojoMod.LOGGER.debug(diff);
//                    }
                    moveTo(pos);
                }
            }
        }
        
        
        double f = getEyeHeight() - 0.11111111;
        Vector3d deltaMovement = getDeltaMovement();
        if (isInWater() && getFluidHeight(FluidTags.WATER) > f) {
            setDeltaMovement(
                    deltaMovement.x * 0.99, 
                    deltaMovement.y + (deltaMovement.y < 0.06 ? 5.0E-4 : 0), 
                    deltaMovement.z * 0.99);
        }
        else if (isInLava() && getFluidHeight(FluidTags.LAVA) > f) {
            setDeltaMovement(
                    deltaMovement.x * 0.95, 
                    deltaMovement.y + (deltaMovement.y < 0.06 ? 5.0E-4 : 0), 
                    deltaMovement.z * 0.95);
        }
        else if (!isNoGravity()) {
            setDeltaMovement(deltaMovement.add(0, -0.04, 0));
        }
        
        if (!onGround || getHorizontalDistanceSqr(getDeltaMovement()) > 1.0E-5 || (tickCount + getId()) % 4 == 0) {
            move(MoverType.SELF, getDeltaMovement());
            double inertia = 0.98;
            if (onGround) {
                inertia = level.getBlockState(new BlockPos(getX(), getY() - 1.0, getZ()))
                        .getSlipperiness(level, new BlockPos(getX(), getY() - 1.0, getZ()), this) * 0.98;
            }
            
            setDeltaMovement(getDeltaMovement().multiply(inertia, 0.98, inertia));
            if (onGround) {
                deltaMovement = getDeltaMovement();
                if (deltaMovement.y < 0.0D) {
                    setDeltaMovement(deltaMovement.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        
        
        refreshDimensions();
        
        super.tick();
    }
    
    private void clTickSound() {
        SoundEvent sound = null;
        float volume = 1;
        float pitch = 1;
        if (tickCount == 1) {
            sound = isTurningBack() ? ModSounds.GOLD_EXPERIENCE_LIFE_REVERT.get() : ModSounds.GOLD_EXPERIENCE_LIFE_START.get();
        }
        
        if (sound != null) {
            level.playLocalSound(getX(), getY(), getZ(), sound, getSoundSource(), volume, pitch, false);
        }
    }
    
    // Mojang?!?
    @Override
    public void refreshDimensions() {
        double x = getX();
        double y = getY();
        double z = getZ();
        super.refreshDimensions(); // why does it shift the entity along the XZ axes when it increases in size anyway?...
        this.setPos(x, y, z);
    }
    
    @Override
    public EntitySize getDimensions(Pose pPose) {
        EntitySize size = new EntitySize(getBbWidth(), getBbHeight(), false);
        float scale = 0;
        
        float tfProgressTime = getTfProgressTime(0);
        if (tfProgressTime < renderAsItemTime) {
            Entity sourceEntity = source.getSourceEntity();
            BlockState sourceBlockState = source.getSourceBlockState();
            if (sourceEntity != null || sourceBlockState != null) {
                scale = 1 - tfProgressTime / renderAsItemTime;
                if (scale > 0) {
                    if (sourceEntity != null) {
                        size = sourceEntity.getDimensions(pPose).scale(scale);
                    }
                    else {
                        size = EntitySize.scalable(1, 1).scale(scale);
                    }
                }
            }
        }
        
        else if (target != null) {
            scale = 1 - (duration - tfProgressTime) / (duration - renderAsItemTime);
            if (scale > 0) {
                size = target.getDimensions(pPose).scale(scale);
            }
        }
        
        return size;
    }
    
    public float getTfProgressTime(float partialTick) {
        float time = Math.min(tickCount + partialTick, duration);
        return isTurningBack() ? duration - time : time;
    }
    
    public float getRenderAsItemTime() {
        return renderAsItemTime;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(LIFE_FORM_SPAWNED, false);
        entityData.define(IS_TURNING_BACK, false);
        entityData.define(REVERSE_SIGNAL, false);
    }
    
    @Override
    public boolean isInvisible() {
        return super.isInvisible() || entityData.get(LIFE_FORM_SPAWNED);
    }
    
    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == REVERSE_SIGNAL && entityData.get(REVERSE_SIGNAL)) {
            reverseTransformation();
        }
    }
    
    private void reverseTransformation() {
        int ticks = TURN_BACK_TICKS;
        if (tickCount < ticks) {
            tickCount = duration - tickCount;
        }
        else {
            float prevItemTime = getRenderAsItemTime();
            
            if (tickCount > prevItemTime) {
                renderAsItemTime = (ticks / 3F);
                duration = (int) ((duration - prevItemTime) * (ticks - renderAsItemTime)
                                    / (tickCount - prevItemTime) + renderAsItemTime);
            }
            else {
                renderAsItemTime = ticks * prevItemTime / tickCount;
                duration = MathHelper.ceil(renderAsItemTime);
            }
            
            tickCount = duration - ticks;
        }
        
        if (level.isClientSide() && ClientUtil.canHearStands()) {
            level.playLocalSound(getX(), getY(), getZ(), 
                    ModSounds.GOLD_EXPERIENCE_LIFE_REVERT.get(), getSoundSource(), 
                    1, 1, false);
        }
    }
    
    
    private static final int TURN_BACK_TICKS = 10;
    public static void turnEntityBack(Entity entity, GETransformationData source, @Nullable LivingEntity owner) {
        if (entity instanceof GETransformationEntity) {
            GETransformationEntity tfEntity = (GETransformationEntity) entity;
            tfEntity.entityData.set(IS_TURNING_BACK, true);
            tfEntity.entityData.set(REVERSE_SIGNAL, true);
        }
        else {
            World world = entity.level;
            GETransformationEntity tf = new GETransformationEntity(world)
                    .withTransformationTarget(entity)
                    .withDuration(TURN_BACK_TICKS)
                    .withOwner(owner);
            tf.entityData.set(IS_TURNING_BACK, true);
            
            if (source.sourceEntity instanceof ItemEntity) {
                ItemStack item = ((ItemEntity) source.sourceEntity).getItem();
                if (!item.isEmpty() && item.getItem() instanceof BlockItem && item.getCount() == 1) {
//                    BlockState blockToPlace = null;
//                    if (blockToPlace != null) {
//                        source.withEntitySource(null).withBlockSource(blockToPlace, null);
//                    }
                }
            }
            tf.source.copyFrom(source, world);
            
            copyStatus(entity, tf);
            
            Vector3d pos = entity.position();
            tf.moveTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot);
            entity.level.addFreshEntity(tf);
            
            entity.remove();
        }
    }
    
    private static void copyStatus(Entity from, Entity to) {
        if (from.isOnFire()) {
            to.setSecondsOnFire((from.getRemainingFireTicks() + 19) / 20);
        }
        if (from.isPassenger()) {
            to.startRiding(from.getVehicle());
        }
        if (from.isVehicle()) {
            from.getPassengers().forEach(passenger -> passenger.startRiding(to));
        }
        if (from.hasCustomName() && !(to instanceof ItemEntity)) {
            to.setCustomName(from.getCustomName());
        }
    }
    
    
    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.tickCount = nbt.getInt("Age");
        withDuration(nbt.getInt("Duration"));
        entityData.set(IS_TURNING_BACK, nbt.getBoolean("TurnBack"));
        actionCooldown = nbt.getInt("ActionCD");
        
        source.readNbt(nbt);
        if (nbt.contains("TargetEntity", MCUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT entityNbt = nbt.getCompound("TargetEntity");
            target = EntityType.create(entityNbt, level).orElse(null);
        }
        owner.loadNbt(nbt, "Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("Age", tickCount);
        nbt.putInt("Duration", duration);
        nbt.putBoolean("TurnBack", entityData.get(IS_TURNING_BACK));
        nbt.putInt("ActionCD", actionCooldown);
        
        source.writeNbt(nbt);
        if (target != null) {
            CompoundNBT entityNbt = target.serializeNBT();
            nbt.put("TargetEntity", entityNbt);
        }
        owner.saveNbt(nbt, "Owner");
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeVarInt(tickCount);
        buffer.writeVarInt(duration);
        owner.writeNetwork(buffer);
        
        writeEntityData(buffer, target);
        source.resolveNbtRead(level);
        source.toBuf(buffer);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        tickCount = additionalData.readVarInt();
        withDuration(additionalData.readVarInt());
        owner.readNetwork(additionalData);
        
        target = readEntityData(additionalData, level);
        source.fromBuf(additionalData, level);
    }
    
    
    
    public static class GETransformationData {
        private UUID aggroTarget;
        private Entity sourceEntity;
        private CompoundNBT sourceEntityNbt = null;
        private BlockState sourceBlockState;
        private BlockPos sourceBlockPos;
        
        
        
        public GETransformationData withEntitySource(Entity entity) {
            this.sourceEntity = entity;
            return this;
        }
        
        public GETransformationData withBlockSource(BlockState blockState, BlockPos blockPos) {
            this.sourceBlockState = blockState;
            this.sourceBlockPos = blockPos;
            return this;
        }
        
        public GETransformationData withAggroTarget(UUID entity) {
            this.aggroTarget = entity;
            return this;
        }
        
        public void copyFrom(GETransformationData other, World world) {
            this.aggroTarget = other.aggroTarget;
            this.sourceEntity = other.sourceEntity;
            this.sourceBlockState = other.sourceBlockState;
            this.sourceBlockPos = other.sourceBlockPos;
        }
        
        
        /**
         * Call this during tick or before sending the data from server.
         */
        public void resolveNbtRead(World world) {
            if (sourceEntityNbt != null) {
                withEntitySource(EntityType.create(sourceEntityNbt, world).orElse(null));
                sourceEntityNbt = null;
            }
        }
        
        
        
        public ItemStack makeSourceItemView() {
            if (sourceEntity != null) {
                if (sourceEntity instanceof ItemEntity) {
                    return ((ItemEntity) sourceEntity).getItem().copy();
                }
                else if (sourceEntity instanceof TNTEntity) {
                    return new ItemStack(Items.TNT);
                }
                else if (sourceEntity instanceof PotionEntity) {
                    ItemStack potionItem;
                    if (sourceEntity.level.isClientSide()) {
                        potionItem = ((PotionEntity) sourceEntity).getItem();
                    }
                    else {
                        potionItem = MCUtil.getItemOnServer((PotionEntity) sourceEntity);
                    }
                    return potionItem.copy();
                }
                else if (sourceEntity.getType() == EntityType.ENDER_PEARL) {
                    return new ItemStack(Items.ENDER_PEARL);
                }
                else if (sourceEntity.getType() == ModEntityTypes.ROAD_ROLLER.get()) {
                    return new ItemStack(ModItems.ROAD_ROLLER.get());
                }
                else if (sourceEntity.getType() == EntityType.END_CRYSTAL) {
                    return new ItemStack(Items.END_CRYSTAL);
                }
                else if (sourceEntity instanceof BoatEntity) {
                    return new ItemStack(((BoatEntity) sourceEntity).getDropItem());
                }
            }
            else if (sourceBlockState != null) {
                Block block = sourceBlockState.getBlock();
                Item blockItem = block.asItem();
                if (blockItem != null && blockItem != Items.AIR) {
                    return new ItemStack(blockItem);
                }
            }
            
            return ItemStack.EMPTY;
        }
        
        public IFormattableTextComponent clMakeSourceName() {
            if (sourceEntity != null) {
                ITextComponent name = sourceEntity.getDisplayName();
                if (name instanceof IFormattableTextComponent) {
                    return (IFormattableTextComponent) name;
                }
                throw new ClassCastException("Why do ITextComponent and IFormattableTextComponent interfaces both exist? Why not just make ITextComponent formattable? Separating them doesn't even do shit, ffs OOP was a mistake");
            }
            
            else if (sourceBlockState != null) {
                return sourceBlockState.getBlock().getName(); // oh, look, this returns IFormattableTextComponent!
            }
            
            return (StringTextComponent) StringTextComponent.EMPTY;
        }
        
        
        @Nullable
        public Entity getSourceEntity() {
            return sourceEntity;
        }
        
        @Nullable
        public BlockState getSourceBlockState() {
            return sourceBlockState;
        }
        
        @Nullable
        public BlockPos getSourceBlockPos() {
            return sourceBlockPos;
        }
        
        @Nullable
        public UUID getAggroTarget() {
            return aggroTarget;
        }
        
        
        
        public void writeNbt(CompoundNBT nbt) {
            if (sourceEntity != null) {
                CompoundNBT entityNbt = sourceEntity.serializeNBT();
                nbt.put("GESourceEntity", entityNbt);
            }
            if (sourceBlockState != null) {
                nbt.put("GESourceBlock", NBTUtil.writeBlockState(sourceBlockState));
            }
            if (sourceBlockPos != null) {
                nbt.put("GESourcePos", NBTUtil.writeBlockPos(sourceBlockPos));
            }
            if (aggroTarget != null) {
                nbt.putUUID("Owner", aggroTarget);
            }
        }
        
        public void readNbt(CompoundNBT nbt) {
            if (nbt.contains("GESourceEntity", MCUtil.getNbtId(CompoundNBT.class))) {
                sourceEntityNbt = nbt.getCompound("GESourceEntity");
            }
            if (nbt.contains("GESourceBlock", MCUtil.getNbtId(CompoundNBT.class))) {
                sourceBlockState = NBTUtil.readBlockState(nbt.getCompound("GESourceBlock"));
            }
            if (nbt.contains("GESourcePos", MCUtil.getNbtId(CompoundNBT.class))) {
                sourceBlockPos = NBTUtil.readBlockPos(nbt.getCompound("GESourcePos"));
            }
            if (nbt.hasUUID("Owner")) {
                aggroTarget = nbt.getUUID("Owner");
            }
        }
        
        public void toBuf(PacketBuffer buffer) {
            writeEntityData(buffer, sourceEntity);
            NetworkUtil.writeOptionally(buffer, sourceBlockState, blockState -> NetworkUtil.writeBlockState(buffer, blockState));
            NetworkUtil.writeOptionally(buffer, sourceBlockPos, blockPos -> buffer.writeBlockPos(blockPos));
        }
        
        public void fromBuf(PacketBuffer buffer, World world) {
            sourceEntity = readEntityData(buffer, world);
            sourceBlockState = NetworkUtil.readOptional(buffer, () -> NetworkUtil.readBlockState(buffer)).orElse(null);
            sourceBlockPos = NetworkUtil.readOptional(buffer, () -> buffer.readBlockPos()).orElse(null);
        }
    }
    
    
    
    private static void writeEntityData(PacketBuffer buffer, Entity entityToWrite) {
        NetworkUtil.writeOptionally(buffer, entityToWrite, entity -> {
            byte pitch = (byte) MathHelper.floor(entity.xRot * 256.0F / 360.0F);
            byte yaw = (byte) MathHelper.floor(entity.yRot * 256.0F / 360.0F);
            byte headYaw = (byte) (entity.getYHeadRot() * 256.0F / 360.0F);
            
            buffer.writeRegistryId(entity.getType());
            buffer.writeByte(pitch);
            buffer.writeByte(yaw);
            buffer.writeByte(headYaw);
            
            if (entity instanceof IEntityAdditionalSpawnData) {
                ((IEntityAdditionalSpawnData) entity).writeSpawnData(buffer);
            }
            
            List<EntityDataManager.DataEntry<?>> entityData = entity.getEntityData().getAll();
            try {
                EntityDataManager.pack(entityData, buffer);
            } catch (IOException e) {
                JojoMod.getLogger().error("Failed to write entity data for Gold Experience's transformation render for entity of type {}", entity.getType().getRegistryName());
                e.printStackTrace();
            }
        });
    }
    
    private static Entity readEntityData(PacketBuffer buffer, World world) {
        return NetworkUtil.readOptional(buffer, () -> {
            EntityType<?> type = buffer.readRegistryIdSafe(EntityType.class);
            Entity entity = type.create(world);
            
            float pitch = (buffer.readByte() * 360) / 256.0F;
            float yaw = (buffer.readByte() * 360) / 256.0F;
            float headYaw = (buffer.readByte() * 360) / 256.0F;
            
            entity.yRot = yaw % 360.0F;
            entity.xRot = MathHelper.clamp(pitch, -90.0F, 90.0F) % 360.0F;
            entity.yRotO = entity.yRot;
            entity.xRotO = entity.xRot;
            entity.setYHeadRot(headYaw);
            entity.setYBodyRot(headYaw);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.yHeadRotO = living.yHeadRot;
                living.yBodyRotO = living.yBodyRot;
            }

            if (entity instanceof IEntityAdditionalSpawnData) {
                ((IEntityAdditionalSpawnData) entity).readSpawnData(buffer);
            }
            
            try {
                List<EntityDataManager.DataEntry<?>> entityData = EntityDataManager.unpack(buffer);
                entity.getEntityData().assignValues(entityData);
            } catch (IOException e) {
                JojoMod.getLogger().error("Failed to read entity data for Gold Experience's transformation render for entity of type {}", entity.getType().getRegistryName());
                e.printStackTrace();
            } catch (Exception e) {
                JojoMod.getLogger().error("Failed to assign entity data for Gold Experience's transformation render for entity of type {}", entity.getType().getRegistryName());
                e.printStackTrace();
            }
            
            return entity;
        }).orElse(null);
    }

}
