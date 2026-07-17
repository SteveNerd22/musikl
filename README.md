# Musikl 🎵

A personal, hobbyist Compose Multiplatform music app. Search, listen, and (eventually) build playlists — built for fun and to learn Kotlin Multiplatform, not to compete with anyone.

## What is this?

Musikl is a side project I'm building in my spare time to learn **Kotlin Multiplatform** and **Compose Multiplatform** properly, by actually shipping something instead of just following tutorials. It's not a product, it's not trying to replace anything you already use, and it comes with **zero roadmap guarantees**.

If you're looking for a polished, feature-complete music app — this isn't it (yet, maybe never). If you're curious about KMP, want to poke around the code, or just want to follow along as I figure things out, welcome aboard.

## Platform status

| Platform   | Status |
|------------|---|
| 🤖 Android | 🚧 In development |
| 🟦 Windows | 🚧 In development |
| 🐧 Linux   | 🚧 In development |
| 🍎 macOS   | 🚧 In development |
| 📱 iOS     | ❌ Not planned |

"In development" means: it runs, it's actively worked on, and it will break sometimes. There is no release schedule.

## Roadmap

Rough order, no promises on timing or on ever finishing the list:

- [x] Search
- [x] Track playback
- [x] Music caching
- [ ] Playback queue
- [ ] Playlist creation
- [ ] Offline music
- [ ] Playlist sharing
- [ ] Audio output device control
- [ ] Personal device sync
- [ ] Personalized feed
- [ ] Playlists shared with friends
- [ ] Jam sessions
- [ ] Listening stats (tracks & artists)
- [ ] Year-end "Wrapped"

Items lower on the list are more speculative than planned — think of them as "ideas I'd like to explore" rather than a commitment.

## Why?

Mainly to learn. Kotlin Multiplatform, Compose for Desktop/Android, dependency architecture across platforms, dealing with the occasional Gradle nightmare — all of it, hands-on. If the app ends up being genuinely useful to me (or to anyone else) along the way, that's a nice bonus, not the goal.

## Disclaimer

This project is provided **as-is**, with **no warranty of any kind**, express or implied — including but not limited to fitness for any particular purpose, reliability, or continued maintenance. Use it at your own risk.

Musikl relies on third-party extraction libraries to fetch publicly available content metadata and streams. **The author is not responsible for how this software is used.** Any use that infringes copyright, violates the terms of service of third-party platforms, or breaks applicable law is solely the responsibility of the person doing it, not of this project or its author.

This is a hobby project maintained in spare time. There is no guarantee of updates, bug fixes, or support.

## Dependencies

Musikl is built on top of these open-source projects:

- **[NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor)** — content search and stream extraction
- **[Kotlin](https://github.com/JetBrains/kotlin)** — the language this whole thing is written in
- **[Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines)** — async/concurrency
- **[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)** — shared UI toolkit (JetBrains)
- **[AndroidX Media3 (ExoPlayer)](https://github.com/androidx/media)** — audio playback on Android
- **[OpenJFX (JavaFX)](https://github.com/openjdk/jfx)** — audio playback on Desktop
- **[Coil](https://github.com/coil-kt/coil)** — image loading (thumbnails)
- **[OkHttp](https://github.com/square/okhttp)** / **[Okio](https://github.com/square/okio)** — HTTP client and I/O, used by NewPipeExtractor and Coil
- **[jsoup](https://github.com/jhy/jsoup)** — HTML parsing, used internally by NewPipeExtractor
- **[AndroidX Jetpack libraries](https://developer.android.com/jetpack/androidx)** (Activity, AppCompat, Core, Lifecycle) — Android platform support
- **[Desugar JDK Libs NIO](https://github.com/google/desugar_jdk_libs)** — core library desugaring, needed on Android for Java APIs (like `URLEncoder.encode(String, Charset)`) not available below API 33

Build-time only (not shipped in the final app, but worth crediting):

- **[ProGuard](https://github.com/Guardsquare/proguard)** — used for shrinking/optimizing Desktop release builds

## License

Musikl is licensed under the **GNU General Public License v3.0** (GPL-3.0). See [`LICENSE`](./LICENSE) for the full text.

This project depends on [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) (GPL-3.0) and other libraries licensed under Apache 2.0 and GPL-2.0-with-Classpath-Exception, all compatible with GPL-3.0.

## Contributing

Not actively seeking contributions at this stage — this is primarily a personal learning project — but feel free to open an issue if you spot something interesting, or fork it if you want to take it in your own direction.