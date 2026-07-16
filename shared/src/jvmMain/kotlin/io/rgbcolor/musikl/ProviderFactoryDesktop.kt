package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.vlcj.VlcjMusicPlayerProvider

actual fun defaultMusicPlayerProvider(): MusicPlayerProvider? = VlcjMusicPlayerProvider()