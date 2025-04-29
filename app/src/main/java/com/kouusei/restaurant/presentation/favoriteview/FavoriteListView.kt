package com.kouusei.restaurant.presentation.favoriteview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.R
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
    onOrderToggled: () -> Unit = {}
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
                onIsFavorite = onIsFavorite,
                onOrderToggled = onOrderToggled
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
    shops: List<FavoriteShopSummary>,
    onNavDetail: (String) -> Unit,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
    onOrderToggled: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    var asc by remember { mutableStateOf(true) }
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total_count, shops.size),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (asc) stringResource(R.string.order_add_date_asc) else stringResource(
                        R.string.order_add_date_desc
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(4.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(4.dp)
                        .clickable {
                            onOrderToggled()
                            asc = !asc
                        }
                )
            }
        }
        items(shops, key = { it.shopSummary.id }) {
            RestaurantItemBar(
                modifier = Modifier.fillMaxWidth(),
                shop = it.shopSummary,
                onNavDetail = onNavDetail,
                isLike = onIsFavorite(it.shopSummary.id),
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

@Preview
@Composable
fun FavoriteListViewPreview() {
    FavoriteListView(
        modifier = Modifier,
        shops = listOf(
            FavoriteShopSummary(
                shopSummary = ShopSummary(
                    id = "1",
                    name = "遊楽旬彩 直",
                    url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
                    budget = "5001～7000円",
                    access = "近鉄大阪上本町駅6出口より徒歩約9分",
                    location = LatLng(1.0, 1.0)
                ), timestamp = System.currentTimeMillis()
            )
        ),
        onNavDetail = { },
        onIsFavorite = { a -> false },
        onFavoriteToggled = { },
    )
}