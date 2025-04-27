package com.kouusei.restaurant

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.collections.List

/**
 * Detail view Top Bar
 * title: detail's title.
 * nav back: pop back action.
 */
@Composable
fun DetailTopBar(
    title: String,
    onNavBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        Button(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterStart),
            onClick = onNavBack,
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "back"
            )
        }

        // TODO title too long. auto scale title?
        Text(
            text = title,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.6f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantTopBar(
    keyword: String,
    suggestions: List<String>,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    var active by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
    ) {
        SearchBar(
            query = keyword,
            onQueryChange = onKeywordChange,
            onSearch = {
                onSearch()
                active = false
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text(text = stringResource(R.string.search)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "search icon")
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
                .padding(horizontal = 8.dp),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(suggestions, key = { it }) {
                    ListItem(
                        headlineContent = { Text(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onKeywordChange(it)
                                onSearch()
                                active = false
                            }
                            .padding(horizontal = 16.dp)
                    )
                }
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
fun BottomNav(
    nav: NavHostController,
) {
    var selectedScreen by rememberSaveable {
        mutableStateOf<String>(Map.route)
    }
    val navBackStackEntry by nav.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        selectedScreen = navBackStackEntry?.destination?.route ?: selectedScreen
    }

    val TAG = "BottomNav"
    LaunchedEffect(selectedScreen) {
        Log.d(TAG, "BottomNav: $selectedScreen")
    }

    val topLevelRoutes = listOf(
        TopLevelRoute(
            stringResource(R.string.map),
            Map,
            selectedIcon = Icons.Filled.Place,
            unSelectedIcon = Icons.Outlined.Place
        ),
        TopLevelRoute(
            stringResource(R.string.list),
            List,
            selectedIcon = Icons.Filled.List,
            unSelectedIcon = Icons.Outlined.List
        ),
        TopLevelRoute(
            stringResource(R.string.save_list),
            Favorites,
            selectedIcon = Icons.Filled.Favorite,
            unSelectedIcon = Icons.Outlined.FavoriteBorder
        )
    )
    NavigationBar {
        topLevelRoutes.forEach { topLevelRoute ->
            val selected = selectedScreen == topLevelRoute.route.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    Log.d(TAG, "BottomNav: Click ${topLevelRoute.route.route}")
                    nav.navigate(topLevelRoute.route.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(nav.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                    selectedScreen = topLevelRoute.route.route
                },
                label = {
                    Text(text = topLevelRoute.name)
                },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (selected) topLevelRoute.selectedIcon else topLevelRoute.unSelectedIcon,
                        contentDescription = topLevelRoute.route.route
                    )
                }
            )
        }
    }
}