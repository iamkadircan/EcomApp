package com.example.feature.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.DomainOrder
import com.example.core.domain.repository.EcomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val ecomRepository: EcomRepository
) : ViewModel() {

    val orders: StateFlow<List<DomainOrder>> = ecomRepository.observeOrders().stateIn(
        viewModelScope,
        SharingStarted.Eagerly, emptyList()
    )


}