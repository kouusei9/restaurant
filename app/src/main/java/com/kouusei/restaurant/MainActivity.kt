package com.kouusei.restaurant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.kouusei.restaurant.presentation.RestaurantViewModel
import com.kouusei.restaurant.presentation.RestaurantViewState
import com.kouusei.restaurant.presentation.entities.ShopSummary
import com.kouusei.restaurant.ui.theme.RestaurantTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        enableEdgeToEdge()
        setContent {
            RestaurantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding), fusedLocationClient)
                }
            }
        }
    }
}

@Composable
fun LocationHandler(
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onFailed: (String) -> Unit
) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf<String?>(null) }

    // permission（recomposition when change）
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // request permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            getLastLocation(
                fusedLocationClient,
                onSuccess = onSuccess, onFailed = onFailed
            )
        } else {
            locationText = "Permission Denied"
        }
    }

    // get permission when launch first time
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            getLastLocation(
                fusedLocationClient,
                onSuccess = onSuccess, onFailed = onFailed
            )
        }
    }

    //TODO move text to string xml.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermission) {
            Text(text = locationText ?: "Loading location...")
        } else {
            Button(onClick = {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text("Request Location Permission")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = locationText ?: "Permission not granted yet")
        }
    }
}

// request location
fun getLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onFailed: (String) -> Unit,
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    onFailed("Location unavailable")
                }
            }
            .addOnFailureListener {
                onFailed("Failed to get location: ${it.message}")
            }
    } catch (e: SecurityException) {
        onFailed("SecurityException: ${e.message}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RestaurantTheme {
        RestaurantList(
            listOf(
                ShopSummary(
                    id = "1",
                    name = "遊楽旬彩 直",
                    url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
                    budget = "5001～7000円",
                    access = "近鉄大阪上本町駅6出口より徒歩約9分"
                ),
                ShopSummary(
                    id = "2",
                    name = "遊楽旬彩 直",
                    url = "https://imgfp.hotp.jp/IMGH/75/56/P027077556/P027077556_100.jpg",
                    budget = "5001～7000円",
                    access = "近鉄大阪上本町駅6出口より徒歩約9分"
                )
            )
        )
    }
}

@Composable
fun HomeScreen(modifier: Modifier, fusedLocationClient: FusedLocationProviderClient) {
    var restaurantViewModel: RestaurantViewModel = viewModel()

    val state by restaurantViewModel.restaurantViewState.collectAsState()
    when (state) {
        is RestaurantViewState.Error -> {
            ErrorScreen((state as RestaurantViewState.Error).message)
        }

        RestaurantViewState.Loading -> LoadingScreen()
        is RestaurantViewState.Success -> {

//            MapView((state as RestaurantViewState.Success).lat, (state as RestaurantViewState.Success).lng)
            RestaurantList(
                shops = (state as RestaurantViewState.Success).shopList,
                modifier = modifier
            )
        }

        RestaurantViewState.RequestPermission -> {
            LocationHandler(
                fusedLocationClient,
                onSuccess = restaurantViewModel::permissionSuccess,
                onFailed = restaurantViewModel::errMessage
            )
        }
    }

}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Loading ...")
    }
}

@Composable
fun ErrorScreen(errorStr: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = errorStr)
    }
}

@Composable
fun MapView(lat: Double, lng: Double) {
    val pos = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pos, 1f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = pos),
            title = "current",
            snippet = "Marker in Singapore"
        )
    }
}

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
                ambientColor = Color(0xffFD7357),
                spotColor = Color(0xffFD7357)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color(0xFFFFFFFB))
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
                color = Color(0xFF000000), fontSize = 20.sp,
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
                    Text(text = shop.budget, color = Color(0xFF222222), fontSize = 16.sp)
                    Text(text = shop.access, color = Color(0xFF222222), fontSize = 10.sp)
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


