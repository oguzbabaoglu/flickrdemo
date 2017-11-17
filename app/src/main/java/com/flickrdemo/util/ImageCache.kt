package com.flickrdemo.util

import android.graphics.Bitmap
import android.util.LruCache

interface ImageCache {
  fun get(key: String): Bitmap?
  fun put(key: String, bitmap: Bitmap): Bitmap?
}

class LruImageCache : ImageCache {

  // We can use a large cache (1/4 of app memory) since all we do is show images
  private val cacheSize = Runtime.getRuntime().maxMemory() / 1024 / 4

  // We could optimise with bitmap.recycle() on remove but we need to somehow guarantee it is not used
  private val lruCache = object : LruCache<String, Bitmap>(cacheSize.toInt()) {

    // The cache size will be measured in kilobytes rather than number of items.
    override fun sizeOf(key: String, bitmap: Bitmap) = bitmap.byteCount / 1024
  }

  override fun get(key: String): Bitmap? = lruCache[key]

  override fun put(key: String, bitmap: Bitmap): Bitmap? = lruCache.put(key, bitmap)
}
