package com.whoami.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class LFUCache<K : Any, V : Any>(
    private val capacity: Int = DEFAULT_CAPACITY,
    private val hook: ICacheHook<K, V>?,
    private val evictionListener: ((K, V) -> Unit)?,
    private val customFrequencyComparator: Comparator<Int>?
) : Cacheable<K, V>, AutoCloseable {
    private val cache: MutableMap<K, CacheNode<K, V>> = ConcurrentHashMap(capacity)
    private val frequencyMap = ConcurrentHashMap<Int, MutableSet<K>>()

    private val loadContext: CoroutineContext = Dispatchers.IO + SupervisorJob()
    private val loadScope: CoroutineScope = CoroutineScope(loadContext)

    private val loaderDeferred: Deferred<Unit> = loadScope.async { loadAll() }
    private var isLoaded: Boolean = false

    override suspend fun get(key: K): V? {
        if (!isLoaded) loaderDeferred.await()
        val node: CacheNode<K, V> = cache[key] ?: return null
        incrementFrequency(node)

        return node.value
    }

    override suspend fun getAll(): List<V> {
        if (!isLoaded) loaderDeferred.await()
        return cache.values.map { it.value }.toList()
    }

    override fun getMap(): Map<K, V> = cache.values.associate { it.key to it.value }

    override suspend fun put(key: K, value: V) {
        if (!isLoaded) loaderDeferred.await()
        val existingNode: CacheNode<K, V>? = cache[key]
        existingNode?.let { node: CacheNode<K, V> ->
            node.value = value
            incrementFrequency(node = node)
            return
        }

        if (cache.size >= capacity) evict()
        val newNode = CacheNode(key = key, value = value)
        cache[key] = newNode
        frequencyMap
            .computeIfAbsent(1) { mutableSetOf() }
            .add(newNode.key)
    }

    override fun remove(key: K): Boolean {
        val node: CacheNode<K, V> = cache.remove(key) ?: return false
        frequencyMap[node.frequency]?.let { keys: MutableSet<K> ->
            keys.remove(key)
            if (keys.isEmpty()) {
                frequencyMap.remove(node.frequency)
            }
        }
        evictionListener?.invoke(key, node.value)
        return true
    }

    override fun close() {
        loadScope.cancel()
    }

    private suspend fun loadAll() {
        val loadedData: MutableMap<K, CacheNode<K, V>> = hook!!.loader()
        cache.putAll(loadedData)

        isLoaded = true
    }

    private fun incrementFrequency(node: CacheNode<K, V>) {
        frequencyMap[node.frequency]?.let { keys: MutableSet<K> ->
            keys.remove(node.key)
            if (keys.isEmpty()) {
                frequencyMap.remove(node.frequency)
            }
        }
        node.frequency++
        frequencyMap.computeIfAbsent(node.frequency) { mutableSetOf() }.add(node.key)
    }

    private fun evict() {
        val leastFrequency: Int? = frequencyMap
            .keys
            .minWithOrNull(customFrequencyComparator ?: Comparator.naturalOrder())
        val keys: MutableSet<K> = frequencyMap[leastFrequency] ?: return
        val keyToEvict: K = keys.first()
        keys.remove(keyToEvict)
        if (keys.isEmpty()) frequencyMap.remove(leastFrequency)

        val evictedNode: CacheNode<K, V> = cache.remove(keyToEvict) ?: return
        evictionListener?.invoke(evictedNode.key, evictedNode.value)
    }

    class Builder<K : Any, V : Any> {
        var capacity: Int = DEFAULT_CAPACITY
        private var hook: ICacheHook<K, V>? = null
        private var evictionListener: ((K, V) -> Unit)? = null
        private var frequencyComparator: Comparator<Int>? = null

        fun hook(loader: () -> MutableMap<K, CacheNode<K, V>>) = apply { this.hook = ICacheHook { loader() } }
        fun evictionListener(listener: (K, V) -> Unit) = apply { this.evictionListener = listener }
        fun customFrequencyComparator(comparator: Comparator<Int>) = apply { this.frequencyComparator = comparator }

        fun build(): LFUCache<K, V> {
            require(capacity > 0) { "Max size must be greater than 0" }
            return LFUCache(
                capacity = capacity,
                hook = hook,
                evictionListener = evictionListener,
                customFrequencyComparator = frequencyComparator
            )
        }
    }

    data class CacheNode<K, V>(
        val key: K,
        var value: V,
        var frequency: Int = 1,
    )

    companion object {
        private const val DEFAULT_CAPACITY = 100
        operator fun <K : Any, V : Any> invoke(block: Builder<K, V>.() -> Unit) = Builder<K, V>().apply(block).build()
    }
}
