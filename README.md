# Flickr Demo App
- Written in Kotlin
- No 3rd party dependencies used other than Mockito in unit tests
- App should be ready to build and run after cloning from repo
- Built apk: https://github.com/oguzbabaoglu/flickrdemo/releases/download/1.0/app-debug.apk

## Tests
Run unit tests:
```
./gradlew test
```
Run automated ui test (needs a connected device on adb):
```
./gradlew connectedCheck
```

## Architecture
- Uses simple MVP with an `ImageService`
- Simple callbacks used for async operations instead of Observables due to time restrictions
- Has a simple `DependencyGraph` for building the dependency tree and injections
- Uses Android's LruCache to cache bitmaps

## Automated UI Test
- Uses the new [IdlingThreadPoolExecutor](https://developer.android.com/reference/android/support/test/espresso/idling/concurrent/IdlingThreadPoolExecutor.html)
- Connects to the real Flickr service, test may fail if service or api key unavailable

## Limitations and issues
- Due to time restrictions UX and UI is kept simple
- Bitmaps are not resized
- No loading indicator for "load more"
- Errors are displayed as Toasts with unlocalised messages
- No state saving / restoration
- There are some bugs with the adapter update on the RecyclerView
