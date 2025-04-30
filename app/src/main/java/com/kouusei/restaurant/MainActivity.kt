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
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.kouusei.restaurant.presentation.EmptyScreen
import com.kouusei.restaurant.presentation.ErrorScreen
import com.kouusei.restaurant.presentation.LoadingScreen
import com.kouusei.restaurant.presentation.RestaurantViewModel
import com.kouusei.restaurant.presentation.RestaurantViewState
import com.kouusei.restaurant.presentation.common.FilterViewBottom
import com.kouusei.restaurant.presentation.common.FilterViewTop
import com.kouusei.restaurant.presentation.detailview.DetailView
import com.kouusei.restaurant.presentation.detailview.DetailViewModel
import com.kouusei.restaurant.presentation.favoriteview.FavoriteListScreen
import com.kouusei.restaurant.presentation.favoriteview.FavoriteShopsModel
import com.kouusei.restaurant.presentation.listview.RestaurantList
import com.kouusei.restaurant.presentation.mapview.MapView
import com.kouusei.restaurant.presentation.splash.SplashViewModel
import com.kouusei.restaurant.presentation.utils.toLatLng
import com.kouusei.restaurant.ui.theme.RestaurantTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { splashViewModel.isLoading.value }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContent {
            RestaurantTheme(dynamicColor = false) {
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
                    bottomBar = {
                        AnimatedVisibility(
                            visible = currentRoute in topRoutes,
                            enter = slideInVertically(
                                animationSpec = tween(500),
                                initialOffsetY = { height ->
                                    0
                                }
                            ),
                            exit = slideOutVertically(
                                animationSpec = tween(500),
                                targetOffsetY = { height ->
                                    0
                                }
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
                        fusedLocationClient = fusedLocationClient,
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
    fusedLocationClient: FusedLocationProviderClient,
) {
    var restaurantViewModel: RestaurantViewModel = viewModel()
    val detailViewModel: DetailViewModel = viewModel()

    val shopNames by restaurantViewModel.shopNames.collectAsState()
    // TODO save history keyword
    val keyword by restaurantViewModel.keyword.collectAsState()

    // favorite shop
    val favoriteShopsModel: FavoriteShopsModel = viewModel()
    val shopIds = favoriteShopsModel.shopIds.collectAsState()
    val favoriteKeyword by favoriteShopsModel.keyword.collectAsState()
    val favoriteState by favoriteShopsModel.favoriteState.collectAsState()

    // request by name when keyword change
    val debounceQuery = remember { mutableStateOf("") }
    LaunchedEffect(keyword) {
        delay(500) // wait 500 after keyword change
        if (keyword != debounceQuery.value) {
            debounceQuery.value = keyword
            restaurantViewModel.loadShopNameList()
        }
    }

    val state by restaurantViewModel.restaurantViewState.collectAsState()


    val isLoading by restaurantViewModel.isLoading.collectAsState()
    val isReloading by restaurantViewModel.isReloading.collectAsState()
    val isReachEnd by restaurantViewModel.isReachEnd.collectAsState()

    val cameraPositionState by restaurantViewModel.cameraPositionState.collectAsState()
    val selectedShop by restaurantViewModel.selectedShop.collectAsState()

    val mapViewListState = rememberLazyListState()
    val filterBottomListState = rememberLazyListState()
    val filterTopListState = rememberLazyListState()
    val listViewListState = rememberLazyListState()

    val infiniteTransition = rememberInfiniteTransition()

    val scope = rememberCoroutineScope()
    val refreshListState = {
        scope.launch {
            listViewListState.animateScrollToItem(0)
            mapViewListState.animateScrollToItem(0)
        }
    }
    NavHost(navController, startDestination = Map.route) {
        composable(Map.route) {
            when (state) {
                RestaurantViewState.Loading -> {
                    LoadingScreen(infiniteTransition)
                }

                is RestaurantViewState.Empty, is RestaurantViewState.Success, is RestaurantViewState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = innerPadding.calculateBottomPadding()
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            RestaurantTopBar(
                                keyword = keyword,
                                onKeywordChange = {
                                    restaurantViewModel.onKeyWordChange(it)
                                },
                                onSearch = {
                                    restaurantViewModel.reloadShopList()
                                    refreshListState()
                                },
                                suggestions = shopNames
                            )
                            AddTopFilterView(
                                restaurantViewModel = restaurantViewModel,
                                refreshListState = {
                                    refreshListState()
                                },
                                innerPadding = innerPadding,
                                listState = filterTopListState
                            )
                            if (state is RestaurantViewState.Success) {
                                MapView(
                                    cameraPositionState = cameraPositionState,
                                    listState = mapViewListState,
                                    viewState = state as RestaurantViewState.Success,
                                    selectedShop = selectedShop,
                                    onSelectedShopChange = {
                                        restaurantViewModel.onSelectedShopChange(it)
                                    },
                                    onNavDetail = {
                                        navController.navigate(route = Detail(id = it).route)
                                    },
                                    onIsFavorite = { shopId ->
                                        shopIds.value.find { it.shopId == shopId } != null
                                    },
                                    isReloading = isReloading,
                                    onFavoriteToggled = {
                                        favoriteShopsModel.toggleFavorite(it)
                                    },
                                    keyword = keyword,
                                    onKeywordChange = {
                                        restaurantViewModel.onKeyWordChange(it)
                                    },
                                    onSearch = {
                                        restaurantViewModel.reloadShopList()
                                        refreshListState()
                                    },
                                    suggestions = shopNames,
                                )
                            } else if (state is RestaurantViewState.Empty) {
                                EmptyScreen {
                                    restaurantViewModel.resetFilterAndReload()
                                }
                            } else {
                                ErrorScreen((state as RestaurantViewState.Error).message)
                            }
                        }
                        AddBottomFilterView(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            restaurantViewModel = restaurantViewModel,
                            refreshListState = {
                                refreshListState()
                            },
                            listState = filterBottomListState
                        )
                    }
                }

                RestaurantViewState.RequestPermission -> {
                    LocationHandler(
                        fusedLocationClient = fusedLocationClient,
                        infiniteTransition = infiniteTransition,
                        onSuccess = restaurantViewModel::permissionSuccess,
                        onFailed = restaurantViewModel::errMessage
                    )
                }
            }
        }
        composable(List.route) {
            when (state) {
                is RestaurantViewState.Loading -> {
                    LoadingScreen(infiniteTransition)
                }

                is RestaurantViewState.Empty, is RestaurantViewState.Success, is RestaurantViewState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = innerPadding.calculateBottomPadding()
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            RestaurantTopBar(
                                keyword = keyword,
                                onKeywordChange = {
                                    restaurantViewModel.onKeyWordChange(it)
                                },
                                onSearch = {
                                    restaurantViewModel.reloadShopList()
                                    refreshListState()
                                },
                                suggestions = shopNames
                            )
                            AddTopFilterView(
                                restaurantViewModel = restaurantViewModel,
                                refreshListState = {
                                    refreshListState()
                                },
                                innerPadding = innerPadding,
                                listState = filterTopListState
                            )
                            if (state is RestaurantViewState.Success) {
                                RestaurantList(
                                    restaurantViewState = state as RestaurantViewState.Success,
                                    onNavDetail = {
                                        navController.navigate(route = Detail(id = it).route)
                                    },
                                    onLoadMore = {
                                        restaurantViewModel.loadMore()
                                    },
                                    isLoadingMore = isLoading,
                                    isReachEnd = isReachEnd,
                                    onIsFavorite = { shopId ->
                                        shopIds.value.find { it.shopId == shopId } != null
                                    },
                                    isReloading = isReloading,
                                    listState = listViewListState,
                                    onFavoriteToggled = {
                                        favoriteShopsModel.toggleFavorite(it)
                                    },
                                    onRefresh = {
                                        restaurantViewModel.reloadShopList()
                                        refreshListState()
                                    }
                                )
                            } else if (state is RestaurantViewState.Empty) {
                                EmptyScreen {
                                    restaurantViewModel.resetFilterAndReload()
                                }
                            } else {
                                ErrorScreen((state as RestaurantViewState.Error).message)
                            }
                        }

                        AddBottomFilterView(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            restaurantViewModel = restaurantViewModel,
                            refreshListState = {
                                refreshListState()
                            },
                            listState = filterBottomListState
                        )
                    }

                }

                is RestaurantViewState.RequestPermission -> {
                    LocationHandler(
                        fusedLocationClient = fusedLocationClient,
                        infiniteTransition = infiniteTransition,
                        onSuccess = restaurantViewModel::permissionSuccess,
                        onFailed = restaurantViewModel::errMessage
                    )
                }
            }

        }
        composable(Favorites.route) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                RestaurantTopBar(
                    keyword = favoriteKeyword,
                    onKeywordChange = {
                        favoriteShopsModel.onKeyWordChange(it)
                    },
                    onSearch = {
                        favoriteShopsModel.filter()
                    },
                    suggestions = favoriteShopsModel.getSuggestionsByKeyword()

                )
                FavoriteListScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewState = favoriteState,
                    onNavDetail = {
                        navController.navigate(route = Detail(id = it).route)
                    },
                    onIsFavorite = { shopId ->
                        shopIds.value.find { it.shopId == shopId } != null
                    },
                    onFavoriteToggled = {
                        favoriteShopsModel.toggleFavorite(it)
                    },
                    onOrderToggled = {
                        favoriteShopsModel.toggleOrder()
                    }
                )
            }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                val shopId = backStackEntry.arguments?.getString("id") ?: ""
                DetailView(
                    modifier = Modifier.fillMaxSize(),
                    id = shopId,
                    detailViewModel = detailViewModel,
                    onIsFavorite = { shopId ->
                        shopIds.value.find { it.shopId == shopId } != null
                    },
                    onFavoriteToggled = {
                        favoriteShopsModel.toggleFavorite(it)
                    },
                    onNavBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun AddTopFilterView(
    restaurantViewModel: RestaurantViewModel,
    refreshListState: () -> Unit,
    innerPadding: PaddingValues,
    listState: LazyListState,
) {
    // address status
    val largeAddressList by restaurantViewModel.largeAddressList.collectAsState()
    val selectedLargeAddress by restaurantViewModel.selectedLargeAddress.collectAsState()
    val middleAddressList by restaurantViewModel.middleAddressList.collectAsState()
    val selectedMiddleAddress by restaurantViewModel.selectedMiddleAddress.collectAsState()
    val smallAddressList by restaurantViewModel.smallAddressList.collectAsState()
    val selectedSmallAddress by restaurantViewModel.selectedSmallAddress.collectAsState()
    val isAddressSelected by restaurantViewModel.isSelectedAddress.collectAsState()

    // filter status
    val distanceRange by restaurantViewModel.distanceRange.collectAsState()
    val orderMethod by restaurantViewModel.orderMethod.collectAsState()
    val genre by restaurantViewModel.genre.collectAsState()

    FilterViewTop(
        onDistanceChange = {
            restaurantViewModel.onDistanceRangeChange(it)
            refreshListState()
        },
        onOrderMethodChange = {
            restaurantViewModel.onOrderMethodChange(it)
            refreshListState()
        },
        selectedDistance = distanceRange,
        selectedOrderMethod = orderMethod,
        listState = listState,
        selectedGenre = genre,
        onSelectedGenreChange = {
            restaurantViewModel.onGenreChange(it)
            refreshListState()
        },
        largeAddress = largeAddressList,
        selectedLarge = selectedLargeAddress,
        onLargeSelect = {
            restaurantViewModel.onSelectedLargeAddressChange(it)
            restaurantViewModel.onSelectedMiddleAddressChange(null)
            restaurantViewModel.onSelectedSmallAddressChange(null)
        },
        middleAddress = middleAddressList,
        selectedMiddle = selectedMiddleAddress,
        onMiddleSelect = {
            restaurantViewModel.onSelectedMiddleAddressChange(it)
            restaurantViewModel.onSelectedSmallAddressChange(null)
        },
        smallAddress = smallAddressList,
        selectedSmall = selectedSmallAddress,
        onSmallSelect = {
            restaurantViewModel.onSelectedSmallAddressChange(it)
        },
        isAddressSelected = isAddressSelected,
        onAddressSelectedConfirm = {
            restaurantViewModel.onSelectedAddressChange(true)
            restaurantViewModel.reloadShopList()
            refreshListState()
        },
        bottomPadding = innerPadding.calculateBottomPadding(),
        topPadding = innerPadding.calculateTopPadding(),
        onAddressSelectedReset = {
            if (isAddressSelected) {
                restaurantViewModel.onSelectedAddressChange(false)
                restaurantViewModel.reloadShopList()
                refreshListState()
            }
        }
    )
}

@Composable
fun AddBottomFilterView(
    modifier: Modifier = Modifier,
    restaurantViewModel: RestaurantViewModel,
    refreshListState: () -> Unit,
    listState: LazyListState,
) {
    val searchFilters by restaurantViewModel.searchFilters.collectAsState()

    FilterViewBottom(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        onFilterChange = {
            restaurantViewModel.toggleFilter(it)
            refreshListState()
        },
        state = searchFilters,
        listState = listState
    )
}

@Composable
fun LocationHandler(
    fusedLocationClient: FusedLocationProviderClient,
    infiniteTransition: InfiniteTransition,
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
                onFailed("Permission not granted ${e.message}")
            }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    if (hasPermission) {
        LoadingScreen(infiniteTransition)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text(text = stringResource(R.string.permission_request))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = locationText ?: stringResource(R.string.permission_deny),
                style = MaterialTheme.typography.bodyMedium
            )
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
                    // TODO instruction change.
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


