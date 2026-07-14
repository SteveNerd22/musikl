-dontobfuscate

# --- OkHttp: piattaforme/TLS opzionali assenti su JVM desktop (Android, Conscrypt, BouncyCastle, JSSE) ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn android.**

# --- OkHttp: supporto opzionale GraalVM native-image, non ci serve su JVM normale ---
-dontwarn okhttp3.internal.graal.**
-dontwarn org.graalvm.**

# --- jsoup: motore regex alternativo opzionale, non incluso ---
-dontwarn com.google.re2j.**

# --- NewPipeExtractor: libreria terze parti, escludi da shrink/optimize per evitare rotture su enum interne ---
-keep class org.schabi.newpipe.extractor.** { *; }
-keepclassmembers class org.schabi.newpipe.extractor.** { *; }

# --- Compose Desktop/AWT: metodi cercati via reflection da AWT internamente (coalesceEvents e simili) ---
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# --- Okio/OkHttp: bug noto dell'optimizer ProGuard con funzioni inline Kotlin (VerifyError bad return type) ---
-keep class okio.** { *; }
-keep class okhttp3.** { *; }
-keepclassmembers class okio.** { *; }
-keepclassmembers class okhttp3.** { *; }