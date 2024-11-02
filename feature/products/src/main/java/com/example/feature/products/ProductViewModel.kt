package com.example.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.DomainProduct
import com.example.core.domain.repository.EcomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _currentlimit = MutableStateFlow(20)
    private val _errorMsg = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    private val _allProducts = _searchQuery.debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            ecomRepository.observeProducts(searchQuery = query)
                .onStart { _isLoading.value= true }
                .catch { thr ->
                    _errorMsg.value = thr.message ?: "unknown error occured"
                    emit(emptyList())

                }

        }.onEach { _isLoading.value = false }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(500), emptyList()
        )

    val uiState: StateFlow<ProductScreenUiState> = combine(
        _searchQuery,
        _currentlimit,
        _errorMsg,
        _isLoading,
        _allProducts
    ) { searchQuery, currentLimit, errorMsg, isLoading, allProducts ->
        val productsToDisplay = allProducts.take(currentLimit)
        println("mylogger :inside ui state, search:$searchQuery , currlimit :$currentLimit , error:$errorMsg , isloading:$isLoading ,")
        println("mylogger : allproducts = ${_allProducts.value.size}")
        ProductScreenUiState(
            products = productsToDisplay,
            searchQuery = searchQuery,
            snackbarMessage = errorMsg,
            isLoading = isLoading
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(500),
        ProductScreenUiState(isLoading = true)
    )

    fun resetSnackbarMessage() {
        _errorMsg.value = null
    }

    fun onEvent(event: ProductScreenEvent) {
        when (event) {
            is ProductScreenEvent.AddToCart -> {
                viewModelScope.launch {
                    ecomRepository.addToCart(productId = event.productId)
                        .onFailure { thr ->
                            _errorMsg.value = thr.message ?: "unknown error occured"
                        }

                }
            }

            is ProductScreenEvent.ToggleFav -> {
                viewModelScope.launch {
                    ecomRepository.toggleFavorite(productId = event.productId).onFailure { thr ->
                        _errorMsg.value = thr.message ?: "unknown error occured"
                    }
                }
            }

            is ProductScreenEvent.UpdateSearchQuery -> {
                _searchQuery.value = event.newQuery
                _currentlimit.value = 20
            }
        }
    }

    fun loadMoreProducts() {
        val state = uiState.value
        if (state.isLoading) return
        val totalProductsSize = _allProducts.value.size
        val currentItemSize = state.products.size
        if (currentItemSize >= totalProductsSize) return

        _currentlimit.value += 10

    }
}


data class ProductScreenUiState(
    val products: List<DomainProduct> = emptyList(),
    val searchQuery: String = "",
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false
)

sealed class ProductScreenEvent {
    data class UpdateSearchQuery(val newQuery: String) : ProductScreenEvent()
    data class AddToCart(val productId: Int) : ProductScreenEvent()
    data class ToggleFav(val productId: Int) : ProductScreenEvent()
}