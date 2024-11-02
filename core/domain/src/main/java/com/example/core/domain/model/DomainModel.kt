package com.example.core.domain.model


data class DomainProduct(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val images: List<String>,
    val thumbnail: String,
    val isFavorite: Boolean
)

data class DomainCart(
    val productId:Int,
    val quantity:Int,
    val price:Double,
    val title:String,
    val description:String,
    val thumbnail:String
)

data class DomainOrder(
    val orderId:String,
    val orderTime:Long,
    val orderStatus:String,
    val orderItems :List<DomainOrderItem>
)

data class DomainOrderItem(
    val productId:Int,
    val quantity:Int,
    val itemsTotalPrice:Double,
    val thumbnail:String,
    val title:String,
    val description:String,
    val orderId: String

)

data class EcomUser(
    val username:String,
    val email:String
)