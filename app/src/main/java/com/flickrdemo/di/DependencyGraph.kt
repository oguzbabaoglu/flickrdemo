package com.flickrdemo.di

import com.flickrdemo.BuildConfig
import com.flickrdemo.network.FixedPoolHttpClient
import com.flickrdemo.network.HttpClient
import com.flickrdemo.search.FlickrSearchPresenter
import com.flickrdemo.search.SearchActivity
import com.flickrdemo.service.FlickrImageService
import com.flickrdemo.service.FlickrJsonParser
import com.flickrdemo.service.FlickrPhotoPage
import com.flickrdemo.service.ImageService
import com.flickrdemo.util.LruImageCache
import com.flickrdemo.util.MainThreadExecutor
import java.util.concurrent.Executors

/**
 * Real app would use something like Dagger for scoping / graph validation etc.
 */
object DependencyGraph {

  fun inject(activity: SearchActivity) = FlickrSearchPresenter(
    view = activity,
    service = provideImageService(),
    uiExecutor = MainThreadExecutor(activity)
  )

  var provideImageService: () -> ImageService<FlickrPhotoPage> = {
    FlickrImageService(
      httpClient = provideHttpClient(),
      parser = FlickrJsonParser(),
      cache = LruImageCache(),
      baseUrl = BuildConfig.BASE_URL,
      apiKey = BuildConfig.FLICKR_API_KEY
    )
  }

  var provideHttpClient: () -> HttpClient = {
    FixedPoolHttpClient(
      executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    )
  }
}