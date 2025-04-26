package com.kouusei.restaurant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.kouusei.restaurant.presentation.RestaurantViewModel
import com.kouusei.restaurant.presentation.RestaurantViewState
import com.kouusei.restaurant.presentation.common.FilterView
import com.kouusei.restaurant.presentation.detailview.DetailView
import com.kouusei.restaurant.presentation.detailview.DetailViewModel
import com.kouusei.restaurant.presentation.favoriteview.FavoriteListScreen
import com.kouusei.restaurant.presentation.favoriteview.FavoriteShopsModel
import com.kouusei.restaurant.presentation.listview.RestaurantList
import com.kouusei.restaurant.presentation.mapview.MapView
import com.kouusei.restaurant.presentation.utils.toLatLng
import com.kouusei.restaurant.ui.theme.RestaurantTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        enableEdgeToEdge()
        setContent {
            RestaurantTheme {
                var restaurantViewModel: RestaurantViewModel = viewModel()
                val detailViewModel: DetailViewModel = viewModel()

                val shopNames by restaurantViewModel.shopNames.collectAsState()
                // TODO save history keyword
                val keyword by restaurantViewModel.keyword.collectAsState()

                val title by detailViewModel.title.collectAsState()

                // request by name when keyword change
                val debounceQuery = remember { mutableStateOf("") }
                LaunchedEffect(keyword) {
                    delay(500) // wait 500 after keyword change
                    if (keyword != debounceQuery.value) {
                        debounceQuery.value = keyword
                        restaurantViewModel.loadShopNameList()
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                var currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(navBackStackEntry) {
                    currentRoute = navBackStackEntry?.destination?.route
                    Log.d(
                        TAG,
                        "onCreate: $currentRoute, Map route:${Map.route} List route:${List.route}"
                    )
                }

                val topRoutes = listOf(Map.route, List.route, Favorites.route)
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    topBar = {
                        AnimatedVisibility(visible = currentRoute in topRoutes) {
                            RestaurantTopBar(
                                keyword = keyword,
                                onKeywordChange = {
                                    restaurantViewModel.onKeyWordChange(it)
                                },
                                onSearch = {
                                    restaurantViewModel.reloadShopList()
                                },
                                suggestions = shopNames
                            )
                        }
                        AnimatedVisibility(
                            visible = currentRoute !in topRoutes,
                            enter =
                            fadeIn(
                                animationSpec = tween(
                                    300, easing = LinearEasing
                                )
                            ) + slideIn(
                                animationSpec = tween(300, easing = EaseIn),
                                initialOffset = { fullSize ->
                                    IntOffset(fullSize.width, 0)
                                }
                            ),
                            exit =
                            fadeOut(
                                animationSpec = tween(
                                    300, easing = LinearEasing
                                )
                            ) + slideOut(
                                animationSpec = tween(300, easing = EaseOut),
                                targetOffset = { fullSize ->
                                    IntOffset(fullSize.width, 0)
                                }
                            )
                        ) {
                            DetailTopBar(
                                title = title,
                                onNavBack = {
                                    navController.popBackStack()
                                })
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = currentRoute in topRoutes,
                            enter =
                            fadeIn(
                                animationSpec = tween(
                                    300, easing = LinearEasing
                                )
                            ),
                            exit =
                            fadeOut(
                                animationSpec = tween(
                                    300, easing = LinearEasing
                                )
                            )
                        ) {
                            BottomNav(
                                nav = navController,
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        innerPadding = innerPadding,
                        restaurantViewModel = restaurantViewModel,
                        fusedLocationClient = fusedLocationClient,
                        keyword = keyword,
                        detailViewModel = detailViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    restaurantViewModel: RestaurantViewModel,
    fusedLocationClient: FusedLocationProviderClient,
    keyword: String,
    detailViewModel: DetailViewModel
) {
    val favoriteShopsModel: FavoriteShopsModel = viewModel()
    val favoriteState by favoriteShopsModel.favoriteState.collectAsState()

    val favoriteShopIds by favoriteShopsModel.shopIds.collectAsState()

    NavHost(navController, startDestination = Map.route) {
        composable(Map.route) {
            HomeScreen(
                nav = navController,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                fusedLocationClient = fusedLocationClient,
                markerType = Map,
                restaurantViewModel = restaurantViewModel,
                keyword = keyword,
                favoriteShopsModel = favoriteShopsModel,
                onIsFavorite = {
                    favoriteShopIds.contains(it)
                }
            )
        }
        composable(List.route) {
            HomeScreen(
                nav = navController,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                fusedLocationClient = fusedLocationClient,
                markerType = List,
                restaurantViewModel = restaurantViewModel,
                keyword = keyword,
                favoriteShopsModel = favoriteShopsModel,
                onIsFavorite = {
                    favoriteShopIds.contains(it)
                }
            )
        }
        composable(Favorites.route) {
            FavoriteListScreen(
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                viewState = favoriteState,
                onNavDetail = {
                    navController.navigate(route = Detail(id = it).route)
                },
                onIsFavorite = {
                    favoriteShopIds.contains(it)
                },
                onFavoriteToggled = {
                    favoriteShopsModel.toggleFavorite(it)
                }
            )
        }
        composable(
            "detail/{id}",
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = LinearEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("id") ?: ""
            DetailView(
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                id = shopId,
                detailViewModel = detailViewModel,
                onIsFavorite = {
                    favoriteShopIds.contains(it)
                },
                onFavoriteToggled = {
                    favoriteShopsModel.toggleFavorite(it)
                }
            )
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

    val permissionDenyInfo = stringResource(R.string.permission_deny)
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
            locationText = permissionDenyInfo
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

    // register Location Callback
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    locationText = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                    onSuccess(location.toLatLng())
                } else {
                    onFailed("Location not found")
                }
            }
        }
    }

    // 启动/停止监听
    DisposableEffect(hasPermission) {
        if (hasPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                onFailed("Permission not granted")
            }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    //TODO request shop info
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermission) {
            Text(text = locationText ?: "Loading...")
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

data class TopLevelRoute<T : Any>(
    val name: String,
    val route: T,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector
)

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
    nav: NavHostController,
    modifier: Modifier,
    fusedLocationClient: FusedLocationProviderClient,
    markerType: Route,
    restaurantViewModel: RestaurantViewModel,
    favoriteShopsModel: FavoriteShopsModel,
    keyword: String,
    onIsFavorite: (id: String) -> Boolean,
) {
    val state by restaurantViewModel.restaurantViewState.collectAsState()

    val distanceRange by restaurantViewModel.distanceRange.collectAsState()
    val orderMethod by restaurantViewModel.orderMethod.collectAsState()
    val genre by restaurantViewModel.genre.collectAsState()
    val searchFilters by restaurantViewModel.searchFilters.collectAsState()

    val isLoading by restaurantViewModel.isLoading.collectAsState()
    val isReloading by restaurantViewModel.isReloading.collectAsState()
    val isReachEnd by restaurantViewModel.isReachEnd.collectAsState()

    val cameraPositionState by restaurantViewModel.cameraPositionState.collectAsState()
    val selectedShop by restaurantViewModel.selectedShop.collectAsState()

    val mapViewListState = rememberLazyListState()
    val filterListState = rememberLazyListState()

    when (state) {
        is RestaurantViewState.Error -> {
            ErrorScreen((state as RestaurantViewState.Error).message)
        }

        RestaurantViewState.Loading -> {
            LoadingScreen()
        }
        is RestaurantViewState.Success -> {
            Column(modifier = modifier) {
                FilterView(
                    onDistanceChange = {
                        restaurantViewModel.onDistanceRangeChange(it)
                    },
                    onOrderMethodChange = {
                        restaurantViewModel.onOrderMethodChange(it)
                    },
                    onFilterChange = {
                        restaurantViewModel.toggleFilter(it)
                    },
                    selectedDistance = distanceRange,
                    selectedOrderMethod = orderMethod,
                    state = searchFilters,
                    keyword = keyword,
                    listState = filterListState,
                    selectedGenre = genre,
                    onSelectedGenreChange = {
                        restaurantViewModel.onGenreChange(it)
                    },
                )
                if (markerType == List) {
                    RestaurantList(
                        restaurantViewState = state as RestaurantViewState.Success,
                        onNavDetail = {
                            nav.navigate(route = Detail(id = it).route)
                        },
                        onLoadMore = {
                            restaurantViewModel.loadMore()
                        },
                        isLoadingMore = isLoading,
                        isReachEnd = isReachEnd,
                        onIsFavorite = onIsFavorite,
                        isReloading = isReloading,
                        onFavoriteToggled = {
                            favoriteShopsModel.toggleFavorite(it)
                        }
                    )
                } else if (markerType == Map) {
                    MapView(
                        cameraPositionState = cameraPositionState,
                        listState = mapViewListState,
                        viewState = state as RestaurantViewState.Success,
                        selectedShop = selectedShop,
                        onSelectedShopChange = {
                            restaurantViewModel.onSelectedShopChange(it)
                        },
                        onNavDetail = {
                            nav.navigate(route = Detail(id = it).route)
                        },
                        onIsFavorite = onIsFavorite,
                        isReloading = isReloading,
                        onFavoriteToggled = {
                            favoriteShopsModel.toggleFavorite(it)
                        }
                    )
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


