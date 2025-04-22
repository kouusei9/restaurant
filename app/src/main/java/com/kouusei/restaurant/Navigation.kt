package com.kouusei.restaurant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * An enumeration of the different marker types.
 */
enum class MarkerType(
    val title: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Map(
        title = R.string.map,
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place,
    ),
    List(
        title = R.string.list,
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,

        ),
    SaveList(
        title = R.string.save_list,
        selectedIcon = Icons.Filled.FavoriteBorder,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantTopBar(
    keyword: String,
    suggestions: List<String> = emptyList(),
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    var active by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        SearchBar(
            query = keyword,
            onQueryChange = onKeywordChange,
            onSearch = {
                onSearch()
                active = false // 关闭搜索框
            },
            active = active,
            onActiveChange = { active = it },
            // TODO string
            placeholder = { Text("探す") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                Row {
                    if (keyword.isNotEmpty()) {
                        IconButton(onClick = {
                            onKeywordChange("")
                            onSearch()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "clear text")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            suggestions.filter { it.isNotEmpty() }.forEach { suggestion ->
                ListItem(
                    headlineContent = { Text(suggestion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onKeywordChange(suggestion)
                            onSearch()
                            active = false
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        searchResult?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * A bottom navigation composable to select the kind of markers to show.
 */
@Composable
fun BottomNav(selectedScreen: MarkerType, onMarkerTypeClicked: (MarkerType) -> Unit) {
    NavigationBar {
        MarkerType.entries.forEach { markerType ->
            val selected = selectedScreen == markerType

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onMarkerTypeClicked(markerType)
                },
                label = {
                    Text(text = stringResource(id = markerType.title))
                },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) markerType.selectedIcon else markerType.unselectedIcon,
                        contentDescription = stringResource(id = markerType.title)
                    )
                }
            )
        }
    }
}