package com.kouusei.restaurant.presentation.detailview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.DetailViewModel
import com.kouusei.restaurant.presentation.DetailViewState
import com.kouusei.restaurant.presentation.entities.ShopDetail
import com.kouusei.restaurant.presentation.listview.debugPlaceholder

val TAG = "DetailView"

@Composable
fun DetailView(
    modifier: Modifier,
    id: String,
    detailViewModel: DetailViewModel,
    onNavBack: () -> Unit
) {
    val state by detailViewModel.detailViewState.collectAsState()

    LaunchedEffect(true) {
        detailViewModel.load(id)
    }

    when (state) {
        is DetailViewState.Error -> {

        }

        DetailViewState.Loading -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading")
            }
        }

        is DetailViewState.Success -> {
            ShopDetailView(
                modifier = modifier,
                (state as DetailViewState.Success).shopDetail,
            )
        }
    }
}

@Composable
fun ShopDetailView(
    modifier: Modifier,
    shopDetail: ShopDetail,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
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
            CustomText(text = shopDetail.budget)
        }

        CustomColumn {
            CustomText(text = "営業時間：")

            CustomText(
                text = shopDetail.openTime,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
            )

            CustomText(text = "閉店時間：")

            CustomText(
                text = shopDetail.closeTime,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        CustomColumn {
            CustomText(text = "アクセス:")
            CustomText(text = shopDetail.access, modifier = Modifier.padding(start = 8.dp))
        }

        CustomColumn {
            CustomText(text = "コース:")
            CustomText(text = "個室:")
            CustomText(text = shopDetail.privateRoom)
        }
    }
}

@Composable
fun CustomText(
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = fontSize,
        modifier = modifier
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
            .padding(8.dp)
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
            smoking = true,
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
            privateRoom = "あり ：4～6名、７～11名、20～50名個室シーンによって使い分けできます！座敷席で最大50名様まで座敷貸切OK！"
        ),
    )
}