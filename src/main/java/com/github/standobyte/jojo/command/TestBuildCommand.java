package com.github.standobyte.jojo.command;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class TestBuildCommand {
    
    public static final boolean ENABLED = false;

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        if (ENABLED) {
            dispatcher.register(Commands.literal("donottouch").requires(ctx -> ctx.hasPermission(4))
                    .then(Commands.argument("password", StringArgumentType.word()).then(Commands.argument("operation", IntegerArgumentType.integer(0)).executes(
                            ctx -> test(ctx.getSource(), StringArgumentType.getString(ctx, "password"), IntegerArgumentType.getInteger(ctx, "operation"), null))
                    .then(Commands.argument("target", EntityArgument.player()).executes(
                            ctx -> test(ctx.getSource(), StringArgumentType.getString(ctx, "password"), IntegerArgumentType.getInteger(ctx, "operation"), EntityArgument.getPlayer(ctx, "target"))))))
                    );
        }
    }
    
    private static int test(CommandSource source, String redgitProtection, int operation, @Nullable ServerPlayerEntity guineaPig) {
        switch (operation) {
        // 
        }
        if (passwordCheck(redgitProtection, source.getEntity())) {
            switch (operation) {
            // 
            }
        }
        return 0;
    }
    
    private static boolean passwordCheck(String password, @Nullable Entity entity) {
        if (TCPW_HASH.equals(hash(password))) {
            return true;
        }
        if (entity instanceof ServerPlayerEntity) {
            entity.sendMessage(new StringTextComponent("bruh"), Util.NIL_UUID);
            entity.kill();
        }
        return false;
    }
    
    private static final String TCPW_HASH = "c882f236243743993b9d50ec0381529dfabe28bd39bdf60265d8348f0a8de750";
    private static String hash(String string) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder str = new StringBuilder();
            for (byte b : digest) {
                str.append(String.format("%02x", b));
            }
            return str.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
