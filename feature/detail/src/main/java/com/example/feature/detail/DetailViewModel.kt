package com.example.feature.detail

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val ecomRepository: EcomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productId = requireNotNull(savedStateHandle.get<Int>("productId"))

    private val _snackbarMessage = MutableStateFlow<String?>(null)

    private val _isLoading = MutableStateFlow(false)

    private val _product = ecomRepository.observeProductByProductId(productId = productId)
        .onStart { _isLoading.value = true }
        .catch { thr -> _snackbarMessage.value = thr.message ?: "unexpected error!" }
        .onCompletion { _isLoading.value = false }

    val uiState: StateFlow<DetailScreenState> =
        combine(_product, _isLoading, _snackbarMessage) { product, isLoading, snackbarMessage ->
            DetailScreenState(
                product = product,
                isLoading = isLoading,
                snackbarMessage = snackbarMessage
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = DetailScreenState(isLoading = true)
        )


    fun onEvent(event: DetailScreenEvent) {
        when (event) {
            is DetailScreenEvent.AddToCart -> {
                viewModelScope.launch {
                    ecomRepository.addToCart(productId = event.productId)
                        .onFailure { thr ->
                            _snackbarMessage.value = thr.message ?: "unexpected error"
                        }
                }
            }

            is DetailScreenEvent.ToggleFavorite -> {
                viewModelScope.launch {
                    ecomRepository.toggleFavorite(productId = event.productId).onFailure { thr ->
                        _snackbarMessage.value = thr.message ?: "unexpected error"
                    }
                }
            }
        }
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

}

sealed class DetailScreenEvent {
    data class ToggleFavorite(val productId: Int) : DetailScreenEvent()
    data class AddToCart(val productId: Int) : DetailScreenEvent()
}

data class DetailScreenState(
    val product: DomainProduct? = null,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)