package com.example.core.database.model


data class QueryCartItem(
    val productId: Int,
    val quantity: Int,
    val productPrice:Double,
    val productTitle: String,
    val productDescription: String,
    val productThumbnail: String,
    val productRating: Double
)


data class QueryOrderItem(
    val orderId: String,
    val orderTime: Long,
    val orderStatus: String,
    val productId: Int,
    val productQuantity: Int,
    val productTitle: String,
    val productDescription: String,
    val productThumbnail: String,
    val orderItemTotalPrice :Double
)
