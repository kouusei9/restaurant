package com.kouusei.restaurant.presentation.detailview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.DetailViewModel
import com.kouusei.restaurant.presentation.DetailViewState
import com.kouusei.restaurant.presentation.entities.ShopDetail
import com.kouusei.restaurant.presentation.listview.debugPlaceholder


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
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading")
            }
        }

        is DetailViewState.Success -> {
            ShopDetailView(
                modifier = modifier,
                (state as DetailViewState.Success).shopDetail,
                onNavBack = onNavBack
            )
        }
    }
}

@Composable
fun ShopDetailView(
    modifier: Modifier,
    shopDetail: ShopDetail,
    onNavBack: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Button(
            modifier = Modifier.wrapContentSize(),
            onClick = onNavBack,
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "back"
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = shopDetail.url,
                contentDescription = "shop image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.FillWidth,
                placeholder = debugPlaceholder(R.drawable.test_shop_img),
                alpha = 0.7f
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 5.dp, start = 5.dp)
            ) {
                Text(text = shopDetail.catch, color = Color.White)
                Text(text = shopDetail.name, color = Color.White, fontSize = 20.sp)
                Text(text = shopDetail.access, color = Color.White)
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
        ) {

        }
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
            catch = "生簀　旬魚旬菜　日本酒　割烹"
        ),
        onNavBack = {},
    )
}