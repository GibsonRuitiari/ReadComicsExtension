/*
 * Copyright 2022 Gibson Ruitiari.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package apitest

import api.ReadComicsDelegate
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import models.ComicChapters
import models.ComicPages
import models.ComicStatus
import models.ComicUpdates
import models.Mangas
import org.junit.Before
import org.junit.Test
import utils.NoOp
import kotlin.test.assertContentEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReadComicsDelegateTest {
  private lateinit var readComicsDelegate: ReadComicsDelegate
  private val homePage = "ReadComicsHome.html"
  private val category = "Read Comics Online _Categories.html"
  private val latestComicsHtml = "Read Comics Online _ Latest Comic Updates.html"
  private val comicDetails = "Tales From Harrow County_ Lost Ones (2022-).html"
  private val comicChapter = "Tales From Harrow County_ Lost Ones (2022-) Chapter 1 - Page 1.html"
  @Before
  fun setUpTestSubjects() {
    readComicsDelegate = ReadComicsDelegate(NoOp)
  }

  private fun getMarkupFromFile(file: String) = javaClass
    .getResource("/__files/$file")
    ?.readText()
  @Test
  fun `test that latest comics are being parsed correctly`() = runTest {
    val bodyString = getMarkupFromFile(latestComicsHtml) ?: return@runTest
    readComicsDelegate.parseLatestComicUpdates(bodyString).onSuccess {
      val expectedLatestComic = ComicUpdates(
        comicName = "Tales From Harrow County: Lost Ones (2022-)",
        comicThumbnailLink = "https://readcomicsonline.ru/uploads/manga/" +
          "tales-from-harrow-county-lost-ones-2022/cover/cover_250x350.jpg",
        comicUrl = "https://readcomicsonline.ru/comic/tales-from-harrow-county-lost-ones-2022",
        addedIssueLink = "https://readcomicsonline.ru/comic/tales-from-harrow-county-lost-ones-2022/4",
        addedIssueTitleNumber = "#4"
      )
      assertThat(it).contains(expectedLatestComic)
    }
  }
  @Test
  fun `test that weekly comic packs links are being returned when home page url is given`() =
    runTest {
      val bodyString = getMarkupFromFile(homePage) ?: return@runTest
      val weeklyPacksLinksArray = readComicsDelegate.weeklyUploadedComicsLinks(bodyString)
      val expectedWeeklyPacksArrayList = listOf(
        "https://readcomicsonline.ru/news/weekly-comic-upload-aug-31st-2022",
        "https://readcomicsonline.ru/news/weekly-comic-upload-aug-24th-2022",
        "https://readcomicsonline.ru/news/weekly-comic-upload-aug-17th-2022",
        "https://readcomicsonline.ru/news/weekly-comic-upload-aug-10th-2022",
        "https://readcomicsonline.ru/news/weekly-comic-upload-aug-3rd-2022",
        "https://readcomicsonline.ru/news/weekly-comic-upload-july-27th-2022"
      )
      assertThat(weeklyPacksLinksArray).isNotNull()
      assertContentEquals(expectedWeeklyPacksArrayList, weeklyPacksLinksArray)
    }
  @Test
  fun `when given a comic category comics are being parsed and list of comics_category is being returned`() =
    runTest {
      val bodyString = getMarkupFromFile(category) ?: return@runTest
      readComicsDelegate.parseComicsByCategory(bodyString).onSuccess { comics ->
        println(comics)
        val expectedComic = Mangas(
          comicName = "47 Ronin (2021)",
          comicUrl = "https://readcomicsonline.ru/comic/47-ronin-2021",
          comicThumbnailLink = "https://readcomicsonline.ru/uploads/manga/47-ronin-2021/cover/cover_250x350.jpg"
        )
        assertThat(comics).contains(expectedComic)
        assertThat(comics).isNotEmpty()
      }
    }
  @Test
  fun `test that when given chapter url comic pages are returned`() = runTest {
    val bodyString = getMarkupFromFile(comicChapter) ?: return@runTest
    readComicsDelegate.parseComicPages(bodyString).onSuccess { comicPages ->
      val expectedSampleComicPages = listOf(
        ComicPages(
          comicMetaData = "Tales From Harrow County: Lost Ones (2022-): Chapter 1 - Page 1",
          comicPageLink = " https://readcomicsonline" +
            ".ru/uploads/manga/tales-from-harrow-county-lost-ones-2022/chapters/1/01.jpg "
        ),
        ComicPages(
          comicMetaData = "Tales From Harrow County: Lost Ones (2022-): Chapter 1 - Page 2",
          comicPageLink = " https://readcomicsonline" +
            ".ru/uploads/manga/tales-from-harrow-county-lost-ones-2022/chapters/1/02.jpg "
        )
      )
      assertThat(comicPages[0]).isEqualTo(expectedSampleComicPages[0])
      assertThat(comicPages[1]).isEqualTo(expectedSampleComicPages[1])
      assertThat(comicPages).isNotEmpty()
    }
  }
  @Test
  fun `test that when given comic url comic summary information is returned`() =
    runTest {
      val bodyString = getMarkupFromFile(comicDetails) ?: return@runTest
      readComicsDelegate.parseComicDetails(bodyString).onSuccess { comic ->
        val expectedComicStatus = ComicStatus.ONGOING
        val expectedName = "Tales From Harrow County: Lost Ones (2022-)"
        val expectedComicChapters = listOf(
          ComicChapters(
            chapterTitle = "Tales From Harrow County: Lost Ones (2022-) #4",
            chapterLink = "https://readcomicsonline.ru/comic/tales-from-harrow-county-lost-ones-2022/4"
          ),
          ComicChapters(
            chapterTitle = "Tales From Harrow County: Lost Ones (2022-) #3",
            chapterLink = "https://readcomicsonline.ru/comic/tales-from-harrow-county-lost-ones-2022/3"
          ),
          ComicChapters(
            chapterTitle = "Tales From Harrow County: Lost Ones (2022-) #2",
            chapterLink = "https://readcomicsonline.ru/comic/tales-from-harrow-county-lost-ones-2022/2"
          ),
          ComicChapters(
            chapterTitle = "Tales From Harrow County: Lost Ones (2022-) #1",
            chapterLink = "https://readcomicsonline.ru/comic/tales-from-harrow-county-lost-ones-2022/1"
          )
        )
        assertThat(comic.comicName).isEqualTo(expectedName)
        assertThat(comic.comicStatus).isEqualTo(expectedComicStatus)
        assertThat(comic.comicChapters).containsExactlyElementsIn(expectedComicChapters)
      }
    }
  @Test
  fun `test that popular comics function parses data source correctly and returns list of popular comics`() =
    runTest {
      val bodyString = getMarkupFromFile(homePage) ?: return@runTest
      readComicsDelegate.parsePopularComics(bodyString).onSuccess { popularComics ->
        val expectedFirstPopularComic = Mangas(
          comicName = "Batman (2016-)",
          comicUrl = "https://readcomicsonline.ru/comic/batman-2016",
          comicThumbnailLink = "https://readcomicsonline.ru/uploads/manga/batman-2016/cover/cover_250x350.jpg"
        )
        assertThat(popularComics).contains(expectedFirstPopularComic)
        assertThat(popularComics).isNotEmpty()
      }
    }
  @Test
  fun `test that hot comic updates function parses data source correctly and returns list of comic updates`() =
    runTest {
      val bodyString = getMarkupFromFile(homePage) ?: return@runTest
      readComicsDelegate.parseHotUpdates(bodyString).onSuccess { comicUpdates ->
        val expectedFirstHotComicUpdate = ComicUpdates(
          comicThumbnailLink = "https://readcomicsonline.ru/uploads/manga/xmen-2021/cover/cover_250x350.jpg",
          comicName = "X-Men (2021-)",
          comicUrl = "https://readcomicsonline.ru/comic/xmen-2021",
          addedIssueLink = "https://readcomicsonline.ru/comic/xmen-2021/14",
          addedIssueTitleNumber = "#14"
        )
        val actualFirstHotComicUpdate = comicUpdates.first()
        assertThat(actualFirstHotComicUpdate).isEqualTo(expectedFirstHotComicUpdate)
        assertThat(comicUpdates).contains(expectedFirstHotComicUpdate)
        assertThat(comicUpdates).hasSize(5)
      }
    }
}
