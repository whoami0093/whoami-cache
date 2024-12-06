package com.whoami.example

import com.whoami.cache.LFUCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() {
    val initialCapacity = 5
    val initState: MutableMap<Int, LFUCache.CacheNode<Int, String>> = mutableMapOf(
        1 to LFUCache.CacheNode(1, "Test1"),
        2 to LFUCache.CacheNode(2, "Test2"),
        3 to LFUCache.CacheNode(3, "Test3"),
        4 to LFUCache.CacheNode(4, "Test4")
    )

    val lfuCache = LFUCache {
        capacity = initialCapacity
        hook { initState }
        evictionListener { key: Int, value: String ->
            println("Evicted: $key -> $value")
        }
        customFrequencyComparator(Comparator.naturalOrder())
    }

    runBlocking {
        lfuCache.put(5, "Test5")
        lfuCache.put(6, "Test6")

        val res: Deferred<Unit> = CoroutineScope(Dispatchers.IO + SupervisorJob())
            .async {
                lfuCache.get(1)
                println("Get key 1: ${lfuCache.get(1)}")
                lfuCache.get(1)
                lfuCache.get(2)
                println("Get key 2: ${lfuCache.get(2)}")

                lfuCache.get(3)
                println("Get key 3: ${lfuCache.get(3)}")

                lfuCache.get(4)
                println("Get key 4: ${lfuCache.get(4)}")

                lfuCache.get(5)
                println("Get key 5: ${lfuCache.get(5)}")

                lfuCache.get(6)
                println("Get key 6: ${lfuCache.get(6)}")

                println(lfuCache.getAll().size)
            }
        res.await()
    }
}