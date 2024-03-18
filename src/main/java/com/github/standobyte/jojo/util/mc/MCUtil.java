package com.github.standobyte.jojo.util.mc;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.item.GlovesItem;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SpawnParticlePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrResetDeathTimePacket;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.INBTType;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NBTTypes;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SSpawnMovingSoundEffectPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class MCUtil {
    public static final IFormattableTextComponent EMPTY_TEXT = new StringTextComponent("");
    public static final IFormattableTextComponent NEW_LINE = new StringTextComponent("\n");
    
    /**
     * Runs a command for the user entity, but with the permissions of the server.
     * 
     * @return The success value of the command, or 0 if an exception occured.
     */
    public static int runCommand(LivingEntity user, String command) {
        if (user.level.isClientSide()) {
            throw new IllegalLogicalSideException("Tried to run a command on client side!");
        }
        MinecraftServer server = ((ServerWorld) user.level).getServer();
        CommandSource src = user.createCommandSourceStack()
                .withMaximumPermission(4)
                .withSuppressedOutput();
        return server.getCommands().performCommand(src, command);
    }
    
    // NBT helper functions
    private static final ImmutableMap<Class<? extends INBT>, Integer> NBT_ID = new ImmutableMap.Builder<Class<? extends INBT>, Integer>()
            .put(EndNBT.class, 0)           .put(ByteNBT.class, 1)      .put(ShortNBT.class, 2)         .put(IntNBT.class, 3)
            .put(LongNBT.class, 4)          .put(FloatNBT.class, 5)     .put(DoubleNBT.class, 6)        .put(ByteArrayNBT.class, 7)
            .put(StringNBT.class, 8)        .put(ListNBT.class, 9)      .put(CompoundNBT.class, 10)     .put(IntArrayNBT.class, 11)
            .put(LongArrayNBT.class, 12)
            .build();
    
    public static int getNbtId(Class<? extends INBT> clazz) {
        return NBT_ID.getOrDefault(clazz, -1);
    }
    
    public static <T extends INBT> Optional<T> getNbtElement(CompoundNBT nbt, String key, Class<T> clazz) {
        int id = getNbtId(clazz);
        if (nbt.contains(key, id)) {
            try {
                return Optional.of((T) nbt.get(key));
            }
            catch (ClassCastException e) {
                INBTType<?> nbtType = NBTTypes.getType(id);
                CrashReport crashreport = CrashReport.forThrowable(e, "Reading NBT data");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
                crashreportcategory.setDetail("Tag type found", () -> {
                    return nbt.get(key).getType().getName();
                });
                crashreportcategory.setDetail("Tag type expected", nbtType::getName);
                crashreportcategory.setDetail("Tag name", key);
                throw new ReportedException(crashreport);
            }
        }
        return Optional.empty();
    }
    
    public static CompoundNBT replaceNbtValues(CompoundNBT original, CompoundNBT replacedEntries, CompoundNBT replacingEntries) {
        int compoundId = getNbtId(CompoundNBT.class);
        for (String key : replacedEntries.getAllKeys()) {
            if (replacedEntries.contains(key) && original.contains(key) && replacedEntries.contains(key)) {
                INBT originalValue = original.get(key);
                INBT replacedValue = replacedEntries.get(key);
                INBT replacingValue = replacingEntries.get(key);
                if (originalValue.getId() == compoundId) {
                    if (replacedValue.getId() == compoundId && replacingValue.getId() == compoundId) {
                        replaceNbtValues((CompoundNBT) originalValue, (CompoundNBT) replacedValue, (CompoundNBT) replacingValue);
                    }
                }
                else if (originalValue.equals(replacedValue)) {
                    original.put(key, replacingValue.copy());
                }
            }
        }
        return original;
    }
    
    public static <T extends Enum<T>> void nbtPutEnum(CompoundNBT nbt, String key, T enumVal) {
        nbt.putInt(key, enumVal.ordinal());
    }
    
    @Nullable
    public static <T extends Enum<T>> T nbtGetEnum(CompoundNBT nbt, String key, Class<T> enumClass) {
        if (!nbt.contains(key, getNbtId(IntNBT.class))) {
            return null;
        }
        
        int ordinal = nbt.getInt(key);
        T[] values = enumClass.getEnumConstants();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }
    
    public static <T extends IForgeRegistryEntry<T>> void nbtPutRegistryEntry(CompoundNBT nbt, String key, T entry) {
        nbt.put(key, StringNBT.valueOf(entry.getRegistryName().toString()));
    }
    
    public static <T extends IForgeRegistryEntry<T>> Optional<T> nbtGetRegistryEntry(CompoundNBT nbt, String key, IForgeRegistry<T> registry) {
        if (nbt.contains(key, getNbtId(StringNBT.class))) {
            String idString = nbt.getString(key);
            return registryEntryFromId(idString, registry);
        }
        
        return Optional.empty();
    }
    
    public static Optional<CompoundNBT> nbtGetCompoundOptional(CompoundNBT nbt, String key) {
        if (nbt.contains(key, getNbtId(CompoundNBT.class))) {
            return Optional.of(nbt.getCompound(key));
        }
        return Optional.empty();
    }
    
    public static Optional<ListNBT> nbtGetList(CompoundNBT nbt, String key, Class<? extends INBT> nbtClass) {
        if (nbt.contains(key, getNbtId(ListNBT.class))) {
            return Optional.of(nbt.getList(key, getNbtId(nbtClass)));
        }
        return Optional.empty();
    }
    
    
    
    public static <T extends IForgeRegistryEntry<T>> Optional<T> registryEntryFromId(String idString, IForgeRegistry<T> registry) {
        if (!idString.isEmpty()) {
            ResourceLocation id = new ResourceLocation(idString);
            if (registry.containsKey(id)) {
                return Optional.of(registry.getValue(id));
            }
        }
        
        return Optional.empty();
    }
    
    public static void nbtPutVec3d(CompoundNBT nbt, String key, Vector3d vec) {
        if (vec != null) {
            ListNBT list = new ListNBT();
            list.add(DoubleNBT.valueOf(vec.x));
            list.add(DoubleNBT.valueOf(vec.y));
            list.add(DoubleNBT.valueOf(vec.z));
            nbt.put(key, list);
        }
    }
    
    @Nullable
    public static Vector3d nbtGetVec3d(CompoundNBT nbt, String key) {
        return getNbtElement(nbt, key, ListNBT.class).map(list -> {
            if (list.size() == 3) {
                double[] nums = new double[3];
                for (int i = 0; i < 3; i++) {
                    INBT nbtElem = list.get(i);
                    if (nbtElem.getId() == 6) {
                        nums[i] = ((DoubleNBT) nbtElem).getAsDouble();
                    }
                    else {
                        return null;
                    }
                }
                return new Vector3d(nums[0], nums[1], nums[2]);
            }
            
            return null;
        }).orElse(null);
    }
    
    public static CompoundNBT getOrCreateCompound(CompoundNBT mainNbt, String key) {
        return nbtGetCompoundOptional(mainNbt, key).orElseGet(() -> {
            CompoundNBT nbt = new CompoundNBT();
            mainNbt.put(key, nbt);
            return nbt;
        });
    }
    
    //
    
    public static class ResLocJson implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
        public static final ResLocJson SERIALIZATION = new ResLocJson();
        
        private ResLocJson() {}

        @Override
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new ResourceLocation(json.getAsString());
        }

        @Override
        public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
    
    
    
    public static Set<ServerPlayerEntity> getTrackingPlayers(Entity entity) {
        if (entity.level.isClientSide()) {
            throw new IllegalStateException();
        }
        
        ChunkManager chunkMap = ((ServerWorld) entity.level).getChunkSource().chunkMap;
        Int2ObjectMap<ChunkManager.EntityTracker> entityMap = chunkMap.entityMap;
        ChunkManager.EntityTracker tracker = entityMap.get(entity.getId());
        return tracker.seenBy;
    }
    
    
    
    @Nonnull
    public static ItemStack findInInventory(IInventory inventory, Predicate<ItemStack> itemMatches) {
        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            if (itemMatches.test(item)) {
                return item;
            }
        }
        
        return ItemStack.EMPTY;
    }

    public static boolean dispenseOnNearbyEntity(IBlockSource blockSource, ItemStack itemStack, Predicate<LivingEntity> action, boolean shrinkStack) {
        BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> entities = blockSource.getLevel().getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(blockPos), EntityPredicates.NO_SPECTATORS);
        for (LivingEntity entity : entities) {
            if (action.test(entity)) {
                if (shrinkStack) {
                    itemStack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }
    
    public static void giveItemTo(LivingEntity entity, ItemStack item, boolean drop) {
        if (!entity.level.isClientSide() && !item.isEmpty()) {
            if (entity instanceof PlayerEntity) {
                drop = !(((PlayerEntity) entity).inventory.add(item) && item.isEmpty());
            }
            if (drop) {
                entity.level.addFreshEntity(dropAt(entity, item));
            }
        }
    }
    
    public static ItemEntity dropAt(LivingEntity entity, ItemStack item) {
        if (item.isEmpty()) {
            return null;
        }
        else {
            ItemEntity itemEntity = new ItemEntity(entity.level, entity.getX(), entity.getEyeY() - 0.3, entity.getZ(), item);
            itemEntity.setNoPickUpDelay();
            itemEntity.setOwner(entity.getUUID());
            return itemEntity;
        }
    }
    

    
    public static Optional<Entity> cloneEntity(Entity entity) {
        CompoundNBT entityNbt = entity.serializeNBT();
        return EntityType.create(entityNbt, entity.level);
    }
    
    
    
    public static Vector3d collide(Entity entity, Vector3d offsetVec) {
        return collide(entity, entity.getBoundingBox(), offsetVec);
    }
    
    public static Vector3d collide(Entity entity, AxisAlignedBB collisionBox, Vector3d offsetVec) {
        ISelectionContext selectionContext = ISelectionContext.of(entity);
        VoxelShape worldBorder = entity.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> worldBorderCollision = VoxelShapes.joinIsNotEmpty(worldBorder, VoxelShapes.create(collisionBox.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(worldBorder);
        Stream<VoxelShape> entityCollisions = entity.level.getEntityCollisions(entity, collisionBox.expandTowards(offsetVec), e -> true);
        ReuseableStream<VoxelShape> collisions = new ReuseableStream<>(Stream.concat(entityCollisions, worldBorderCollision));
        Vector3d vector3d = offsetVec.lengthSqr() == 0 ? offsetVec : Entity.collideBoundingBoxHeuristically(entity, offsetVec, collisionBox, entity.level, selectionContext, collisions);
        boolean flag = offsetVec.x != vector3d.x;
        boolean flag2 = offsetVec.z != vector3d.z;
        boolean flag3 = entity.isOnGround() || offsetVec.y != vector3d.y && offsetVec.y < 0.0D;
        if (entity.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
            Vector3d vector3d1 = Entity.collideBoundingBoxHeuristically(entity, new Vector3d(offsetVec.x, entity.maxUpStep, offsetVec.z), collisionBox, entity.level, selectionContext, collisions);
            Vector3d vector3d2 = Entity.collideBoundingBoxHeuristically(entity, new Vector3d(0, entity.maxUpStep, 0), collisionBox.expandTowards(offsetVec.x, 0.0D, offsetVec.z), entity.level, selectionContext, collisions);
            if (vector3d2.y < entity.maxUpStep) {
                Vector3d vector3d3 = Entity.collideBoundingBoxHeuristically(entity, new Vector3d(offsetVec.x, 0.0D, offsetVec.z), collisionBox.move(vector3d2), entity.level, selectionContext, collisions).add(vector3d2);
                if (Entity.getHorizontalDistanceSqr(vector3d3) > Entity.getHorizontalDistanceSqr(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }
            
            if (Entity.getHorizontalDistanceSqr(vector3d1) > Entity.getHorizontalDistanceSqr(vector3d)) {
                return vector3d1.add(Entity.collideBoundingBoxHeuristically(entity, new Vector3d(0.0D, -vector3d1.y + offsetVec.y, 0.0D), collisionBox.move(vector3d1), entity.level, selectionContext, collisions));
            }
        }
        
        return vector3d;
    }

    public static void rotateTowards(Entity entity, Vector3d targetPos, float maxAngle) {
        Vector3d targetVec = targetPos.subtract(entity.getEyePosition(1.0F));

        float yRot = MathUtil.yRotDegFromVec(targetVec);
        float xRot = MathUtil.xRotDegFromVec(targetVec);

        yRot = entity.yRot + MathHelper.clamp(MathHelper.degreesDifference(entity.yRot, yRot), -maxAngle, maxAngle);
        xRot = entity.xRot + MathHelper.clamp(MathHelper.degreesDifference(entity.xRot, xRot), -maxAngle, maxAngle);

        entity.yRot = yRot % 360.0F;
        entity.xRot = xRot % 360.0F;
        entity.setYHeadRot(yRot);
    }
    
    public static <T extends Entity> List<T> entitiesAround(Class<? extends T> clazz, Entity centerEntity, double radius, boolean includeSelf, @Nullable Predicate<? super T> filter) {
        Vector3d centerPos = centerEntity.getBoundingBox().getCenter();
        AxisAlignedBB aabb = new AxisAlignedBB(centerPos.subtract(radius, radius, radius), centerPos.add(radius, radius, radius));
        return centerEntity.level.getEntitiesOfClass(clazz, aabb, entity -> (includeSelf || entity != centerEntity) && (filter == null || filter.test(entity)));
    }

    public static Iterable<Entity> getAllEntities(World world) {
        return world.isClientSide() ? ((ClientWorld) world).entitiesForRendering() : ((ServerWorld) world).getAllEntities();
    }
    
    public static Vector3d getEntityPosition(Entity entity, float partialTick) {
        return partialTick == 1.0F ? entity.position() : entity.getPosition(partialTick);
    }
    
    
    
    public static boolean rayTraceTargetEquals(RayTraceResult r1, RayTraceResult r2) {
        if (r1 == null || r2 == null) return r1 == null && r2 == null;
        if (r1.getType() != r2.getType()) return false;
        
        switch (r1.getType()) {
        case MISS:
            return true;
        case BLOCK:
            BlockRayTraceResult br1 = (BlockRayTraceResult) r1;
            BlockRayTraceResult br2 = (BlockRayTraceResult) r2;
            return br1.getBlockPos().equals(br2.getBlockPos()) && br1.getDirection() == br2.getDirection();
        case ENTITY:
            EntityRayTraceResult er1 = (EntityRayTraceResult) r1;
            EntityRayTraceResult er2 = (EntityRayTraceResult) r2;
            return er1.getEntity() == er2.getEntity();
        default:
            throw new IllegalArgumentException("Unknown RayTraceResult type (it's an enum wtf)");
        }
    }
    
    
    
    public static AxisAlignedBB scale(AxisAlignedBB aabb, double scale) {
        return scale(aabb, scale, scale, scale);
    }
    
    public static AxisAlignedBB scale(AxisAlignedBB aabb, double scaleX, double scaleY, double scaleZ) {
        Vector3d center = aabb.getCenter();
        double inflX = aabb.getXsize() * scaleX / 2;
        double inflY = aabb.getYsize() * scaleY / 2;
        double inflZ = aabb.getZsize() * scaleZ / 2;
        return new AxisAlignedBB(
                center.x - inflX, center.y - inflY, center.z - inflZ,
                center.x + inflX, center.y + inflY, center.z + inflZ);
    }
    
    
    
    public static void multipliedAttrModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier, float mult) {
        ModifiableAttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifier);
            if (mult != 0) {
                attributeInstance.addTransientModifier(new AttributeModifier(modifier.getId(), 
                        modifier.getName() + " " + mult, modifier.getAmount() * mult, modifier.getOperation()));
            }
        }
    }
    
    
    
    public static boolean isControlledThisSide(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return ((PlayerEntity) entity).isLocalPlayer();
        }
        return !entity.level.isClientSide() || entity.isControlledByLocalInstance();
    }
    
    

    public static void playSound(World world, @Nullable PlayerEntity clientHandled, BlockPos blockPos, 
            SoundEvent sound, SoundCategory category, float volume, float pitch, Predicate<PlayerEntity> condition) {
        playSound(world, clientHandled, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, 
                sound, category, volume, pitch, condition);
    }

    public static void playSound(World world, @Nullable PlayerEntity clientHandled, double x, double y, double z, 
            SoundEvent sound, SoundCategory category, float volume, float pitch, Predicate<PlayerEntity> condition) {
        if (!world.isClientSide()) {
            PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(null, sound, category, volume, pitch);
            if (event.isCanceled() || event.getSound() == null) return;
            sound = event.getSound();
            category = event.getCategory();
            volume = event.getVolume();
            pitch = event.getPitch();
            NetworkUtil.broadcastWithCondition(((ServerWorld) world).getServer().getPlayerList().getPlayers(), clientHandled, 
                    x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D, world, 
                            new SPlaySoundEffectPacket(sound, category, x, y, z, volume, pitch), condition);
        }
        else if (clientHandled != null && condition.test(clientHandled)) {
            world.playSound(clientHandled, x, y, z, sound, category, volume, pitch);
        }
    }

    public static void playEitherSound(World world, @Nullable PlayerEntity clientHandled, double x, double y, double z, 
            Predicate<PlayerEntity> predicate, SoundEvent soundTrue, SoundEvent soundFalse, SoundCategory category, float volume, float pitch) {
        if (soundTrue != null) playSound(world, clientHandled, x, y, z, soundTrue, category, volume, pitch, predicate);
        if (soundFalse != null) playSound(world, clientHandled, x, y, z, soundFalse, category, volume, pitch, predicate.negate());
    }

    public static void playSound(World world, @Nullable PlayerEntity clientHandled, Entity entity, 
            SoundEvent sound, SoundCategory category, float volume, float pitch, Predicate<PlayerEntity> condition) {
        if (!world.isClientSide()) {
            PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(entity, sound, category, volume, pitch);
            if (event.isCanceled() || event.getSound() == null) return;
            sound = event.getSound();
            category = event.getCategory();
            volume = event.getVolume();
            pitch = event.getPitch();
            NetworkUtil.broadcastWithCondition(((ServerWorld) world).getServer().getPlayerList().getPlayers(), clientHandled, 
                    entity.getX(), entity.getY(), entity.getZ(), volume > 1.0F ? (double)(16.0F * volume) : 16.0D, world, 
                            new SSpawnMovingSoundEffectPacket(sound, category, entity, volume, pitch), condition);
        }
        else if (clientHandled != null && condition.test(clientHandled)) {
            world.playSound(clientHandled, entity, sound, category, volume, pitch);
        }
    }
    
    
    
    public static boolean removeEffectInstance(LivingEntity entity, EffectInstance effectInstance) {
        if (entity.getActiveEffectsMap().get(effectInstance.getEffect()) == effectInstance) {
            return entity.removeEffect(effectInstance.getEffect());
        }
        return false;
    }
    
    public static int getEffectLevel(LivingEntity entity, Effect effect) {
        EffectInstance effInstance = entity.getEffect(effect);
        return effInstance != null ? effInstance.getAmplifier() : -1;
    }
    
    
    
    public static <T extends IParticleData> int sendParticles(ServerWorld world, T particleType, 
            double x, double y, double z, int count, float xDist, float yDist, float zDist, float maxSpeed, 
            SpawnParticlePacket.SpecialContext context) {
        SpawnParticlePacket packet = new SpawnParticlePacket(particleType, false, x, y, z, xDist, yDist, zDist, maxSpeed, count, context);
        int i = 0;

        for (ServerPlayerEntity player : world.players()) {
            if (sendParticles(world, player, false, x, y, z, packet)) {
                ++i;
            }
        }

        return i;
    }

    private static boolean sendParticles(ServerWorld world, ServerPlayerEntity player, boolean force, double x, double y, double z, Object packet) {
        if (player.getLevel() != world) {
            return false;
        } else {
            BlockPos blockpos = player.blockPosition();
            if (blockpos.closerThan(new Vector3d(x, y, z), force ? 512.0D : 32.0D)) {
                PacketManager.sendToClient(packet, player);
                return true;
            } else {
                return false;
            }
        }
    }
    
    
    
    public static boolean isHandFree(LivingEntity entity, Hand hand) {
        if (entity.level.isClientSide() && entity.is(ClientUtil.getClientPlayer()) && ClientUtil.arePlayerHandsBusy()) {
            return false;
        }
        
        ItemStack item = entity.getItemInHand(hand);
        if (item.isEmpty()) {
            return true;
        }
        if (item.getItem() instanceof GlovesItem) {
            return ((GlovesItem) item.getItem()).openFingers();
        }
        return false;
    }
    
    public static HandSide getHandSide(LivingEntity entity, Hand hand) {
        return hand == Hand.MAIN_HAND ? entity.getMainArm() : getOppositeSide(entity.getMainArm());
    }
    
    public static HandSide getOppositeSide(HandSide side) {
        return side == HandSide.LEFT ? HandSide.RIGHT : HandSide.LEFT;
    }
    
    
    
    public static void loseTarget(MobEntity attackingMob, LivingEntity target) {
        if (attackingMob.getTarget() == target) {
            attackingMob.setTarget(null);
            attackingMob.targetSelector.getRunningGoals()
            .forEach(goal -> goal.stop());
        }
    }
    
    public static void makeMobNeutralTo(MobEntity mob, LivingEntity neutralTo) {
        Class<? extends LivingEntity> clazz = neutralTo.getClass();
        UUID userUuid = neutralTo.getUUID();
        Set<PrioritizedGoal> goals = CommonReflection.getGoalsSet(mob.targetSelector);
        for (PrioritizedGoal prGoal : goals) {
            Goal goal = prGoal.getGoal();
            if (goal instanceof NearestAttackableTargetGoal) {
                NearestAttackableTargetGoal<?> targetGoal = (NearestAttackableTargetGoal<?>) goal;
                Class<? extends LivingEntity> targetClass = CommonReflection.getTargetClass(targetGoal);
                
                if (targetClass == null || targetClass.isAssignableFrom(clazz)) {
                    EntityPredicate selector = CommonReflection.getTargetConditions(targetGoal);
                    if (selector != null) {
                        Predicate<LivingEntity> oldPredicate = CommonReflection.getTargetSelector(selector);
                        Predicate<LivingEntity> geUserPredicate = target -> !userUuid.equals(target.getUUID());
                        CommonReflection.setTargetConditions(targetGoal, new EntityPredicate().range(CommonReflection.getTargetDistance(targetGoal)).selector(
                                oldPredicate != null ? oldPredicate.and(geUserPredicate) : geUserPredicate));
                    }
                }
            }
        }
    }
    
    
    
    public static void onEntityResurrect(LivingEntity entity) {
        entity.deathTime = 0;
        if (!entity.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrResetDeathTimePacket(entity.getId()), entity);
            
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                if (!player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator()) {
                    player.setExperienceLevels(0);
                    player.setExperiencePoints(0);
                }
            }
        }
    }
    
    
    
    public static boolean isPotionWaterBottle(PotionEntity entity) {
        ItemStack potionItem = entity.getItem();
        return PotionUtils.getPotion(potionItem) == Potions.WATER && PotionUtils.getMobEffects(potionItem).isEmpty();
    }
    
    public static ItemStack getItemOnServer(ProjectileItemEntity entity) {
        ItemStack item = CommonReflection.getItemRaw(entity);
        return item.isEmpty() ? new ItemStack(CommonReflection.getDefaultItem(entity)) : item;
    }
    
    
    
    public static void leap(Entity entity, float leapStrength) {
        entity.setOnGround(false);
        entity.hasImpulse = true;
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setJumping(true);
        }
        Vector3d leap = Vector3d.directionFromRotation(Math.min(entity.xRot, -30F), entity.yRot).scale(leapStrength);
        entity.setDeltaMovement(leap.x, leap.y * 0.5, leap.z);
    }
    
    
    
    public static String getLanguageCode(MinecraftServer server) {
        return server.isDedicatedServer() ? "en_us" : ClientUtil.getCurrentLanguageCode();
    }
    
    
    
    public static class EntityEvents { // TODO
        public static final int HONEY_PARTICLES = 53;
    }
}
