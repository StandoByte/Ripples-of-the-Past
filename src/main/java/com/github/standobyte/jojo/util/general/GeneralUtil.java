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
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.ObjectArrays;
import com.mojang.datafixers.util.Either;

import net.minecraft.util.Util;
import net.minecraftforge.common.util.LazyOptional;

public class GeneralUtil {
    private static final Random RANDOM = new Random();
    
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
    
    public static <T> T merge(Either<T, T> either) {
        return either.left().orElseGet(either.right()::get);
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
    
    public static <E> List<E> limitRandom(List<E> randomAccessMutableList, int limit) {
        int size = randomAccessMutableList.size();
        if (size > limit) {
            for (int i = 0; i < limit; i++){
                int index = i + RANDOM.nextInt(size - i);
                E tmp = randomAccessMutableList.get(index);
                randomAccessMutableList.set(index, randomAccessMutableList.get(i));
                randomAccessMutableList.set(i, tmp);
            }
            return randomAccessMutableList.stream().limit(limit).collect(Collectors.toList());
        }
        return randomAccessMutableList;
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
    
    public static <T extends Enum<T>> T nextEnumValCycle(T enumVal) {
        Class<?> enumClass = enumVal.getClass();
        // to make it work with enums with abstract methods
        Class<?> superClass = enumClass.getSuperclass();
        if (superClass != Enum.class) {
            enumClass = superClass;
        }
        
        @SuppressWarnings("unchecked")
        T[] values = ((Class<T>) enumClass).getEnumConstants();
        int ordinal = enumVal.ordinal();
        return values[(ordinal + 1) % values.length];
    }
    
    public static <T extends Enum<T>> int[] toOrdinals(T[] values) {
        int[] value = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            T elem = values[i];
            value[i] = elem != null ? elem.ordinal() : -1;
        }
        return value;
    }
    
    public static <T extends Enum<T>> T[] fromOrdinals(int[] ordinals, Class<T> enumClass) {
        T[] enumValues = enumClass.getEnumConstants();
        T[] ret = ObjectArrays.newArray(enumClass, ordinals.length);
        for (int i = 0; i < ordinals.length; i++) {
            int ordinal = ordinals[i];
            if (ordinal >= 0 && ordinal < enumValues.length) {
                ret[i] = enumValues[ordinal];
            }
        }
        return ret;
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
