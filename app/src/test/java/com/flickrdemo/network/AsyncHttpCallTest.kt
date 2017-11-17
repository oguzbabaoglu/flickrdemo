package com.flickrdemo.network

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class AsyncHttpCallTest {

  @Mock private lateinit var executor: ExecutorService
  @Mock private lateinit var future: Future<*>

  @Before
  fun setup() {
    MockitoAnnotations.initMocks(this)

    given(executor.submit(any())).will {
      it.getArgument<Runnable>(0).run()
      future
    }
  }

  @Test
  fun cancel() {
    val call = AsyncHttpCall(executor, task = { })

    given(future.isCancelled).willReturn(false)

    call.async { _, _ -> }
    call.cancel()

    verify(future).cancel(false)
  }

  @Test
  fun cancelAlreadyCanceled() {
    val call = AsyncHttpCall(executor, task = { })

    given(future.isCancelled).willReturn(true)

    call.async { _, _ -> }
    call.cancel()

    verify(future, never()).cancel(false)
  }

  @Test
  fun asyncSuccess() {
    var result: String? = null
    var error: Exception? = null
    val call = AsyncHttpCall(executor, task = { "success" })

    call.async { r, e ->
      result = r
      error = e
    }

    verify(executor).submit(any())
    assertEquals("success", result)
    assertNull(error)
  }

  @Test
  fun asyncException() {
    val exception = Exception()
    var result: String? = null
    var error: Exception? = null
    val call = AsyncHttpCall(executor, task = { throw exception })

    call.async { r, e ->
      result = r
      error = e
    }

    verify(executor).submit(any())
    assertNull(result)
    assertEquals(exception, error)
  }

}