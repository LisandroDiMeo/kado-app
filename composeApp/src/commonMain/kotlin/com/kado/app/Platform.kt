package com.kado.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform