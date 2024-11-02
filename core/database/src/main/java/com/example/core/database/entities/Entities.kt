package com.example.core.database.entities


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products_table")
data class ProductEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val images: String,
    val thumbnail: String,
    val isFavorite: Boolean = false
)

@Entity(
    tableName = "carts_table",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]

)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = false)
    val productId: Int,
    val quantity: Int
)

@Entity(tableName = "orders_table")
data class OrderEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val orderTime: Long = System.currentTimeMillis(),
    val orderStatus: String = "processing"
)

@Entity(
    tableName = "order_items_table",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"], childColumns = ["productId"], onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"], childColumns = ["orderId"], onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"]), Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val quantity: Int,
    val productId: Int,
    val orderId: String,
    val totalPrice: Double
)
