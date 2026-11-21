package net.biodiscus.androidcontroltv.feature.remote

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.biodiscus.androidcontroltv.R
import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvAppGroups
import net.biodiscus.androidcontroltv.core.model.TvAppsState
import net.biodiscus.androidcontroltv.core.model.groupTvApps

private val AppIconSize = 56.dp

private val fullWidth: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

internal fun LazyGridScope.tvAppsSection(
    tvApps: TvAppsState,
    favoritePackageNames: Set<String>,
    onAppClick: (TvApp) -> Unit,
    onAppFavoriteChange: (TvApp, Boolean) -> Unit,
) {
    when (tvApps) {
        TvAppsState.Loading -> {
            appsHeader()
            item(span = fullWidth) { LoadingIndicator() }
        }
        TvAppsState.Unavailable -> {
            appsHeader()
            item(span = fullWidth) { Text(stringResource(R.string.remote_apps_unavailable)) }
        }
        is TvAppsState.Loaded -> {
            val groups = groupTvApps(tvApps.apps, favoritePackageNames)
            loadedApps(groups, onAppClick, onAppFavoriteChange)
        }
    }
}

private fun LazyGridScope.loadedApps(
    groups: TvAppGroups,
    onAppClick: (TvApp) -> Unit,
    onAppFavoriteChange: (TvApp, Boolean) -> Unit,
) {
    if (groups.favorites.isNotEmpty()) {
        item(span = fullWidth) { SectionTitle(R.string.remote_favorites_title) }
        appTiles(groups.favorites, isFavorite = true, onAppClick, onAppFavoriteChange)
    }

    appsHeader()
    if (groups.isEmpty) {
        item(span = fullWidth) { Text(stringResource(R.string.remote_apps_empty)) }
    } else if (groups.favorites.isEmpty()) {
        item(span = fullWidth) { Text(stringResource(R.string.remote_apps_favorites_hint)) }
    }
    appTiles(groups.others, isFavorite = false, onAppClick, onAppFavoriteChange)
}

private fun LazyGridScope.appsHeader() {
    item(span = fullWidth) { SectionTitle(R.string.remote_apps_title) }
}

private fun LazyGridScope.appTiles(
    apps: List<TvApp>,
    isFavorite: Boolean,
    onAppClick: (TvApp) -> Unit,
    onAppFavoriteChange: (TvApp, Boolean) -> Unit,
) {
    items(apps, key = TvApp::packageName) { app ->
        TvAppTile(
            app = app,
            isFavorite = isFavorite,
            onClick = { onAppClick(app) },
            onFavoriteChange = { shouldBeFavorite -> onAppFavoriteChange(app, shouldBeFavorite) },
        )
    }
}

@Composable
private fun SectionTitle(@StringRes titleId: Int) {
    Text(
        text = stringResource(titleId),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 24.dp),
    )
}

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TvAppTile(app: TvApp, isFavorite: Boolean, onClick: () -> Unit, onFavoriteChange: (Boolean) -> Unit) {
    var isMenuVisible by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    role = Role.Button,
                    onClick = onClick,
                    onLongClickLabel = stringResource(R.string.remote_app_show_options),
                    onLongClick = { isMenuVisible = true },
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TvAppIcon(app)
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        TvAppMenu(
            isVisible = isMenuVisible,
            isFavorite = isFavorite,
            onFavoriteChange = onFavoriteChange,
            onDismiss = { isMenuVisible = false },
        )
    }
}

@Composable
private fun TvAppMenu(
    isVisible: Boolean,
    isFavorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val textId = if (isFavorite) R.string.remote_app_remove_favorite else R.string.remote_app_add_favorite
    val icon = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder

    DropdownMenu(expanded = isVisible, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(textId)) },
            leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
            onClick = {
                onDismiss()
                onFavoriteChange(!isFavorite)
            },
        )
    }
}

@Composable
private fun TvAppIcon(app: TvApp) {
    val icon = remember(app.iconPngBase64) { decodeTvAppIcon(app.iconPngBase64) }
    if (icon == null) {
        Icon(imageVector = Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(AppIconSize))
    } else {
        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(AppIconSize))
    }
}
