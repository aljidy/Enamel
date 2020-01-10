package com.thorebenoit.enamel.android.caching

import android.os.SystemClock
import com.thorebenoit.enamel.kotlin.caching.Store
import com.thorebenoit.enamel.kotlin.caching.GeySystemTime
import com.thorebenoit.enamel.kotlin.caching.store.StoredData
import io.paperdb.Book
import io.paperdb.Paper

object AndroidGeySystemTime : GeySystemTime {
    override fun invoke(): Long = SystemClock.currentThreadTimeMillis()
}

object PaperCachedData {
    fun <T : Any> create(key: String, cachingTime: Long, bookName: String? = null, refresh: suspend () -> T?) =
        Store(
            cachingTime = cachingTime,
            store = PaperStore(key, AndroidGeySystemTime, bookName = bookName),
            getSystemTime = AndroidGeySystemTime,
            refresh = refresh
        )
}

class PaperStore<T : Any>(
    val key: String,
    val androidGeySystemTime: AndroidGeySystemTime,
    val bookName: String? = null,
    val book: Book = if (bookName != null) Paper.book(bookName) else Paper.book()
) : StoredData<T, Any> {
    private fun _get(): Pair<Long, T?>? = book.read<Pair<Long, T?>>(key)

    override fun getStoredTime(): Long = _get()?.first ?: 0

    override fun get(): Pair<Long, T?> {
        return _get()?.let { (time, value) ->
            time to value
        } ?: 0L to null
    }

    override fun store(value: T) {
        book.write(key, androidGeySystemTime() to value)
    }

}
