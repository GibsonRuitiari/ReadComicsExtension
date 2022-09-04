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

enum class ComicCategories(val categoryName: String, val categoryLink: String) {
  Marvel("Marvel", "$BASECATEGORYURL/marvel-comics"),
  Dc("Dc comics", "$BASECATEGORYURL/dc-comics"),
  BoomStudios("Boom Studios", "$BASECATEGORYURL/boom-studios"),
  DarkHorse("Dark Horse", "$BASECATEGORYURL/dark-horse"),
  Dynamite("Dynamite", "$BASECATEGORYURL/dynamite"),
  IDW("IDW", "$BASECATEGORYURL/idw"),
  OniPress("Oni Press", "$BASECATEGORYURL/oni-press"),
  OneShots("One Shots", "$BASECATEGORYURL/one-shot"),
  Vertigo("Vertigo", "$BASECATEGORYURL/vertigo"),
  AftershockComics("Aftershock comics", "$BASECATEGORYURL/aftershock-comics"),
  ImageComics("Image Comics", "$BASECATEGORYURL/image-comics"),
  Zenescope("Zenescope", "$BASECATEGORYURL/zenescope"),
  AvatarPress("Avatar press", "$BASECATEGORYURL/avatar-press"),
  ActionLab("Action lab", "$BASECATEGORYURL/action-lab"),
  BlackMask("Black mask", "$BASECATEGORYURL/black-mask"),
  AmericanMythology("American Mythology", "$BASECATEGORYURL/american-mythology"),
  Valiant("Valiant", "$BASECATEGORYURL/valiant"),
  AntarticPress("Antartic Press", "$BASECATEGORYURL/antartic-press"),
  MadCave("Mad Cave", "$BASECATEGORYURL/mad-cave"),
  EuropeComics("Europe Comics", "$BASECATEGORYURL/europe-comics"),
  AhoyComics("Ahoy comics", "$BASECATEGORYURL/ahoy"),
  MagneticPress("Magnetic Press", "$BASECATEGORYURL/magneticpress"),
  StrangerComics("Stranger Comics", "$BASECATEGORYURL/strangercomics"),
  Upshot("Upshot", "$BASECATEGORYURL/upshot")
}
private const val BASECATEGORYURL = "https://readcomicsonline.ru/comic-list/category"
