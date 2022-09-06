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

import models.ComicCategories
import models.ComicDetails
import models.ComicPages
import models.ComicUpdates
import models.Mangas
import models.WeeklyPacks

/**
 * Main entry point for read comics extension.
 * To be extended by client if the client desires a completely different implementation
 */
interface ReadComics {
  /**
   * Returns a list of search results encapsulated in [Result]
   * @param searchTerm The search term to be used as search query
   */
  suspend fun searchForComic(searchTerm: String): Result<List<Mangas>>

  /**
   * Fetches the latest comics and returns a list of comic updates wrapped in [Result]
   * @param pageNumber
   */
  suspend fun getLatestComics(pageNumber: Int): Result<List<ComicUpdates>>

  /**
   * Fetches popular comics & returns a list of
   * comic books wrapped in [Result]
   */
  suspend fun getPopularComics(): Result<List<Mangas>>
  /**
   * Fetches the latest comic updates comics returns a list of
   * comic books wrapped in [Result]. The comic updates contain the recently updated added issue
   */
  suspend fun getHotComicUpdates(): Result<List<ComicUpdates>>
  /**
   * Fetches comics when given a certain category comics when given a page number + category,
   * and returns a list of comic books wrapped in [Result]
   * @param pageNumber
   * @param category  the comic category eg [ComicCategories.AftershockComics]
   */
  suspend fun getComicsByCategory(pageNumber: Int, category: ComicCategories): Result<List<Mangas>>
  /**
   * Fetches weekly comics packs. A comic pack is a list/collection of comic books issues
   */
  suspend fun getWeeklyComicPacks(): Result<List<WeeklyPacks>>
  /**
   * Fetches comic details when given a comic absolute url
   * @param comicAbsoluteUrl
   */
  suspend fun getComicDetails(comicAbsoluteUrl: String): Result<ComicDetails>
  /**
   * Gets comic-pages when given a chapter url.
   * @param chapterUrl url of chapter
   */
  suspend fun getChapterPages(chapterUrl: String): Result<List<ComicPages>>
  suspend fun getOngoingComics(pageNumber: Int): Result<List<Mangas>>
  suspend fun getCompletedComics(pageNumber: Int): Result<List<Mangas>>
}
