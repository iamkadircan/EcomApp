package com.example.core.network.dto


data class RemoteProduct(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val images: List<String>,
    val thumbnail: String
)

data class ProductResponse(
    val products: List<RemoteProduct>
)