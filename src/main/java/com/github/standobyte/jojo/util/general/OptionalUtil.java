package com.github.standobyte.jojo.util.general;

import java.util.Optional;
import java.util.function.Supplier;

public class OptionalUtil {

    public static <T> Optional<T> or(Optional<T> optional, Supplier<T> or) {
        return optional.isPresent() ? optional : Optional.ofNullable(or.get());
    }
}
