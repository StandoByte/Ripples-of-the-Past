package com.github.standobyte.jojo.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class StandArgument implements ArgumentType<StandType<?>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("star_platinum", "jojo:hierophant_green");
    public static final DynamicCommandExceptionType STAND_UNKNOWN = new DynamicCommandExceptionType((key) -> new TranslationTextComponent("stand.unknown", key));
    public static final DynamicCommandExceptionType AMBIGUOUS_ID = new DynamicCommandExceptionType((key) -> new TranslationTextComponent("jojo.command.id_ambiguous", key));
    
    public static void commonSetupRegister() {
        ArgumentTypes.register("stand", StandArgument.class, new ArgumentSerializer<>(StandArgument::new));
    }

    @Override
    public StandType<?> parse(StringReader reader) throws CommandSyntaxException {
        String input = read(reader);
        return bestFittingValue(input, JojoCustomRegistries.STANDS.getRegistry(), STAND_UNKNOWN);
    }
    
    public static <V extends IForgeRegistryEntry<V>> V bestFittingValue(String idInput, IForgeRegistry<V> registry, 
            DynamicCommandExceptionType unknownException) throws CommandSyntaxException { 
        ResourceLocation id;
        if (idInput.contains(":")) {
            id = new ResourceLocation(idInput);
            
            if (registry.containsKey(id)) {
                V value = registry.getValue(id);
                if (value != null) {
                    return value;
                }
            }
            throw unknownException.create(id);
        }
        else {
            List<Map.Entry<RegistryKey<V>, V>> addonKeys = registry.getEntries().stream()
                    .filter(registryKey -> registryKey.getKey().location().getPath().equals(idInput))
                    .collect(Collectors.toList());
            if (addonKeys.size() == 1) {
                return addonKeys.get(0).getValue();
            }
            else if (addonKeys.isEmpty()) {
                throw unknownException.create(idInput);
            }
            else {
                throw AMBIGUOUS_ID.create(idInput);
            }
        }
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

    public static <S> StandType<?> getStandType(CommandContext<S> context, String name) {
        return context.getArgument(name, StandType.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
        return suggestIterable(StandUtil.availableStands(isClientSide).map(IForgeRegistryEntry::getRegistryName), builder);
    }

    public static CompletableFuture<Suggestions> suggestIterable(Stream<ResourceLocation> registryKeys, SuggestionsBuilder builder) {
        String s = builder.getRemaining().toLowerCase(Locale.ROOT);
        addSuggestions(registryKeys, s, (resLoc) -> resLoc, (resLoc) -> builder.suggest(resLoc.toString()));
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

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
