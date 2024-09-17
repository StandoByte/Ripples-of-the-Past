package com.github.standobyte.jojo.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ActionArgument implements ArgumentType<Action<?>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("star_platinum_time_stop", "the_world_barrage");
    public static final DynamicCommandExceptionType TYPE_UNKNOWN = new DynamicCommandExceptionType((key) -> new TranslationTextComponent("action.unknown", key));
    
    public static void commonSetupRegister() {
        ArgumentTypes.register("jojo_action", ActionArgument.class, new ArgumentSerializer<>(ActionArgument::new));
    }
    
    @Override
    public Action<?> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation resourceLocation = read(reader);
        Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(resourceLocation);
        if (action == null) {
            throw TYPE_UNKNOWN.create(resourceLocation);
        }
        return action;
    }

    private static ResourceLocation read(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        while(reader.canRead() && isValidPathCharacter(reader.peek())) {
            reader.skip();
        }
        String s = reader.getString().substring(i, reader.getCursor());
        try {
            return modResourceLocation(s);
        } catch (ResourceLocationException var4) {
            reader.setCursor(i);
            throw INVALID_EXCEPTION.createWithContext(reader);
        }
    }

    private static boolean isValidPathCharacter(char charIn) {
        return charIn >= '0' && charIn <= '9' || charIn >= 'a' && charIn <= 'z' || charIn == '_' || charIn == ':' || charIn == '/' || charIn == '.' || charIn == '-';
    }

    private static ResourceLocation modResourceLocation(String s) {
        return s.contains(":") ? new ResourceLocation(s) : new ResourceLocation(JojoMod.MOD_ID, s);
    }

    private static final SimpleCommandExceptionType INVALID_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("argument.id.invalid"));

    public static <S> Action<?> getAction(CommandContext<S> context, String name) {
        return context.getArgument(name, Action.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return suggestIterable(JojoCustomRegistries.ACTIONS.getRegistry().getValues()
                .stream().map(IForgeRegistryEntry::getRegistryName), builder);
    }

    private static CompletableFuture<Suggestions> suggestIterable(Stream<ResourceLocation> registryKeys, SuggestionsBuilder builder) {
        String s = builder.getRemaining().toLowerCase(Locale.ROOT);
        addSuggestions(registryKeys, s, (resLoc) -> resLoc, (resLoc) -> builder.suggest(resLoc.toString()));
        return builder.buildFuture();
    }

    private static <T> void addSuggestions(Stream<T> stream, String remaining, Function<T, ResourceLocation> toResLoc, Consumer<T> suggest) {
        boolean inputColon = remaining.indexOf(':') > -1;
        stream.forEach(t -> {
            ResourceLocation resourceLocation = toResLoc.apply(t);
            if (inputColon) {
                String s = resourceLocation.toString();
                if (s.startsWith(remaining)) {
                    suggest.accept(t);
                }
            } else if (resourceLocation.getNamespace().startsWith(remaining) || 
                    resourceLocation.getNamespace().equals(JojoMod.MOD_ID) && resourceLocation.getPath().startsWith(remaining)) {
                suggest.accept(t);
            }
        });
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
