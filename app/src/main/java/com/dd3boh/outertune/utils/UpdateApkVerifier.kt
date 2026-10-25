package com.dd3boh.outertune.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.dd3boh.outertune.BuildConfig
import java.io.File
import java.security.MessageDigest

object UpdateApkVerifier {
    @Suppress("DEPRECATION")
    fun verify(context: Context, apk: File) {
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive = requireNotNull(context.packageManager.getPackageArchiveInfo(apk.path, flags)) { "Pacchetto aggiornamento non valido" }
        require(archive.packageName == context.packageName) { "Aggiornamento destinato a un’altra applicazione" }
        require(PackageInfoCompat.getLongVersionCode(archive) > BuildConfig.VERSION_CODE) { "La versione scaricata non è più recente" }
        val current = context.packageManager.getPackageInfo(context.packageName, flags)
        val archiveSigners = if (Build.VERSION.SDK_INT >= 28) archive.signingInfo?.apkContentsSigners else archive.signatures
        val currentSigners = if (Build.VERSION.SDK_INT >= 28) current.signingInfo?.apkContentsSigners else current.signatures
        fun digests(signers: Array<android.content.pm.Signature>?) = signers.orEmpty().map { MessageDigest.getInstance("SHA-256").digest(it.toByteArray()).toList() }.toSet()
        val expected = digests(currentSigners)
        require(expected.isNotEmpty() && expected == digests(archiveSigners)) { "La firma dell’aggiornamento non corrisponde a HiFi Player" }
    }
}
