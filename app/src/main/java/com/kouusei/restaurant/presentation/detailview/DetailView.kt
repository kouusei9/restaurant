package com.kouusei.restaurant.presentation.detailview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.ErrorScreen
import com.kouusei.restaurant.presentation.LoadingScreen
import com.kouusei.restaurant.presentation.entities.ShopDetail
import com.kouusei.restaurant.presentation.listview.debugPlaceholder
import com.kouusei.restaurant.presentation.utils.splitBusinessHours

const val TAG = "DetailView"

@Composable
fun DetailView(
    modifier: Modifier,
    id: String,
    detailViewModel: DetailViewModel,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
) {
    val state by detailViewModel.detailViewState.collectAsState()

    LaunchedEffect(true) {
        detailViewModel.load(id)
    }

    when (state) {
        is DetailViewState.Error -> {
            ErrorScreen((state as DetailViewState.Error).message)
        }

        DetailViewState.Loading -> {
            LoadingScreen()
        }

        is DetailViewState.Success -> {
            ShopDetailView(
                modifier = modifier,
                (state as DetailViewState.Success).shopDetail,
                onFavoriteToggled = onFavoriteToggled,
                onIsFavorite = onIsFavorite
            )
        }
    }
}

@Composable
fun ShopDetailView(
    modifier: Modifier,
    shopDetail: ShopDetail,
    onIsFavorite: (id: String) -> Boolean,
    onFavoriteToggled: (id: String) -> Unit,
) {
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                AsyncImage(
                    model = shopDetail.url,
                    contentDescription = "shop image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.FillWidth,
                    placeholder = debugPlaceholder(R.drawable.test_shop_img),
                    colorFilter = ColorFilter.tint(Color(0x66000000), BlendMode.Multiply)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 5.dp, start = 5.dp)
                ) {
                    Text(text = shopDetail.catch, color = Color.White)
                    Text(text = shopDetail.name, color = Color.White, fontSize = 20.sp)
                    Text(text = shopDetail.budget, color = Color.White)
                }
            }

            CustomColumn {
                CustomTitle(
                    text = "¥ ${shopDetail.budget}",
                    modifier = Modifier.padding(start = 8.dp)
                )
                Row {
                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_card),
                        isCheck = shopDetail.card,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            CustomColumn {
                CustomTitle(text = stringResource(R.string.title_open))
                Column {
                    splitBusinessHours(shopDetail.openTime).forEach { pair ->
                        CustomText(text = pair.first, lineHeight = 12.sp)
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = pair.second,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }

                CustomDivider()
                CustomTitle(text = stringResource(R.string.title_close))
                CustomText(text = shopDetail.closeTime)
            }
            CustomColumn {
                CustomTitle(text = stringResource(R.string.title_menu))
                CustomDivider()

                Row {
                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_course),
                        isCheck = shopDetail.course,
                        modifier = Modifier.weight(1f)
                    )

                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_free_food),
                        isCheck = shopDetail.freeFood,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row {
                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_free_drink),
                        isCheck = shopDetail.freeDrink,
                        modifier = Modifier.weight(1f)
                    )
                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_launch),
                        isCheck = shopDetail.lunch,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row {
                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_english),
                        isCheck = shopDetail.english,
                        modifier = Modifier.weight(1f)
                    )
                    CustomRowWithIcon(
                        text = stringResource(R.string.detail_pet),
                        isCheck = shopDetail.pet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            val cameraPositionState = rememberCameraPositionState {}
            cameraPositionState.position = CameraPosition.fromLatLngZoom(shopDetail.location, 16f)
            CustomColumn(
                modifier = Modifier.clickable {
                    showDialog = true
                }
            ) {
                CustomTitle(text = stringResource(R.string.title_access))
                CustomDivider()
                CustomText(text = shopDetail.access)

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = {
                        showDialog = true
                    }
                ) {
                    Marker(
                        state = MarkerState(position = shopDetail.location),
                        onClick = { marker ->
                            false
                        }
                    )
                }
            }

            CustomColumn {
                CustomTitle(text = stringResource(R.string.title_equipment))
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_private_room),
                    text2 = shopDetail.privateRoom
                )
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_no_smoking),
                    text2 = shopDetail.nonSmoking
                )
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_horigotatsu),
                    text2 = shopDetail.horigotatsu
                )
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_parking),
                    text2 = shopDetail.parking
                )
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_barrier_free),
                    text2 = shopDetail.barrierFree
                )
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_wifi),
                    text2 = shopDetail.wifi
                )
            }

            CustomColumn {
                CustomTitle(text = stringResource(R.string.title_related))
                CustomDivider()
                CustomRowTwoText(
                    text1 = stringResource(R.string.detail_child),
                    text2 = shopDetail.child
                )
            }
        }

        FloatingActionButton(
            onClick = {
                onFavoriteToggled(shopDetail.id)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .clip(CircleShape)
        ) {
            if (onIsFavorite(shopDetail.id)) {
                Icon(
                    Icons.Default.Favorite, contentDescription = "favorite",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.FavoriteBorder, contentDescription = "not favorite",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showDialog) {
            CustomBottomSheet(
                latLng = shopDetail.location,
                name = shopDetail.name,
                onDismiss = {
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    modifier: Modifier = Modifier,
    latLng: LatLng?,
    name: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    latLng?.let {
                        openGoogleMapsNavigation(context, latLng, name)
                        onDismiss()
                    }
                }) {
                Text(text = "Google Mapで開く")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("キャンセル")
            }
        }
    }
}

private fun openGoogleMapsNavigation(context: Context, latLng: LatLng, name: String) {
    val gmmIntentUri = Uri.parse("geo:0,0?q=${latLng.latitude},${latLng.longitude}($name)")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps") // 指定Google地图App

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        Toast.makeText(context, "Google Map no exist", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CustomDivider() {
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.inverseOnSurface),
    )
}

@Composable
fun CustomRowWithIcon(
    text: String,
    isCheck: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        CustomText(text = text, modifier = Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (isCheck) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "have",
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.cancel_48px),
                    contentDescription = "not have",
                    modifier = Modifier
                        .size(20.dp),
                )
            }
        }
    }
}

@Composable
fun CustomRowTwoText(
    text1: String,
    text2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        CustomText(text = text1, modifier = Modifier.weight(1f))
        CustomText(text = text2, modifier = Modifier.weight(3f))
    }
}

@Composable
fun CustomTitle(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
        style = style
    )
}

@Composable
fun CustomText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(start = 8.dp),
        style = style,
        lineHeight = lineHeight
    )
}

@Composable
fun CustomColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface)
            .shadow(elevation = 1.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview
@Composable
fun ShopDetailViewPreview() {
    ShopDetailView(
        modifier = Modifier,
        ShopDetail(
            id = "1",
            name = "遊楽旬彩 直",
            logoUrl = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
            openTime = "月～金、祝前日: 16:00～翌0:00 （料理L.O. 23:00 ドリンクL.O. 23:00）土、日、祝日: 15:00～翌0:00 （料理L.O. 23:00 ドリンクL.O. 23:00）",
            url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
            budget = "3001～4000円",
            access = "JR国分寺駅 徒歩4分",
            location = LatLng(1.1, 2.2),
            catch = "生簀　旬魚旬菜　日本酒　割烹",
            closeTime = "月ごとに異なります。",
            genre = "居酒屋",
            course = true,
            freeDrink = true,
            freeFood = false,
            nonSmoking = "禁煙席なし",
            card = true,
            show = false,
            lunch = false,
            english = false,
            pet = false,
            wifi = "未確認",
            child = "お子様連れOK",
            midNight = "営業していない",
            barrierFree = "なし ：ご不便な点が御座いましたら、お手伝いいたしますのでお気軽にスタッフまでお声掛けください。",
            parking = "なし ：近隣のコインパーキングをご利用ください。お車の方はお酒はご遠慮いただいております。",
            privateRoom = "あり ：4～6名、７～11名、20～50名個室シーンによって使い分けできます！座敷席で最大50名様まで座敷貸切OK！",
            horigotatsu = "あり ：掘り炬燵式　ゆったりお過ごし頂ける空間をご用意しております♪"
        ),
        onIsFavorite = { true },
        onFavoriteToggled = {},
    )
}