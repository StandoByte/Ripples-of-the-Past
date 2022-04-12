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
import com.github.standobyte.jojo.network.packets.fromclient.ClHeldActionTargetPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClOnLeapPacket;
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
import com.github.standobyte.jojo.network.packets.fromserver.ResolveEffectStartPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandCancelManualMovementPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncHamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncInputBufferPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncLeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolveLimitPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncResolvePacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStaminaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandActionLearningClearPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandActionLearningPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandControlStatusPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandOffsetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonParticlesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSetStandEntityPacket;
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

        channel.registerMessage(index++, ClToggleStandSummonPacket.class, ClToggleStandSummonPacket::encode, ClToggleStandSummonPacket::decode, ClToggleStandSummonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClToggleStandManualControlPacket.class, ClToggleStandManualControlPacket::encode, ClToggleStandManualControlPacket::decode, ClToggleStandManualControlPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClClickActionPacket.class, ClClickActionPacket::encode, ClClickActionPacket::decode, ClClickActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClHeldActionTargetPacket.class, ClHeldActionTargetPacket::encode, ClHeldActionTargetPacket::decode, ClHeldActionTargetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClStopHeldActionPacket.class, ClStopHeldActionPacket::encode, ClStopHeldActionPacket::decode, ClStopHeldActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClHamonWindowOpenedPacket.class, ClHamonWindowOpenedPacket::encode, ClHamonWindowOpenedPacket::decode, ClHamonWindowOpenedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClHamonLearnButtonPacket.class, ClHamonLearnButtonPacket::encode, ClHamonLearnButtonPacket::decode, ClHamonLearnButtonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClHamonResetSkillsButtonPacket.class, ClHamonResetSkillsButtonPacket::encode, ClHamonResetSkillsButtonPacket::decode, ClHamonResetSkillsButtonPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClHamonStartMeditationPacket.class, ClHamonStartMeditationPacket::encode, ClHamonStartMeditationPacket::decode, ClHamonStartMeditationPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClRunAwayPacket.class, ClRunAwayPacket::encode, ClRunAwayPacket::decode, ClRunAwayPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClStandManualMovementPacket.class, ClStandManualMovementPacket::encode, ClStandManualMovementPacket::decode, ClStandManualMovementPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClOnLeapPacket.class, ClOnLeapPacket::encode, ClOnLeapPacket::decode, ClOnLeapPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClSoulRotationPacket.class, ClSoulRotationPacket::encode, ClSoulRotationPacket::decode, ClSoulRotationPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        channel.registerMessage(index++, ClRemovePlayerSoulEntityPacket.class, ClRemovePlayerSoulEntityPacket::encode, ClRemovePlayerSoulEntityPacket::decode, ClRemovePlayerSoulEntityPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        channel.registerMessage(index++, TrSyncPowerTypePacket.class, TrSyncPowerTypePacket::encode, TrSyncPowerTypePacket::decode, TrSyncPowerTypePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSyncHeldActionPacket.class, TrSyncHeldActionPacket::encode, TrSyncHeldActionPacket::decode, TrSyncHeldActionPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncInputBufferPacket.class, SyncInputBufferPacket::encode, SyncInputBufferPacket::decode, SyncInputBufferPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncEnergyPacket.class, SyncEnergyPacket::encode, SyncEnergyPacket::decode, SyncEnergyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSyncCooldownPacket.class, TrSyncCooldownPacket::encode, TrSyncCooldownPacket::decode, TrSyncCooldownPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSyncHamonStatsPacket.class, TrSyncHamonStatsPacket::encode, TrSyncHamonStatsPacket::decode, TrSyncHamonStatsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncHamonExercisesPacket.class, SyncHamonExercisesPacket::encode, SyncHamonExercisesPacket::decode, SyncHamonExercisesPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, HamonTeachersSkillsPacket.class, HamonTeachersSkillsPacket::encode, HamonTeachersSkillsPacket::decode, HamonTeachersSkillsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, HamonSkillLearnPacket.class, HamonSkillLearnPacket::encode, HamonSkillLearnPacket::decode, HamonSkillLearnPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, HamonSkillsResetPacket.class, HamonSkillsResetPacket::encode, HamonSkillsResetPacket::decode, HamonSkillsResetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrHamonParticlesPacket.class, TrHamonParticlesPacket::encode, TrHamonParticlesPacket::decode, TrHamonParticlesPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSyncNonStandFlagPacket.class, TrSyncNonStandFlagPacket::encode, TrSyncNonStandFlagPacket::decode, TrSyncNonStandFlagPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncStaminaPacket.class, SyncStaminaPacket::encode, SyncStaminaPacket::decode, SyncStaminaPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncResolvePacket.class, SyncResolvePacket::encode, SyncResolvePacket::decode, SyncResolvePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncResolveLimitPacket.class, SyncResolveLimitPacket::encode, SyncResolveLimitPacket::decode, SyncResolveLimitPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, ResolveEffectStartPacket.class, ResolveEffectStartPacket::encode, ResolveEffectStartPacket::decode, ResolveEffectStartPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncStandActionLearningPacket.class, SyncStandActionLearningPacket::encode, SyncStandActionLearningPacket::decode, SyncStandActionLearningPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncStandActionLearningClearPacket.class, SyncStandActionLearningClearPacket::encode, SyncStandActionLearningClearPacket::decode, SyncStandActionLearningClearPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSetStandEntityPacket.class, TrSetStandEntityPacket::encode, TrSetStandEntityPacket::decode, TrSetStandEntityPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncStandStatsDataPacket.class, SyncStandStatsDataPacket::encode, SyncStandStatsDataPacket::decode, SyncStandStatsDataPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncStandControlStatusPacket.class, SyncStandControlStatusPacket::encode, SyncStandControlStatusPacket::decode, SyncStandControlStatusPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, StandCancelManualMovementPacket.class, StandCancelManualMovementPacket::encode, StandCancelManualMovementPacket::decode, StandCancelManualMovementPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSyncStandTargetPacket.class, TrSyncStandTargetPacket::encode, TrSyncStandTargetPacket::decode, TrSyncStandTargetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSetStandOffsetPacket.class, TrSetStandOffsetPacket::encode, TrSetStandOffsetPacket::decode, TrSetStandOffsetPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrStandEntitySwingsPacket.class, TrStandEntitySwingsPacket::encode, TrStandEntitySwingsPacket::decode, TrStandEntitySwingsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, UpdateClientCapCachePacket.class, UpdateClientCapCachePacket::encode, UpdateClientCapCachePacket::decode, UpdateClientCapCachePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, TrSyncKnivesCountPacket.class, TrSyncKnivesCountPacket::encode, TrSyncKnivesCountPacket::decode, TrSyncKnivesCountPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncLeapCooldownPacket.class, SyncLeapCooldownPacket::encode, SyncLeapCooldownPacket::decode, SyncLeapCooldownPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, PlayVoiceLinePacket.class, PlayVoiceLinePacket::encode, PlayVoiceLinePacket::decode, PlayVoiceLinePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, PlaySoundAtClientPacket.class, PlaySoundAtClientPacket::encode, PlaySoundAtClientPacket::decode, PlaySoundAtClientPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, SyncWorldTimeStopPacket.class, SyncWorldTimeStopPacket::encode, SyncWorldTimeStopPacket::decode, SyncWorldTimeStopPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        channel.registerMessage(index++, RefreshMovementInTimeStopPacket.class, RefreshMovementInTimeStopPacket::encode, RefreshMovementInTimeStopPacket::decode, RefreshMovementInTimeStopPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
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
