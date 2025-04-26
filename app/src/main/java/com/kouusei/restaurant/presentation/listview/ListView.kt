package com.kouusei.restaurant.presentation.listview

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    isReloading: Boolean,
    onLoadMore: () -> Unit,
    onNavDetail: (id: String) -> Unit,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
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
            .padding(start = 10.dp, end = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (shops.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.total_count, restaurantViewState.totalSize),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        items(shops, key = { it.id }) {
            if (isReloading) {
                RestaurantItemBarLoading()
            } else {
                RestaurantItemBar(
                    modifier = Modifier.fillMaxWidth(),
                    shop = it,
                    onNavDetail = onNavDetail,
                    isLike = onIsFavorite(it.id),
                    onLoveToggled = onFavoriteToggled
                )
            }
        }

        item {
            if (isReachEnd) {
                Text(text = stringResource(R.string.last_item))
            } else {
                RestaurantItemBarLoading()
            }
        }
    }
}

@Composable
fun RestaurantItemBar(
    modifier: Modifier = Modifier,
    shop: ShopSummary,
    isLike: Boolean = false,
    onNavDetail: (id: String) -> Unit,
    onLoveToggled: (id: String) -> Unit = {}
) {
    Box(modifier = modifier
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
        }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 40.dp)
                ) {
                    Text(
                        text = shop.name,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
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

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .wrapContentSize()
                .clickable {
                    onLoveToggled(shop.id)
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLike) {
                Icon(
                    Icons.Default.Favorite, contentDescription = "love icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.FavoriteBorder, contentDescription = "love icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@Composable
fun RestaurantItemBarLoading(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()

    Box(
        modifier = modifier
            .padding(top = 4.dp, bottom = 4.dp)
            .height(120.dp)
            .shadow(
                elevation = 12.dp,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(2f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(end = 8.dp, top = 8.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(end = 8.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(22.dp)
                                .background(brush, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(22.dp)
                                .background(brush, RoundedCornerShape(4.dp))
                        )
                    }
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
                        name = "遊楽旬彩 直222 dfdsafdsa Resutaurant is Long",
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
            isLoadingMore = true,
            isReachEnd = false,
            onIsFavorite = { false },
            onFavoriteToggled = {},
            isReloading = false
        )
    }
}