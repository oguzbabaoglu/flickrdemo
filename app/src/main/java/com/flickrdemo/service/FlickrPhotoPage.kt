package com.flickrdemo.service

data class FlickrPhotoPage(
  val page: Int,
  val pages: Int,
  val photos: List<FlickrPhoto>
)

data class FlickrPhoto(
  val id: String,
  val secret: String,
  val farm: Int,
  val server: String
) {
  val url = "http://farm$farm.static.flickr.com/$server/${id}_$secret.jpg"
}
