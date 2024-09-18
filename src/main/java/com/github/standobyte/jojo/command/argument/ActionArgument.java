package com.github.standobyte.jojo.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ActionArgument implements ArgumentType<Action<?>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("star_platinum_time_stop", "the_world_barrage");
    public static final DynamicCommandExceptionType TYPE_UNKNOWN = new DynamicCommandExceptionType((key) -> new TranslationTextComponent("action.unknown", key));
    private static Map<String, List<Action<?>>> BY_LOCATION;
    
    public static void commonSetupRegister() {
        ArgumentTypes.register("jojo_action", ActionArgument.class, new ArgumentSerializer<>(ActionArgument::new));
        BY_LOCATION = ArgumentUtil.groupByKeyLocation(JojoCustomRegistries.ACTIONS.getRegistry());
        makeExtraSuggestionMap();
    }
    
    @Override
    public Action<?> parse(StringReader reader) throws CommandSyntaxException {
        String input = ArgumentUtil.read(reader);
        return ArgumentUtil.bestFittingValue(input, JojoCustomRegistries.ACTIONS.getRegistry(), BY_LOCATION, TYPE_UNKNOWN);
    }

    public static <S> Action<?> getAction(CommandContext<S> context, String name) {
        return context.getArgument(name, Action.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String s = builder.getRemaining().toLowerCase(Locale.ROOT);
        
        Set<String> suggestionsSet = new HashSet<>();
        ArgumentUtil.addSuggestions(JojoCustomRegistries.ACTIONS.getRegistry().getValues()
                .stream().map(IForgeRegistryEntry::getRegistryName), s, Function.identity(), resLoc -> {
                    String str = resLoc.toString();
                    if (suggestionsSet.add(str)) {
                        builder.suggest(str);
                    }
                });
        
        if (!s.isEmpty() && !s.contains(":")) {
            for (Map.Entry<String, List<Action<?>>> entry : BY_LOCATION_NO_POWER_TYPE.entrySet()) {
                if (entry.getKey().startsWith(s)) {
                    for (Action<?> action : entry.getValue()) {
                        String actionId = action.getRegistryName().toString();
                        if (suggestionsSet.add(actionId)) {
                            builder.suggest(actionId);
                        }
                    }
                }
            }
        }
        
        return builder.buildFuture();
    }
    
    private static Map<String, List<Action<?>>> BY_LOCATION_NO_POWER_TYPE;
    private static void makeExtraSuggestionMap() {
        BY_LOCATION_NO_POWER_TYPE = JojoCustomRegistries.ACTIONS.getRegistry().getEntries().stream()
                .collect(Collectors.groupingBy(entry -> {
                    Stream<ResourceLocation> prefixIds;
                    switch (entry.getValue().getPowerClassification()) {
                    case STAND:
                        prefixIds = JojoCustomRegistries.STANDS.getRegistry().getKeys().stream();
                        break;
                    case NON_STAND:
                        prefixIds = Streams.concat(
                                JojoCustomRegistries.NON_STAND_POWERS.getRegistry().getKeys().stream(),
                                JojoCustomRegistries.HAMON_CHARACTER_TECHNIQUES.getRegistry().getKeys().stream());
                        break;
                    default:
                        throw new AssertionError();
                    }

                    ResourceLocation actionKey = entry.getKey().location();
                    String actionFullName = actionKey.getPath();
                    Optional<ResourceLocation> fromPowerType = prefixIds
                            .filter(key -> actionKey.getNamespace().equals(key.getNamespace()) && actionFullName.startsWith(key.getPath() + "_"))
                            .findFirst();

                    if (fromPowerType.isPresent()) {
                        String powerTypePrefix = fromPowerType.get().getPath();
                        return actionFullName.substring(powerTypePrefix.length() + 1);
                    }
                    return "";
                }, 
                        Collectors.mapping(entry -> entry.getValue(), Collectors.toList())));
        if (BY_LOCATION_NO_POWER_TYPE.containsKey("")) {
            try {
                BY_LOCATION_NO_POWER_TYPE.remove("");
            }
            catch (UnsupportedOperationException e) {
                Map<String, List<Action<?>>> copy = new HashMap<>();
                for (Map.Entry<String, List<Action<?>>> entry : BY_LOCATION_NO_POWER_TYPE.entrySet()) {
                    if (!"".equals(entry.getKey())) {
                        copy.put(entry.getKey(), entry.getValue());
                    }
                }
                BY_LOCATION_NO_POWER_TYPE = copy;
            }
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
