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
package models

/**
 * default implementation of Comics interface. Represents basic comic details
 * Note:comicUrl property may represent url to an existing issue so check out
 * if the url trails off with a number eg https://xxx.xxxx/xx/1 -> issue
 * https://xxxx.xxx/xx - link
 */
data class Mangas(
  override val comicName: String,
  override val comicUrl: String,
  override val comicThumbnailLink: String
) : Comics
