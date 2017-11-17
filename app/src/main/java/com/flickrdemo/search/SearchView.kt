package com.flickrdemo.search

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.flickrdemo.R
import com.flickrdemo.di.DependencyGraph
import com.flickrdemo.util.inflate
import com.flickrdemo.util.onEditorAction
import com.flickrdemo.util.onScrollToBottom
import kotlinx.android.synthetic.main.search_activity.*

interface SearchView {
  fun updateItems(count: Int)
  fun addItemRange(start: Int, count: Int, totalCount: Int)
  fun setBitmapAt(index: Int, bitmap: Bitmap)
  fun setErrorImageAt(index: Int)
  fun showError(message: String?)
  fun showProgress()
  fun hideProgress()
}

class SearchActivity : AppCompatActivity(), SearchView {

  private val columns = 3
  private val presenter = DependencyGraph.inject(activity = this)
  private val adapter = SearchAdapter(presenter)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.search_activity)
    setupViews()
  }

  private fun setupViews() {
    searchEditText.onEditorAction { presenter.onSearch(searchEditText.text.toString()) }

    searchRecyclerView.layoutManager = StaggeredGridLayoutManager(columns, VERTICAL)
    searchRecyclerView.adapter = adapter
    searchRecyclerView.onScrollToBottom { presenter.onScrollToBottom() }
  }

  /**
   * Quick solution, ideally we would update the view model and notify the adapter to update holder at index.
   * Setting the bitmap would then be taken care of inside onBindViewHolder.
   */
  override fun setBitmapAt(index: Int, bitmap: Bitmap) {
    adapter.viewHolderAt(index)?.imageView?.setImageBitmap(bitmap)
  }

  override fun setErrorImageAt(index: Int) {
    adapter.viewHolderAt(index)?.imageView?.setImageResource(R.drawable.ic_broken_image)
  }

  override fun updateItems(count: Int) {
    adapter.count = count
    adapter.notifyDataSetChanged()
    searchRecyclerView.scrollToPosition(0)
  }

  override fun addItemRange(start: Int, count: Int, totalCount: Int) {
    adapter.count = totalCount
    adapter.notifyItemRangeInserted(start, count)
  }

  override fun showError(message: String?) {
    Toast.makeText(this, message ?: getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
  }

  override fun showProgress() {
    searchRecyclerView.visibility = View.GONE
    searchProgressBar.visibility = View.VISIBLE
  }

  override fun hideProgress() {
    searchRecyclerView.visibility = View.VISIBLE
    searchProgressBar.visibility = View.GONE
  }

  private class SearchAdapter(val presenter: SearchPresenter) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val boundViewHolders = hashMapOf<Int, ViewHolder>()

    var count: Int = 0

    fun viewHolderAt(index: Int) = boundViewHolders[index]

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.imageView.setImageResource(R.drawable.ic_image_placeholder)
      boundViewHolders[position] = holder
      presenter.onImageAppeared(position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
      val position = holder.adapterPosition
      holder.imageView.setImageDrawable(null)
      boundViewHolders.remove(position)
      presenter.onImageDisappeared(position)
    }

    override fun getItemCount() = count

    override fun onCreateViewHolder(parent: ViewGroup, type: Int) = ViewHolder(parent.inflate(R.layout.search_item))

    class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

  }
}
