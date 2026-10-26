package com.dd3boh.outertune.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.utils.LibrarySearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LibrarySearchField(query: String, onQueryChange: (String) -> Unit, description: String, modifier: Modifier = Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(modifier = modifier, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.heightIn(min = 48.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Search, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            BasicTextField(
                value = query, onValueChange = { onQueryChange(it.take(200)) }, singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier.weight(1f).semantics { contentDescription = description },
                decorationBox = { input ->
                    Box { if (query.isEmpty()) Text("Cerca", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); input() }
                },
            )
            if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.Close, "Svuota ricerca", modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Filtering is local, preserves the selected ordering, and never changes playback. */
@Composable
fun <T> rememberLibrarySearch(items: List<T>?, query: String, title: (T) -> String, artists: (T) -> Iterable<String> = { emptyList() }): List<T>? {
    val result by produceState(items, items, query) {
        value = withContext(Dispatchers.Default) {
            if (query.isBlank()) items else items?.filter { LibrarySearch.matches(query, title(it), artists(it)) }
        }
    }
    return result
}
