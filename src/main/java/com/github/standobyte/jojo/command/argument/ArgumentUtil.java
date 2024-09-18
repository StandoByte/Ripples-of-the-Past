package com.github.standobyte.jojo.command.argument;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ArgumentUtil {
    public static final DynamicCommandExceptionType AMBIGUOUS_ID = new DynamicCommandExceptionType((key) -> new TranslationTextComponent("jojo.command.id_ambiguous", key));
    
    public static <V extends IForgeRegistryEntry<V>> V bestFittingValue(String idInput, IForgeRegistry<V> registry, 
            Map<String, List<V>> aliasMap, 
            DynamicCommandExceptionType unknownException) throws CommandSyntaxException { 
        ResourceLocation id;
        if (idInput.contains(":")) {
            id = new ResourceLocation(idInput);
        }
        else if (aliasMap != null) {
            List<V> addonKeys = aliasMap.get(idInput);
            if (addonKeys == null || addonKeys.isEmpty()) {
                throw unknownException.create(idInput);
            }
            else if (addonKeys.size() > 1) {
                throw AMBIGUOUS_ID.create(idInput);
            }
            else {
                return addonKeys.get(0);
            }
        }
        else {
            id = new ResourceLocation(JojoMod.MOD_ID, idInput);
        }
        
        if (registry.containsKey(id)) {
            V value = registry.getValue(id);
            if (value != null) {
                return value;
            }
        }
        throw unknownException.create(id);
    }
    
    
    public static <V extends IForgeRegistryEntry<V>> Map<String, List<V>> groupByKeyLocation(IForgeRegistry<V> registry) {
        return registry.getEntries().stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getKey().location().getPath(), 
                        Collectors.mapping(entry -> entry.getValue(), Collectors.toList())));
    }

    public static String read(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        while(reader.canRead() && isValidPathCharacter(reader.peek())) {
            reader.skip();
        }
        String s = reader.getString().substring(i, reader.getCursor());
        try {
            return s;
        } catch (ResourceLocationException var4) {
            reader.setCursor(i);
            throw INVALID_EXCEPTION.createWithContext(reader);
        }
    }

    public static boolean isValidPathCharacter(char charIn) {
        return charIn >= '0' && charIn <= '9' || charIn >= 'a' && charIn <= 'z' || charIn == '_' || charIn == ':' || charIn == '/' || charIn == '.' || charIn == '-';
    }
    
    private static final SimpleCommandExceptionType INVALID_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("argument.id.invalid"));


    public static CompletableFuture<Suggestions> suggestIterable(Stream<ResourceLocation> registryKeys, SuggestionsBuilder builder) {
        String s = builder.getRemaining().toLowerCase(Locale.ROOT);
        addSuggestions(registryKeys, s, Function.identity(), resLoc -> builder.suggest(resLoc.toString()));
        return builder.buildFuture();
    }

    public static <T> void addSuggestions(Stream<T> stream, String remaining, Function<T, ResourceLocation> toResLoc, Consumer<T> suggest) {
        boolean inputColon = remaining.indexOf(':') > -1;
        stream.forEach(t -> {
            ResourceLocation resourceLocation = toResLoc.apply(t);
            if (inputColon) {
                String s = resourceLocation.toString();
                if (s.startsWith(remaining)) {
                    suggest.accept(t);
                }
            }
            else if (resourceLocation.getNamespace().startsWith(remaining) || resourceLocation.getPath().startsWith(remaining)) {
                suggest.accept(t);
            }
        });
    }
}
