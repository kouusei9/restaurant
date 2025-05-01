package com.kouusei.restaurant.presentation.favoriteview

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.kouusei.restaurant.presentation.listview.RestaurantItemBarLoading
import com.kouusei.restaurant.presentation.utils.ZigzagDivider
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@Composable
fun FavoriteListScreen(
    favoriteViewModel: FavoriteShopsModel,
    modifier: Modifier,
    viewState: FavoriteState,
    onNavDetail: (String) -> Unit,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
) {
    val isLoading by favoriteViewModel.isLoading.collectAsState()
    val isReachEnd by favoriteViewModel.isReachEnd.collectAsState()
    val isLoadingMore by favoriteViewModel.isLoadingMore.collectAsState()
    val shopIds by favoriteViewModel.shopIds.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val asc by favoriteViewModel.isAscending.collectAsState()

    LaunchedEffect(shopIds) {
        scope.launch {
            listState.scrollToItem(0)
        }
    }

    when (viewState) {
        is FavoriteState.Error -> {
            ErrorScreen(viewState.message)
        }

        FavoriteState.Loading -> LoadingScreen()
        is FavoriteState.Success -> {
            LazyListWithLoadMore(
                modifier = modifier,
                listState = listState,
                items = viewState.shops,
                isLoadingMore = isLoadingMore,
                isReachEnd = isReachEnd,
                isReloading = isLoading,
                onLoadMore = {
                    favoriteViewModel.onLoadMore()
                },
                onRefresh = {
                    favoriteViewModel.reload()
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                },
                key = { it.shopSummary.id },
                itemContent = { item, index ->
                    RestaurantItemBar(
                        modifier = Modifier.fillMaxWidth(),
                        shop = item.shopSummary,
                        onNavDetail = onNavDetail,
                        isLike = onIsFavorite(item.shopSummary.id),
                        onLoveToggled = onFavoriteToggled
                    )
                },
                footerContent = {
                    if (isReachEnd) {
                        ZigzagDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        RestaurantItemBarLoading()
                    }
                },
                headerContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.total_count, shopIds.size),
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
                                    favoriteViewModel.toggleOrder()
                                    scope.launch {
                                        listState.scrollToItem(0)
                                    }
                                }
                        )
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LazyListWithLoadMore(
    modifier: Modifier = Modifier,
    items: List<T>,
    listState: LazyListState = rememberLazyListState(),
    isLoadingMore: Boolean,
    isReachEnd: Boolean,
    isReloading: Boolean = false,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit = {},
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable (item: T, index: Int) -> Unit,
    footerContent: @Composable () -> Unit = {},
    headerContent: @Composable () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                val totalItems = listState.layoutInfo.totalItemsCount
                Pair(lastVisibleItemIndex, totalItems)
            }
            .distinctUntilChanged()
            .conflate()
            .collect { (lastVisibleItemIndex, totalItems) ->
                Log.d(
                    TAG,
                    "LazyListWithLoadMore: isLoadingMore = $isLoadingMore, isReachEnd = $isReachEnd"
                )
                if (lastVisibleItemIndex >= totalItems - 1 && !isLoadingMore && !isReachEnd) {
                    onLoadMore()
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = isReloading,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
        ) {
            item { headerContent() }
            if (key != null) {
                itemsIndexed(items, key = { _, item -> key(item) }) { index, item ->
                    itemContent(item, index)
                }
            } else {
                itemsIndexed(items) { index, item ->
                    itemContent(item, index)
                }
            }
            item {
                footerContent()
            }
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