package com.github.standobyte.jojo.mechanics.speechbubble;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.particle.custom.MenacingParticleEmitter;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.actionshud.ElementTransparency;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.mechanics.speechbubble.client.ChatScreenSpeechBubbleHandler;
import com.github.standobyte.jojo.mechanics.speechbubble.client.ClientChatMode;
import com.github.standobyte.jojo.mechanics.speechbubble.client.ClientSpeechBubblesStorage;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.subsystems.timestop.EntityTimeStop;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.network.NetworkDirection;

public class SpeechBubblesFunctionality {

    public static boolean clientSideSendChatMessage(ClientChatMode curChatMode, String messageToSend, Entity entity) {
        if (curChatMode != null && !ChatScreenSpeechBubbleHandler.isTypingCommand) {
            SpeechBubbleType speechType = curChatMode.getSpeechBubble();
            if (speechType != null) {
                boolean menacing = curChatMode.gogogogogoParticles();
                PacketManager.sendToServer(new ClSaySpeechBubblePacket(entity.getId(), messageToSend, speechType, menacing));
                return true;
            }
        }
        
        return false;
    }
    
    public static void broadcastSaySpeechBubble(Entity entity, ITextComponent text, SpeechBubbleType type, boolean gogogogogoParticles) {
        if (entity != null) {
            boolean standOnly = false;
            if (entity instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) entity;
                standOnly = !standEntity.isVisibleForAll();
            }
            else if (entity instanceof SoulEntity) {
                standOnly = true;
            }
            
            SaySpeechBubblePacket packet = new SaySpeechBubblePacket(entity.getId(), text, type, gogogogogoParticles);
            IPacket<?> vanillaPacket = PacketManager.serverChannel.toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT);
            ServerChunkProvider serverChunkProvider = (ServerChunkProvider)entity.getCommandSenderWorld().getChunkSource();
            ChunkManager chunkMap = serverChunkProvider.chunkMap;
            ChunkManager.EntityTracker entityTracker = chunkMap.entityMap.get(entity.getId());
            // broadcast with a condition
            for (ServerPlayerEntity player : entityTracker.seenBy) {
                boolean playerCanNotHear = 
                        standOnly && !StandUtil.playerCanHearStands(player)
                        || !EntityTimeStop.canUpdate(player) && !TimeStopHandler.canPlayerSeeInStoppedTime(player);
                if (!playerCanNotHear) {
                    player.connection.send(vanillaPacket);
                }
            }
            // ...and send to self
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                player.connection.send(vanillaPacket);
            }
        }
    }
    
    public static void clientSideAddSpeechBubble(Entity entity, ITextComponent text, SpeechBubbleType type, boolean gogogogogoParticles) {
        if (entity != null) {
            CharacterRecentlySaidSpeechBubbles speech = ClientSpeechBubblesStorage.getOrCreate(entity);
            if (speech != null) {
                SpeechBubble speechBubble = new SpeechBubble(type, text, gogogogogoParticles, new ElementTransparency(120, 20));
                speech.add(speechBubble);
                if (gogogogogoParticles) {
                    MenacingParticleEmitter existing = ClientSpeechBubblesStorage.particleEmitters.get(entity.getId());
                    if (existing != null && existing.isAlive()) {
                        existing.life -= existing.interval * (int) (existing.life / existing.interval);
                    }
                    else {
                        MenacingParticleEmitter emitter = (MenacingParticleEmitter) CustomParticlesHelper
                                .addMenacingParticleEmitter(entity, ModParticles.MENACING.get(), 2);
                        ClientSpeechBubblesStorage.particleEmitters.put(entity.getId(), emitter);
                    }
                }
            }
        }
    }
    
    public static final char SINGLE_Q_MARK_OPEN = 0x300c;
    public static final char SINGLE_Q_MARK_CLOSE = 0x300d;
    public static final char DOUBLE_Q_MARK_OPEN = 0x300e;
    public static final char DOUBLE_Q_MARK_CLOSE = 0x300f;
    public static void onClientSideEntityVoiceLine(Entity entity, ITextComponent subtitle, SoundEvent soundEvent) {
        String text = subtitle.getString();
        if (!text.isEmpty()) {
            unpackStandNameShouts();
            boolean standName = STAND_NAMES_COLOR_AND_DOUBLE_QUOTATION_MARKS.contains(soundEvent);
            
            char firstChar = text.charAt(0);
            char lastChar = text.charAt(text.length() - 1);
            if (firstChar == '"' && lastChar == '"'
                    || firstChar == SINGLE_Q_MARK_OPEN && lastChar == SINGLE_Q_MARK_CLOSE) {
                text = text.substring(1, text.length() - 1);
            }
            if (standName && firstChar != DOUBLE_Q_MARK_OPEN && lastChar != DOUBLE_Q_MARK_CLOSE) {
                text = DOUBLE_Q_MARK_OPEN + text + DOUBLE_Q_MARK_CLOSE;
            }
            IFormattableTextComponent line = new StringTextComponent(text);
            if (standName && entity instanceof LivingEntity) {
                LivingEntity standUser = (LivingEntity) entity;
                IStandPower standPower = IStandPower.getStandPowerOptional(standUser).orElse(null);
                if (standPower != null) {
                    int uiColor = StandSkinsManager.getUiColor(standPower);
                    if (uiColor != -1) {
                        line = line
                                .withStyle(Style.EMPTY.withColor(Color.fromRgb(uiColor)))
                                //.withStyle(TextFormatting.BOLD)
                                ;
                    }
                }
            }
            SpeechBubblesFunctionality.clientSideAddSpeechBubble(entity, line, SpeechBubbleType.REGULAR, false);
        }
    }

    public static Set<Supplier<SoundEvent>> _STAND_NAME_SUPPLIERS = new HashSet<>();
    public static Set<SoundEvent> STAND_NAMES_COLOR_AND_DOUBLE_QUOTATION_MARKS = new HashSet<>();
    static void unpackStandNameShouts() {
        if (_STAND_NAME_SUPPLIERS != null) {
            for (Supplier<SoundEvent> supplier : _STAND_NAME_SUPPLIERS) {
                SoundEvent soundEvent = supplier.get();
                if (soundEvent != null) {
                    STAND_NAMES_COLOR_AND_DOUBLE_QUOTATION_MARKS.add(soundEvent);
                }
            }
            _STAND_NAME_SUPPLIERS = null;
        }
    }
}
