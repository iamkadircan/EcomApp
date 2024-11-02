package com.example.feature.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.repository.EcomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {



    private val _uiState = MutableStateFlow(RegisterScreenState())
    val uiState = _uiState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun updatePassword(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = newConfirmPassword) }
    }

    fun updateUsername(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }

    private fun fieldsValid(): Boolean {
        val username = uiState.value.username.trim()
        val email = uiState.value.email.trim()
        val password = uiState.value.password.trim()
        val confirmPassword = uiState.value.confirmPassword.trim()
        return username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && (password == confirmPassword)
    }

    fun register() {
        if (fieldsValid()) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                ecomRepository.register(
                    email = _uiState.value.email,
                    password = _uiState.value.password,
                    username = _uiState.value.username
                ).onSuccess {
                    _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                }
                    .onFailure { thr ->
                        _uiState.update { it.copy(isLoading = false, errorMsg = thr.message) }
                    }
            }
        }else{
            _uiState.update {
                it.copy(errorMsg = "fields are not valid!")
            }
        }
    }

    fun resetRegisterSuccess(){
        _uiState.update { it.copy(registerSuccess = false) }
    }

    fun resetErrorMsg(){
        _uiState.update { it.copy(errorMsg = null) }
    }

}


data class RegisterScreenState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val registerSuccess :Boolean = false,
    val errorMsg :String? = null
)
