package io.rgbcolor.musikl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform