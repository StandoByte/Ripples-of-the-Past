package com.github.standobyte.jojo.network;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.network.packets.fromclient.ClClickActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonLearnButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonStartMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonWindowOpenedPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHasInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHeldActionTargetPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnStandDashPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSGameInputPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRPSPickThoughtsPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRunAwayPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSoulRotationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BrokenChunkBlocksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.CommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.EntityTimeResumeSoundPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillLearnPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillsResetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.LeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.MaxAchievedResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetResolveValuePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetSyncedCommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveBoostsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveEffectStartPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveLevelPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionsClearLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityPosPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNoMotionLerpPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNonStandFlagPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandTaskModifierPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandTaskTargetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeNonStandPowerPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeStandInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.UpdateClientCapCachePacket;
import com.github.standobyte.jojo.network.packets.fromserver.stand_specific.CDBlocksRestoredPacket;
import com.github.standobyte.jojo.network.packets.fromserver.stand_specific.RPSGameStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.stand_specific.RPSOpponentPickThoughtsPacket;

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

    public static void init() {
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(JojoMod.MOD_ID, "main_channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();
        int index = 0;
        
        channel.registerMessage(index++, ClHasInputPacket.class,
                ClHasInputPacket::encode,
                ClHasInputPacket::decode,
                ClHasInputPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        channel.registerMessage(index++, ClToggleStandSummonPacket.class,
                ClToggleStandSummonPacket::encode,
                ClToggleStandSummonPacket::decode,
                ClToggleStandSummonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClToggleStandManualControlPacket.class,
                ClToggleStandManualControlPacket::encode,
                ClToggleStandManualControlPacket::decode,
                ClToggleStandManualControlPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClClickActionPacket.class,
                ClClickActionPacket::encode,
                ClClickActionPacket::decode,
                ClClickActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClHeldActionTargetPacket.class,
                ClHeldActionTargetPacket::encode,
                ClHeldActionTargetPacket::decode,
                ClHeldActionTargetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClStopHeldActionPacket.class,
                ClStopHeldActionPacket::encode,
                ClStopHeldActionPacket::decode,
                ClStopHeldActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClHamonWindowOpenedPacket.class,
                ClHamonWindowOpenedPacket::encode,
                ClHamonWindowOpenedPacket::decode,
                ClHamonWindowOpenedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClHamonLearnButtonPacket.class,
                ClHamonLearnButtonPacket::encode,
                ClHamonLearnButtonPacket::decode,
                ClHamonLearnButtonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClHamonResetSkillsButtonPacket.class,
                ClHamonResetSkillsButtonPacket::encode,
                ClHamonResetSkillsButtonPacket::decode,
                ClHamonResetSkillsButtonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClHamonStartMeditationPacket.class,
                ClHamonStartMeditationPacket::encode,
                ClHamonStartMeditationPacket::decode,
                ClHamonStartMeditationPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClRunAwayPacket.class,
                ClRunAwayPacket::encode,
                ClRunAwayPacket::decode,
                ClRunAwayPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClStandManualMovementPacket.class,
                ClStandManualMovementPacket::encode,
                ClStandManualMovementPacket::decode,
                ClStandManualMovementPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClOnLeapPacket.class,
                ClOnLeapPacket::encode,
                ClOnLeapPacket::decode,
                ClOnLeapPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClOnStandDashPacket.class,
                ClOnStandDashPacket::encode,
                ClOnStandDashPacket::decode,
                ClOnStandDashPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClSoulRotationPacket.class,
                ClSoulRotationPacket::encode,
                ClSoulRotationPacket::decode,
                ClSoulRotationPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClRemovePlayerSoulEntityPacket.class,
                ClRemovePlayerSoulEntityPacket::encode,
                ClRemovePlayerSoulEntityPacket::decode,
                ClRemovePlayerSoulEntityPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClRPSGameInputPacket.class,
                ClRPSGameInputPacket::encode,
                ClRPSGameInputPacket::decode,
                ClRPSGameInputPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        channel.registerMessage(index++, ClRPSPickThoughtsPacket.class,
                ClRPSPickThoughtsPacket::encode,
                ClRPSPickThoughtsPacket::decode,
                ClRPSPickThoughtsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        
        

        channel.registerMessage(index++, TrTypeNonStandPowerPacket.class, 
                TrTypeNonStandPowerPacket::encode, 
                TrTypeNonStandPowerPacket::decode, 
                TrTypeNonStandPowerPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrTypeStandInstancePacket.class, 
                TrTypeStandInstancePacket::encode, 
                TrTypeStandInstancePacket::decode, 
                TrTypeStandInstancePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrHeldActionPacket.class,
                TrHeldActionPacket::encode,
                TrHeldActionPacket::decode,
                TrHeldActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrEnergyPacket.class,
                TrEnergyPacket::encode,
                TrEnergyPacket::decode,
                TrEnergyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrCooldownPacket.class,
                TrCooldownPacket::encode,
                TrCooldownPacket::decode,
                TrCooldownPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, BloodParticlesPacket.class,
                BloodParticlesPacket::encode,
                BloodParticlesPacket::decode,
                BloodParticlesPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrHamonStatsPacket.class,
                TrHamonStatsPacket::encode,
                TrHamonStatsPacket::decode,
                TrHamonStatsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, HamonExercisesPacket.class,
                HamonExercisesPacket::encode,
                HamonExercisesPacket::decode,
                HamonExercisesPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, HamonTeachersSkillsPacket.class,
                HamonTeachersSkillsPacket::encode,
                HamonTeachersSkillsPacket::decode,
                HamonTeachersSkillsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, HamonSkillLearnPacket.class,
                HamonSkillLearnPacket::encode,
                HamonSkillLearnPacket::decode,
                HamonSkillLearnPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, HamonSkillsResetPacket.class,
                HamonSkillsResetPacket::encode,
                HamonSkillsResetPacket::decode,
                HamonSkillsResetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrHamonParticlesPacket.class,
                TrHamonParticlesPacket::encode,
                TrHamonParticlesPacket::decode,
                TrHamonParticlesPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrNonStandFlagPacket.class,
                TrNonStandFlagPacket::encode,
                TrNonStandFlagPacket::decode,
                TrNonStandFlagPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrStaminaPacket.class,
                TrStaminaPacket::encode,
                TrStaminaPacket::decode,
                TrStaminaPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, ResolvePacket.class,
                ResolvePacket::encode,
                ResolvePacket::decode,
                ResolvePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrStandEffectPacket.class,
                TrStandEffectPacket::encode,
                TrStandEffectPacket::decode,
                TrStandEffectPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, ResetResolveValuePacket.class,
                ResetResolveValuePacket::encode,
                ResetResolveValuePacket::decode,
                ResetResolveValuePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, ResolveLevelPacket.class,
                ResolveLevelPacket::encode,
                ResolveLevelPacket::decode,
                ResolveLevelPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, ResolveBoostsPacket.class,
                ResolveBoostsPacket::encode,
                ResolveBoostsPacket::decode,
                ResolveBoostsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, MaxAchievedResolvePacket.class,
                MaxAchievedResolvePacket::encode,
                MaxAchievedResolvePacket::decode,
                MaxAchievedResolvePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, SkippedStandProgressionPacket.class,
                SkippedStandProgressionPacket::encode,
                SkippedStandProgressionPacket::decode,
                SkippedStandProgressionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, ResolveEffectStartPacket.class,
                ResolveEffectStartPacket::encode,
                ResolveEffectStartPacket::decode,
                ResolveEffectStartPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, StandActionLearningPacket.class,
                StandActionLearningPacket::encode,
                StandActionLearningPacket::decode,
                StandActionLearningPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, StandActionsClearLearningPacket.class,
                StandActionsClearLearningPacket::encode,
                StandActionsClearLearningPacket::decode,
                StandActionsClearLearningPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrSetStandEntityPacket.class,
                TrSetStandEntityPacket::encode,
                TrSetStandEntityPacket::decode,
                TrSetStandEntityPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, StandStatsDataPacket.class,
                StandStatsDataPacket::encode,
                StandStatsDataPacket::decode,
                StandStatsDataPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, StandControlStatusPacket.class,
                StandControlStatusPacket::encode,
                StandControlStatusPacket::decode,
                StandControlStatusPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, StandCancelManualMovementPacket.class,
                StandCancelManualMovementPacket::encode,
                StandCancelManualMovementPacket::decode,
                StandCancelManualMovementPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrStandTaskTargetPacket.class,
                TrStandTaskTargetPacket::encode,
                TrStandTaskTargetPacket::decode,
                TrStandTaskTargetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrStandTaskModifierPacket.class,
                TrStandTaskModifierPacket::encode,
                TrStandTaskModifierPacket::decode,
                TrStandTaskModifierPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, UpdateClientCapCachePacket.class,
                UpdateClientCapCachePacket::encode,
                UpdateClientCapCachePacket::decode,
                UpdateClientCapCachePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrKnivesCountPacket.class,
                TrKnivesCountPacket::encode,
                TrKnivesCountPacket::decode,
                TrKnivesCountPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, LeapCooldownPacket.class,
                LeapCooldownPacket::encode,
                LeapCooldownPacket::decode,
                LeapCooldownPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, PlayVoiceLinePacket.class,
                PlayVoiceLinePacket::encode,
                PlayVoiceLinePacket::decode,
                PlayVoiceLinePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, PlaySoundAtClientPacket.class,
                PlaySoundAtClientPacket::encode,
                PlaySoundAtClientPacket::decode,
                PlaySoundAtClientPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrBarrageHitSoundPacket.class,
                TrBarrageHitSoundPacket::encode,
                TrBarrageHitSoundPacket::decode,
                TrBarrageHitSoundPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TimeStopInstancePacket.class,
                TimeStopInstancePacket::encode,
                TimeStopInstancePacket::decode,
                TimeStopInstancePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TimeStopPlayerStatePacket.class,
                TimeStopPlayerStatePacket::encode,
                TimeStopPlayerStatePacket::decode,
                TimeStopPlayerStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TimeStopPlayerJoinPacket.class,
                TimeStopPlayerJoinPacket::encode,
                TimeStopPlayerJoinPacket::decode,
                TimeStopPlayerJoinPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, RefreshMovementInTimeStopPacket.class,
                RefreshMovementInTimeStopPacket::encode,
                RefreshMovementInTimeStopPacket::decode,
                RefreshMovementInTimeStopPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, EntityTimeResumeSoundPacket.class,
                EntityTimeResumeSoundPacket::encode,
                EntityTimeResumeSoundPacket::decode,
                EntityTimeResumeSoundPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrNoMotionLerpPacket.class,
                TrNoMotionLerpPacket::encode,
                TrNoMotionLerpPacket::decode,
                TrNoMotionLerpPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, TrDirectEntityPosPacket.class,
                TrDirectEntityPosPacket::encode,
                TrDirectEntityPosPacket::decode,
                TrDirectEntityPosPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, CommonConfigPacket.class,
                CommonConfigPacket::encode,
                CommonConfigPacket::decode,
                CommonConfigPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, ResetSyncedCommonConfigPacket.class,
                ResetSyncedCommonConfigPacket::encode,
                ResetSyncedCommonConfigPacket::decode,
                ResetSyncedCommonConfigPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, BrokenChunkBlocksPacket.class,
                BrokenChunkBlocksPacket::encode,
                BrokenChunkBlocksPacket::decode,
                BrokenChunkBlocksPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, CDBlocksRestoredPacket.class,
                CDBlocksRestoredPacket::encode,
                CDBlocksRestoredPacket::decode,
                CDBlocksRestoredPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, RPSGameStatePacket.class,
                RPSGameStatePacket::encode,
                RPSGameStatePacket::decode,
                RPSGameStatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.registerMessage(index++, RPSOpponentPickThoughtsPacket.class,
                RPSOpponentPickThoughtsPacket::encode,
                RPSOpponentPickThoughtsPacket::decode,
                RPSOpponentPickThoughtsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
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
        channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
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
