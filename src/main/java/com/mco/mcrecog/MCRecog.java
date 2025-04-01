package com.mco.mcrecog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MCRecog {
    // The web socket
    private ServerSocket server;
    // The blocking (thread-safe) queue to put our input onto in order to communicate between the socket thread and the main thread
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    
    public static void init() {
        MCRecog mcRecog = new MCRecog();
        mcRecog.connectToSocket();
        MinecraftForge.EVENT_BUS.register(mcRecog);
    }

    private void connectToSocket() {
        // Connect to the server
        try {
            server = new ServerSocket(7777);
        } catch (IOException e){
            JojoMod.LOGGER.error(e.getMessage());
        }

        JojoMod.LOGGER.debug("Starting the socket...");
        // Spawn a new thread that reads from the socket on the specified localhost:port and adds it to the blocking queue
        new Thread(() -> {
            try {
                Socket client = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Receive input while the program is running
                while (true) {
                    String fromClient = in.readLine();
                    if (fromClient != null)
                        queue.put(fromClient);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SubscribeEvent
    public void onTickEvent(ClientTickEvent event) {
        if (queue.isEmpty()) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        IStandPower stand = IStandPower.getStandPowerOptional(mc.player).orElse(null);
        INonStandPower power = INonStandPower.getNonStandPowerOptional(mc.player).orElse(null);
        if ((stand == null || !stand.hasPower()) && (power == null || !power.hasPower())) return;
        
        String msg;
        while ((msg = queue.poll()) != null) {
            if (!msg.isEmpty()) {
                handleCommand(msg, stand, power, true);
            }
        }
    }
    
    public static void handleCommand(String command, IStandPower stand, INonStandPower power, boolean printNotTriggered) {
        CommandsMap commands = CustomResources.getAprilFools25VoiceCommands();
        if (commands != null) {
            boolean result = commands.onVoiceCommand(command, stand, power);
            if (result || printNotTriggered) {
                Minecraft.getInstance().gui.setOverlayMessage(new StringTextComponent(command), result);
            }
        }
    }
}
