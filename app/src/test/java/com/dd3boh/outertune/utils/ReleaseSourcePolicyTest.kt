package com.dd3boh.outertune.utils

import org.junit.Assert.*
import org.junit.Test

class ReleaseSourcePolicyTest {
    private val repository = "bybatttt-collab/hifi-player"
    private val download = "https://github.com/$repository/releases/download/v0.2.0/HiFiPlayer-0.2.0-core-release-2.apk"

    @Test fun ownReleaseAccepted() { assertTrue(ReleaseSourcePolicy.trustedDownload(download, repository)) }

    @Test fun otherSourcesRejected() {
        listOf(
            download.replace("https:", "http:"),
            download.replace("github.com/", "github.com.evil.example/"),
            download.replace("github.com/", "github.com@evil.example/"),
            download.replace("hifi-player", "another-app"),
            download.replace("/download/", "/download/../../"),
            download.replace("/download/", "/download/%2e%2e/"),
            "$download?redirect=other", "$download#fragment", "not a URL",
        ).forEach { assertFalse(it, ReleaseSourcePolicy.trustedDownload(it, repository)) }
    }
}
