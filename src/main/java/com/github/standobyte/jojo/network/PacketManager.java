package com.github.standobyte.jojo.network;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromclient.ClActionsLayoutPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClClickActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClDoubleShiftPressPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonAbandonButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonLearnButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonPickTechniquePacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonWindowOpenedPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHasInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHeldActionTargetPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClLeavesGliderColorPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnStandDashPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSGameInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSPickThoughtsPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClReadHamonBreathTabPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRunAwayPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSoulRotationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClWalkmanControlsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ActionCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ActionsLayoutPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.CommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.CustomExplosionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HadPowerTypesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillAddPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillRemovePacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSyncOnLoadPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonUiEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.LeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.MaxAchievedResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.NotificationSyncPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetResolveValuePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetSyncedCommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveBoostsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveEffectStartPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveLevelPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SpawnParticlePacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionsClearLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityPosPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDoubleShiftPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonAuraColorPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonBreathStabilityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonCharacterTechniquePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEnergyTicksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNoMotionLerpPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerContinuousActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandTaskModifierPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandTaskTargetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeNonStandPowerPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrVampirismDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrWalkmanEarbudsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.UpdateClientCapCachePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.CDBlocksRestoredPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.RPSGameStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.RPSOpponentPickThoughtsPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

// FIXME barrage clashes cause client logs getting spammed with network exceptions (either the payload is an EmptyByteBuf or "Received invalid discriminator byte" error)
public class PacketManager {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel channel;
    private static int packetIndex = 0;

    public static void init() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(JojoMod.MOD_ID, "main_channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();
        
        registerMessage(channel, new ClHasInputPacket.Handler(),                     Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClDoubleShiftPressPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClActionsLayoutPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClToggleStandSummonPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClToggleStandManualControlPacket.Handler(),     Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClClickActionPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHeldActionTargetPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClStopHeldActionPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHamonWindowOpenedPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHamonLearnButtonPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHamonPickTechniquePacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHamonAbandonButtonPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHamonResetSkillsButtonPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClHamonMeditationPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClLeavesGliderColorPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClRunAwayPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClStandManualMovementPacket.Handler(),          Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClOnLeapPacket.Handler(),                       Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClOnStandDashPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClSoulRotationPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClRemovePlayerSoulEntityPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClWalkmanControlsPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClReadHamonBreathTabPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClRPSGameInputPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(channel, new ClRPSPickThoughtsPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        registerMessage(channel, new TrDoubleShiftPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrTypeNonStandPowerPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrTypeStandInstancePacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHeldActionPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ActionsLayoutPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrEnergyPacket.Handler(),                       Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ActionCooldownPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new BloodParticlesPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonStatsPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HamonExercisesPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonMeditationPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HamonTeachersSkillsPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HamonSkillAddPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonCharacterTechniquePacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HamonSkillRemovePacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonParticlesPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonBreathStabilityPacket.Handler(),         Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonEnergyTicksPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrHamonAuraColorPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HamonUiEffectPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HamonSyncOnLoadPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrVampirismDataPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrStaminaPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new HadPowerTypesPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ResolvePacket.Handler(),                        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrStandEffectPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ResetResolveValuePacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ResolveLevelPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ResolveBoostsPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new MaxAchievedResolvePacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new SkippedStandProgressionPacket.Handler(),        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ResolveEffectStartPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new StandActionLearningPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new StandActionsClearLearningPacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrSetStandEntityPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new StandStatsDataPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new StandControlStatusPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new StandCancelManualMovementPacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrStandTaskTargetPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrStandTaskModifierPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new UpdateClientCapCachePacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrPlayerContinuousActionPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrKnivesCountPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrWalkmanEarbudsPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new LeapCooldownPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new NotificationSyncPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new PlayVoiceLinePacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new PlaySoundAtClientPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new PlaySoundAtStandEntityPacket.Handler(),         Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrBarrageHitSoundPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TimeStopInstancePacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TimeStopPlayerStatePacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TimeStopPlayerJoinPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new RefreshMovementInTimeStopPacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrNoMotionLerpPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new TrDirectEntityPosPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new CommonConfigPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new ResetSyncedCommonConfigPacket.Handler(),        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new SpawnParticlePacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new CustomExplosionPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new BrokenChunkBlocksPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new CDBlocksRestoredPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new RPSGameStatePacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(channel, new RPSOpponentPickThoughtsPacket.Handler(),        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    
    private static <MSG> void registerMessage(SimpleChannel channel, IModPacketHandler<MSG> handler, Optional<NetworkDirection> networkDirection) {
        if (packetIndex > 127) {
            throw new IllegalStateException("Too many packets (> 127) registered for a single channel!");
        }
        channel.registerMessage(packetIndex++, handler.getPacketClass(), handler::encode, handler::decode, handler::enqueueHandleSetHandled, networkDirection);
    }
    
    public static void sendToServer(Object msg) {
        channel.sendToServer(msg);
    }

    public static void sendToClient(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer)) {
            channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
    }

    public static void sendToClientsTracking(Object msg, Entity entity) {
        channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    public static void sendToClientsTrackingAndSelf(Object msg, Entity entity) {
        channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    public static void sendToNearby(Object msg, @Nullable ServerPlayerEntity excluded, double x, double y, double z, double radius, RegistryKey<World> dimension) {
        channel.send(PacketDistributor.NEAR.with(() -> new TargetPoint(excluded, x, y, z, radius, dimension)), msg);
    }

    public static void sendToTrackingChunk(Object msg, Chunk chunk) {
        if (chunk != null) {
            channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
        }
    }
    
    public static void sendGlobally(Object msg, @Nullable RegistryKey<World> dimension) {
        if (dimension != null) {
            channel.send(PacketDistributor.DIMENSION.with(() -> dimension), msg);
        }
        else {
            channel.send(PacketDistributor.ALL.noArg(), msg);
        }
    }
    
    
    
    public static void sendGloballyWithCondition(Object msg, @Nullable RegistryKey<World> dimension, Predicate<ServerPlayerEntity> condition) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            if ((dimension == null || player.level.dimension() == dimension) && condition.test(player)) {
                channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
            }
        }
    }
}
