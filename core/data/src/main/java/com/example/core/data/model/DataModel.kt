package com.example.core.data.model



data class FrbUser(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
)


data class FirebaseFavorite(
    val id: Int = 0
)

data class FirebaseCart(
    val productId: Int = 0,
    val quantity: Int = 0
)

data class FirebaseOrder(
    val id: String = "", val orderTime: Long = 0L,
    val orderStatus: String = "processing",
    val orderItems: List<FirebaseOrderItem> = emptyList()
)

data class FirebaseOrderItem(
    val id: String = "",
    val quantity: Int = 0,
    val productId: Int = 0,
    val orderId: String = "",
    val totalPrice: Double = 0.0,
)