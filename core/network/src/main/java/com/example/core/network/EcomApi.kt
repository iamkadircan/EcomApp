package com.example.core.network


import com.example.core.network.dto.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EcomApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 194
    ): ProductResponse


    companion object {
        const val BASE_URL = "https://dummyjson.com/"
    }
}