package com.flickrdemo.service

import org.junit.Assert.assertEquals
import org.junit.Test

class FlickrPhotoTest {

  @Test
  fun url() {
    val photo = FlickrPhoto(id = "id", secret = "secret", farm = 1, server = "server")

    val expected = "http://farm1.static.flickr.com/server/id_secret.jpg"

    assertEquals(expected, photo.url)
  }
}