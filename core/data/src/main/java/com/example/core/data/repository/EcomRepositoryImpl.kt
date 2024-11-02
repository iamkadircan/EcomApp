package com.example.core.data.repository



import androidx.room.Transaction
import com.example.core.common.di.ApplicationScope
import com.example.core.common.di.IoDispatcher
import com.example.core.data.mapper.mapQueryOrderItemsToDomainOrders
import com.example.core.data.mapper.toCartItemEntityList
import com.example.core.data.mapper.toDomainCartList
import com.example.core.data.mapper.toDomainProduct
import com.example.core.data.mapper.toDomainProductList
import com.example.core.data.mapper.toProductEntityList
import com.example.core.data.model.FirebaseCart
import com.example.core.data.model.FirebaseFavorite
import com.example.core.data.model.FirebaseOrder
import com.example.core.data.model.FirebaseOrderItem
import com.example.core.data.model.FrbUser
import com.example.core.data.util.getUniqueStringId
import com.example.core.database.ecomdatabase.EcomDatabase
import com.example.core.database.entities.CartItemEntity
import com.example.core.database.entities.OrderEntity
import com.example.core.database.entities.OrderItemEntity
import com.example.core.datastore.EcomDataStore
import com.example.core.domain.model.DomainCart
import com.example.core.domain.model.DomainOrder
import com.example.core.domain.model.DomainOrderItem
import com.example.core.domain.model.DomainProduct
import com.example.core.domain.model.EcomUser
import com.example.core.domain.repository.EcomRepository
import com.example.core.network.EcomApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EcomRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val ecomDatastore: EcomDataStore,
    private val ecomApi: EcomApi,
    @ApplicationScope private val scope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ecomDb: EcomDatabase,
) : EcomRepository {

    private val getUserId: Flow<String?> = ecomDatastore.userId

    private var observeFavoritesFromFirestoreJob: Job? = null
    private var favoritesListenerRegistration: ListenerRegistration? = null

    private var observeCartItemsFromFirestoreJob: Job? = null
    private var cartITemsListenerRegistration: ListenerRegistration? = null

    private var observeOrdersFromFirestoreJob: Job? = null
    private var ordersListenerRegistration: ListenerRegistration? = null


    private suspend fun observeFavoritesFromFirestore() {
        val userId = getUserId.firstOrNull()
        userId?.let {
            favoritesListenerRegistration = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener

                    if (snapshots != null) {
                        val favoriteIdsList =
                            snapshots.documents.mapNotNull { it.toObject(FirebaseFavorite::class.java) }
                                .map { it.id }
                        observeFavoritesFromFirestoreJob?.cancel()

                        observeFavoritesFromFirestoreJob = scope.launch {
                            delay(3000)
                            ecomDb.productDao()
                                .synFavoritesWithFavoritesList(favoritesList = favoriteIdsList)
                        }
                    }
                }
        }
    }

    private suspend fun observeCartsFromFirestore() {
        val userId = getUserId.firstOrNull()
        userId?.let {
            cartITemsListenerRegistration = firestore.collection("users")
                .document(userId)
                .collection("cart")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshots != null) {
                        val carts =
                            snapshots.documents.mapNotNull { it.toObject(FirebaseCart::class.java) }
                                .toCartItemEntityList()

                        observeCartItemsFromFirestoreJob?.cancel()
                        observeCartItemsFromFirestoreJob = scope.launch {
                            delay(3000)
                            ecomDb.cartDao().syncCartsWithFirestore(carts = carts)
                        }
                    }
                }

        }
    }

    private suspend fun observeOrdersFromFirestore() {
        val userId = getUserId.firstOrNull()
        userId?.let {
            cartITemsListenerRegistration = firestore.collection("users")
                .document(userId)
                .collection("orders")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshots != null) {
                        val frbOrder =
                            snapshots.documents.mapNotNull { it.toObject(FirebaseOrder::class.java) }
                        val orders = frbOrder.map {
                            OrderEntity(
                                id = it.id,
                                orderTime = it.orderTime,
                                orderStatus = it.orderStatus
                            )
                        }
                        val orderItems =
                            frbOrder.flatMap { it.orderItems }.map { firebaseOrderItem ->
                                OrderItemEntity(
                                    id = firebaseOrderItem.id,
                                    quantity = firebaseOrderItem.quantity,
                                    productId = firebaseOrderItem.productId,
                                    orderId = firebaseOrderItem.orderId,
                                    totalPrice = firebaseOrderItem.totalPrice

                                )
                            }

                        observeOrdersFromFirestoreJob?.cancel()
                        observeOrdersFromFirestoreJob = scope.launch {
                            ecomDb.orderDao().syncOrdersWithFirestore(
                                orders = orders,
                                orderItems = orderItems
                            )
                        }
                    }
                }
        }
    }

    init {
        scope.launch(ioDispatcher) {
            observeOrdersFromFirestore()
            observeFavoritesFromFirestore()
            observeCartsFromFirestore()
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid

                if (uid != null) {
                    ecomDatastore.saveUserId(uid = uid)

                    val favorites = firestore.collection("users").document(uid)
                        .collection("favorites")
                        .get().await().mapNotNull { it.toObject(FirebaseFavorite::class.java) }
                        .map { it.id }

                    val remoteProducts = ecomApi.getProducts().products
                    val productEntities =
                        remoteProducts.toProductEntityList(favIds = favorites.toSet())
                    ecomDb.productDao().insertProducts(products = productEntities)


                    Result.success(Unit)
                } else {
                    Result.failure<Unit>(Exception("Login failed"))
                }
            } catch (e: Exception) {
                Result.failure<Unit>(exception = e)
            }
        }

    }

    override suspend fun register(email: String, password: String, username: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val uid = result?.user?.uid
                if (uid != null) {
                    ecomDatastore.saveUserId(uid = uid)
                    val user = FrbUser(uid = uid, username = username, email = email)
                    firestore.collection("users").document(uid).set(user).await()

                    val remoteProducts = ecomApi.getProducts().products
                    val productEntities = remoteProducts.toProductEntityList(favIds = emptySet())
                    ecomDb.productDao().insertProducts(products = productEntities)
                    Result.success(Unit)
                } else {
                    Result.failure<Unit>(Exception("Register failed"))
                }
            } catch (e: Exception) {
                Result.failure<Unit>(exception = e)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            observeOrdersFromFirestoreJob?.cancel()
            ordersListenerRegistration?.remove()

            observeCartItemsFromFirestoreJob?.cancel()
            cartITemsListenerRegistration?.remove()

            observeFavoritesFromFirestoreJob?.cancel()
            favoritesListenerRegistration?.remove()

            firebaseAuth.signOut()
            ecomDatastore.clearUserId()
            ecomDb.productDao().deleteAllProducts()
            ecomDb.cartDao().deleteAllCarts()
            ecomDb.orderDao().clearOrdersAndOrderItems()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure<Unit>(exception = e)
        }
    }

    override val hasUser: Boolean
        get() = firebaseAuth.currentUser != null


    override fun observeProducts(searchQuery: String): Flow<List<DomainProduct>> {
        val productsFlow = if (searchQuery.length < 3) {
            ecomDb.productDao().observeAllProducts()
        } else {
            ecomDb.productDao().observeProductsWithQuery(query = searchQuery)
        }
        return productsFlow.map { it.toDomainProductList() }
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    override fun observeFavoriteProducts(): Flow<List<DomainProduct>> {
        return ecomDb.productDao().observeFavoriteProducts().distinctUntilChanged()
            .map { productEntities -> productEntities.toDomainProductList() }
            .flowOn(ioDispatcher)
    }

    override fun observeCarts(): Flow<List<DomainCart>> {
        return ecomDb.cartDao().observeCarts().distinctUntilChanged()
            .map { queryCartItems -> queryCartItems.toDomainCartList() }
            .flowOn(ioDispatcher)
    }

    override fun observeOrders(): Flow<List<DomainOrder>> {
        return ecomDb.orderDao().observeAllOrderItems()
            .distinctUntilChanged()
            .map { orderItems -> mapQueryOrderItemsToDomainOrders(queryOrderItems = orderItems) }
            .flowOn(ioDispatcher)
    }

    override suspend fun toggleFavorite(productId: Int): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                ecomDb.productDao().toggleFavoriteByProductId(productId = productId)
                val userId = getUserId.firstOrNull()
                if (userId != null) {
                    val favoriteRef = firestore.collection("users")
                        .document(userId)
                        .collection("favorites")
                        .document(productId.toString())
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(favoriteRef)
                        if (snapshot.exists()) {
                            transaction.delete(favoriteRef)
                        } else {
                            val favorite = FirebaseFavorite(id = productId)
                            transaction.set(favoriteRef, favorite)
                        }
                    }
                    Result.success(Unit)
                } else {
                    Result.failure<Unit>(Exception("User not logged in"))
                }


            } catch (e: Exception) {
                Result.failure<Unit>(e)
            }
        }

    }

    override suspend fun addToCart(productId: Int): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                ecomDb.cartDao().insertCartItem(
                    cartItemEntity = CartItemEntity(
                        productId = productId,
                        quantity = 1
                    )
                )

                //firestore
                val userId = getUserId.firstOrNull()
                if (userId != null) {

                    val cartRef = firestore.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(productId.toString())
                    firestore.runTransaction { transaction ->
                        val snapshot = transaction.get(cartRef)
                        if (!snapshot.exists()) {
                            val cartItem = FirebaseCart(productId = productId, quantity = 1)
                            transaction.set(cartRef, cartItem)
                        }
                    }.await()
                    Result.success(Unit)
                } else {
                    Result.failure<Unit>(Exception("User not logged in"))
                }

            } catch (e: Exception) {
                Result.failure<Unit>(e)
            }
        }
    }

    override suspend fun deleteCartItemByProductId(productId: Int): Result<Unit> {
        return withContext(ioDispatcher) {
            try {

                ecomDb.cartDao().deleteCartItemByProductId(productId = productId)

                //firestore
                val userID = getUserId.firstOrNull()
                if (userID != null) {
                    val cartsDosRef = firestore.collection("users")
                        .document(userID)
                        .collection("cart")
                        .document(productId.toString())
                    cartsDosRef.delete().await()
                    Result.success(Unit)
                } else {
                    Result.failure<Unit>(Exception("User not logged in"))
                }

            } catch (e: Exception) {
                Result.failure<Unit>(e)

            }
        }
    }

    override suspend fun updateCartQuantityByProductId(
        newQuantity: Int,
        productId: Int
    ): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                ecomDb.cartDao()
                    .updateCartItemQuantity(productId = productId, newQuantity = newQuantity)

                //firestore
                val userId = getUserId.firstOrNull()
                if (userId != null) {
                    val cartDoc = firestore.collection("users")
                        .document(userId)
                        .collection("cart")
                        .document(productId.toString())
                    val cartItem = FirebaseCart(productId = productId, quantity = newQuantity)
                    cartDoc.set(cartItem)
                        .await()
                    Result.success(Unit)
                } else {
                    Result.failure<Unit>(Exception("User not logged in"))
                }


            } catch (e: Exception) {
                Result.failure<Unit>(e)
            }
        }

    }

    @Transaction
    override suspend fun createOrder(orderItems: List<DomainOrderItem>): Result<Unit> {
        return withContext(ioDispatcher) {

            try {
                val orderId = getUniqueStringId()
                val orderTime = System.currentTimeMillis()
                val orderStatus = "processing"
                val orderEntitiy =
                    OrderEntity(id = orderId, orderTime = orderTime, orderStatus = orderStatus)
                //todo create mapper for code below and clean code,
                val orderItemEntities = orderItems.map { orderItem ->
                    OrderItemEntity(
                        id = getUniqueStringId(),
                        quantity = orderItem.quantity,
                        productId = orderItem.productId,
                        orderId = orderId,
                        totalPrice = orderItem.itemsTotalPrice
                    )
                }
                val itemsToBeRemovedFromCart = orderItems.map { it.productId }
                ecomDb.orderDao().insertOrderWithOrderItems(
                    orderItems = orderItemEntities,
                    orderEntity = orderEntitiy
                )
                ecomDb.cartDao().deleteCartsByProductIdsList(productIds = itemsToBeRemovedFromCart)

                //firestore
                val firebaseOrderITems = orderItemEntities.map { orderItemEntity ->
                    FirebaseOrderItem(
                        id = orderItemEntity.id,
                        quantity = orderItemEntity.quantity,
                        productId = orderItemEntity.productId,
                        orderId = orderItemEntity.orderId,
                        totalPrice = orderItemEntity.totalPrice
                    )
                }

                val firebaseOrder = FirebaseOrder(
                    id = orderId,
                    orderTime = orderTime,
                    orderStatus = orderStatus,
                    orderItems = firebaseOrderITems
                )

                val userId = getUserId.firstOrNull()
                if (userId != null) {
                    //order doc ref
                    val orderCollectionRef = firestore.collection("users")
                        .document(userId)
                        .collection("orders")


                    //users cart collection ref
                    val cartColectionRef = firestore.collection("users")
                        .document(userId)
                        .collection("cart")


                    val batch = firestore.batch()

                    //set the order doc in firestore
                    val orderDocRef = orderCollectionRef.document(orderId)
                    batch.set(orderDocRef, firebaseOrder)

                    itemsToBeRemovedFromCart.forEach { productId ->
                        val cartItemDocRef = cartColectionRef.document(productId.toString())
                        batch.delete(cartItemDocRef)
                    }

                    batch.commit().await()

                } else {
                    Result.failure<Unit>(Exception("User not logged in"))
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure<Unit>(e)
            }
        }

    }

    override suspend fun getEcomUserData(): Result<EcomUser> {
        return withContext(ioDispatcher) {
            try {
                val userId = getUserId.firstOrNull()
                if (userId != null) {

                    val userDoc = firestore.collection("users").document(userId).get().await()
                    val user = userDoc.toObject(FrbUser::class.java)
                    if (user != null) {
                        Result.success(EcomUser(username = user.username, email = user.email))
                    } else {
                        Result.failure<EcomUser>(Exception("User not found"))
                    }
                } else {
                    Result.failure<EcomUser>(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure<EcomUser>(e)
            }
        }
    }

    override fun observeProductByProductId(productId: Int): Flow<DomainProduct> {
        return ecomDb.productDao().observeProductByProductId(id = productId)
            .map { it.toDomainProduct() }.distinctUntilChanged()
            .flowOn(ioDispatcher)
    }


}