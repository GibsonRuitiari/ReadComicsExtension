# ReadComicsExtension

`ReadComicsExtension`, inspired by Tayichomi extensions, is a kotlin library that exposes apis which enables one to pull comics from https://readcomicsonline.ru/.

```kotlin
val readComicsExtension = ReadComicsDelegate(Println)
// should be called from another suspend function/coroutine scope
readComicsExtension.getHotComicUpdates().fold({comicUpdates->
// on success
  comicUpdates.forEach{updatedComic->
    println(updatedComic)
  }
},{
  // on error
  println(it.localizedMessa) // do something with the error
})
```

`ReadComicsExtension` is extensible. See [ReadComicsDelegate](https://github.com/GibsonRuitiari/ReadComicsExtension/blob/master/src/main/kotlin/api/ReadComicsDelegate.kt) as an example
for pulling comics book data from the website. You are free to provide your own implementation.


## Set up

```
allprojects {
  repositories {
       maven { url 'https://jitpack.io' }
  }
}
```  

groovy

```
implementation "com.github.GibsonRuitiari:ReadComicsExtension:1.0.2"

```

gradle.kts 

```
implementation("com.github.GibsonRuitiari:ReadComicsExtension:1.0.2")

```

## License

```
Copyright 2022 Gibson Ruitiari.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
