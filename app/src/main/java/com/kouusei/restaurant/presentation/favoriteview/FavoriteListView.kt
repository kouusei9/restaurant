package com.kouusei.restaurant.presentation.favoriteview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kouusei.restaurant.presentation.EmptyScreen
import com.kouusei.restaurant.presentation.ErrorScreen
import com.kouusei.restaurant.presentation.LoadingScreen
import com.kouusei.restaurant.presentation.entities.ShopSummary
import com.kouusei.restaurant.presentation.listview.RestaurantItemBar
import com.kouusei.restaurant.presentation.utils.ZigzagDivider


@Composable
fun FavoriteListScreen(
    modifier: Modifier,
    viewState: FavoriteState,
    onNavDetail: (String) -> Unit,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
) {
    when (viewState) {
        is FavoriteState.Error -> {
            ErrorScreen(viewState.message)
        }

        FavoriteState.Loading -> LoadingScreen()
        is FavoriteState.Success -> {
            FavoriteListView(
                modifier = modifier,
                shops = viewState.shops,
                onNavDetail = onNavDetail,
                onFavoriteToggled = onFavoriteToggled,
                onIsFavorite = onIsFavorite
            )
        }

        FavoriteState.Empty -> {
            EmptyScreen(isShowReload = false) { }
        }
    }
}

@Composable
fun FavoriteListView(
    modifier: Modifier,
    shops: List<ShopSummary>,
    onNavDetail: (String) -> Unit,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(shops, key = { it.id }) {
            RestaurantItemBar(
                modifier = Modifier.fillMaxWidth(),
                shop = it,
                onNavDetail = onNavDetail,
                isLike = onIsFavorite(it.id),
                onLoveToggled = onFavoriteToggled
            )
        }
        item {
            ZigzagDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
