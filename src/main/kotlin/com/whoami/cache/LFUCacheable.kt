package com.whoami.cache

import org.slf4j.LoggerFactory
import java.util.PriorityQueue
import kotlin.collections.HashMap

class LFUCacheable<K, V>(
    private val capacity: Int = 10
) : Cacheable<K, V> {
    private val cache: MutableMap<K, CacheNode<K, V>> = HashMap()
    private val frequencyQueue: PriorityQueue<CacheNode<K, V>> = PriorityQueue()

    override fun get(key: K): V? {
        val node = cache[key] ?: return null
        frequencyQueue.remove(node)
        node.frequency++
        frequencyQueue.add(node)

        return node.value
    }

    override fun getAll(): List<V> = cache.values.map { it.value }.toList()

    override fun getMap(): Map<K, V> = cache.values.associate { it.key to it.value }

    override fun put(key: K, value: V) {
        if (capacity == 0) return

        putWithEviction(key, value)
    }

    override fun putAll(from: Map<K, V>) {
        if (cache.size + from.size >= capacity) {
            logger.warn("New entries + existing size > capacity size")
            return
        }

        from.forEach { (key: K, value: V) ->
            putWithEviction(key = key, value = value)
        }
    }

    override fun remove(key: K) {
        val node: CacheNode<K, V> = cache[key] ?: return
        frequencyQueue.remove(node)
        cache.remove(key)
    }

    fun printCacheState() {
        println("Cache State:")
        frequencyQueue
            .sortedWith(compareBy({ it.frequency }, { it.insertionTime }))
            .forEach { node: CacheNode<K, V> ->
                println("Key: ${node.key}, Value: ${node.value}, Frequency: ${node.frequency}")
            }
    }

    private fun putWithEviction(key: K, value: V) {
        val existingNode: CacheNode<K, V>? = cache[key]
        if (existingNode != null) {
            frequencyQueue.remove(existingNode)
            existingNode.value = value
            existingNode.frequency++
            frequencyQueue.add(existingNode)
        } else {
            if (cache.size >= capacity) {
                val firstElem: CacheNode<K, V> = frequencyQueue.poll()
                cache.remove(firstElem.key)
            }
            val cacheNode = CacheNode(key = key, value = value)
            cache[key] = cacheNode
            frequencyQueue.add(cacheNode)
        }
    }

    private data class CacheNode<K, V>(
        val key: K,
        var value: V,
        var frequency: Int = 1,
        val insertionTime: Long = System.nanoTime()
    ) : Comparable<CacheNode<K, V>> {
        override fun compareTo(other: CacheNode<K, V>): Int {
            val frequencyComparison: Int = this.frequency.compareTo(other.frequency)
            if (frequencyComparison != 0) {
                return frequencyComparison
            }
            return this.insertionTime.compareTo(other.insertionTime)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}
