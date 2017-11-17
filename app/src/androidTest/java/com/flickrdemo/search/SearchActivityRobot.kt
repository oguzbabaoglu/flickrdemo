package com.flickrdemo.search

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.pressImeActionButton
import android.support.test.espresso.action.ViewActions.swipeUp
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.v7.widget.RecyclerView
import android.view.View
import com.flickrdemo.R
import org.hamcrest.Description

fun searchScreen(actions: SearchActivityRobot.() -> Unit) = SearchActivityRobot().apply(actions)

class SearchActivityRobot {

  fun assert(assertions: Screen.() -> Unit) = Screen().apply(assertions)

  fun typeSearch(query: String) {
    onView(withId(R.id.searchEditText))
      .perform(typeText(query))
  }

  fun clickSearch() {
    onView(withId(R.id.searchEditText))
      .perform(pressImeActionButton())
  }

  fun scrollDown() {
    onView(withId(R.id.searchRecyclerView))
      .perform(swipeUp())
  }

  class Screen {

    fun hasImageCount(count: Int) {
      onView(withId(R.id.searchRecyclerView))
        .check(matches(object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
          override fun describeTo(description: Description) {
            description.appendText("item count: $count")
          }

          override fun matchesSafely(item: RecyclerView) = item.adapter.itemCount == count
        }))
    }
  }
}