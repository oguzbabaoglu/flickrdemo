package com.flickrdemo.search

import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import android.support.test.rule.ActivityTestRule
import com.flickrdemo.di.DependencyGraph
import com.flickrdemo.network.FixedPoolHttpClient
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class SearchActivityTest {

  private val testRule = ActivityTestRule(SearchActivity::class.java, false, false)

  @Before
  fun setUp() {
    // Required for Espresso to wait for the http client threads
    // In a real app this should be initialised once in a custom runner
    val idlingExecutor = IdlingThreadPoolExecutor(
      "httpClient",
      Runtime.getRuntime().availableProcessors(),
      Runtime.getRuntime().availableProcessors(),
      0L,
      TimeUnit.MILLISECONDS,
      LinkedBlockingQueue<Runnable>(),
      Executors.defaultThreadFactory()
    )
    IdlingRegistry.getInstance().register(idlingExecutor)

    DependencyGraph.provideHttpClient = { FixedPoolHttpClient(idlingExecutor) }

    testRule.launchActivity(null)
  }

  @Test
  fun search() {
    searchScreen {
      typeSearch(query = "flower")
      clickSearch()

      scrollDown()
      scrollDown()

      assert {
        hasImageCount(count = 100)
      }
    }
  }
}