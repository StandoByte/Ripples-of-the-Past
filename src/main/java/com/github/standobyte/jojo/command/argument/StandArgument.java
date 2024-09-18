package com.github.standobyte.jojo.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class StandArgument implements ArgumentType<StandType<?>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("star_platinum", "jojo:hierophant_green");
    public static final DynamicCommandExceptionType STAND_UNKNOWN = new DynamicCommandExceptionType((key) -> new TranslationTextComponent("stand.unknown", key));
    private static Map<String, List<StandType<?>>> BY_LOCATION;
    
    public static void commonSetupRegister() {
        ArgumentTypes.register("stand", StandArgument.class, new ArgumentSerializer<>(StandArgument::new));
        BY_LOCATION = ArgumentUtil.groupByKeyLocation(JojoCustomRegistries.STANDS.getRegistry());
    }
    
    @Override
    public StandType<?> parse(StringReader reader) throws CommandSyntaxException {
        String input = ArgumentUtil.read(reader);
        return ArgumentUtil.bestFittingValue(input, JojoCustomRegistries.STANDS.getRegistry(), BY_LOCATION, STAND_UNKNOWN);
    }
    
    public static <S> StandType<?> getStandType(CommandContext<S> context, String name) {
        return context.getArgument(name, StandType.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        boolean isClientSide = Thread.currentThread().getThreadGroup() == SidedThreadGroups.CLIENT;
        return ArgumentUtil.suggestIterable(StandUtil.availableStands(isClientSide).map(IForgeRegistryEntry::getRegistryName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
