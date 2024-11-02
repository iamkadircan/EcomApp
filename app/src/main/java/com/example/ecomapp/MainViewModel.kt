package com.example.ecomapp

import androidx.lifecycle.ViewModel
import com.example.core.domain.repository.EcomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: EcomRepository
):ViewModel() {
    val hasUser = repository.hasUser
}