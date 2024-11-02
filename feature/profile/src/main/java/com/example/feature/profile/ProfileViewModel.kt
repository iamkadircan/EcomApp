package com.example.feature.profile


import android.text.TextUtils.TruncateAt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.DomainOrder
import com.example.core.domain.model.EcomUser
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
import kotlin.math.log

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _logoutSucceeded = MutableStateFlow(false)
    private val _ecomUser = MutableStateFlow<EcomUser?>(null)

    private val _orders = ecomRepository.observeOrders().onStart {
        _isLoading.value = true
    }.catch { thr ->
        _snackbarMessage.value = thr.message ?: "failed to get orders"
    }.onCompletion {
        _isLoading.value = false
    }

    val uiState = combine(
        _ecomUser,
        _orders,
        _snackbarMessage,
        _isLoading,
        _logoutSucceeded
    ) { ecomUser, orders, snackbarMessage, isLoading, logoutSucceeded ->
        ProfileScreenState(
            userName = ecomUser?.username ?: "",
            email = ecomUser?.email ?: "",
            orders = orders,
            snackbarMessage = snackbarMessage,
            logoutSucceed = logoutSucceeded,
            isLoading = isLoading

        )

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(500),
        ProfileScreenState(isLoading = true)
    )


    init {
        viewModelScope.launch {
            _isLoading.value = true
            ecomRepository.getEcomUserData()
                .onSuccess { user ->
                    _ecomUser.value = user
                }.onFailure { thr ->
                    _snackbarMessage.value = thr.message ?: "failed to get user data"
                }
            _isLoading.value = false
        }
    }

    fun onEvent(event: ProfileScreenEvent) {
        when (event) {
            ProfileScreenEvent.Logout -> {
                viewModelScope.launch {
                    _isLoading.value = true
                    ecomRepository.logout().onSuccess {
                        _logoutSucceeded.value = true
                    }.onFailure { thr ->
                        _snackbarMessage.value = thr.message ?: "failed to logout"
                    }
                }
                _isLoading.value = false
            }
        }
    }

    fun resetSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun resetLogoutSucceeded() {
        _logoutSucceeded.value = false
    }

}


data class ProfileScreenState(
    val userName: String = "",
    val email: String = "",
    val orders: List<DomainOrder> = emptyList(),
    val snackbarMessage: String? = null,
    val isLoading: Boolean = false,
    val logoutSucceed: Boolean = false
)


sealed class ProfileScreenEvent {
    data object Logout : ProfileScreenEvent()
}