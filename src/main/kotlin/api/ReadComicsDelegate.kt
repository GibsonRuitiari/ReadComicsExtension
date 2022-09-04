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
package api/*
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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import models.ComicCategories
import models.ComicChapters
import models.ComicDetails
import models.ComicPages
import models.ComicStatus
import models.ComicUpdates
import models.Mangas
import models.WeeklyPacks
import org.http4k.client.JavaHttpClient
import org.http4k.client.withAsyncApi
import org.http4k.core.Response
import utils.Logger
import utils.doOnBackground
import utils.get
import utils.parse
import java.io.IOException

/**
 * Default implementation of [ReadComics] api
 * example usage
 * ```
 * val readComicsExtension = api.ReadComicsDelegate(Println)
 *  // called in a coroutine scope/suspend function since this is a suspend function
 * readComicsExtension.getHotComicUpdates().fold({comicUpdates->
 *   // on success
 *   comicUpdates.forEach{comicUpdate->
 *    // do something with the update
 *    println(comicUpdate)
 *   }
 * },{
 *   // on error
 *   println(it.localizedMessage) // do something with the error
 * })
 * ```
 * @property logger logger to be used to log messages
 */
@OptIn(ExperimentalSerializationApi::class)
class ReadComicsDelegate constructor(private val logger: Logger) : ReadComics {
  private val comicUpdates = ArrayList<ComicUpdates>(maxInitialCapacity) // initially they can never be more than 6
  private val client by lazy { JavaHttpClient() }

  // some comics don't have image links, so we create them manually
  private fun String.toImageUrl(): String? {
    if (!startsWith(httpsPrefix)) return null
    return "https://readcomicsonline.ru/uploads/manga/" +
      "${substringAfter(comicPrefix)}/cover/cover_250x350.jpg"
  }
  internal suspend fun weeklyUploadedComicsLinks(bodyString: String): List<String> = try {
    val comicLinks = arrayListOf<String>()
    bodyString.parse { document ->
      comicLinks
        .addAll(
          document.select(weeklyComicsUploadSelector)
            .map { it.attr(hrefTag) }
        )
    }
    comicLinks
  } catch (ex: IOException) {
    logger.log("[Parsing-Weekly-Uploaded-Comics-Links-Error]", ex)
    emptyList()
  }
  internal suspend fun parseSearchComic(searchAbsUrl: String): Result<List<Mangas>> {
    val searchResults = arrayListOf<Mangas>()
    return try {
      doOnBackground {
        client.withAsyncApi()(searchAbsUrl.get()) { res: Response ->
          val obj = Json.decodeFromStream<SearchResults>(res.body.stream)
          val result = obj.suggestions
            .map { suggestions ->
              val data = suggestions.data
              Mangas(
                comicUrl = "https://readcomicsonline.ru/comic/$data",
                comicName = data,
                comicThumbnailLink =
                "https://readcomicsonline.ru/uploads/manga/$data/cover/cover_250x350.jpg"
              )
            }
          searchResults.addAll(result)
        }
      }
      Result.success(searchResults)
    } catch (ex: IOException) {
      logger.log("[Parsing-Search-Comic-Error]", ex)
      Result.failure(ex)
    }
  }
  internal suspend fun parseLatestComicUpdates(bodyString: String): Result<List<ComicUpdates>> {
    val comicUpdatesArrayList = arrayListOf<ComicUpdates>()
    return try {
      bodyString
        .parse { document ->
          val latestComicIssueLinks = document
            .select(latestIssueLinkSelector)
            .map { it.attr(hrefTag) }
          val latestComicTitles = document
            .select(latestComicReleaseDetailsSelector)
            .map { it.text() }
          val latestComicUrls = document
            .select(latestComicReleaseDetailsSelector)
            .map { it.attr(hrefTag) }

          val latestComicIssuesLinkIterator = latestComicIssueLinks.iterator()
          val latestComicTitlesIterator = latestComicTitles.iterator()
          val latestComicUrlsIterator = latestComicUrls.iterator()

          while (latestComicIssuesLinkIterator.hasNext() &&
            latestComicTitlesIterator.hasNext() &&
            latestComicUrlsIterator.hasNext()
          ) {
            val comicName = latestComicTitlesIterator.next()
            val comicLink = latestComicUrlsIterator.next()
            val comicThumbnailUrl = comicLink.toImageUrl() ?: return@parse
            val releasedIssueLink = latestComicIssuesLinkIterator.next()
            val releasedIssueTitleNumber = "#${releasedIssueLink.substringAfterLast("/")}"

            val latestComicUpdate = ComicUpdates(
              comicThumbnailLink = comicThumbnailUrl,
              comicUrl = comicLink,
              comicName = comicName, addedIssueLink = releasedIssueLink,
              addedIssueTitleNumber = releasedIssueTitleNumber
            )

            comicUpdatesArrayList.add(latestComicUpdate)
          }
        }
      Result.success(comicUpdatesArrayList)
    } catch (ex: IOException) {
      logger.log("[Latest Comic Updates Error]", ex)
      Result.failure(ex)
    }
  }

  internal suspend fun parseWeeklyComicPacks(): Result<List<WeeklyPacks>> {
    val weeklyPacks = arrayListOf<WeeklyPacks>()
    return try {
      weeklyUploadedComicsLinks(client(getBaseUrl().get()).bodyString()).forEach { link ->
        val weeklyPackComics = arrayListOf<Mangas>()
        val request = link.get()
        client(request).parse { doc ->
          val weeklyUploadComicLinks = doc.select(weeklyPackComicsSelector)
            .map { it.attr(hrefTag) }
          val weeklyUploadsComicName = doc.select(weeklyPackComicsSelector)
            .map { it.text() }
          val weeklyUploadsComicThumbnailUrls = weeklyUploadComicLinks
            .map { it.substringBeforeLast("/").toImageUrl() }
          val param0 = weeklyUploadsComicName.iterator()
          val param1 = weeklyUploadsComicThumbnailUrls.iterator()
          val param2 = weeklyUploadComicLinks.iterator()
          while (param0.hasNext() && param1.hasNext() &&
            param2.hasNext()
          ) {
            val weeklyPackComicName = param0.next()
            val weeklyPackComicThumbnailUrl = param1.next()
            val weeklyPackComicLink = param2.next()
            // for sanity check
            if (weeklyPackComicThumbnailUrl == null) {
              logger.log("thumbnail url for comic $weeklyPackComicName was null")
              return@parse
            }
            val manga = Mangas(
              comicName = weeklyPackComicName, comicUrl = weeklyPackComicLink,
              comicThumbnailLink = weeklyPackComicThumbnailUrl
            )
            weeklyPackComics.add(manga)
          }
        }
        val weeklyPackAddedDate = link.substringAfter(weeklyComicUploadPrefix)
        weeklyPacks.add(WeeklyPacks(weeklyPackAddedDate, weeklyPackComics))
      }
      Result.success(weeklyPacks)
    } catch (ex: IOException) {
      logger.log("[Parsing-Weekly-Comic-Packs-Error]", ex)
      Result.failure(ex)
    }
  }
  internal suspend fun parsePopularComics(bodyString: String): Result<List<Mangas>> {
    val popularMangas = arrayListOf<Mangas>()
    return try {
      bodyString.parse { document ->
        val popularComicsLinks = document.select(popularComicsSelector)
          .map { it.attr(hrefTag) }
        val popularComicsNames = document.select(popularComicsSelector)
          .map { it.text() }
        val popularComicsThumbnailUrls = popularComicsLinks
          .map { it.toImageUrl() }
        val param0 = popularComicsNames.iterator()
        val param1 = popularComicsLinks.iterator()
        val param2 = popularComicsThumbnailUrls.iterator()
        while (param0.hasNext() && param1.hasNext() &&
          param2.hasNext()
        ) {
          val comicName = param0.next()
          val comicLink = param1.next()
          val comicThumbnailUrl = param2.next()
          if (comicThumbnailUrl == null) {
            logger.log("[PopularComics] thumbnail url for comic $comicName was null")
            return@parse
          }
          popularMangas.add(Mangas(comicUrl = comicLink, comicThumbnailLink = comicThumbnailUrl, comicName = comicName))
        }
      }
      Result.success(popularMangas)
    } catch (ex: IOException) {
      logger.log("[Parsing-Popular-Comics-Error]", ex)
      Result.failure(ex)
    }
  }
  internal suspend fun parseComicDetails(
    bodyString: String
  ): Result<ComicDetails> {
    var comicDetails: ComicDetails? = null
    return try {
      bodyString.parse { document ->
        val comicTitle = document.select(comicDetailsTitleSelector)
          .map { it.text() }
          .first()
        val comicSummary = document.select(comicDetailsSummarySelector)
          .map { it.text() }
          .first()
        val comicStatus = document.select(comicDetailsStatusSelector)
          .map { it.text() }
          .first()
        val comicYearReleased = document.select(comicDetailsYearReleasedSelector)
          .map { it.text() }
          .first()
        val comicCover = document.select(comicDetailsCoverImageSelector)
          .map { it.attr(srcTag) }
          .firstOrNull()
        val comicType = document.select(comicDetailsTypeSelector)
          .map { it.text() }
          .first()
        val chapterLinks = document
          .select(comicDetailsChaptersSelector)
          .map { it.attr(hrefTag) }
        val comicCategory = document
          .select(comicDetailsCategorySelector)
          .map { it.text() }
          .firstOrNull()
        val chaptersTitle = document.select(comicDetailsChaptersSelector)
          .map { it.text() }
        val chaptersList = chaptersTitle.zip(chapterLinks)
          .map { ComicChapters(chapterTitle = it.first, chapterLink = it.second) }
        val status = if (comicStatus.contains("Ongoing")) ComicStatus.ONGOING else ComicStatus.COMPLETED
        val cover = if (comicCover != null && comicCover.startsWith("//")) "https:$comicCover" else comicCover
        comicDetails = ComicDetails(
          comicChapters = chaptersList, comicName = comicTitle,
          comicCategory = comicCategory, comicCover = cover, comicReleaseYear = comicYearReleased.toInt(),
          comicSummary = comicSummary, comicStatus = status, comicType = comicType
        )
      }
      Result.success(comicDetails!!)
    } catch (ex: IOException) {
      logger.log("[Comic-Details-Parsing-Error]", ex)
      Result.failure(ex)
    }
  }
  internal suspend fun parseComicPages(bodyString: String): Result<List<ComicPages>> {
    val comicPages = arrayListOf<ComicPages>()
    return try {

      bodyString.parse { document ->
        val comicPagesUrls = document.select(comicPagesSelector).map { it.attr(dataSrcTag) }
        val comicPagesMetadata = document.select(comicPagesSelector).map { it.attr(altTag) }
        val urlIterator = comicPagesUrls.iterator()
        val metadataIterator = comicPagesMetadata.iterator()
        while (urlIterator.hasNext() && metadataIterator.hasNext()) {
          val comicPageUrl = urlIterator.next()
          val comicPageMetadata = metadataIterator.next()
          comicPages.add(
            ComicPages(
              comicMetaData = comicPageMetadata,
              comicPageLink = comicPageUrl
            )
          )
        }
      }
      Result.success(comicPages)
    } catch (ex: IOException) {
      logger.log("[Parsing-Comic-Pages-Error]", ex)
      Result.failure(ex)
    }
  }
  internal suspend fun parseComicsByCategory(bodyString: String):
    Result<List<Mangas>> {
    val comics = mutableListOf<Mangas>()
    return try {
      bodyString.parse { document ->
        val scrapedComicNames = document.select(comicsByCategoryNameSelector)
          .map { it.text() }
        val scrapedComicLinks = document.select(comicByCategoryComicLinkSelector)
          .map { it.attr(hrefTag) }
        val scrapedComicThumbnailUrls = document.select(comicByCategoryThumbnailUrlSelector)
          .map { it.attr(srcTag) }

        val comicNamesIterator = scrapedComicNames.iterator()
        val comicLinksIterator = scrapedComicLinks.iterator()
        val comicThumbnailUrlsIterator = scrapedComicThumbnailUrls.iterator()

        while (comicNamesIterator.hasNext() && comicLinksIterator.hasNext() &&
          comicThumbnailUrlsIterator.hasNext()
        ) {
          val comicName = comicNamesIterator.next()
          val comicLink = comicLinksIterator.next()
          val comicThumbnailUrl = "https:${comicThumbnailUrlsIterator.next()}"
          val mangas = Mangas(
            comicName = comicName, comicUrl = comicLink,
            comicThumbnailLink = comicThumbnailUrl
          )
          comics.add(mangas)
        }
      }
      Result.success(comics)
    } catch (ex: IOException) {
      logger.log("[Parsing comic by category error]", ex)
      Result.failure(ex)
    }
  }

  override suspend fun searchForComic(searchTerm: String): Result<List<Mangas>> {
    if (searchTerm.isEmpty() || searchTerm.isBlank()) {
      return Result.success(emptyList())
    }
    val query = searchTerm.replace("\\s+".toRegex(), "+")
    val searchAbsUrl = "$searchUrl=$query"
    return parseSearchComic(searchAbsUrl)
  }

  override suspend fun getLatestComics(pageNumber: Int): Result<List<ComicUpdates>> {
    if (pageNumber> maxPageNumber) return Result.success(emptyList())
    val absoluteLink = "$latestComicsUpdatesLink?page=$pageNumber"
    val bodyString = client(absoluteLink.get()).bodyString()
    return parseLatestComicUpdates(bodyString)
  }

  override suspend fun getPopularComics(): Result<List<Mangas>> {
    val bodyString = client(getBaseUrl().get()).bodyString()
    return parsePopularComics(bodyString)
  }

  override suspend fun getHotComicUpdates(): Result<List<ComicUpdates>> =
    parseHotUpdates(
      client(getBaseUrl().get())
        .bodyString()
    )

  override suspend fun getComicsByCategory(pageNumber: Int, category: ComicCategories): Result<List<Mangas>> {
    val absoluteLink = "${category.categoryLink}?page=$pageNumber"
    return parseComicsByCategory(client(absoluteLink.get()).bodyString())
  }

  override suspend fun getWeeklyComicPacks(): Result<List<WeeklyPacks>> =
    parseWeeklyComicPacks()

  override suspend fun getComicDetails(comicAbsoluteUrl: String): Result<ComicDetails> =
    parseComicDetails(
      client(comicAbsoluteUrl.get())
        .bodyString()
    )

  override suspend fun getChapterPages(chapterUrl: String): Result<List<ComicPages>> =
    parseComicPages(
      client(chapterUrl.get())
        .bodyString()
    )

  private fun getBaseUrl(url: String? = null): String = url ?: baseUrl

  /**
   * Returns the hot updated comics with the updated issue
   */
  internal suspend fun parseHotUpdates(bodyString: String): Result<List<ComicUpdates>> {
    return try {
      bodyString.parse {
        val comicThumbnailUrls = it.select(hotUpdatesThumbnailUrlsSelector)
          .map { value -> value.attr(srcTag) }
        val comicNames = it.select(hotUpdatesComicNamesSelector)
          .map { value -> value.text() }
        val updatedIssueLinks = it.select(hotUpdatesIssueSelector)
          .map { value -> value.attr(hrefTag) }
        val updatedIssuesNumber = it.select(hotUpdatesIssueSelector)
          .map { value -> value.text() }
        val comicLinks = it.select(hotUpdatesComicLinksSelector)
          .map { value -> value.attr(hrefTag) }
        val param0 = comicNames.iterator()
        val param1 = comicThumbnailUrls.iterator()
        val param2 = updatedIssueLinks.iterator()
        val param3 = updatedIssuesNumber.iterator()
        val param4 = comicLinks.iterator()
        while (param0.hasNext() && param1.hasNext() &&
          param2.hasNext() &&
          param3.hasNext() && param4.hasNext()
        ) {
          val comicName = param0.next()
          val comicThumbnail = param1.next()
          val comicUpdatedIssueLink = param2.next()
          val comicUpdatedIssueNumber = param3.next()
          val comicLink = param4.next()
          comicUpdates.add(
            ComicUpdates(
              comicName = comicName, comicUrl = comicLink,
              comicThumbnailLink =
              comicThumbnail,
              addedIssueLink = comicUpdatedIssueLink,
              addedIssueTitleNumber = comicUpdatedIssueNumber
            )
          )
        }
      }
      Result.success(comicUpdates)
    } catch (ex: IOException) {
      logger.log("[HotComic-Updates-Parsing-Error]", ex)
      Result.failure(ex)
    }
  }

  companion object {
    internal const val baseUrl = "https://readcomicsonline.ru/"
    internal const val httpsPrefix = "https"
    internal const val comicPrefix = "/comic/"
    internal const val searchUrl = "https://readcomicsonline.ru/search?query"
    internal const val latestComicsUpdatesLink = "https://readcomicsonline.ru/latest-release"
    internal const val weeklyComicUploadPrefix = "weekly-comic-upload-"
    // common css tags
    internal const val srcTag = "src"
    internal const val hrefTag = "href"
    internal const val dataSrcTag = "data-src"
    internal const val altTag = "alt"

    // css selectors for hot updates
    internal const val hotUpdatesThumbnailUrlsSelector = "div.schedule-avatar img[src]"
    internal const val hotUpdatesComicNamesSelector = "div.schedule-name"
    internal const val hotUpdatesIssueSelector = "a.schedule-add"
    internal const val hotUpdatesComicLinksSelector = "div.schedule-name a[href]"

    // css selectors for weekly upload
    internal const val weeklyComicsUploadSelector = "div.manganews h3.manga-heading>a[href]"
    internal const val weeklyPackComicsSelector = "p ~ ul>li a[href]"
    // css selectors for comic pages
    internal const val comicPagesSelector = "div#all img"
    // css selectors for latest comics
    internal const val latestIssueLinkSelector = "div.mangalist h6.events-subtitle a[href]"
    internal const val latestComicReleaseDetailsSelector = "h3.manga-heading > a[href]"

    // css selectors for comics by category
    internal const val comicsByCategoryNameSelector = "h5.media-heading > a.chart-title"
    internal const val comicByCategoryThumbnailUrlSelector = "div.media-left > a[href] > img[src]"
    internal const val comicByCategoryComicLinkSelector = "div.media-left > a[href]"

    // css selectors for comic details
    private const val comicDetailsTableSelector = "dl.dl-horizontal"
    internal const val comicDetailsTypeSelector = "$comicDetailsTableSelector dd:eq(1)"
    internal const val comicDetailsStatusSelector = "$comicDetailsTableSelector dd:eq(3)"
    internal const val comicDetailsYearReleasedSelector = "$comicDetailsTableSelector dd:eq(5)"
    internal const val comicDetailsCategorySelector = "$comicDetailsTableSelector dd:eq(7)"
    internal const val comicDetailsSummarySelector = "div.manga:has(p) p"
    internal const val comicDetailsCoverImageSelector = "div.boxed > img.img-responsive"
    internal const val comicDetailsTitleSelector = "h2.listmanga-header"
    internal const val comicDetailsChaptersSelector = "ul.chapters a[href]"
    // css selectors for popular comics
    internal const val popularComicsSelector = "li.list-group-item h5.media-heading a[href]"

    internal const val maxPageNumber = 10
    internal const val maxInitialCapacity = 6
  }
  @Serializable
  private data class SearchResults(val suggestions: List<SearchSuggestions>)

  @Serializable
  private data class SearchSuggestions(val value: String, val data: String)
}
