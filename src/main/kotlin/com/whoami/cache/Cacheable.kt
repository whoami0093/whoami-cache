package com.whoami.cache

interface Cacheable<K, V> {

    fun get(key: K): V?

    fun getAll(): List<V>

    fun getMap(): Map<K, V>

    fun put(key: K, value: V)

    fun putAll(from: Map<K, V>)

    fun remove(key: K)
}