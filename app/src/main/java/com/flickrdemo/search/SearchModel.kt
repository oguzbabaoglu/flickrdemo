package com.flickrdemo.search

data class SearchModel(
  val query: String,
  val page: Int,
  val totalPages: Int,
  val images: List<ImageModel>
)

data class ImageModel(
  val url: String
)
