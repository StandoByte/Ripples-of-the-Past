package com.github.standobyte.jojo.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class HamonSendoOverdriveEntity extends Entity implements IEntityAdditionalSpawnData {
    private Entity user;
    private UUID userUUID;
    private int userNetworkId;
    
    private BlockPos targetedBlockPos;
    private Direction targetedFace;
    
    private int gavePoints = 0;
    private float points;
    
    private Direction.Axis axis;
    private float radius;
    
    private int wavesToAdd;
    private int addedWaves = 0;
    private List<Wave> waves = new LinkedList<>();
    private int tickLifeSpan;
    private float damage;
    public static final float KNOCKBACK_FACTOR = 0.0F;

    public HamonSendoOverdriveEntity(World world, LivingEntity user, Direction.Axis axis) {
        this(ModEntityTypes.SENDO_HAMON_OVERDRIVE.get(), world);
        this.user = user;
        this.userUUID = user.getUUID();
        this.axis = axis;
        refreshDimensions();
    }

    public HamonSendoOverdriveEntity(EntityType<?> type, World world) {
        super(type, world);
    }
    
    public HamonSendoOverdriveEntity setRadius(float radius) {
        this.radius = radius;
        return this;
    }
    
    public HamonSendoOverdriveEntity setWaveDamage(float damage) {
        this.damage = damage;
        return this;
    }
    
    public HamonSendoOverdriveEntity setWavesCount(int waves) {
        this.wavesToAdd = waves;
        this.tickLifeSpan = waves * WAVE_ADD_TICK + WAVE_TICK_LENGTH;
        return this;
    }
    
    public HamonSendoOverdriveEntity setStatPoints(float cost) {
        this.points = cost;
        return this;
    }
    
    @Override
    public void tick() {
        if (tickCount <= tickLifeSpan) {
            super.tick();
            if (tickCount % WAVE_ADD_TICK == 0 && addedWaves++ < wavesToAdd) {
                if (!level.isClientSide()) {
                    waves.add(new Wave());
                    Vector3d soundPos = getBoundingBox().getCenter();
                    if (addedWaves < wavesToAdd) {
                        level.playSound(ClientUtil.getClientPlayer(), soundPos.x, soundPos.y, soundPos.z, ModSounds.HAMON_SPARK.get(), 
                                SoundCategory.AMBIENT, 0.25f, 1.0F + (random.nextFloat() - 0.5F) * 0.15F);
                    }
                }
                else {
                    Vector3d center = getBoundingBox().getCenter();
                    switch (axis) {
                    case X:
                        spawnSparksCircle(center.add( 0.55, 0, 0), axis, radius);
                        spawnSparksCircle(center.add(-0.55, 0, 0), axis, radius);
                        break;
                    case Y:
                        spawnSparksCircle(center.add(0,  0.55, 0), axis, radius);
                        spawnSparksCircle(center.add(0, -0.55, 0), axis, radius);
                        break;
                    case Z:
                        spawnSparksCircle(center.add(0, 0,  0.55), axis, radius);
                        spawnSparksCircle(center.add(0, 0, -0.55), axis, radius);
                        break;
                    }
                }
            }
            
            if (!level.isClientSide()) {
                Iterator<Wave> it = waves.iterator();
                while (it.hasNext()) {
                    Wave wave = it.next();
                    wave.tick(this);   
                    if (wave.remove()) {
                        it.remove();
                    }
                }
            }
            else {
                AxisAlignedBB box = makeHurtHitBox(radius);
                Vector3d cameraPos = ClientUtil.getCameraPos();
                Vector3d soundPos = new Vector3d(
                        MathHelper.clamp(cameraPos.x, box.minX, box.maxX),
                        MathHelper.clamp(cameraPos.y, box.minY, box.maxY),
                        MathHelper.clamp(cameraPos.z, box.minZ, box.maxZ));
                HamonSparksLoopSound.playSparkSound(this, soundPos, 1.0F, true);
            }
        }
        else if (!level.isClientSide()) {
            remove();
        }
    }
    


    private static class Wave {
        private int tick = 0;
        private final int length = WAVE_TICK_LENGTH;
        private List<Entity> hitEntities = new ArrayList<>();
        
        private void tick(HamonSendoOverdriveEntity entity) {
            if (!entity.level.isClientSide()) {
                List<LivingEntity> targets = entity.level.getEntitiesOfClass(LivingEntity.class, 
                        entity.getHurtHitbox(tick), entity.filter.and(target -> !hitEntities.contains(target)));
                for (LivingEntity target : targets) {
                    if (target.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.tryHurtFromSendoOverdrive(entity, WAVE_ADD_TICK)).orElse(true)
                            && DamageUtil.dealHamonDamage(target, entity.damage, entity, entity.getUser())) {
                        entity.givePointsToUser();
                    }
                    hitEntities.add(target);
                }
                tick++;
            }
        }
        
        private boolean remove() {
            if (tick >= length) {
                hitEntities.clear();
                return true;
            }
            return false;
        }
        
        
        
        private static final Class<? extends INBT> WAVE_NBT_CLASS = IntNBT.class;
        private static ListNBT saveWavesToNBT(List<Wave> waves) {
            ListNBT nbt = new ListNBT();
            for (Wave wave : waves) {
                nbt.add(IntNBT.valueOf(wave.tick));
            }
            return nbt;
        }
        
        private static List<Wave> loadWavesFromNBT(ListNBT nbt) {
            List<Wave> waves = new LinkedList<>();
            if (nbt.getElementType() == MCUtil.getNbtId(WAVE_NBT_CLASS)) {
                for (INBT waveNBT : nbt) {
                    Wave wave = new Wave();
                    wave.tick = ((IntNBT) waveNBT).getAsInt();
                }
            }
            return waves;
        }
    }
    
    
    
    private static final int WAVE_TICK_LENGTH = 15;
    private static final int WAVE_ADD_TICK = 4;
    private final Predicate<LivingEntity> filter = 
            EntityPredicates.LIVING_ENTITY_STILL_ALIVE.and(EntityPredicates.NO_CREATIVE_OR_SPECTATOR)
            .and(entity -> !entity.is(getUser()));
    private final List<AxisAlignedBB> hitboxes = Util.make(new ArrayList<>(WAVE_TICK_LENGTH), list -> {
        for (int i = 0; i < WAVE_TICK_LENGTH; i++) {
            list.add(null);
        }
    });
    
    private AxisAlignedBB getHurtHitbox(int tick) {
        AxisAlignedBB cache = hitboxes.get(tick);
        if (cache == null) {
            cache = makeHurtHitBox(this.radius * (double) (tick + 1) / (double) WAVE_TICK_LENGTH);
            hitboxes.set(tick, cache);
        }
        return cache;
    }
    
    private AxisAlignedBB makeHurtHitBox(double radius) {
        Vector3d center = getBoundingBox().getCenter();
        AxisAlignedBB hitBox = new AxisAlignedBB(center, center);
        if (axis != null) {
            switch (axis) {
            case X:
                hitBox = hitBox.inflate(WIDTH * 0.5, radius, radius);
                break;
            case Y:
                hitBox = hitBox.inflate(radius, WIDTH * 0.5, radius);
                break;
            case Z:
                hitBox = hitBox.inflate(radius, radius, WIDTH * 0.5);
                break;
            }
        }
        return hitBox;
    }
    
    @Override
    public EntitySize getDimensions(Pose pose) {
        if (axis == null) {
            return super.getDimensions(pose);
        }
        else if (axis == Direction.Axis.Y) {
            return EntitySize.fixed(radius * 2, (float) WIDTH);
        }
        else {
            return EntitySize.fixed(radius * 2, radius * 2);
        }
    }
    private static final double WIDTH = 2;
    
    @Override
    public void setPos(double pX, double pY, double pZ) {
        this.setPosRaw(pX, pY, pZ);
        AxisAlignedBB aabb = this.getDimensions(null).makeBoundingBox(pX, pY, pZ);
        this.setBoundingBox(aabb);
    }
    
    private Entity getUser() {
        if (user != null && !user.isAlive()) {
            user = null;
        }
        if (user == null) {
            if (userUUID != null && level instanceof ServerWorld) {
                user = ((ServerWorld) level).getEntity(userUUID);
            } else if (userNetworkId != 0) {
                user = level.getEntity(userNetworkId);
            }
        }
        return user;
    }
    
    private void givePointsToUser() {
        if (!level.isClientSide() && (gavePoints++ < 6 || gavePoints % 4 == 0) && points > 0) {
            Entity user = getUser();
            if (user instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional(((LivingEntity) user)).resolve().flatMap(power -> 
                power.getTypeSpecificData(ModPowers.HAMON.get())).ifPresent(hamon -> {
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, points * 0.25F);
                });
            }
        }
    }
    
    private void spawnSparksCircle(Vector3d center, Direction.Axis axis, float radius) {
        if (level.isClientSide() && axis != null && radius > 0) {
            double step = 0.2 / radius;
            for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI * step) {
                Vector3d particleVec = null;
                switch (axis) {
                case X:
                    particleVec = new Vector3d(0, Math.sin(angle), Math.cos(angle));
                    break;
                case Y:
                    particleVec = new Vector3d(Math.cos(angle), 0, Math.sin(angle));
                    break;
                case Z:
                    particleVec = new Vector3d(Math.sin(angle), Math.cos(angle), 0);
                    break;
                }
                particleVec = particleVec.scale(radius / WAVE_TICK_LENGTH);
                CustomParticlesHelper.addSendoHamonOverdriveParticle(level, ModParticles.HAMON_SPARK.get(), axis, 
                        center.x, center.y, center.z, particleVec.x, particleVec.y, particleVec.z, WAVE_TICK_LENGTH);
            }
        }
    }
    
    public void setBlockTarget(BlockPos targetedBlockPos, Direction targetedFace) {
        this.targetedBlockPos = targetedBlockPos;
        this.targetedFace = targetedFace;
    }
    
    public BlockPos getTargetedBlockPos() {
        return targetedBlockPos;
    }
    
    public Direction getTargetedFace() {
        return targetedFace;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.hasUUID("Owner")) {
            this.userUUID = nbt.getUUID("Owner");
        }
        this.gavePoints = nbt.getInt("GavePoints");
        this.points = nbt.getFloat("Points");
        this.axis = MCUtil.nbtGetEnum(nbt, "Axis", Direction.Axis.class);
        this.radius = nbt.getFloat("Radius");
        this.wavesToAdd = nbt.getInt("WavesToAdd");
        this.addedWaves = nbt.getInt("WavesAdded");
        if (nbt.contains("Waves", MCUtil.getNbtId(ListNBT.class))) {
            this.waves = Wave.loadWavesFromNBT(nbt.getList("Waves", MCUtil.getNbtId(Wave.WAVE_NBT_CLASS)));
        }
        this.tickLifeSpan = nbt.getInt("LifeSpan");
        this.tickCount = nbt.getInt("Age");
        this.damage = nbt.getFloat("Damage");
        
        if (nbt.contains("TargetedBlock", MCUtil.getNbtId(CompoundNBT.class))) {
            this.targetedBlockPos = NBTUtil.readBlockPos(nbt.getCompound("TargetedBlock"));
        }
        if (nbt.contains("TargetedFace")) {
            this.targetedFace = MCUtil.nbtGetEnum(nbt, "TargetedFace", Direction.class);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        if (userUUID != null) {
            nbt.putUUID("Owner", userUUID);
        }
        nbt.putInt("GavePoints", gavePoints);
        nbt.putFloat("Points", points);
        MCUtil.nbtPutEnum(nbt, "Axis", axis);
        nbt.putFloat("Radius", radius);
        nbt.putInt("WavesToAdd", wavesToAdd);
        nbt.putInt("WavesAdded", addedWaves);
        nbt.put("Waves", Wave.saveWavesToNBT(waves));
        nbt.putInt("LifeSpan", tickLifeSpan);
        nbt.putInt("Age", tickCount);
        nbt.putFloat("Damage", damage);
        
        if (targetedBlockPos != null) {
            nbt.put("TargetedBlock", NBTUtil.writeBlockPos(targetedBlockPos));
        }
        if (targetedFace != null) {
            MCUtil.nbtPutEnum(nbt, "TargetedFace", targetedFace);
        }
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        NetworkUtil.writeOptionally(buffer, axis, e -> buffer.writeEnum(e));
        buffer.writeFloat(radius);
        buffer.writeVarInt(wavesToAdd);
        buffer.writeVarInt(addedWaves);
        buffer.writeVarInt(tickLifeSpan);
        buffer.writeVarInt(tickCount);
        
        NetworkUtil.writeOptionally(buffer, targetedBlockPos, pos -> buffer.writeBlockPos(pos));
        NetworkUtil.writeOptionally(buffer, targetedFace, face -> buffer.writeEnum(face));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.axis = NetworkUtil.readOptional(additionalData, () -> additionalData.readEnum(Direction.Axis.class)).orElse(null);
        this.radius = additionalData.readFloat();
        this.wavesToAdd = additionalData.readVarInt();
        this.addedWaves = additionalData.readVarInt();
        this.tickLifeSpan = additionalData.readVarInt();
        this.tickCount = additionalData.readVarInt();
        absMoveTo(xo, yo, zo, yRot, xRot);
        
        NetworkUtil.readOptional(additionalData, () -> additionalData.readBlockPos()).ifPresent(pos -> this.targetedBlockPos = pos);
        NetworkUtil.readOptional(additionalData, () -> additionalData.readEnum(Direction.class)).ifPresent(face -> this.targetedFace = face);
    }
}
