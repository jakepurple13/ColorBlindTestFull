package com.programmersbox.colorblindtestfull

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform