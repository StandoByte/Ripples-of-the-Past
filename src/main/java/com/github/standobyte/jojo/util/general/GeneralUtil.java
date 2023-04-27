package com.github.standobyte.jojo.util.general;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.Util;

public class GeneralUtil {
    
    public static int doFractionTimes(Runnable action, double times) {
        return doFractionTimes(action, times, null);
    }
    
    public static int doFractionTimes(Runnable action, double times, @Nullable Supplier<Boolean> breakCondition) {
        if (times < 0) {
            return 0;
        }
        int timesInt = MathUtil.fractionRandomInc(times);
        for (int i = 0; i < timesInt; i++) {
            if (breakCondition != null && breakCondition.get()) {
                return i;
            }
            action.run();
        }
        return timesInt;
    }

    public static <T> void ifPresentOrElse(Optional<T> optional, Consumer<? super T> action, Runnable emptyAction) {
        if (optional.isPresent()) {
            action.accept(optional.get());
        } else {
            emptyAction.run();
        }
    }
    
    public static <T> LinkedHashMap<Predicate<T>, List<T>> groupByPredicatesOrdered(Stream<T> elements, List<Predicate<T>> predicates, 
            @Nullable Predicate<T> commonCondition, boolean elementRepeats) {
        LinkedHashMap<Predicate<T>, List<T>> map = Util.make(new LinkedHashMap<>(), m -> {
            predicates.forEach(key -> m.put(key, new ArrayList<>()));
        });
        elements.forEach(element -> {
            if (commonCondition == null || commonCondition.test(element)) {
                for (Predicate<T> predicate : predicates) {
                    if (predicate.test(element)) {
                        map.get(predicate).add(element);
                        if (!elementRepeats) {
                            break;
                        }
                    }
                }
            }
        });
        return map;
    }
    
    @Nullable
    public static <E> E getOrLast(List<E> list, int index) {
        return list.isEmpty() ? null : list.get(Math.min(index, list.size() - 1));
    }
    
    @Nullable
    public static <T extends Enum<T>> T enumValueOfNullable(Class<T> enumType, @Nonnull String name) {
        try {
            return Enum.valueOf(enumType, name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
