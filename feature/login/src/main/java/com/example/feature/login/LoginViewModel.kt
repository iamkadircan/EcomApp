package com.example.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.repository.EcomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginScreenState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.newEmailValue) }
            }

            is LoginScreenEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.newPasswordValue) }
            }

            LoginScreenEvent.Login -> {
                val email = uiState.value.email.trim()
                val password = uiState.value.password.trim()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    _uiState.update { it.copy(isLoading = true, loginErrorMessage = null) }

                    viewModelScope.launch {
                        ecomRepository.login(email = email, password = password)
                            .onSuccess {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false, loginSuccess = true
                                    )
                                }
                            }.onFailure { thr ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        loginErrorMessage = thr.message ?: "unknown error"
                                    )
                                }
                            }
                    }
                }
            }

        }
    }

    fun resetLoginSuccess() {
        _uiState.update {
            it.copy(loginSuccess = false)
        }
    }

    fun resetLoginErrorMessage() {
        _uiState.update { it.copy(loginErrorMessage = null) }
    }




}


sealed class LoginScreenEvent {
    data class EmailChanged(val newEmailValue: String) : LoginScreenEvent()
    data class PasswordChanged(val newPasswordValue: String) : LoginScreenEvent()
    data object Login : LoginScreenEvent()
}


data class LoginScreenState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val loginErrorMessage: String? = null
)
