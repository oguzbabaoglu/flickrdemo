package com.flickrdemo.search

import android.graphics.Bitmap
import com.flickrdemo.service.FlickrPhoto
import com.flickrdemo.service.FlickrPhotoPage
import com.flickrdemo.service.ImageService
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.Executor

class FlickrSearchPresenterTest {

  @Mock private lateinit var view: SearchView
  @Mock private lateinit var service: ImageService<FlickrPhotoPage>
  @Mock private lateinit var uiExecutor: Executor
  @Mock private lateinit var bitmap: Bitmap

  private val photo = FlickrPhoto(id = "id", secret = "secret", farm = 1, server = "server")
  private val page = FlickrPhotoPage(page = 1, pages = 2, photos = listOf(photo))
  private val image = ImageModel(url = photo.url)

  private lateinit var presenter: SearchPresenter
  private lateinit var presenterWithPage: SearchPresenter

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    given(uiExecutor.execute(any())).will { it.getArgument<Runnable>(0).run() }

    presenter = FlickrSearchPresenter(view, service, uiExecutor)
    presenterWithPage = FlickrSearchPresenter(view, service, uiExecutor,
      SearchModel("query", 1, 2, listOf(image, image))
    )
  }

  @Test
  fun onSearch() {
    presenter.onSearch("query")

    verify(view).showProgress()
    verify(service).cancelAll()
    verify(service).searchImages(eq("query"), eq(1), any())
  }

  @Test
  fun onSearchSuccess() {
    given(service.searchImages(any(), any(), any())).will {
      it.getArgument<(FlickrPhotoPage?, Exception?) -> Unit>(2).invoke(page, null)
    }

    presenter.onSearch("query")

    verify(uiExecutor).execute(any())
    verify(view).updateItems(count = 1)
    verify(view).hideProgress()

    verify(view, never()).showError(any())
  }

  @Test
  fun onSearchException() {
    val exception = Exception("message")

    given(service.searchImages(any(), any(), any())).will {
      it.getArgument<(FlickrPhotoPage?, Exception?) -> Unit>(2).invoke(null, exception)
    }

    presenter.onSearch("query")

    verify(uiExecutor).execute(any())
    verify(view).showError("message")
    verify(view).hideProgress()

    verify(view, never()).updateItems(any())
  }

  @Test
  fun onScrollToBottomSuccess() {
    given(service.searchImages(any(), any(), any())).will {
      it.getArgument<(FlickrPhotoPage?, Exception?) -> Unit>(2).invoke(page, null)
    }

    presenterWithPage.onScrollToBottom()

    verify(service).searchImages(eq("query"), eq(2), any())
    verify(uiExecutor).execute(any())
    verify(view).addItemRange(1, 1, 3)
  }

  @Test
  fun onScrollToBottomException() {
    val exception = Exception("message")

    given(service.searchImages(any(), any(), any())).will {
      it.getArgument<(FlickrPhotoPage?, Exception?) -> Unit>(2).invoke(null, exception)
    }

    presenterWithPage.onScrollToBottom()

    verify(service).searchImages(eq("query"), eq(2), any())
    verify(uiExecutor).execute(any())
    verify(view).showError("message")

    verify(view, never()).addItemRange(any(), any(), any())
  }

  @Test
  fun onImageAppearedSuccess() {
    given(service.loadImage(any(), any())).will {
      it.getArgument<(Bitmap?, Exception?) -> Unit>(1).invoke(bitmap, null)
    }

    presenterWithPage.onImageAppeared(0)

    verify(service).loadImage(eq(photo.url), any())
    verify(uiExecutor).execute(any())
    verify(view).setBitmapAt(0, bitmap)
  }

  @Test
  fun onImageAppearedException() {
    given(service.loadImage(any(), any())).will {
      it.getArgument<(Bitmap?, Exception?) -> Unit>(1).invoke(null, Exception())
    }

    presenterWithPage.onImageAppeared(0)

    verify(service).loadImage(eq(photo.url), any())
    verify(uiExecutor).execute(any())
    verify(view).setErrorImageAt(0)
  }

  @Test
  fun onImageAppearedInvalid() {
    presenterWithPage.onImageAppeared(-1)

    verifyZeroInteractions(service)
  }

  @Test
  fun onImageDisappeared() {
    presenterWithPage.onImageDisappeared(0)

    verify(service).cancel(photo.url)
  }

  @Test
  fun onImageDisappearedInvalid() {
    presenterWithPage.onImageDisappeared(-1)

    verifyZeroInteractions(service)
  }

}