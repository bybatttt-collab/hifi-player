package com.dd3boh.outertune.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.utils.*
import com.dd3boh.outertune.viewmodels.RymAlbumActionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayInputStream
import java.net.URI

class RymBrowserViewModel : ViewModel() {
    var saved: Bundle? = null
    var appliedRequest = -1L
}
private val RymAdBlockKey = booleanPreferencesKey("hifiRymBlockAds")

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RymBrowserScreen() {
    val context = LocalContext.current
    val owner = context as ComponentActivity
    val model: RymBrowserViewModel = viewModel(viewModelStoreOwner = owner)
    val actions: RymAlbumActionsViewModel = hiltViewModel(viewModelStoreOwner = owner)
    val request by RymBrowser.request.collectAsState()
    val busy by actions.busy.collectAsState()
    val notice by actions.notice.collectAsState()
    val choices by actions.choices.collectAsState()
    LaunchedEffect(notice) {
        val text = notice ?: return@LaunchedEffect
        if (text.startsWith("Album salvato") || text.startsWith("Già nella ")) {
            delay(2500)
            if (actions.notice.value == text) actions.notice.value = null
        }
    }
    var blockAds by rememberPreference(RymAdBlockKey, true)
    val enabled = rememberUpdatedState(blockAds)
    val blocker = remember { RymAdBlocker(context) }
    val extractor = remember { context.assets.open("hifi/rym-context.js").bufferedReader().use { it.readText() } }
    val main = remember { Handler(Looper.getMainLooper()) }
    var web by remember { mutableStateOf<WebView?>(null) }
    var album by remember { mutableStateOf<RymAlbumContext?>(null) }
    var blocked by remember { mutableIntStateOf(0) }
    var back by remember { mutableStateOf(false) }
    var forward by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(1f) }
    var error by remember { mutableStateOf(false) }
    var more by remember { mutableStateOf(false) }
    var previousBlock by remember { mutableStateOf(blockAds) }
    LaunchedEffect(blockAds) {
        if (blockAds != previousBlock) { previousBlock = blockAds; web?.reload() }
    }
    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { blocker.blocks("https://example.test/"); blocker.css } }
    fun updateNavigation(view: WebView) { back = view.canGoBack(); forward = view.canGoForward() }
    fun pageContext(view: WebView, url: String) {
        val uri = runCatching { URI(url) }.getOrNull() ?: return
        if (uri.host !in setOf("rateyourmusic.com", "www.rateyourmusic.com")) { album = null; return }
        val css = if (enabled.value) blocker.css else ""
        val decorate = "(function(){let m=document.querySelector('meta[name=viewport]');if(m)m.content='width=device-width, initial-scale=1, minimum-scale=0.25, maximum-scale=5, user-scalable=yes';let s=document.getElementById('hifi-ad-style');if(!s){s=document.createElement('style');s.id='hifi-ad-style';document.head.appendChild(s);}s.textContent=" + JSONObject.quote(css) + ";})()"
        view.evaluateJavascript(decorate, null)
        if (!uri.path.orEmpty().startsWith("/release/")) { album = null; return }
        view.evaluateJavascript(extractor) { encoded ->
            if (view.url?.substringBefore('#') != url.substringBefore('#')) return@evaluateJavascript
            album = runCatching {
                if (encoded.length > 25000) return@runCatching null
                val json = JSONTokener(encoded).nextValue() as? String ?: return@runCatching null
                if (json == "null") return@runCatching null
                val obj = JSONObject(json)
                val title = obj.getString("title").trim().take(300)
                val array = obj.getJSONArray("artists")
                val artists = (0 until minOf(array.length(), 12)).map { array.getString(it).trim().take(300) }.filter { it.isNotBlank() }
                if (title.isBlank() || artists.isEmpty()) null else RymAlbumContext(title, artists, url)
            }.getOrNull()
        }
    }
    fun resolve(save: Boolean) {
        val selected = album ?: return
        if (web?.url?.substringBefore('#') == selected.url.substringBefore('#')) actions.resolve(selected, save)
        else { album = null; web?.url?.let { url -> web?.let { pageContext(it, url) } } }
    }
    BackHandler(enabled = back) { web?.goBack() }
    if (choices.isNotEmpty()) AlertDialog(
        onDismissRequest = { actions.choices.value = emptyList() },
        title = { Text("Scegli l’edizione") },
        text = {
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                items(choices, key = { it.id }) { candidate ->
                    TextButton(onClick = { actions.choose(candidate) }, modifier = Modifier.fillMaxWidth()) {
                        Text(candidate.title + "\n" + candidate.artists.orEmpty().joinToString { it.name } +
                            (candidate.year?.let { " · " + it } ?: ""), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { actions.choices.value = emptyList() }) { Text("Annulla") } },
    )
    Column(Modifier.fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
        .padding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues())) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("RYM", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { web?.goBack() }, enabled = back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Pagina precedente") }
            IconButton(onClick = { web?.goForward() }, enabled = forward) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Pagina successiva") }
            IconButton(onClick = { web?.reload() }) { Icon(Icons.Rounded.Refresh, "Ricarica RYM") }
            Box {
                IconButton(onClick = { more = true }) { Icon(Icons.Rounded.MoreVert, "Menu browser RYM") }
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(text = { Text("Home RYM") }, onClick = { more = false; web?.loadUrl(RymBrowserPolicy.HOME) })
                    DropdownMenuItem(text = { Text("Zoom +") }, onClick = { web?.zoomIn() })
                    DropdownMenuItem(text = { Text("Zoom −") }, onClick = { web?.zoomOut() })
                    DropdownMenuItem(text = { Text(if (blockAds) "Blocco annunci attivo · " + blocked else "Blocco annunci disattivo") },
                        onClick = { blockAds = !blockAds; more = false })
                }
            }
        }
        album?.let { selected ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { resolve(false) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Apri in HiFi", maxLines = 1) }
                OutlinedButton(onClick = { resolve(true) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Salva in raccolta", maxLines = 1) }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        notice?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) }
        if (progress < 1f) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        if (error) Text("Pagina non disponibile. Puoi riprovare con Ricarica.", modifier = Modifier.padding(12.dp))
        AndroidView(modifier = Modifier.fillMaxWidth().weight(1f), factory = { viewContext ->
            WebView(viewContext).apply {
                web = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.mediaPlaybackRequiresUserGesture = true
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView, target: WebResourceRequest): WebResourceResponse? {
                        if (!target.isForMainFrame && enabled.value && blocker.blocks(target.url.toString())) {
                            main.post { if (web === view) blocked++ }
                            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(byteArrayOf()))
                        }
                        return null
                    }
                    override fun shouldOverrideUrlLoading(view: WebView, target: WebResourceRequest): Boolean {
                        if (!target.isForMainFrame || RymBrowserPolicy.internalUrl(target.url.toString())) return false
                        if (target.hasGesture() && target.url.scheme in setOf("https", "http"))
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, target.url)) }
                        return true
                    }
                    override fun onPageStarted(view: WebView, url: String?, icon: Bitmap?) {
                        error = false; album = null; blocked = 0; updateNavigation(view)
                    }
                    override fun onPageFinished(view: WebView, url: String?) {
                        updateNavigation(view)
                        url?.let { pageContext(view, it) }
                    }
                    override fun onReceivedError(view: WebView, target: WebResourceRequest, failure: WebResourceError) {
                        if (target.isForMainFrame) { error = true; progress = 1f }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView, value: Int) { progress = value / 100f; updateNavigation(view) }
                }
                val pending = request
                if (pending != null && model.appliedRequest != pending.id) {
                    model.appliedRequest = pending.id; loadUrl(pending.url)
                } else if (model.saved?.let { restoreState(it) } == null) loadUrl(RymBrowserPolicy.HOME)
            }
        }, update = { view ->
            request?.takeIf { it.id != model.appliedRequest }?.let { model.appliedRequest = it.id; view.loadUrl(it.url) }
        }, onRelease = { view ->
            model.saved = Bundle().also { view.saveState(it) }
            CookieManager.getInstance().flush()
            view.stopLoading(); view.destroy(); web = null
        })
    }
}
