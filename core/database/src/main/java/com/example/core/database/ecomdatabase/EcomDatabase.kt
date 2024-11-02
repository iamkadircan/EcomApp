package com.example.core.database.ecomdatabase



import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.core.database.dao.CartDao
import com.example.core.database.dao.OrderDao
import com.example.core.database.dao.ProductDao
import com.example.core.database.entities.CartItemEntity
import com.example.core.database.entities.OrderEntity
import com.example.core.database.entities.OrderItemEntity
import com.example.core.database.entities.ProductEntity


@Database(
    entities = [ProductEntity::class, CartItemEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EcomDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao


}