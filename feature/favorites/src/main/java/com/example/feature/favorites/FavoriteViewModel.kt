package com.example.feature.favorites

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _favoriteProductsFlow = ecomRepository.observeFavoriteProducts().catch { thr ->
        _errorMessage.value = thr.message ?: "unexpected error occured"
        emit(emptyList())
    }

    val uiState: StateFlow<FavoriteScreenUiState> =
        combine(_favoriteProductsFlow, _errorMessage) { favorites, errorMsg ->
            FavoriteScreenUiState(
                favoriteProducts = favorites,
                isLoading = false,
                snackbarMessage = errorMsg
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(500), FavoriteScreenUiState(isLoading = true)
        )


    fun onEvent(event: FavoriteScreenEvent) {
        when (event) {
            is FavoriteScreenEvent.AddToCart -> {
                viewModelScope.launch {
                    ecomRepository.addToCart(productId = event.productId).onFailure { thr ->
                        _errorMessage.value = thr.message ?: "unexpected error occured"
                    }
                }

            }

            is FavoriteScreenEvent.ToggleFav -> {
                viewModelScope.launch {
                    ecomRepository.toggleFavorite(productId = event.productId).onFailure { thr ->
                        _errorMessage.value = thr.message ?: "unexpected error occured"
                    }
                }
            }
        }
    }

    fun resetErrorMsg() {
        _errorMessage.value = null
    }

}

data class FavoriteScreenUiState(
    val favoriteProducts: List<DomainProduct> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

sealed class FavoriteScreenEvent {
    data class ToggleFav(val productId: Int) : FavoriteScreenEvent()
    data class AddToCart(val productId: Int) : FavoriteScreenEvent()
}

