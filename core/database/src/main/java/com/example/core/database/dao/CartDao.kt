package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.core.database.entities.CartItemEntity
import com.example.core.database.model.QueryCartItem
import kotlinx.coroutines.flow.Flow


@Dao
interface CartDao {
    /**
     * inserts cart item to room
     * should be only called from ui
     * it is ignored if the product is alread in the cart
     * cart item quantity shold be 1
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCartItem(cartItemEntity: CartItemEntity)


    /**
     * UPDATES CART İTEM QUANTİTY
     * Should be only called from ui
     */
    @Query("UPDATE carts_table SET quantity = :newQuantity WHERE productId = :productId")
    suspend fun updateCartItemQuantity(productId: Int, newQuantity: Int)


    /**
     * deletes all cart items from room
     * should be only called from ui
     */
    @Query("DELETE FROM carts_table WHERE productId =:productId")
    suspend fun deleteCartItemByProductId(productId: Int)

    /**
     * observes cart count to be displayed in bottom navigation
     * as badge count
     */
    @Query("SELECT COUNT(*) FROM carts_table")
    fun observeCartCount(): Flow<Int>


    /**
     * deletes cart items by productIds list given as parameter
     * it is used for deleting cart items when order is created
     */
    @Query("DELETE FROM carts_table WHERE productId in (:productIds)")
    suspend fun deleteCartsByProductIdsList(productIds: List<Int>)

    /**
     * observe all cart items as flow from room db
     */
    @Query(
        """
        SELECT  
        ct.productId as productId,
        ct.quantity as quantity,
        pt.title as productTitle,
        pt.description as productDescription,
        pt.thumbnail as productThumbnail,
        pt.rating as productRating,
        pt.price as productPrice
        FROM carts_table ct 
        INNER JOIN products_table pt
        ON ct.productId = pt.id
    """
    )
    fun observeCarts(): Flow<List<QueryCartItem>>

    /**
     * deletes all carts
     * it is used when user logs out
     */
    @Query("DELETE FROM carts_table")
    suspend fun deleteAllCarts()

    //firebase syncing
    /**
     *for syncing
     *insert or update carts to  room db
     * the parameter carts is coming from firebase
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertOrUpdateCarts(carts: List<CartItemEntity>)

    /**
     * for syncing with firebase
     * deletes carts not in firebase cart collection
     * productIds list is the productids in firebase cart collection
     */
    @Query("DELETE FROM carts_table WHERE productId NOT IN (:productIds)")
    suspend fun _deleteCartsNotInFirebaseCartIds(productIds: List<Int>)


    @Transaction
    suspend fun syncCartsWithFirestore(carts: List<CartItemEntity>) {
        _insertOrUpdateCarts(carts = carts)
        val inComingProductIds = carts.map { it.productId }
        _deleteCartsNotInFirebaseCartIds(productIds = inComingProductIds)

    }


}