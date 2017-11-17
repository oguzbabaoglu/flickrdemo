package com.flickrdemo.search

import com.flickrdemo.service.FlickrPhotoPage
import com.flickrdemo.service.ImageService
import java.util.concurrent.Executor

// Presenter interface usually not needed since view is not unit tested, still helps with organisation
interface SearchPresenter {
  fun onSearch(query: String)
  fun onScrollToBottom()
  fun onImageAppeared(index: Int)
  fun onImageDisappeared(index: Int)
}

class FlickrSearchPresenter(
  private val view: SearchView,
  private val service: ImageService<FlickrPhotoPage>,
  private val uiExecutor: Executor,
  // In a real app we would save this model to the state bundle and restore
  private var model: SearchModel = SearchModel(query = "", page = 1, totalPages = 1, images = emptyList())
) : SearchPresenter {

  private var loading = false

  private fun runOnUiThread(runnable: () -> Unit) = uiExecutor.execute(runnable)

  override fun onSearch(query: String) {
    view.showProgress()
    service.cancelAll()
    service.searchImages(query) { result, error ->
      runOnUiThread {
        result?.let {
          model = SearchModel(query, it.page, it.pages, it.photos.map { ImageModel(it.url) })
          view.updateItems(count = model.images.size)
        }
        // should be run through an error handler first for logging / localisation
        error?.let { view.showError(it.message) }
        view.hideProgress()
      }
    }
  }

  override fun onScrollToBottom() {
    if (loading || model.page == model.totalPages) return

    // Ideally we would show some kind of loading indicator at the end of the list
    loading = true
    service.searchImages(model.query, model.page + 1) { result, error ->
      runOnUiThread {
        loading = false
        result?.let {
          val start = maxOf(0, model.images.size - 1)
          model = model.copy(page = it.page, images = model.images + it.photos.map { ImageModel(it.url) })
          view.addItemRange(start, count = it.photos.size, totalCount = model.images.size)
        }
        error?.let { view.showError(it.message) }
      }
    }
  }

  // RecyclerView can sometimes report wrong adapter position
  private fun Int.isInvalid() = this < 0 || this >= model.images.size

  override fun onImageAppeared(index: Int) {
    if (index.isInvalid()) return

    service.loadImage(model.images[index].url) { result, error ->
      runOnUiThread {
        result?.let { view.setBitmapAt(index, it) }
        error?.let { view.setErrorImageAt(index) }
      }
    }
  }

  override fun onImageDisappeared(index: Int) {
    if (index.isInvalid()) return

    service.cancel(model.images[index].url)
  }
}
