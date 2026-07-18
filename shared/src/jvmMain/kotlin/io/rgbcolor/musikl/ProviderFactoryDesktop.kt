package io.rgbcolor.musikl

import io.rgbcolor.musikl.player.MusicPlayerProvider
import io.rgbcolor.musikl.player.opuswebm.DesktopOpusMusicPlayerProvider

actual fun defaultMusicPlayerProvider(): MusicPlayerProvider? = DesktopOpusMusicPlayerProvider()