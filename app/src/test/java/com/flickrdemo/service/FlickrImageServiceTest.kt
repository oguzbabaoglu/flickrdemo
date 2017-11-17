package com.flickrdemo.service

import android.graphics.Bitmap
import com.flickrdemo.network.HttpCall
import com.flickrdemo.network.HttpClient
import com.flickrdemo.util.ImageCache
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FlickrImageServiceTest {

  @Mock private lateinit var httpClient: HttpClient
  @Mock private lateinit var httpStringCall: HttpCall<String>
  @Mock private lateinit var httpBitmapCall: HttpCall<Bitmap>
  @Mock private lateinit var parser: JsonParser<FlickrPhotoPage>
  @Mock private lateinit var cache: ImageCache
  @Mock private lateinit var bitmap: Bitmap

  @Mock private lateinit var call1: HttpCall<String>
  @Mock private lateinit var call2: HttpCall<Bitmap>
  @Mock private lateinit var call3: HttpCall<String>

  private val photo = FlickrPhoto(id = "id", secret = "secret", farm = 1, server = "server")
  private val page = FlickrPhotoPage(page = 1, pages = 2, photos = listOf(photo))

  private lateinit var service: ImageService<FlickrPhotoPage>

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    given(httpClient.loadBitmap(any())).willReturn(httpBitmapCall)
    given(httpClient.loadString(any())).willReturn(httpStringCall)

    service = FlickrImageService(httpClient, parser, cache, "baseUrl", "apiKey")
  }

  @Test
  fun searchImagesEncodedQuery() {
    service.searchImages("dogs&cats", 1, { _, _ -> })

    val encodedQuery = "dogs%26cats"
    val expectedUrl = "baseUrl?method=flickr.photos.search&format=json&nojsoncallback=1&safe_search=1" +
      "&api_key=apiKey&text=$encodedQuery&page=1"

    verify(httpClient).loadString(expectedUrl)
  }

  @Test
  fun searchImagesSuccess() {
    given(httpStringCall.async(any())).will {
      it.getArgument<(String?, Exception?) -> Unit>(0).invoke("json", null)
      httpStringCall
    }
    given(parser.parse("json")).willReturn(page)

    var result: FlickrPhotoPage? = null
    var error: Exception? = null

    service.searchImages("dogs&cats", 1) { r, e ->
      result = r
      error = e
    }

    assertEquals(page, result)
    assertNull(error)
  }

  @Test
  fun searchImagesException() {
    val exception = Exception()
    given(httpStringCall.async(any())).will {
      it.getArgument<(String?, Exception?) -> Unit>(0).invoke(null, exception)
      httpStringCall
    }

    var result: FlickrPhotoPage? = null
    var error: Exception? = null

    service.searchImages("dogs&cats", 1) { r, e ->
      result = r
      error = e
    }

    assertEquals(exception, error)
    assertNull(result)
  }

  @Test
  fun searchImagesParseException() {
    val parseExeption = Exception()
    given(httpStringCall.async(any())).will {
      it.getArgument<(String?, Exception?) -> Unit>(0).invoke("json", null)
      httpStringCall
    }
    given(parser.parse("json")).will { throw parseExeption }

    var result: FlickrPhotoPage? = null
    var error: Exception? = null

    service.searchImages("dogs&cats", 1) { r, e ->
      result = r
      error = e
    }

    assertEquals(parseExeption, error)
    assertNull(result)
  }

  @Test
  fun loadImageCached() {
    given(cache.get("url")).willReturn(bitmap)

    var result: Bitmap? = null
    var error: Exception? = null

    service.loadImage("url") { r, e ->
      result = r
      error = e
    }

    assertEquals(bitmap, result)
    assertNull(error)
    verifyZeroInteractions(httpClient)
  }

  @Test
  fun loadImageSuccess() {
    given(httpBitmapCall.async(any())).will {
      it.getArgument<(Bitmap?, Exception?) -> Unit>(0).invoke(bitmap, null)
      httpBitmapCall
    }

    var result: Bitmap? = null
    var error: Exception? = null

    service.loadImage("url") { r, e ->
      result = r
      error = e
    }

    assertEquals(bitmap, result)
    assertNull(error)
  }

  @Test
  fun loadImageFail() {
    val exception = Exception()
    given(httpBitmapCall.async(any())).will {
      it.getArgument<(Bitmap?, Exception?) -> Unit>(0).invoke(null, exception)
      httpBitmapCall
    }

    var result: Bitmap? = null
    var error: Exception? = null

    service.loadImage("url") { r, e ->
      result = r
      error = e
    }

    assertEquals(exception, error)
    assertNull(result)
  }

  @Test
  fun cancelAll() {
    given(httpStringCall.async(any())).willReturn(call1, call3)
    given(httpBitmapCall.async(any())).willReturn(call2)

    service.searchImages("call1") { _, _ -> }
    service.loadImage("call2") { _, _ -> }
    service.searchImages("call3") { _, _ -> }

    service.cancelAll()

    verify(call1).cancel()
    verify(call2).cancel()
    verify(call3).cancel()
  }

  @Test
  fun cancel() {
    given(httpBitmapCall.async(any())).willReturn(call2)

    service.loadImage("call2") { _, _ -> }

    service.cancel("call2")
    service.cancel("another")

    verify(call2).cancel()
  }

}