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
    val monthTotal: Double = 0.0,
    val monthDeals: Int = 0,
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
            // Hoje e Este mês em paralelo
            val todayResult = repo.getRevenue("today")
            val monthResult = repo.getRevenue("this-month")

            todayResult.onSuccess { r ->
                _state.update { it.copy(todayTotal = r.total, todayDeals = r.dealCount) }
            }
            monthResult.onSuccess { r ->
                _state.update { it.copy(monthTotal = r.total, monthDeals = r.dealCount) }
            }
            val err = todayResult.exceptionOrNull() ?: monthResult.exceptionOrNull()
            _state.update { it.copy(isLoading = false, error = err?.message) }
        }
    }
}
