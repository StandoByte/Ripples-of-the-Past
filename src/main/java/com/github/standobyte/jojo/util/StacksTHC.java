package com.github.standobyte.jojo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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
	
	public <T> boolean isEmpty(Class<T> stackType) {
		return store.containsKey(stackType) ? store.get(stackType).isEmpty() : true;
	}
}
