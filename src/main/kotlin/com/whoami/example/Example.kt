package com.whoami.example

import com.whoami.cache.LRUCacheable

fun main() {
    val initialCapacity = 10
    val cache = LRUCacheable<String, Int>(capacity = initialCapacity)

    cache.put("key", 1)
    cache.put("key2", 2)
    cache.put("key3", 2)
    cache.put("key4", 2)
    cache.put("key5", 2)
    cache.put("key6", 2)
    cache.put("key7", 2)
    cache.put("key8", 2)
    cache.put("key9", 2)
    cache.put("key10", 2)
}