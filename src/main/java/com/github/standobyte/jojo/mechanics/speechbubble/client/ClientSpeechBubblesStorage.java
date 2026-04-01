package com.github.standobyte.jojo.mechanics.speechbubble.client;

import com.github.standobyte.jojo.client.particle.custom.MenacingParticleEmitter;
import com.github.standobyte.jojo.mechanics.speechbubble.CharacterRecentlySaidSpeechBubbles;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;

public class ClientSpeechBubblesStorage {
    public static Int2ObjectMap<CharacterRecentlySaidSpeechBubbles> perEntityId = new Int2ObjectArrayMap<>();
    public static Int2ObjectMap<MenacingParticleEmitter> particleEmitters = new Int2ObjectArrayMap<>();
    
    public static CharacterRecentlySaidSpeechBubbles getOrCreate(Entity entity) {
        return perEntityId.computeIfAbsent(entity.getId(), __ -> new CharacterRecentlySaidSpeechBubbles());
    }

}
