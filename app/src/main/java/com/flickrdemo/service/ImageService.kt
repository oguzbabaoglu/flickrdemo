package com.flickrdemo.service

import android.graphics.Bitmap
import com.flickrdemo.network.HttpCall
import com.flickrdemo.network.HttpClient
import com.flickrdemo.util.ImageCache
import java.net.URLEncoder

interface ImageService<out T> {
  fun searchImages(query: String, page: Int = 1, callback: (T?, Exception?) -> Unit)
  fun loadImage(url: String, callback: (Bitmap?, Exception?) -> Unit)
  fun cancelAll()
  fun cancel(url: String)
}

class FlickrImageService(
  private val httpClient: HttpClient,
  private val parser: JsonParser<FlickrPhotoPage>,
  private val cache: ImageCache,
  baseUrl: String,
  apiKey: String
) : ImageService<FlickrPhotoPage> {

  // Much better to build this url with something like HttpUrl.Builder from OkHttp.
  private val commonParams = "format=json&nojsoncallback=1&safe_search=1"
  private val searchMethod = "flickr.photos.search"
  private val searchUrl = "$baseUrl?method=$searchMethod&$commonParams&api_key=$apiKey"

  // Assumes url and calls have a one-to-one relationship (true for this app)
  private val httpCalls = hashMapOf<String, HttpCall<*>>()

  private fun String.urlEncode() = URLEncoder.encode(this, "UTF-8")

  override fun searchImages(query: String, page: Int, callback: (FlickrPhotoPage?, Exception?) -> Unit) {
    val url = "$searchUrl&text=${query.urlEncode()}&page=$page"

    httpCalls[url] = httpClient.loadString(url)
      .async { result, error ->
        try {
          callback(parser.parse(result), error)
        } catch (parseError: Exception) {
          callback(null, parseError)
        }
      }
  }

  override fun loadImage(url: String, callback: (Bitmap?, Exception?) -> Unit) {
    val cached = cache.get(url)
    if (cached != null) {
      callback(cached, null)
      return
    }
    httpCalls[url] = httpClient.loadBitmap(url)
      .async { result, error ->
        result?.let { cache.put(url, it) }
        callback(result, error)
      }
  }

  override fun cancelAll() {
    httpCalls.values.forEach { it.cancel() }
    httpCalls.clear()
  }

  override fun cancel(url: String) {
    httpCalls[url]?.cancel()
    httpCalls.remove(url)
  }
}
