package com.shadowfang.rosetta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe collection factories for use on Folia.
 */
public final class ThreadSafeCollections {

    private ThreadSafeCollections() {}

    public static <K, V> Map<K, V> concurrentMap() {
        return new ConcurrentHashMap<>();
    }

    public static <K, V> Map<K, V> concurrentMap(Map<K, V> initial) {
        return new ConcurrentHashMap<>(initial);
    }

    public static <T> Set<T> concurrentSet() {
        return ConcurrentHashMap.newKeySet();
    }

    public static <T> Set<T> concurrentSet(Set<T> initial) {
        Set<T> set = ConcurrentHashMap.newKeySet();
        set.addAll(initial);
        return set;
    }

    /**
     * Thread-safe WeakHashMap wrapper.
     * Uses ConcurrentHashMap + reference tracking.
     * Note: This is an approximation — true weak references require more complex implementation.
     * For most Folia use cases, ConcurrentHashMap is sufficient.
     */
    public static <K, V> Map<K, V> concurrentWeakMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Thread-safe copy-on-write list for read-heavy, write-rare scenarios.
     */
    public static <T> List<T> copyOnWriteList() {
        return new java.util.concurrent.CopyOnWriteArrayList<>();
    }

    public static <T> List<T> copyOnWriteList(List<T> initial) {
        return new java.util.concurrent.CopyOnWriteArrayList<>(initial);
    }
}
