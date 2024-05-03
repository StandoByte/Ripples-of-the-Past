package com.github.standobyte.jojo.network;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromclient.ClClickActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClDoubleShiftPressPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonAbandonButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonInteractAskTeacherPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonInteractTeachPacket;
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
import com.github.standobyte.jojo.network.packets.fromclient.ClPhotoAssignIdPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClPhotoRequestPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClPhotoSaveDataPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSGameInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSPickThoughtsPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClReadHamonBreathTabPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRunAwayPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSoulRotationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClWalkmanControlsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ActionCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ArrowXpLevelsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.CommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.CustomExplosionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillAddPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillRemovePacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSyncOnLoadPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonUiEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.LeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.MaxAchievedResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.NotificationSyncPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PhotoDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PhotoForOtherPlayerPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PhotoIdAssignedPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.network.packets.fromserver.PreviousPowerTypesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PreviousStandTypesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetResolveValuePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetSyncedCommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveBoostsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveEffectStartPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveLevelPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ServerIdPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SoulSpawnPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SpawnParticlePacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandAssignmentDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandFullClearPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrCosmeticItemsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityPosPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDoubleShiftPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrEntitySpecialEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonAuraColorPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonBreathStabilityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonCharacterTechniquePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEnergyTicksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEntityChargePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonFlagsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonLiquidWalkingPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonSyncPlayerLearnerPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNoMotionLerpPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerContinuousActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerVisualDetailPacket;
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
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.TrSYOBarrageFinisherPacket;

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
    private static SimpleChannel serverChannel;
    private static SimpleChannel clientChannel;
    private static int packetIndex = 0;

    public static void init() {
        serverChannel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(JojoMod.MOD_ID, "server_channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();
        clientChannel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(JojoMod.MOD_ID, "client_channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        packetIndex = 0;
        registerMessage(clientChannel, new ClHasInputPacket.Handler(),                     Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClDoubleShiftPressPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClToggleStandSummonPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClToggleStandManualControlPacket.Handler(),     Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClClickActionPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHeldActionTargetPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClStopHeldActionPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonWindowOpenedPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonInteractAskTeacherPacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonInteractTeachPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonLearnButtonPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonPickTechniquePacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonAbandonButtonPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonResetSkillsButtonPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClHamonMeditationPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClLeavesGliderColorPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClRunAwayPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClStandManualMovementPacket.Handler(),          Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClOnLeapPacket.Handler(),                       Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClOnStandDashPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClSetStandSkinPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClSoulRotationPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClRemovePlayerSoulEntityPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClWalkmanControlsPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClReadHamonBreathTabPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClPhotoAssignIdPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClPhotoSaveDataPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClPhotoRequestPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClRPSGameInputPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(clientChannel, new ClRPSPickThoughtsPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_SERVER));

        packetIndex = 0;
        registerMessage(serverChannel, new ServerIdPacket.Handler(),                       Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrDoubleShiftPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrTypeNonStandPowerPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrTypeStandInstancePacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHeldActionPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrEnergyPacket.Handler(),                       Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ActionCooldownPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new BloodParticlesPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonStatsPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new HamonExercisesPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonMeditationPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonSyncPlayerLearnerPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new HamonTeachersSkillsPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new HamonSkillAddPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonCharacterTechniquePacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new HamonSkillRemovePacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonParticlesPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonBreathStabilityPacket.Handler(),         Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonEnergyTicksPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonEntityChargePacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonAuraColorPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonLiquidWalkingPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrHamonFlagsPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new HamonUiEffectPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new HamonSyncOnLoadPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrVampirismDataPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrStaminaPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ArrowXpLevelsDataPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PreviousStandTypesPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PreviousPowerTypesPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ResolvePacket.Handler(),                        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrStandEffectPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ResetResolveValuePacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ResolveLevelPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ResolveBoostsPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new MaxAchievedResolvePacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new SkippedStandProgressionPacket.Handler(),        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ResolveEffectStartPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new StandActionLearningPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new StandFullClearPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrSetStandEntityPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new StandStatsDataPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new StandAssignmentDataPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new StandControlStatusPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new StandCancelManualMovementPacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrStandTaskTargetPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrStandTaskModifierPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new UpdateClientCapCachePacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new SoulSpawnPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrPlayerContinuousActionPacket.Handler(),       Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrKnivesCountPacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrCosmeticItemsPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrWalkmanEarbudsPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new LeapCooldownPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new NotificationSyncPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrEntitySpecialEffectPacket.Handler(),          Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrPlayerVisualDetailPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PlayVoiceLinePacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PlaySoundAtClientPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PlaySoundAtStandEntityPacket.Handler(),         Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrBarrageHitSoundPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TimeStopInstancePacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TimeStopPlayerStatePacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TimeStopPlayerJoinPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new RefreshMovementInTimeStopPacket.Handler(),      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrNoMotionLerpPacket.Handler(),                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrDirectEntityPosPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new CommonConfigPacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new ResetSyncedCommonConfigPacket.Handler(),        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new SpawnParticlePacket.Handler(),                  Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrSYOBarrageFinisherPacket.Handler(),           Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new CustomExplosionPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new TrDirectEntityDataPacket.Handler(),             Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new BrokenChunkBlocksPacket.Handler(),              Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new CDBlocksRestoredPacket.Handler(),               Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PhotoIdAssignedPacket.Handler(),                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PhotoDataPacket.Handler(),                      Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new RPSGameStatePacket.Handler(),                   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new RPSOpponentPickThoughtsPacket.Handler(),        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(serverChannel, new PhotoForOtherPlayerPacket.Handler(),            Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    
    private static <MSG> void registerMessage(SimpleChannel channel, IModPacketHandler<MSG> handler, Optional<NetworkDirection> networkDirection) {
        if (packetIndex > 127) {
            throw new IllegalStateException("Too many packets (> 127) registered for a single channel!");
        }
        channel.registerMessage(packetIndex++, handler.getPacketClass(), handler::encode, handler::decode, handler::enqueueHandleSetHandled, networkDirection);
    }
    
    public static void sendToServer(Object msg) {
        clientChannel.sendToServer(msg);
    }

    public static void sendToClient(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer)) {
            serverChannel.send(PacketDistributor.PLAYER.with(() -> player), msg);
        }
    }

    public static void sendToClientsTracking(Object msg, Entity entity) {
        serverChannel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    public static void sendToClientsTrackingAndSelf(Object msg, Entity entity) {
        serverChannel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    public static void sendToNearby(Object msg, @Nullable ServerPlayerEntity excluded, double x, double y, double z, double radius, RegistryKey<World> dimension) {
        serverChannel.send(PacketDistributor.NEAR.with(() -> new TargetPoint(excluded, x, y, z, radius, dimension)), msg);
    }

    public static void sendToTrackingChunk(Object msg, Chunk chunk) {
        if (chunk != null) {
            serverChannel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
        }
    }
    
    public static void sendGlobally(Object msg, @Nullable RegistryKey<World> dimension) {
        if (dimension != null) {
            serverChannel.send(PacketDistributor.DIMENSION.with(() -> dimension), msg);
        }
        else {
            serverChannel.send(PacketDistributor.ALL.noArg(), msg);
        }
    }
    
    
    
    public static void sendGloballyWithCondition(Object msg, @Nullable RegistryKey<World> dimension, Predicate<ServerPlayerEntity> condition) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            if ((dimension == null || player.level.dimension() == dimension) && condition.test(player)) {
                serverChannel.send(PacketDistributor.PLAYER.with(() -> player), msg);
            }
        }
    }
}
