package com.whoami.cache

import org.slf4j.LoggerFactory

class LRUCacheable<K,V>(
    private val capacity: Int = 10
) : Cacheable<K, V> {
    private val cache: LinkedHashMap<K, V> = LinkedHashMap(capacity, 0.75f)

    override suspend fun get(key: K): V? = cache[key]

    override suspend fun getAll(): List<V> = cache.values.toList()

    override fun getMap(): Map<K, V> = cache.toMap()

    override suspend fun put(key: K, value: V) {
        cleanUp()
        cache[key] = value
    }

    override fun remove(key: K) : Boolean {
        cache.remove(key)
        return true
    }

    private fun cleanUp() {
        if (cache.size >= capacity) {
            val firstEntry: MutableMap.MutableEntry<K, V> = cache.entries.iterator().next()
            cache.remove(firstEntry.key)
        }
        logger.info("Cleaning up entries")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
