package com.flickrdemo.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test

class FlickrJsonParserTest {

  private fun loadFixture(name: String) = Thread.currentThread().contextClassLoader.getResource(name).readText()

  @Test
  fun parseSuccess() {
    val json = loadFixture("flickr_search_ok.json")
    val parsedPage = FlickrJsonParser().parse(json)

    val photo = FlickrPhoto(id = "26704142019", secret = "4155b4e76b", farm = 5, server = "4574")
    val expectedPage = FlickrPhotoPage(page = 1, pages = 1682, photos = listOf(photo))

    assertEquals(expectedPage, parsedPage)
  }

  @Test
  fun parseStatNotOK() {
    val json = loadFixture("flickr_search_not_ok.json")
    try {
      FlickrJsonParser().parse(json)
      fail("Should throw")
    } catch (e: Exception) {
      assertEquals("Bad response - status: not_ok", e.message)
    }
  }

  @Test
  fun parseCorrupted() {
    val json = loadFixture("corrupted.json")
    try {
      FlickrJsonParser().parse(json)
      fail("Should throw")
    } catch (e: Exception) {
      assertNotNull(e)
    }
  }
}