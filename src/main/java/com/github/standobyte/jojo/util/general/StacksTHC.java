package com.github.standobyte.jojo.util.general;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nullable;

public class StacksTHC {
    private final Map<Class<?>, Stack<Object>> store = new HashMap<>();
    
    public <T> void push(Class<T> stackType, T instance) {
        store.computeIfAbsent(stackType, t -> new Stack<>()).push(instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T pop(Class<T> stackType) {
        return ((Stack<T>) store.get(stackType)).pop();
    }

    @SuppressWarnings("unchecked")
    public <T> T peek(Class<T> stackType) {
        return ((Stack<T>) store.get(stackType)).peek();
    }
    
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T peekOrNull(Class<T> stackType) {
        if (!store.containsKey(stackType)) return null;
        Stack<T> stack = ((Stack<T>) store.get(stackType));
        return stack.isEmpty() ? null : peek(stackType);
    }
    
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T popOrNull(Class<T> stackType) {
        if (!store.containsKey(stackType)) return null;
        Stack<T> stack = ((Stack<T>) store.get(stackType));
        return stack.isEmpty() ? null : pop(stackType);
    }
    
    public <T> boolean isEmpty(Class<T> stackType) {
        return store.containsKey(stackType) ? store.get(stackType).isEmpty() : true;
    }
}
