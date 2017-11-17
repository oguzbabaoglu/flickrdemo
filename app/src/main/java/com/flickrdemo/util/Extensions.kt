package com.flickrdemo.util

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Runs provided action, clears focus and closes the keyboard.
 */
fun EditText.onEditorAction(action: () -> Unit) = setOnEditorActionListener { _, _, _ ->
  action()
  closeKeyboard()
  clearFocus()
  false
}

fun View.closeKeyboard() {
  val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.hideSoftInputFromWindow(windowToken, 0)
}

@Suppress("UNCHECKED_CAST") // Developer error if wrong type is inflated
fun <T : View> ViewGroup.inflate(layoutRes: Int) =
  LayoutInflater.from(context).inflate(layoutRes, this, false) as T

/**
 * Simple callback for scroll to bottom. Only triggers when hard bottom is reached.
 * In a real application we would want to start loading earlier to create a smoother experience.
 */
fun RecyclerView.onScrollToBottom(callback: () -> Unit) =
  addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
      if (!canScrollVertically(1)) {
        callback()
      }
    }
  })
