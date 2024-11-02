package com.example.ecomapp.navigation.route

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppRoute (val route:String, val icon: ImageVector? = null){

    data object Login :AppRoute("login")
    data object Register :AppRoute("register")
    data object Products :AppRoute("products", Icons.Outlined.Home)
    data object Favorites :AppRoute("favorites", Icons.Outlined.FavoriteBorder)
    data object Details :AppRoute("details/{productId}"){
        fun createRoute(productId:Int) = "details/$productId"
    }
    data object Cart :AppRoute("cart", Icons.Outlined.ShoppingCart)
    data object Profile :AppRoute("profile", Icons.Outlined.Person)
    data object Orders :AppRoute("orders")

}