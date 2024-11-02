package com.example.core.database.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.core.database.entities.OrderEntity
import com.example.core.database.entities.OrderItemEntity
import com.example.core.database.model.QueryOrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    /**
     *  inserts order to room db
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(orderEntity: OrderEntity)


    /**
     * inserts order items to room db
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrderItems(orderItems: List<OrderItemEntity>)


    /**
     * inserts order and order items to room db
     * the order items that has been created will be removed from cart
     *(this cart removal should be in repository layer)
     */
    @Transaction
    suspend fun insertOrderWithOrderItems(
        orderEntity: OrderEntity,
        orderItems: List<OrderItemEntity>
    ) {
        insertOrder(orderEntity)
        insertOrderItems(orderItems = orderItems)
    }


    /**
     * observe all orders with orderItems as flow
     */
    @Query(
        """
        SELECT 
        ot.id as orderId,
        ot.orderTime as orderTime,
        ot.orderStatus as orderStatus,
        oit.productId as productId,
        oit.quantity as productQuantity,
        pt.title as productTitle,
        pt.description as productDescription,
        pt.thumbnail as productThumbnail,
        oit.totalPrice as orderItemTotalPrice 
        FROM order_items_table oit
        INNER JOIN orders_table ot 
        ON oit.orderId = ot.id
        INNER JOIN products_table pt 
        ON oit.productId = pt.id
        ORDER BY ot.orderTime DESC

    """
    )
    fun observeAllOrderItems(): Flow<List<QueryOrderItem>>

    /**
     * clears orders and orderitems when user logs out
     * since orderderitems are related to orders, orderitems and orders will be deleted together
     */
    @Query("DELETE FROM orders_table")
    suspend fun clearOrdersAndOrderItems()


    //firebase sync
    /**
     * for firebase sync
     */
    @Transaction
    suspend fun syncOrdersWithFirestore(
        orders: List<OrderEntity>,
        orderItems: List<OrderItemEntity>
    ) {
        _insertOrders(orders = orders)
        _insertOrderItems(orderItems = orderItems)
    }

    /**
     * for firebase sync
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertOrders(orders: List<OrderEntity>)

    /**
     * firebase sync
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertOrderItems(orderItems: List<OrderItemEntity>)

}