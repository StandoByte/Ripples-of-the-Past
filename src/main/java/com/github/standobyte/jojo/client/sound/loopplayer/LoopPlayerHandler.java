package com.github.standobyte.jojo.client.sound.loopplayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.standobyte.jojo.client.ClientTicking;
import com.github.standobyte.jojo.client.ClientTicking.ITicking;

@Deprecated // forgot why i made this, though there's no reason to throw it away i guess
public class LoopPlayerHandler implements ITicking {
    private static LoopPlayerHandler instance;
    private final List<SoundLoopPlayer> loopPlayers = new ArrayList<>();
    
    public static void init() {
        instance = new LoopPlayerHandler();
        ClientTicking.addTicking(instance);
    }
    
    public static LoopPlayerHandler getInstance() {
        return instance;
    }
    
    
    
    public void add(SoundLoopPlayer loopPlayer) {
        loopPlayers.add(loopPlayer);
    }
    
    public void tick() {
        if (loopPlayers.isEmpty()) return;
        
        Iterator<SoundLoopPlayer> iterator = loopPlayers.iterator();
        while (iterator.hasNext()) {
            SoundLoopPlayer loopPlayer = iterator.next();
            loopPlayer.tick();
            if (loopPlayer.isStopped()) {
                iterator.remove();
            }
        }
    }
}
