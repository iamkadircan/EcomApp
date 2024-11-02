package com.example.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.DomainCart
import com.example.core.domain.model.DomainOrderItem
import com.example.core.domain.repository.EcomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {

    private val _checkedItems = MutableStateFlow<List<CheckedCart>>(emptyList())
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    private val _carts = ecomRepository.observeCarts().onStart {
        _isLoading.value = true
    }.catch { thr ->
        _snackbarMessage.value = thr.message ?: "unknown error occured"
    }.onCompletion {
        _isLoading.value = false
    }


    val uiState = combine(
        _carts,
        _isLoading,
        _snackbarMessage,
        _checkedItems
    ) { carts, isLoading, errorMsg, checkedItems ->
        val totalPrice =
            carts.filter { cartITem -> cartITem.productId in checkedItems.map { it.productId } }
                .sumOf { it.quantity * it.price }

        CartScreenState(
            carts = carts,
            isLoading = false,
            totalPrice = totalPrice,
            checkedItems = checkedItems,
            snackbarMessage = errorMsg
        )


    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(500), CartScreenState(isLoading = false)
    )

    fun onEvent(event: CartScreenEvent) {
        when (event) {
            is CartScreenEvent.ChangeCartQuantity -> {
                viewModelScope.launch {
                    ecomRepository.updateCartQuantityByProductId(
                        newQuantity = event.newQuantity,
                        productId = event.productId
                    ).onFailure { thr ->
                        _snackbarMessage.value = thr.message ?: "unknow error !"
                    }
                }
            }

            CartScreenEvent.CreateOrder -> {
                viewModelScope.launch {
                    val checkedItems = _carts.first()
                        .filter { cartItem -> cartItem.productId in _checkedItems.value.map { it.productId } }

                    val domainOrderItems = checkedItems.map {
                        DomainOrderItem(
                            productId = it.productId,
                            quantity = it.quantity,
                            itemsTotalPrice = it.quantity * it.price,
                            title = "", thumbnail = "", description = "", orderId = ""
                        )
                    }

                    ecomRepository.createOrder(domainOrderItems).onFailure { thr ->
                        _snackbarMessage.value = thr.message ?: "unknown error occured !"
                    }.onSuccess {
                        _snackbarMessage.value = "order created successfully"
                    }
                }
            }

            is CartScreenEvent.RemoveCart -> {
                viewModelScope.launch {
                    ecomRepository.deleteCartItemByProductId(
                        productId = event.productId
                    ).onFailure { thr ->
                        _snackbarMessage.value = thr.message ?: "unknown error"
                    }
                }
            }

            is CartScreenEvent.ToggleCartCheck -> {
                val checkedItems = _checkedItems.value.map { it.productId }.toMutableList()
                if (event.productId in checkedItems) {
                    checkedItems.remove(event.productId)
                } else {
                    checkedItems.add(event.productId)
                }
                _checkedItems.value = checkedItems.map { CheckedCart(productId = it) }

            }
        }
    }


    fun resetErrorMsg() {
        _snackbarMessage.value = null
    }

}

data class CartScreenState(
    val carts: List<DomainCart> = emptyList(),
    val isLoading: Boolean = false,
    val totalPrice: Double = 0.0,
    val checkedItems: List<CheckedCart> = emptyList(),
    val snackbarMessage: String? = null
)

data class CheckedCart(
    val productId: Int
)

sealed class CartScreenEvent {
    data class ToggleCartCheck(val productId: Int) : CartScreenEvent()
    data class ChangeCartQuantity(val productId: Int, val newQuantity: Int) : CartScreenEvent()
    data class RemoveCart(val productId: Int) : CartScreenEvent()
    data object CreateOrder : CartScreenEvent()
}