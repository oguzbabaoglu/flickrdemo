package com.flickrdemo.service

import org.json.JSONArray
import org.json.JSONObject

interface JsonParser<out T> {
  fun parse(json: String?): T
}

/**
 * Quick and dirty implementation of a Json parser with hardcoded values and no real error handling.
 * If any value is missing or of the wrong type we will just handle the thrown exception in the call site.
 * A real application would use something like Gson or Moshi.
 */
class FlickrJsonParser : JsonParser<FlickrPhotoPage> {

  override fun parse(json: String?): FlickrPhotoPage = with(JSONObject(json)) {
    val status = getString("stat")
    if (status != "ok") throw Exception("Bad response - status: $status")

    parsePhotoPage(getJSONObject("photos"))
  }

  private fun parsePhotoPage(page: JSONObject) = FlickrPhotoPage(
    page = page.getInt("page"),
    pages = page.getInt("pages"),
    photos = parsePhotos(page.getJSONArray("photo"))
  )

  /**
   * [JSONArray] does not implement [Collection], so we can't simply use map here :(
   */
  private fun parsePhotos(photos: JSONArray) = ArrayList<FlickrPhoto>(photos.length()).apply {
    (0 until photos.length()).forEach { i ->
      val photo = photos.getJSONObject(i)
      add(FlickrPhoto(
        id = photo.getString("id"),
        secret = photo.getString("secret"),
        farm = photo.getInt("farm"),
        server = photo.getString("server")
      ))
    }
  }
}