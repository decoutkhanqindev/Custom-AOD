package com.decoutkhanqindev.custom_aod.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> suspendRunCatching(
    crossinline action: suspend () -> T,
): Result<T> = try {
    Result.success(action())
} catch (c: CancellationException) {
    throw c
} catch (e: Throwable) {
    Result.failure(e)
}

suspend inline fun <T> withContextCatching(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline action: suspend () -> T,
    crossinline catch: (Exception) -> T,
): T = withContext(context) {
    try {
        action()
    } catch (c: CancellationException) {
        throw c
    } catch (e: Exception) {
        catch(e)
    }
}

suspend inline fun <T> Flow<T>.collectCatching(
    crossinline action: suspend (T) -> Unit,
    crossinline catch: (Exception) -> Unit,
) = try {
    collect { action(it) }
} catch (c: CancellationException) {
    throw c
} catch (e: Exception) {
    catch(e)
}

// Giá trị mới huỷ khối action đang chạy (huỷ đó nằm trong collectLatest, không tới catch); lỗi thật của upstream hay action thì tới catch.
suspend inline fun <T> Flow<T>.collectLatestCatching(
    crossinline action: suspend (T) -> Unit,
    crossinline catch: (Exception) -> Unit,
) = try {
    collectLatest { action(it) }
} catch (c: CancellationException) {
    throw c
} catch (e: Exception) {
    catch(e)
}

fun <T> Flow<T>.recoverCatching(
    action: suspend FlowCollector<T>.(Throwable) -> Unit,
): Flow<T> = catch { t ->
    if (t is CancellationException) throw t
    action(t)
}
