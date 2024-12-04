package ru.job4j.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public abstract class AbstractCache<K, V> {

    private final Map<K, SoftReference<V>> cache = new HashMap<>();

    public final void put(K key, V value) {
        cache.put(key, new SoftReference<>(value));
    }

    public final V get(K key) {
        V v  = nonNull(cache.get(key)) ? cache.get(key).get() : null;
        if (v == null) {
            v = load(key);
            this.put(key, v);
        }
        return v;
    }

    protected abstract V load(K key);
}