package com.flickrdemo.util

import android.app.Activity
import java.util.concurrent.Executor

/**
 * An executor that will execute the command in the Android main thread.
 */
class MainThreadExecutor(private val activity: Activity) : Executor {
  override fun execute(command: Runnable) {
    if (activity.isFinishing) return
    activity.runOnUiThread(command)
  }
}
