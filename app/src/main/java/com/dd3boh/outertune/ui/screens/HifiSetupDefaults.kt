package com.dd3boh.outertune.ui.screens

import androidx.datastore.preferences.core.MutablePreferences
import com.dd3boh.outertune.constants.*

/** Complete setup without changing account/session data or existing opt-in choices. */
fun completeHifiSetup(prefs: MutablePreferences, updates: Boolean, versionCode: Int) {
    SetupChoices.from(prefs, lastFmAskable = true).copy(updates = updates).store(prefs)
    applyNewInstallDefaults(prefs)
    prefs[WalkthroughSeenVersionKey] = versionCode
    prefs[AccountNoticeSeenKey] = true
}
