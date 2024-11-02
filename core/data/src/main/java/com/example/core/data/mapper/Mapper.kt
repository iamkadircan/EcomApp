package com.example.core.data.mapper

import com.example.core.data.model.FirebaseCart
import com.example.core.database.entities.CartItemEntity
import com.example.core.database.entities.ProductEntity
import com.example.core.database.model.QueryCartItem
import com.example.core.database.model.QueryOrderItem
import com.example.core.domain.model.DomainCart
import com.example.core.domain.model.DomainOrder
import com.example.core.domain.model.DomainOrderItem
import com.example.core.domain.model.DomainProduct
import com.example.core.network.dto.RemoteProduct


fun List<RemoteProduct>.toProductEntityList(favIds: Set<Int>): List<ProductEntity> {

    return this.map { remoteProduct ->
        ProductEntity(
            id = remoteProduct.id,
            title = remoteProduct.title,
            description = remoteProduct.description,
            category = remoteProduct.category,
            price = remoteProduct.price,
            rating = remoteProduct.rating,
            images = remoteProduct.images.joinToString(","),
            thumbnail = remoteProduct.thumbnail,
            isFavorite = remoteProduct.id in favIds
        )
    }
}


fun ProductEntity.toDomainProduct(): DomainProduct {
    val images = this.images.split(",")
    return DomainProduct(
        id = this.id,
        title = this.title,
        description = this.description,
        category = this.category,
        price = this.price,
        rating = this.rating,
        thumbnail = this.thumbnail,
        images = images,
        isFavorite = this.isFavorite
    )
}

fun List<ProductEntity>.toDomainProductList(): List<DomainProduct> {
    return this.map { it.toDomainProduct() }
}

fun List<QueryCartItem>.toDomainCartList(): List<DomainCart> {
    return this.map { cartItemEntity ->
        DomainCart(
            productId = cartItemEntity.productId,
            quantity = cartItemEntity.quantity,
            price = cartItemEntity.productPrice,
            title = cartItemEntity.productTitle,
            description = cartItemEntity.productDescription,
            thumbnail = cartItemEntity.productThumbnail
        )
    }
}

fun FirebaseCart.toCartItemEntity(): CartItemEntity {
    return CartItemEntity(
        productId = this.productId,
        quantity = this.quantity
    )
}

fun List<FirebaseCart>.toCartItemEntityList(): List<CartItemEntity> {
    return this.map { it.toCartItemEntity() }
}

fun mapQueryOrderItemsToDomainOrders(queryOrderItems: List<QueryOrderItem>): List<DomainOrder> {
    val grouped = queryOrderItems.groupBy { it.orderId }
    val result = grouped.map { (orderId, items) ->
        DomainOrder(
            orderId = orderId,
            orderTime = items.first().orderTime,
            orderStatus = items.first().orderStatus,
            orderItems = items.map { orderItem ->
                DomainOrderItem(
                    productId = orderItem.productId,
                    quantity = orderItem.productQuantity,
                    itemsTotalPrice = orderItem.orderItemTotalPrice,
                    thumbnail = orderItem.productThumbnail,
                    title = orderItem.productTitle, description = orderItem.productDescription,
                    orderId = orderId

                )
            }
        )
    }

    return result
}
