package com.whoami.example

import com.whoami.cache.LFUCacheable
import com.whoami.cache.LRUCacheable

fun main() {
    val initialCapacity = 10
    val lruCacheable = LRUCacheable<String, Int>(capacity = initialCapacity)

    lruCacheable.put("key", 1)
    lruCacheable.put("key2", 2)
    lruCacheable.put("key3", 2)
    lruCacheable.put("key4", 2)
    lruCacheable.put("key5", 2)
    lruCacheable.put("key6", 2)
    lruCacheable.put("key7", 2)
    lruCacheable.put("key8", 2)
    lruCacheable.put("key9", 2)
    lruCacheable.put("key10", 2)

    val lfuCacheable = LFUCacheable<Int, String>(capacity = initialCapacity)

    lfuCacheable.put(1, "A")
    lfuCacheable.put(2, "B")
    lfuCacheable.put(3, "C")
    lfuCacheable.printCacheState()

    lfuCacheable.get(1)
    lfuCacheable.get(1)
    lfuCacheable.get(2)
    lfuCacheable.printCacheState()

    lfuCacheable.put(4, "D")
    lfuCacheable.printCacheState()

    lfuCacheable.get(3)
    println("Get key 3: ${lfuCacheable.get(3)}")

    lfuCacheable.put(5, "E")
    lfuCacheable.printCacheState()
}