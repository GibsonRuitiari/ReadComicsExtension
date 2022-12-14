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
/**
 * Makeshift logger that logs messages and exception messages to the std out/any other platform (files for instance)
 */
interface Logger {
  fun log(msg: String, throwable: Throwable)
  fun log(msg: String)
}

/**
 * Logs messages to the std out
 */
object Println : Logger {
  override fun log(msg: String, throwable: Throwable) {
    println(msg)
    println(throwable.stackTraceToString())
  }

  override fun log(msg: String) {
    println(msg)
  }
}

/**
 * No operation logger useful in tests
 */
object NoOp : Logger {
  override fun log(msg: String, throwable: Throwable) = Unit

  override fun log(msg: String) = Unit
}
