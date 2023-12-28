/*
 * Copyright (c) 2024 Taskeren and Contributors - All Rights Reserved.
 */

package cn.taskeren.encore.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * The debug helper, should not be used in product.
 */
@SuppressWarnings("ALL")
public class DebugHelper {

	private DebugHelper() {
		throw new UnsupportedOperationException();
	}

	private static Logger getSLF4JLogger() {
		return LoggerFactory.getLogger("Encore|DebugHelper");
	}

	public static void debug(String message, Object... args) {
		getSLF4JLogger().info(message, args);
	}

	public static void getObjectDetails(Object what) {
		if(what == null) {
			getSLF4JLogger().info("The object is null");
			return;
		}
		if(what instanceof Class<?>) {
			getSLF4JLogger().info("The object is a class ({})", what);
			return;
		}
		getObjectDetails(what.getClass(), what);
	}

	public static void getObjectDetails(Class<?> whatClass, @Nullable Object what) {
		var className = whatClass.getCanonicalName();
		getSLF4JLogger().info("======[ {} ]======", className);
		if(what instanceof Iterable<?>) {
			getIterableObjectDetails(what);
			return;
		}

		for(Field f : whatClass.getDeclaredFields()) {
			try {
				if(f.trySetAccessible()) {
					var value = f.get(what);
					var modifiers = f.getModifiers();

					var modifierPrefixes = new StringBuilder();
					if(Modifier.isFinal(modifiers)) modifierPrefixes.append("final ");
					if(Modifier.isStatic(modifiers)) modifierPrefixes.append("static ");

					getSLF4JLogger().info("{}{} ({}) = {}", modifierPrefixes, f.getName(), f.getGenericType().getTypeName(), value);
				} else {
					getSLF4JLogger().info("Cannot get value of field {}, because we are not accessible", f.getName());
				}
			} catch(Exception ex) {
				getSLF4JLogger().info("Cannot get value of field {}, which gives the exception {}, {}", f.getName(), ex.getClass().getCanonicalName(), ex.getMessage());
			}
		}
		getSLF4JLogger().info("========{}========", "=".repeat(className.length()));
	}

	public static void getIterableObjectDetails(Object iterableWhat) {
		var iterator = ((Iterable<?>) iterableWhat).iterator();

		var counter = 0;
		while(iterator.hasNext()) {
			var next = iterator.next();
			getSLF4JLogger().info("#{}", counter++);
			getObjectDetails(next.getClass(), next);
		}
		getSLF4JLogger().info("End. Totally iterated {} objects.", counter);
	}

	public static Object getField(Object what, String name) {
		return getField(what.getClass(), what, name);
	}

	public static Object getField(Class<?> whatClass, @Nullable Object what, String name) {
		try {
			var f = whatClass.getDeclaredField(name);
			if(f.trySetAccessible()) {
				return f.get(what);
			} else {
				getSLF4JLogger().info("Cannot get value of field {}, because we are not accessible", f.getName());
				throw new RuntimeException();
			}
		} catch(Exception ex) {
			getSLF4JLogger().info("Cannot get value of field {}, which gives the exception {}, {}", name, ex.getClass().getCanonicalName(), ex.getMessage());
			throw new RuntimeException();
		}
	}

}
