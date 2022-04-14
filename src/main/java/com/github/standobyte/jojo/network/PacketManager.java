package com.github.standobyte.jojo.network;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.network.packets.fromclient.ClClickActionPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonLearnButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonStartMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonWindowOpenedPacket;
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
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillLearnPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillsResetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.network.packets.fromserver.RefreshMovementInTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetResolveValuePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetSyncedCommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveEffectStartPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SkippedStandProgressionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncCommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncHamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncInputBufferPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncLeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncMaxAchievedResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolveLevelPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandActionLearningClearPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandControlStatusPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandOffsetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEntitySwingsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncHamonStatsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncKnivesCountPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncNonStandFlagPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncPowerTypePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncStandTargetPacket;
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
import net.minecraftforge.fml.network.NetworkEvent;
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
        
        

        channel.registerMessage(index++, TrSyncPowerTypePacket.class, 
                TrSyncPowerTypePacket::encode, 
                TrSyncPowerTypePacket::decode, 
                TrSyncPowerTypePacket::handle);
        
        channel.messageBuilder(TrSyncHeldActionPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSyncHeldActionPacket::encode)
        .decoder(TrSyncHeldActionPacket::decode)
        .consumer(TrSyncHeldActionPacket::handle).add();
        
        channel.messageBuilder(SyncInputBufferPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncInputBufferPacket::encode)
        .decoder(SyncInputBufferPacket::decode)
        .consumer(SyncInputBufferPacket::handle).add();
        
        channel.messageBuilder(SyncEnergyPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncEnergyPacket::encode)
        .decoder(SyncEnergyPacket::decode)
        .consumer(SyncEnergyPacket::handle).add();
        
        channel.messageBuilder(TrSyncCooldownPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSyncCooldownPacket::encode)
        .decoder(TrSyncCooldownPacket::decode)
        .consumer(TrSyncCooldownPacket::handle).add();
        
        channel.messageBuilder(TrSyncHamonStatsPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSyncHamonStatsPacket::encode)
        .decoder(TrSyncHamonStatsPacket::decode)
        .consumer(TrSyncHamonStatsPacket::handle).add();
        
        channel.messageBuilder(SyncHamonExercisesPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncHamonExercisesPacket::encode)
        .decoder(SyncHamonExercisesPacket::decode)
        .consumer(SyncHamonExercisesPacket::handle).add();
        
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
        
        channel.messageBuilder(TrSyncNonStandFlagPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSyncNonStandFlagPacket::encode)
        .decoder(TrSyncNonStandFlagPacket::decode)
        .consumer(TrSyncNonStandFlagPacket::handle).add();
        
        channel.messageBuilder(SyncStaminaPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncStaminaPacket::encode)
        .decoder(SyncStaminaPacket::decode)
        .consumer(SyncStaminaPacket::handle).add();
        
        channel.messageBuilder(SyncResolvePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncResolvePacket::encode)
        .decoder(SyncResolvePacket::decode)
        .consumer(SyncResolvePacket::handle).add();
        
        channel.messageBuilder(ResetResolveValuePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResetResolveValuePacket::encode)
        .decoder(ResetResolveValuePacket::decode)
        .consumer(ResetResolveValuePacket::handle).add();
        
        channel.messageBuilder(SyncResolveLevelPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncResolveLevelPacket::encode)
        .decoder(SyncResolveLevelPacket::decode)
        .consumer(SyncResolveLevelPacket::handle).add();
        
        channel.messageBuilder(SyncMaxAchievedResolvePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncMaxAchievedResolvePacket::encode)
        .decoder(SyncMaxAchievedResolvePacket::decode)
        .consumer(SyncMaxAchievedResolvePacket::handle).add();
        
        channel.messageBuilder(SkippedStandProgressionPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SkippedStandProgressionPacket::encode)
        .decoder(SkippedStandProgressionPacket::decode)
        .consumer(SkippedStandProgressionPacket::handle).add();
        
        channel.messageBuilder(ResolveEffectStartPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ResolveEffectStartPacket::encode)
        .decoder(ResolveEffectStartPacket::decode)
        .consumer(ResolveEffectStartPacket::handle).add();
        
        channel.messageBuilder(SyncStandActionLearningPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncStandActionLearningPacket::encode)
        .decoder(SyncStandActionLearningPacket::decode)
        .consumer(SyncStandActionLearningPacket::handle).add();
        
        channel.messageBuilder(SyncStandActionLearningClearPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncStandActionLearningClearPacket::encode)
        .decoder(SyncStandActionLearningClearPacket::decode)
        .consumer(SyncStandActionLearningClearPacket::handle).add();
        
        channel.messageBuilder(TrSetStandEntityPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSetStandEntityPacket::encode)
        .decoder(TrSetStandEntityPacket::decode)
        .consumer(TrSetStandEntityPacket::handle).add();
        
        channel.messageBuilder(SyncStandStatsDataPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncStandStatsDataPacket::encode)
        .decoder(SyncStandStatsDataPacket::decode)
        .consumer(SyncStandStatsDataPacket::handle).add();
        
        channel.messageBuilder(SyncStandControlStatusPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncStandControlStatusPacket::encode)
        .decoder(SyncStandControlStatusPacket::decode)
        .consumer(SyncStandControlStatusPacket::handle).add();
        
        channel.messageBuilder(StandCancelManualMovementPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(StandCancelManualMovementPacket::encode)
        .decoder(StandCancelManualMovementPacket::decode)
        .consumer(StandCancelManualMovementPacket::handle).add();
        
        channel.messageBuilder(TrSyncStandTargetPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSyncStandTargetPacket::encode)
        .decoder(TrSyncStandTargetPacket::decode)
        .consumer(TrSyncStandTargetPacket::handle).add();
        
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
        
        channel.messageBuilder(TrSyncKnivesCountPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(TrSyncKnivesCountPacket::encode)
        .decoder(TrSyncKnivesCountPacket::decode)
        .consumer(TrSyncKnivesCountPacket::handle).add();
        
        channel.messageBuilder(SyncLeapCooldownPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncLeapCooldownPacket::encode)
        .decoder(SyncLeapCooldownPacket::decode)
        .consumer(SyncLeapCooldownPacket::handle).add();
        
        channel.messageBuilder(PlayVoiceLinePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(PlayVoiceLinePacket::encode)
        .decoder(PlayVoiceLinePacket::decode)
        .consumer(PlayVoiceLinePacket::handle).add();
        
        channel.messageBuilder(PlaySoundAtClientPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(PlaySoundAtClientPacket::encode)
        .decoder(PlaySoundAtClientPacket::decode)
        .consumer(PlaySoundAtClientPacket::handle).add();
        
        channel.messageBuilder(SyncWorldTimeStopPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncWorldTimeStopPacket::encode)
        .decoder(SyncWorldTimeStopPacket::decode)
        .consumer(SyncWorldTimeStopPacket::handle).add();
        
        channel.messageBuilder(RefreshMovementInTimeStopPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(RefreshMovementInTimeStopPacket::encode)
        .decoder(RefreshMovementInTimeStopPacket::decode)
        .consumer(RefreshMovementInTimeStopPacket::handle).add();
        
        channel.messageBuilder(RefreshMovementInTimeStopPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(RefreshMovementInTimeStopPacket::encode)
        .decoder(RefreshMovementInTimeStopPacket::decode)
        .consumer(RefreshMovementInTimeStopPacket::handle).add();
        
        channel.messageBuilder(SyncCommonConfigPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(SyncCommonConfigPacket::encode)
        .decoder(SyncCommonConfigPacket::decode)
        .consumer(SyncCommonConfigPacket::handle).add();
        
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
    
    
    
    public static <MSG> void replyToServerHandshake(MSG msgToReply, NetworkEvent.Context context) {
        channel.reply(msgToReply, context);
    }
}
