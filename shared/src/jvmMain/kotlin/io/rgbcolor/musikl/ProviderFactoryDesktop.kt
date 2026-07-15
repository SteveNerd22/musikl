package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.javafx.JavaFxMusicPlayerProvider

actual fun defaultMusicPlayerProvider(): MusicPlayerProvider? = JavaFxMusicPlayerProvider()