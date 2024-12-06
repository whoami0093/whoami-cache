package com.whoami.cache

interface Cacheable<K, V> {

    suspend fun get(key: K): V?

    suspend fun getAll(): List<V>

    fun getMap(): Map<K, V>

    suspend fun put(key: K, value: V)

    fun remove(key: K): Boolean
}