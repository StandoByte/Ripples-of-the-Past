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
import com.github.standobyte.jojo.network.packets.fromclient.ClRemovePlayerSoulEntityPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClRunAwayPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClSoulRotationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStandManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandManualControlPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClToggleStandSummonPacket;
import com.github.standobyte.jojo.network.packets.fromserver.BloodParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.CommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillLearnPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillsResetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.InputBufferPacket;
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
import com.github.standobyte.jojo.network.packets.fromserver.StaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionsClearLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopInstancePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerJoinPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TimeStopPlayerStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityPosPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNoMotionLerpPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNonStandFlagPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPowerTypePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandOffsetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEntitySwingsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEntityTargetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.UpdateClientCapCachePacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        
        channel.messageBuilder(ClHasInputPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClHasInputPacket::encode)
        .decoder(ClHasInputPacket::decode)
        .consumer(ClHasInputPacket::handle).add();

        channel.messageBuilder(ClToggleStandSummonPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClToggleStandSummonPacket::encode)
        .decoder(ClToggleStandSummonPacket::decode)
        .consumer(ClToggleStandSummonPacket::handle).add();
        
        channel.messageBuilder(ClToggleStandManualControlPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClToggleStandManualControlPacket::encode)
        .decoder(ClToggleStandManualControlPacket::decode)
        .consumer(ClToggleStandManualControlPacket::handle).add();
        
        channel.messageBuilder(ClClickActionPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClClickActionPacket::encode)
        .decoder(ClClickActionPacket::decode)
        .consumer(ClClickActionPacket::handle).add();
        
        channel.messageBuilder(ClHeldActionTargetPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClHeldActionTargetPacket::encode)
        .decoder(ClHeldActionTargetPacket::decode)
        .consumer(ClHeldActionTargetPacket::handle).add();
        
        channel.messageBuilder(ClStopHeldActionPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClStopHeldActionPacket::encode)
        .decoder(ClStopHeldActionPacket::decode)
        .consumer(ClStopHeldActionPacket::handle).add();
        
        channel.messageBuilder(ClHamonWindowOpenedPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClHamonWindowOpenedPacket::encode)
        .decoder(ClHamonWindowOpenedPacket::decode)
        .consumer(ClHamonWindowOpenedPacket::handle).add();
        
        channel.messageBuilder(ClHamonLearnButtonPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClHamonLearnButtonPacket::encode)
        .decoder(ClHamonLearnButtonPacket::decode)
        .consumer(ClHamonLearnButtonPacket::handle).add();
        
        channel.messageBuilder(ClHamonResetSkillsButtonPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClHamonResetSkillsButtonPacket::encode)
        .decoder(ClHamonResetSkillsButtonPacket::decode)
        .consumer(ClHamonResetSkillsButtonPacket::handle).add();
        
        channel.messageBuilder(ClHamonStartMeditationPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClHamonStartMeditationPacket::encode)
        .decoder(ClHamonStartMeditationPacket::decode)
        .consumer(ClHamonStartMeditationPacket::handle).add();
        
        channel.messageBuilder(ClRunAwayPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClRunAwayPacket::encode)
        .decoder(ClRunAwayPacket::decode)
        .consumer(ClRunAwayPacket::handle).add();
        
        channel.messageBuilder(ClStandManualMovementPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClStandManualMovementPacket::encode)
        .decoder(ClStandManualMovementPacket::decode)
        .consumer(ClStandManualMovementPacket::handle).add();
        
        channel.messageBuilder(ClOnLeapPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClOnLeapPacket::encode)
        .decoder(ClOnLeapPacket::decode)
        .consumer(ClOnLeapPacket::handle).add();
        
        channel.messageBuilder(ClOnStandDashPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClOnStandDashPacket::encode)
        .decoder(ClOnStandDashPacket::decode)
        .consumer(ClOnStandDashPacket::handle).add();
        
        channel.messageBuilder(ClSoulRotationPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClSoulRotationPacket::encode)
        .decoder(ClSoulRotationPacket::decode)
        .consumer(ClSoulRotationPacket::handle).add();
        
        channel.messageBuilder(ClRemovePlayerSoulEntityPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
        .encoder(ClRemovePlayerSoulEntityPacket::encode)
        .decoder(ClRemovePlayerSoulEntityPacket::decode)
        .consumer(ClRemovePlayerSoulEntityPacket::handle).add();
        
        

        channel.registerMessage(index++, TrPowerTypePacket.class, 
                TrPowerTypePacket::encode, 
                TrPowerTypePacket::decode, 
                TrPowerTypePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        
        channel.messageBuilder(TrHeldActionPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrHeldActionPacket::encode)
        .decoder(TrHeldActionPacket::decode)
        .consumer(TrHeldActionPacket::handle).add();
        
        channel.messageBuilder(InputBufferPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(InputBufferPacket::encode)
        .decoder(InputBufferPacket::decode)
        .consumer(InputBufferPacket::handle).add();
        
        channel.messageBuilder(TrEnergyPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrEnergyPacket::encode)
        .decoder(TrEnergyPacket::decode)
        .consumer(TrEnergyPacket::handle).add();
        
        channel.messageBuilder(TrCooldownPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrCooldownPacket::encode)
        .decoder(TrCooldownPacket::decode)
        .consumer(TrCooldownPacket::handle).add();
        
        channel.messageBuilder(BloodParticlesPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(BloodParticlesPacket::encode)
        .decoder(BloodParticlesPacket::decode)
        .consumer(BloodParticlesPacket::handle).add();
        
        channel.messageBuilder(TrHamonStatsPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrHamonStatsPacket::encode)
        .decoder(TrHamonStatsPacket::decode)
        .consumer(TrHamonStatsPacket::handle).add();
        
        channel.messageBuilder(HamonExercisesPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(HamonExercisesPacket::encode)
        .decoder(HamonExercisesPacket::decode)
        .consumer(HamonExercisesPacket::handle).add();
        
        channel.messageBuilder(HamonTeachersSkillsPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(HamonTeachersSkillsPacket::encode)
        .decoder(HamonTeachersSkillsPacket::decode)
        .consumer(HamonTeachersSkillsPacket::handle).add();
        
        channel.messageBuilder(HamonSkillLearnPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(HamonSkillLearnPacket::encode)
        .decoder(HamonSkillLearnPacket::decode)
        .consumer(HamonSkillLearnPacket::handle).add();
        
        channel.messageBuilder(HamonSkillsResetPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(HamonSkillsResetPacket::encode)
        .decoder(HamonSkillsResetPacket::decode)
        .consumer(HamonSkillsResetPacket::handle).add();
        
        channel.messageBuilder(TrHamonParticlesPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrHamonParticlesPacket::encode)
        .decoder(TrHamonParticlesPacket::decode)
        .consumer(TrHamonParticlesPacket::handle).add();
        
        channel.messageBuilder(TrNonStandFlagPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrNonStandFlagPacket::encode)
        .decoder(TrNonStandFlagPacket::decode)
        .consumer(TrNonStandFlagPacket::handle).add();
        
        channel.messageBuilder(StaminaPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StaminaPacket::encode)
        .decoder(StaminaPacket::decode)
        .consumer(StaminaPacket::handle).add();
        
        channel.messageBuilder(ResolvePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResolvePacket::encode)
        .decoder(ResolvePacket::decode)
        .consumer(ResolvePacket::handle).add();
        
        channel.messageBuilder(ResetResolveValuePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResetResolveValuePacket::encode)
        .decoder(ResetResolveValuePacket::decode)
        .consumer(ResetResolveValuePacket::handle).add();
        
        channel.messageBuilder(ResolveLevelPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResolveLevelPacket::encode)
        .decoder(ResolveLevelPacket::decode)
        .consumer(ResolveLevelPacket::handle).add();
        
        channel.messageBuilder(ResolveBoostsPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResolveBoostsPacket::encode)
        .decoder(ResolveBoostsPacket::decode)
        .consumer(ResolveBoostsPacket::handle).add();
        
        channel.messageBuilder(MaxAchievedResolvePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(MaxAchievedResolvePacket::encode)
        .decoder(MaxAchievedResolvePacket::decode)
        .consumer(MaxAchievedResolvePacket::handle).add();
        
        channel.messageBuilder(SkippedStandProgressionPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SkippedStandProgressionPacket::encode)
        .decoder(SkippedStandProgressionPacket::decode)
        .consumer(SkippedStandProgressionPacket::handle).add();
        
        channel.messageBuilder(ResolveEffectStartPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResolveEffectStartPacket::encode)
        .decoder(ResolveEffectStartPacket::decode)
        .consumer(ResolveEffectStartPacket::handle).add();
        
        channel.messageBuilder(StandActionLearningPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StandActionLearningPacket::encode)
        .decoder(StandActionLearningPacket::decode)
        .consumer(StandActionLearningPacket::handle).add();
        
        channel.messageBuilder(StandActionsClearLearningPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StandActionsClearLearningPacket::encode)
        .decoder(StandActionsClearLearningPacket::decode)
        .consumer(StandActionsClearLearningPacket::handle).add();
        
        channel.messageBuilder(TrSetStandEntityPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSetStandEntityPacket::encode)
        .decoder(TrSetStandEntityPacket::decode)
        .consumer(TrSetStandEntityPacket::handle).add();
        
        channel.messageBuilder(StandStatsDataPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StandStatsDataPacket::encode)
        .decoder(StandStatsDataPacket::decode)
        .consumer(StandStatsDataPacket::handle).add();
        
        channel.messageBuilder(StandControlStatusPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StandControlStatusPacket::encode)
        .decoder(StandControlStatusPacket::decode)
        .consumer(StandControlStatusPacket::handle).add();
        
        channel.messageBuilder(StandCancelManualMovementPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StandCancelManualMovementPacket::encode)
        .decoder(StandCancelManualMovementPacket::decode)
        .consumer(StandCancelManualMovementPacket::handle).add();
        
        channel.messageBuilder(TrStandEntityTargetPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrStandEntityTargetPacket::encode)
        .decoder(TrStandEntityTargetPacket::decode)
        .consumer(TrStandEntityTargetPacket::handle).add();
        
        channel.messageBuilder(TrSetStandOffsetPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSetStandOffsetPacket::encode)
        .decoder(TrSetStandOffsetPacket::decode)
        .consumer(TrSetStandOffsetPacket::handle).add();
        
        channel.messageBuilder(TrStandEntitySwingsPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrStandEntitySwingsPacket::encode)
        .decoder(TrStandEntitySwingsPacket::decode)
        .consumer(TrStandEntitySwingsPacket::handle).add();
        
        channel.messageBuilder(UpdateClientCapCachePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(UpdateClientCapCachePacket::encode)
        .decoder(UpdateClientCapCachePacket::decode)
        .consumer(UpdateClientCapCachePacket::handle).add();
        
        channel.messageBuilder(TrKnivesCountPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrKnivesCountPacket::encode)
        .decoder(TrKnivesCountPacket::decode)
        .consumer(TrKnivesCountPacket::handle).add();
        
        channel.messageBuilder(LeapCooldownPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(LeapCooldownPacket::encode)
        .decoder(LeapCooldownPacket::decode)
        .consumer(LeapCooldownPacket::handle).add();
        
        channel.messageBuilder(PlayVoiceLinePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(PlayVoiceLinePacket::encode)
        .decoder(PlayVoiceLinePacket::decode)
        .consumer(PlayVoiceLinePacket::handle).add();
        
        channel.messageBuilder(PlaySoundAtClientPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(PlaySoundAtClientPacket::encode)
        .decoder(PlaySoundAtClientPacket::decode)
        .consumer(PlaySoundAtClientPacket::handle).add();
        
        channel.messageBuilder(TimeStopInstancePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TimeStopInstancePacket::encode)
        .decoder(TimeStopInstancePacket::decode)
        .consumer(TimeStopInstancePacket::handle).add();
        
        channel.messageBuilder(TimeStopPlayerStatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TimeStopPlayerStatePacket::encode)
        .decoder(TimeStopPlayerStatePacket::decode)
        .consumer(TimeStopPlayerStatePacket::handle).add();
        
        channel.messageBuilder(TimeStopPlayerJoinPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TimeStopPlayerJoinPacket::encode)
        .decoder(TimeStopPlayerJoinPacket::decode)
        .consumer(TimeStopPlayerJoinPacket::handle).add();
        
        channel.messageBuilder(RefreshMovementInTimeStopPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(RefreshMovementInTimeStopPacket::encode)
        .decoder(RefreshMovementInTimeStopPacket::decode)
        .consumer(RefreshMovementInTimeStopPacket::handle).add();
        
        channel.messageBuilder(TrNoMotionLerpPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrNoMotionLerpPacket::encode)
        .decoder(TrNoMotionLerpPacket::decode)
        .consumer(TrNoMotionLerpPacket::handle).add();
        
        channel.messageBuilder(TrDirectEntityPosPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrDirectEntityPosPacket::encode)
        .decoder(TrDirectEntityPosPacket::decode)
        .consumer(TrDirectEntityPosPacket::handle).add();
        
        channel.messageBuilder(CommonConfigPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(CommonConfigPacket::encode)
        .decoder(CommonConfigPacket::decode)
        .consumer(CommonConfigPacket::handle).add();
        
        channel.messageBuilder(ResetSyncedCommonConfigPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResetSyncedCommonConfigPacket::encode)
        .decoder(ResetSyncedCommonConfigPacket::decode)
        .consumer(ResetSyncedCommonConfigPacket::handle).add();
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
