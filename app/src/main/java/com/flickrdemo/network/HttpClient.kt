package com.flickrdemo.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import java.util.concurrent.ExecutorService

interface HttpClient {
  fun loadString(url: String): HttpCall<String>
  fun loadBitmap(url: String): HttpCall<Bitmap>
}

/**
 * Simple [HttpClient] that can load a String or a Bitmap from a URL.
 * A real application would use something like OkHttp for proper support of protocols/headers/methods etc.
 */
class FixedPoolHttpClient(private val executor: ExecutorService) : HttpClient {

  /**
   * This will read the entire stream without a buffer, not ideal but works for this scope.
   */
  override fun loadString(url: String) = AsyncHttpCall(executor) {
    URL(url).readText()
  }

  /**
   * In a real app we would resize the bitmap before we return it,
   * most image loading libs handle this during decoding from the stream to save memory.
   */
  override fun loadBitmap(url: String) = AsyncHttpCall(executor) {
    URL(url).openStream().use { BitmapFactory.decodeStream(it) }
  }

}
