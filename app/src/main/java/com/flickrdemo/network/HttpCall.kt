package com.flickrdemo.network

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

interface HttpCall<T> {
  fun cancel()
  fun async(callback: (T?, Exception?) -> Unit): HttpCall<T>
}

/**
 * Simple cancellable [HttpCall] that submits the loading task on the provided executor service.
 * Only supports async and uses a callback for the result.
 * A real application would wrap the result in a Result object with more information (http status etc.)
 */
class AsyncHttpCall<T>(
  private val executor: ExecutorService,
  private val task: () -> T
) : HttpCall<T> {

  private var future: Future<*>? = null

  override fun cancel() {
    future?.let { if (!it.isCancelled) it.cancel(false) }
  }

  override fun async(callback: (T?, Exception?) -> Unit) = apply {
    future = executor.submit {
      try {
        val result = task()
        callback(result, null)

      } catch (e: Exception) {
        callback(null, e)
      }
    }
  }
}
