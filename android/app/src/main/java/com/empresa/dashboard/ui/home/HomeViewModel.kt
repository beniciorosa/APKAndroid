package com.empresa.dashboard.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.dashboard.data.RevenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val todayTotal: Double = 0.0,
    val todayDeals: Int = 0,
    val last30Total: Double = 0.0,
    val last30Deals: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: RevenueRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Hoje e Últimos 30 dias em paralelo
            val todayResult = repo.getRevenue("today")
            val last30Result = repo.getRevenue("last-30-days")

            todayResult.onSuccess { r ->
                _state.update { it.copy(todayTotal = r.total, todayDeals = r.dealCount) }
            }
            last30Result.onSuccess { r ->
                _state.update { it.copy(last30Total = r.total, last30Deals = r.dealCount) }
            }
            val err = todayResult.exceptionOrNull() ?: last30Result.exceptionOrNull()
            _state.update { it.copy(isLoading = false, error = err?.message) }
        }
    }
}
