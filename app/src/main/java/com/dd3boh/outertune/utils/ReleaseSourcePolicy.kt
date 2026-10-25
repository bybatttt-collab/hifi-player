package com.dd3boh.outertune.utils

import java.net.URI

object ReleaseSourcePolicy {
    fun trustedDownload(raw: String, repository: String): Boolean = runCatching {
        val uri = URI(raw)
        uri.scheme == "https" && uri.host == "github.com" && uri.userInfo == null && uri.port in setOf(-1, 443) &&
            uri.query == null && uri.fragment == null && '\\' !in uri.path &&
            uri.path.split('/').none { it == "." || it == ".." } &&
            uri.path.startsWith("/$repository/releases/download/") && uri.path.endsWith(".apk")
    }.getOrDefault(false)
}
