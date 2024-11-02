package com.example.ecomapp.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ecomapp.MainViewModel
import com.example.ecomapp.navigation.route.AppRoute
import com.example.ecomapp.ui.theme.ColorOrange
import com.example.feature.cart.CartScreen
import com.example.feature.cart.CartScreenTopBar
import com.example.feature.detail.DetailScreen
import com.example.feature.detail.DetailScreenTopBar
import com.example.feature.favorites.FavoriteScreen
import com.example.feature.favorites.FavoriteScreenTopBar
import com.example.feature.login.LoginScreen
import com.example.feature.order.OrderScreen
import com.example.feature.order.OrderScreenTopBar
import com.example.feature.products.ProductScreen
import com.example.feature.products.ProductScreenTopBar
import com.example.feature.products.ProductViewModel
import com.example.feature.profile.ProfileScreen
import com.example.feature.profile.ProfileScreenTopBar
import com.example.feature.register.RegisterScreen

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val appState = rememberAppState()
    val navController = appState.navController
    val shoSnackbar: (String) -> Unit = { message -> appState.showSnackbar(message) }
    val mainViewModel: MainViewModel = hiltViewModel()
    val startDestination =
        if (mainViewModel.hasUser) AppRoute.Products.route else AppRoute.Login.route

    Scaffold(modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = appState.snackbarHostState) },
        bottomBar = { AppBotomBar(navController = navController) },
        topBar = { AppTopBar(navController = navController) }

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Login.route) {
                LoginScreen(onShowSnackbar = shoSnackbar, onNavigateToRegister = {
                    navController.navigate(AppRoute.Register.route)

                }, onNavigateToProducts = {
                    navController.navigate(AppRoute.Products.route) {
                        popUpTo(AppRoute.Login.route) {
                            inclusive = true
                        }
                    }
                }

                )
            }


            composable(AppRoute.Register.route) {
                RegisterScreen(onShowSnackbar = shoSnackbar, onNavigateToProducts = {
                    navController.navigate(AppRoute.Products.route) {
                        popUpTo(AppRoute.Login.route) {
                            inclusive = true
                        }
                    }
                }, onNavigateToLogin = {
                    navController.navigateUp()
                })
            }

            composable(AppRoute.Products.route) {
                ProductScreen(onShowSnackbar = shoSnackbar, onNavToDetails = { productId ->
                    navController.navigate(AppRoute.Details.createRoute(productId = productId))
                })
            }

            composable(AppRoute.Favorites.route) {
                FavoriteScreen(onShowSnackbar = shoSnackbar, onNavToDetailScreen = {
                    navController.navigate(AppRoute.Details.createRoute(productId = it))
                })
            }

            composable(AppRoute.Cart.route) {
                CartScreen(onShowSnackbar = shoSnackbar, onNavToDetails = { productId ->
                    navController.navigate(AppRoute.Details.createRoute(productId = productId))
                })
            }

            composable(AppRoute.Profile.route) {
                ProfileScreen(onShowSnackbar = shoSnackbar, onNavigateToLogin = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Profile.route) {
                            inclusive = true
                        }
                    }
                }, onNavigateToOrders = {
                    navController.navigate(AppRoute.Orders.route)
                })
            }

            composable(AppRoute.Orders.route, enterTransition = {
                slideInHorizontally { it }
            }, exitTransition = {
                slideOutHorizontally { it }
            }) {
                OrderScreen(onNavigateToDetails = { productId ->
                    navController.navigate(AppRoute.Details.createRoute(productId = productId))
                })
            }

            composable(
                AppRoute.Details.route,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })

            ) {
                DetailScreen(onShowSnackbar = shoSnackbar)
            }
        }
    }
}


@Composable
fun AppTopBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentroute = navBackStackEntry?.destination?.route

    when (currentroute) {
        AppRoute.Products.route -> {
            val productNavBackStackEntry =
                remember { navController.getBackStackEntry(AppRoute.Products.route) }
            val viewModel: ProductViewModel = hiltViewModel(productNavBackStackEntry)
            ProductScreenTopBar(viewModel = viewModel)
        }

        AppRoute.Favorites.route -> {
            FavoriteScreenTopBar()
        }

        AppRoute.Cart.route -> {
            CartScreenTopBar()
        }

        AppRoute.Profile.route -> {
            ProfileScreenTopBar()
        }

        AppRoute.Orders.route -> {
            OrderScreenTopBar(onNavigateUp = { navController.navigateUp() })
        }

        AppRoute.Details.route -> {
            DetailScreenTopBar(onNavigateUp = { navController.navigateUp() })
        }

        else -> {

        }
    }
}

@Composable
fun AppBotomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomItems = listOf(AppRoute.Products, AppRoute.Favorites, AppRoute.Cart, AppRoute.Profile)

    AnimatedVisibility(
        visible = currentRoute in bottomItems.map { it.route },
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar {
            bottomItems.forEach { bottomItem ->
                NavigationBarItem(selected = currentRoute == bottomItem.route,
                    onClick = {
                        if (currentRoute != bottomItem.route) {
                            navController.navigate(bottomItem.route) {
                                popUpTo(AppRoute.Products.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true

                            }
                        }
                    },
                    icon = {
                        Icon(contentDescription = "", imageVector = bottomItem.icon!!)
                    }, label = {
                        Text(
                            color = if (currentRoute == bottomItem.route) ColorOrange else Color.LightGray,
                            text = bottomItem.route
                        )

                    }, colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.LightGray, selectedIconColor = ColorOrange
                    ), alwaysShowLabel = false
                )
            }
        }
    }

}