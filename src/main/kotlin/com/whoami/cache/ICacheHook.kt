package com.whoami.cache

fun interface ICacheHook<K : Any, V : Any> {
    suspend fun loader(): MutableMap<K, LFUCache.CacheNode<K, V>>
}