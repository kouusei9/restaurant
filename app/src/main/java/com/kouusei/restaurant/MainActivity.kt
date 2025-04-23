package com.kouusei.restaurant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.presentation.RestaurantViewModel
import com.kouusei.restaurant.presentation.RestaurantViewState
import com.kouusei.restaurant.presentation.common.FilterView
import com.kouusei.restaurant.presentation.listview.RestaurantList
import com.kouusei.restaurant.presentation.mapview.MapView
import com.kouusei.restaurant.ui.theme.RestaurantTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        enableEdgeToEdge()
        setContent {
            RestaurantTheme {
                var selectedMarkerType by rememberSaveable {
                    mutableStateOf(MarkerType.List)
                }

                var restaurantViewModel: RestaurantViewModel = viewModel()
                val shopNames by restaurantViewModel.shopNames.collectAsState()

                // TODO save history keyword
                val keyword by restaurantViewModel.keyword.collectAsState()
                val debounceQuery = remember { mutableStateOf("") }

                LaunchedEffect(keyword) {
                    delay(500) // wait 500 after keyword change
                    if (keyword != debounceQuery.value) {
                        debounceQuery.value = keyword
                        restaurantViewModel.loadShopNameList()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    topBar = {
                        RestaurantTopBar(
                            keyword,
                            onKeywordChange = {
                                restaurantViewModel.onKeyWordChange(it)
                            },
                            onSearch = {
                                restaurantViewModel.reloadShopList()
                            },
                            suggestions = shopNames
                        )
                    },
                    bottomBar = {
                        BottomNav(
                            selectedScreen = selectedMarkerType
                        ) {
                            selectedMarkerType = it
                        }
                    }
                ) { innerPadding ->
                    when (selectedMarkerType) {
                        MarkerType.List -> {
                            HomeScreen(
                                Modifier.padding(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = innerPadding.calculateBottomPadding()
                                ),
                                fusedLocationClient,
                                MarkerType.List,
                                restaurantViewModel,
                            )
                        }

                        MarkerType.Map -> {
                            HomeScreen(
                                Modifier.padding(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = innerPadding.calculateBottomPadding()
                                ),
                                fusedLocationClient,
                                MarkerType.Map,
                                restaurantViewModel,
                            )
                        }

                        MarkerType.SaveList -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun LocationHandler(
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (LatLng) -> Unit,
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
    onSuccess: (LatLng) -> Unit,
    onFailed: (String) -> Unit,
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onSuccess(LatLng(location.latitude, location.longitude))
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

@Composable
fun HomeScreen(
    modifier: Modifier,
    fusedLocationClient: FusedLocationProviderClient,
    markerType: MarkerType,
    restaurantViewModel: RestaurantViewModel,
) {
    val state by restaurantViewModel.restaurantViewState.collectAsState()

    val distanceRange by restaurantViewModel.distanceRange.collectAsState()
    when (state) {
        is RestaurantViewState.Error -> {
            ErrorScreen((state as RestaurantViewState.Error).message)
        }

        RestaurantViewState.Loading -> LoadingScreen()
        is RestaurantViewState.Success -> {
            Column(modifier = modifier) {
                FilterView(
                    onDistanceChange = {
                        restaurantViewModel.distanceRangeChange(it)
                    },
                    onFilterChange = {

                    },
                    selectedDistance = distanceRange
                )
                if (markerType == MarkerType.List) {
                    RestaurantList(
                        shops = (state as RestaurantViewState.Success).shopList,
                    )
                } else if (markerType == MarkerType.Map) {
                    MapView(state as RestaurantViewState.Success)
                }
            }
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


