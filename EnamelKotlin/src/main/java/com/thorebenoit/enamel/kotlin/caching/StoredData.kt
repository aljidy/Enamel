package com.thorebenoit.enamel.kotlin.caching

import com.thorebenoit.enamel.kotlin.caching.store.FileStore
import com.thorebenoit.enamel.kotlin.caching.store.StoredData
import com.thorebenoit.enamel.kotlin.core.data.fromJson
import com.thorebenoit.enamel.kotlin.core.data.toJson
import com.thorebenoit.enamel.kotlin.core.print
import com.thorebenoit.enamel.kotlin.core.tryCatch
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.*
import java.nio.charset.Charset

typealias GeySystemTime = () -> Long


object FileCachedData {
    inline fun <reified T : Any> create(
        file: File, cachingTime: Long,
        useInFlight: Boolean = true,
        noinline refresh: () -> T?
    ): CachedData<T> = CachedData(
        cachingTime = cachingTime,
        store = FileStore.create<T>(file) as StoredData<T, Any>,
        useInFlight = useInFlight,
        getSystemTime = { System.currentTimeMillis() },
        refresh = refresh
    )
}

open class CachedData<T : Any>(
    val cachingTime: Long,
    val store: StoredData<T, Any>,
    val useInFlight: Boolean = true,
    val getSystemTime: GeySystemTime = { System.currentTimeMillis() },
    val refresh: () -> T?
) {


    val lastCached get() = store.get().first

    private fun store(value: T) {
        store.store(value)
    }

    private var inFlight: Deferred<T?>? = null

    private fun createDeferred() {
        if (useInFlight && inFlight != null) {
            return
        }

        inFlight = GlobalScope.async {

            val result = let {
                val localData = store.get().second
                if (localData != null && isCacheValid()) {
                    return@let localData
                }

                val remoteData = refresh()

                if (remoteData != null) {
                    store(remoteData)
                }

                return@let remoteData
            }

            inFlight = null

            result
        }


    }

    fun get(): Deferred<T?> {
        createDeferred()
        return inFlight!!
    }

    private fun isCacheValid(): Boolean = getSystemTime() < lastCached + cachingTime
}


////////////////
////////////////
////////////////




