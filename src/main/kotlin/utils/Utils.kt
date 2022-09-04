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
package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

suspend inline fun doOnBackground(crossinline work: () -> Unit) =
  withContext(Dispatchers.IO) { work() }
suspend inline fun Response.parse(crossinline action: (document: Document) -> Unit) =
  doOnBackground {
    Jsoup
      .parse(bodyString())
      .run { action(this) }
  }

suspend inline fun String.parse(crossinline action: (document: Document) -> Unit) =
  doOnBackground { Jsoup.parse(this).run { action(this) } }
fun getClient(): HttpHandler = JavaHttpClient()
fun String.get(): Request = Request(Method.GET, this)
