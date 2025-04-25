package com.kouusei.restaurant.presentation.mapview

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.RestaurantViewState
import com.kouusei.restaurant.presentation.entities.ShopSummary
import com.kouusei.restaurant.presentation.listview.RestaurantItemBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val TAG = "MapView"

@Composable
fun MapView(
    viewState: RestaurantViewState.Success,
    onNavDetail: (id: String) -> Unit
) {
    val center = viewState.boundingBox.center
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 30f)
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = viewState.boundingBox) {
        zoomAll(scope, cameraPositionState, viewState.boundingBox)
    }

    val selectedShop = remember { mutableStateOf(viewState.shopList.firstOrNull()) }

    LaunchedEffect(selectedShop.value) {
        selectedShop.value?.let { shop ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(shop.location, 16f),
                durationMs = 500
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // show current location
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            viewState.shopList.forEach { shop ->
                Marker(
                    state = MarkerState(position = shop.location),
                    tag = shop.id,
                    icon = bitmapDescriptorFromVector(
                        if (selectedShop.value?.id == shop.id)
                            R.drawable.ic_food_location_selected
                        else
                            R.drawable.ic_food_location
                    ),
                    onClick = { marker ->
                        selectedShop.value = viewState.shopList.first { it.id == marker.tag }
                        false
                    }
                )
            }
        }

        FloatingPositionButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            cameraPositionState = cameraPositionState
        )

        FloatList(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter),
            shops = viewState.shopList,
            onNavDetail = onNavDetail,
            selectedShop = selectedShop.value,
            onSelectedShopChange = {
                selectedShop.value = it
            }
        )
    }
}

@Composable
fun FloatingPositionButton(
    modifier: Modifier,
    cameraPositionState: CameraPositionState
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(bottom = 128.dp)
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    val permissionState = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    if (permissionState == PackageManager.PERMISSION_GRANTED) {
                        try {
                            val locationProvider =
                                LocationServices.getFusedLocationProviderClient(context)
                            val location = locationProvider.lastLocation.await()
                            location?.let {
                                val latLng = LatLng(it.latitude, it.longitude)
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 16f),
                                    durationMs = 1000
                                )
                            }
                        } catch (e: SecurityException) {
                            Log.e(
                                "MapScreen",
                                "SecurityException when accessing location: ${e.message}"
                            )
                        }
                    } else {
                        Toast.makeText(context, "No Permission", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(CircleShape)
        ) {
            Icon(Icons.Default.Place, contentDescription = "Go to my location")
        }
    }
}

@Composable
fun bitmapDescriptorFromVector(@DrawableRes vectorResId: Int): BitmapDescriptor {
    val context = LocalContext.current
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!

    val scaleFactor = 1.5f
    val width = (vectorDrawable.intrinsicWidth * scaleFactor).toInt()
    val height = (vectorDrawable.intrinsicHeight * scaleFactor).toInt()
    vectorDrawable.setBounds(0, 0, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun FloatList(
    modifier: Modifier,
    shops: List<ShopSummary>,
    selectedShop: ShopSummary?,
    onSelectedShopChange: (ShopSummary) -> Unit,
    onNavDetail: (id: String) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val padding = screenWidth * 0.05f
    val itemWidth = screenWidth * 0.9f

    val density = LocalDensity.current
    val screenCenter by remember {
        derivedStateOf {
            with(density) { screenWidth.toPx() } / 2
        }
    }

    val currentIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val center = item.offset + item.size / 2
                kotlin.math.abs(center - screenCenter)
            }?.index ?: 0
        }
    }

    LaunchedEffect(selectedShop) {
        if (selectedShop != null) {
            val index = shops.indexOf(selectedShop)
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    // 使用snap行为实现吸附效果
    val snappingLayout = remember { SnapLayoutInfoProvider(listState) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)


    var isBarVisible by remember { mutableStateOf(true) }
    AnimatedVisibility(
        modifier = modifier,
        visible = isBarVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 30) isBarVisible = false
                        }
                    )
                }
        ) {
            LazyRow(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((8).dp),
                contentPadding = PaddingValues(horizontal = padding),
                userScrollEnabled = true
            ) {
                itemsIndexed(shops) { index, shop ->
                    RestaurantItemBar(
                        modifier = Modifier
                            .width(itemWidth),
                        shop = shop,
                        onNavDetail = onNavDetail
                    )
                }
            }

            val scope = rememberCoroutineScope()
            LaunchedEffect(currentIndex) {
                if (currentIndex >= 0) {
                    Log.d(TAG, "FloatList: $currentIndex")
                    onSelectedShopChange(shops[currentIndex])
                    // in case of block code below.
                    scope.launch {
                        listState.animateScrollToItem(currentIndex)
                    }
                }
            }
        }
    }
}

fun zoomAll(
    scope: CoroutineScope,
    cameraPositionState: CameraPositionState,
    boundingBox: LatLngBounds
) {
    scope.launch {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngBounds(boundingBox, 64),
            durationMs = 500
        )
    }
}

@Preview
@Composable
fun MapViewPreview(
) {
    MapView(
        viewState = RestaurantViewState.Success(
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
        )
    ) { }
}