package com.kouusei.restaurant.presentation.listview

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.RestaurantViewState
import com.kouusei.restaurant.presentation.entities.ShopSummary
import com.kouusei.restaurant.ui.theme.RestaurantTheme
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun RestaurantList(
    restaurantViewState: RestaurantViewState.Success,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean,
    isReachEnd: Boolean,
    onLoadMore: () -> Unit,
    onNavDetail: (id: String) -> Unit
) {
    val TAG = "ListView"
    Log.d(TAG, "RestaurantList: Enter, isLoadingMore: $isLoadingMore, isReachEnd: $isReachEnd")
    val listState = rememberLazyListState()
    val shops = restaurantViewState.shopList

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                // only take the last visible item index
                // and total items count for the collection
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                val totalItems = listState.layoutInfo.totalItemsCount
                Pair(lastVisibleItemIndex, totalItems)
            }
            // only tack changed one
            .distinctUntilChanged()
            // add wait time
            .debounce(300)
            //
            .conflate()
            .collect { (lastVisibleItemIndex, totalItems) ->
                Log.d(TAG, "RestaurantList: $isLoadingMore, $isReachEnd")
                if (lastVisibleItemIndex >= totalItems - 1 && !isLoadingMore && !isReachEnd) {
                    // load more trending news user reached at the bottom of the list
                    Log.d(
                        TAG,
                        "RestaurantList: total items: $totalItems, lastvisible item index:$lastVisibleItemIndex"
                    )
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (shops.isNotEmpty()) {
            item() {
                Text(
                    text = stringResource(R.string.total_count, restaurantViewState.totalSize),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        items(shops, key = { it.id }) {
            RestaurantItemBar(
                modifier = Modifier.fillMaxWidth(),
                shop = it,
                onNavDetail = onNavDetail
            )
        }

        item {
            if (isReachEnd) {
                Text(text = "最後")
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun RestaurantItemBar(
    modifier: Modifier = Modifier,
    shop: ShopSummary,
    onNavDetail: (id: String) -> Unit
) {
    // TODO move color to theme
    Row(
        modifier = modifier
//            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .height(120.dp)
            .shadow(
                elevation = 12.dp,
                // 0xffFD7357
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .clickable {
                onNavDetail(shop.id)
            }
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = shop.url, contentDescription = "shop image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                placeholder = debugPlaceholder(R.drawable.test_shop_img),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = shop.name,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = shop.budget,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                    Text(
                        text = shop.access,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun debugPlaceholder(@DrawableRes debugPreview: Int) =
    if (LocalInspectionMode.current) {
        painterResource(id = debugPreview)
    } else {
        null
    }


@Preview(showBackground = true)
@Composable
fun RestaurantListPreview() {
    RestaurantTheme {
        RestaurantList(
            restaurantViewState = RestaurantViewState.Success(
                shopList = listOf(
                    ShopSummary(
                        id = "1",
                        name = "遊楽旬彩 直",
                        url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
                        budget = "5001～7000円",
                        access = "近鉄大阪上本町駅6出口より徒歩約9分",
                        location = LatLng(1.0, 1.0)
                    ),
                    ShopSummary(
                        id = "2",
                        name = "遊楽旬彩 直222",
                        url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
                        budget = "5001～7000円",
                        access = "近鉄大阪上本町駅6出dddddddddd口より徒歩約9分",
                        location = LatLng(1.0, 1.0)
                    )
                ),
                boundingBox = LatLngBounds(LatLng(1.0, 1.0), LatLng(1.0, 1.0)),
                totalSize = 10,
            ),
            onNavDetail = {},
            onLoadMore = {},
            isLoadingMore = false,
            isReachEnd = false
        )
    }
}