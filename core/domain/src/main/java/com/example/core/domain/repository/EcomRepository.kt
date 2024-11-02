package com.example.core.domain.repository


import com.example.core.domain.model.DomainCart
import com.example.core.domain.model.DomainOrder
import com.example.core.domain.model.DomainOrderItem
import com.example.core.domain.model.DomainProduct
import com.example.core.domain.model.EcomUser
import kotlinx.coroutines.flow.Flow

interface EcomRepository {
    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun register(email: String, password: String, username: String): Result<Unit>

    suspend fun logout(): Result<Unit>

    val hasUser: Boolean

    fun observeProducts(searchQuery:String) : Flow<List<DomainProduct>>

    fun observeFavoriteProducts() :Flow<List<DomainProduct>>

    fun observeCarts() :Flow<List<DomainCart>>

    fun observeOrders() :Flow<List<DomainOrder>>

    suspend fun toggleFavorite(productId: Int): Result<Unit>

    suspend fun addToCart(productId: Int): Result<Unit>

    suspend fun deleteCartItemByProductId(productId: Int): Result<Unit>

    suspend fun updateCartQuantityByProductId(newQuantity: Int, productId: Int): Result<Unit>

    suspend fun createOrder(orderItems: List<DomainOrderItem>) :Result<Unit>

    suspend fun getEcomUserData() :Result<EcomUser>

    fun observeProductByProductId(productId: Int) :Flow<DomainProduct>

}