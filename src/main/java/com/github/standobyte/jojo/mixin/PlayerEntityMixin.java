package com.github.standobyte.jojo.mixin;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.capability.entity.player.PlayerMixinExtension;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrPossessEntityPacket;
import com.github.standobyte.jojo.util.mc.EntityOwnerResolver;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.IPlayerLeap;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerMixinExtension, IPlayerLeap, IPlayerPossess {
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void jojoMixinTick(CallbackInfo ci) {
        leapFlagTick();
        jojoTickEntityPossession();
    }
    
    @Override
    public void jojoPlayerUndeadCreature(CallbackInfoReturnable<CreatureAttribute> ci) {
        if (JojoModUtil.playerUndeadAttribute((LivingEntity) (Object) this)) {
            ci.setReturnValue(CreatureAttribute.UNDEAD);
        }
    }
    
    @Override
    public boolean _isEntityOnGround() {
        return isOnGround();
    }
    
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void jojoPlayerWallClimb(Vector3d pTravelVector, CallbackInfo ci) {
        PlayerEntity thisPlayer = (PlayerEntity) (Object) this;
        if (HamonWallClimbing2.travelWallClimb(thisPlayer, pTravelVector)) {
            ci.cancel();
        }
    }
    

    private boolean isDoingLeap;
    @Override
    public void setIsDoingLeap(boolean isDoingLeap) {
        this.isDoingLeap = isDoingLeap;
    }
    
    @Override
    public boolean isDoingLeap() {
        return isDoingLeap;
    }
    
//    private boolean isDoingDash = false;
    
    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    public void jojoBackOffFromEdgeFlag(CallbackInfoReturnable<Boolean> ci) {
        if (isDoingLeap) {
            ci.setReturnValue(false);
        }
//        else if (isDoingDash) {
//            ci.setReturnValue(true);
//        }
    }
    
    
    
    private final EntityOwnerResolver jojoPossessedEntity = new EntityOwnerResolver();
    private Optional<GameType> jojoPossessPrevGameMode = Optional.empty();
    private boolean jojoPossessingAsAlive;
    private IForgeRegistryEntry<?> jojoPossessionContext;
    
    /* TODO specific interactions when possessing someone with asAlive flag:
     *   render hp/hunger/etc.
     *   tick the player
     *     tick status effects
     *   allow the power HUD to be opened
     *   ...?
     */
    @Override
    public void jojoPossessEntity(@Nullable Entity entity, boolean asAlive, IForgeRegistryEntry<?> context) {
        jojoPossessedEntity.setOwner(entity);
        if (!level.isClientSide()) {
            ServerPlayerEntity player = ((ServerPlayerEntity) (Entity) this);
            if (entity != null) {
                jojoPossessPrevGameMode = Optional.of(player.gameMode.getGameModeForPlayer());
                player.setGameMode(GameType.SPECTATOR);
                player.setCamera(entity);
            }
            else {
                jojoPossessPrevGameMode.ifPresent(player::setGameMode);
                jojoPossessPrevGameMode = Optional.empty();
                player.setCamera(player);
            }
            PacketManager.sendToClientsTrackingAndSelf(new TrPossessEntityPacket(
                    this.getId(), jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                    jojoPossessPrevGameMode, context), this);
        }
        this.jojoPossessingAsAlive = asAlive;
        this.jojoPossessionContext = context;
    }
    
    private void jojoTickEntityPossession() {
        if (!level.isClientSide() && jojoPossessedEntity.hasEntityId()) {
            Entity possessed = jojoGetPossessedEntity();
            if (possessed == null || !possessed.isAlive() || !this.isAlive()) {
                jojoPossessEntity(null, jojoPossessingAsAlive, jojoPossessionContext);
            }
        }
    }
    
    @Override
    @Nullable public Entity jojoGetPossessedEntity() {
        return jojoPossessedEntity.getEntity(level);
    }
    
    @Override
    public boolean jojoIsPossessingAsAlive() {
        return jojoPossessingAsAlive;
    }
    
    @Override
    public Optional<GameType> jojoGetPrePossessGameMode() {
        return jojoPossessPrevGameMode;
    }
    
    @Override
    public void jojoSetPrePossessGameMode(@Nonnull Optional<GameType> gameMode) {
        this.jojoPossessPrevGameMode = gameMode;
        if (!level.isClientSide()) {
            PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                    jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                    jojoPossessPrevGameMode, jojoPossessionContext), ((ServerPlayerEntity) (Entity) this));
        }
    }
    
    @Override
    public IForgeRegistryEntry<?> jojoGetPossessionContext() {
        return jojoPossessionContext;
    }
    
    @Override
    public void jojoOnPossessingDead() {
        if (!level.isClientSide() && getType() == EntityType.PLAYER) {
            Entity possessedEntity = jojoGetPossessedEntity();
            if (possessedEntity != null) {
                jojoPossessEntity(null, false, jojoPossessionContext);
            }
        }
    }
    
    
    @Override
    public void toNBT(CompoundNBT forgeCapNbt) {
        jojoPossessedEntity.saveNbt(forgeCapNbt, "Possessed");
        jojoPossessPrevGameMode.ifPresent(gameMode -> forgeCapNbt.putString("PossessPrevMode", gameMode.getName()));
        forgeCapNbt.putBoolean("PossessAsAlive", jojoPossessingAsAlive);
        if (jojoPossessionContext != null) {
            CompoundNBT ctxNbt = new CompoundNBT();
            IForgeRegistry<?> retrievedRegistry = RegistryManager.ACTIVE.getRegistry(jojoPossessionContext.getRegistryType());
            if (retrievedRegistry != null) {
                ctxNbt.putString("Registry", retrievedRegistry.getRegistryName().toString());
                ctxNbt.putString("Obj", jojoPossessionContext.getRegistryName().toString());
                forgeCapNbt.put("Ctx", ctxNbt);
            }
        }
    }
    
    @Override
    public void fromNBT(CompoundNBT forgeCapNbt) {
        jojoPossessedEntity.loadNbt(forgeCapNbt, "Possessed");
        jojoPossessPrevGameMode = Optional.ofNullable(GameType.byName(forgeCapNbt.getString("PossessPrevMode"), null));
        jojoPossessingAsAlive = forgeCapNbt.getBoolean("PossessAsAlive");
        jojoPossessionContext = MCUtil.nbtGetCompoundOptional(forgeCapNbt, "Ctx").map(ctxNbt -> {
            if (ctxNbt.contains("Registry", Constants.NBT.TAG_STRING) && ctxNbt.contains("Obj", Constants.NBT.TAG_STRING)) {
                ResourceLocation registryId = new ResourceLocation(ctxNbt.getString("Registry"));
                ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(registryId);
                if (registry != null) {
                    ResourceLocation objId = new ResourceLocation(ctxNbt.getString("Obj"));
                    if (registry.containsKey(objId)) {
                        return registry.getValue(objId);
                    }
                }
            }
            
            return null;
        }).orElse(null);
    }

    @Override
    public void syncToClient(ServerPlayerEntity thisAsPlayer) {
        PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                jojoPossessPrevGameMode, jojoPossessionContext), thisAsPlayer);
        Entity cameraEntity = jojoPossessedEntity.getEntity(level);
        if (cameraEntity != null) {
            thisAsPlayer.setCamera(cameraEntity);
        }
    }

    @Override
    public void syncToTracking(ServerPlayerEntity tracking) {
        PacketManager.sendToClient(new TrPossessEntityPacket(this.getId(), 
                jojoPossessedEntity.getNetworkId(), jojoPossessingAsAlive, 
                Optional.empty(), null), tracking);
    }
}
