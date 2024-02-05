package com.github.standobyte.jojo.util.general;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import net.minecraft.util.Util;
import net.minecraftforge.common.util.LazyOptional;

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
    
    public static <T> boolean orElseFalse(LazyOptional<T> optional, Predicate<T> predicate) {
        return orElseFalse(optional.resolve(), predicate);
    }
    
    public static <T> boolean orElseFalse(Optional<T> optional, Predicate<T> predicate) {
        return optional.map(element -> predicate.test(element)).orElse(false);
    }
    
    public static <S, T> Predicate<T> mapPredicate(Predicate<S> predicate, Function<T, S> function) {
        if (predicate == null) {
            return null;
        }
        
        return original -> predicate.test(function.apply(original));
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
    
    public static <T> boolean contains(T[] array, T element) {
        for (T t : array) {
            if (t == element) {
                return true;
            }
        }
        
        return false;
    }
    
    public static int largestLessOrEqualBinarySearch(int[] arr, int num) {
        if (arr.length == 0) return -1;
        
        int left = 0;
        int right = arr.length - 1;
        if (num >= arr[right]) return right;
        
        int index = -1;
        while (left <= right) {
            int mid = (left + right) / 2;
            
            if (arr[mid] > num) {
                right = mid - 1;
            }
            else {
                index = mid;
                left = mid + 1;
            }
        }
        return index;
    }
    
    public static BufferedWriter newWriterMkDir(File file, Charset charset) throws IOException {
        checkNotNull(file);
        checkNotNull(charset);
        return new BufferedWriter(new OutputStreamWriter(FileUtils.openOutputStream(file), charset));
    }
}
