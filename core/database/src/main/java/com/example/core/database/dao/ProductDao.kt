package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {


    /**
     * inserts products to room db
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProducts(products:List<ProductEntity>)

    /**
     * observes all products from room db
     */
    @Query("SELECT * FROM products_table")
    fun observeAllProducts() :Flow<List<ProductEntity>>


    /**
     * returns products that contains query
     *
     */
    @Query("""
        SELECT * FROM products_table 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
    """)
    fun observeProductsWithQuery(query:String) :Flow<List<ProductEntity>>

    /**
     * deletes all products from room db
     */
    @Query("DELETE FROM products_table")
    suspend fun deleteAllProducts()

    /**
     * gets favorite products from room db
     */
    @Query("SELECT * FROM products_table WHERE isFavorite = 1")
    fun observeFavoriteProducts() :Flow<List<ProductEntity>>

    /**
     * observes product by id from room db
     */
    @Query("SELECT * FROM products_table WHERE id = :id")
    fun observeProductByProductId(id:Int) :Flow<ProductEntity>

    /**
     * toggles favorite by product id from room db
     */
    @Query("UPDATE products_table SET isFavorite = NOT isFavorite WHERE id = :productId")
    suspend fun toggleFavoriteByProductId(productId:Int)

    //firebase syncing
    /**
     * it is used for syncinc favorites with favorites list from firebase
     */
    @Query("""
        UPDATE products_table SET isFavorite = CASE WHEN id IN (:favoritesList) THEN 1 ELSE 0 END
    """)
    suspend fun synFavoritesWithFavoritesList(favoritesList:List<Int>)


}