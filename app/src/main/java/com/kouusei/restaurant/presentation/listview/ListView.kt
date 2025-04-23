package com.kouusei.restaurant.presentation.listview

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.R
import com.kouusei.restaurant.presentation.entities.ShopSummary
import com.kouusei.restaurant.ui.theme.RestaurantTheme

@Composable
fun RestaurantList(shops: List<ShopSummary>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
            .background(Color(0xFFFFFFFC))
    ) {
        items(shops, key = { it.id }) {
            RestaurantItemBar(it)
        }
    }
}

@Composable
fun RestaurantItemBar(shop: ShopSummary) {
    // TODO move color to theme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .height(120.dp)
            .shadow(
                elevation = 10.dp,
                // 0xffFD7357
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .clip(RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
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
            listOf(
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
                    name = "遊楽旬dddddddddddddddddd彩 直222",
                    url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
                    budget = "5001～7000円",
                    access = "近鉄大阪上本町駅6出dddddddddd口より徒歩約9分",
                    location = LatLng(1.0, 1.0)
                )
            )
        )
    }
}