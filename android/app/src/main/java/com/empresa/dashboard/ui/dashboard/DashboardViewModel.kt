package com.empresa.dashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.dashboard.data.RevenueRepository
import com.empresa.dashboard.data.models.ProductDto
import com.empresa.dashboard.data.models.SellerDto
import com.empresa.dashboard.ui.model.Period
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class DashboardUiState(
    val period: Period = Period.TODAY,
    val customFrom: String? = null,
    val customTo: String? = null,
    val total: Double = 0.0,
    val sellers: List<SellerDto> = emptyList(),
    val dealCount: Int = 0,
    val periodLabel: String = "Este mês",
    val updatedAt: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<ProductDto> = emptyList(),
    val productsTotal: Double = 0.0,
    val isLoadingProducts: Boolean = false,
    val productsError: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: RevenueRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectPeriod(period: Period, from: String? = null, to: String? = null) {
        _state.update { it.copy(period = period, customFrom = from, customTo = to) }
        refresh()
    }

    fun refresh() {
        val current = _state.value
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repo.getRevenueBySeller(
                period = current.period.apiKey,
                from = current.customFrom,
                to = current.customTo,
            )
            result.onSuccess { resp ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        total = resp.total,
                        sellers = resp.sellers,
                        dealCount = resp.dealCount,
                        periodLabel = resp.period.label,
                        updatedAt = formatUpdatedAt(resp.updatedAt),
                        error = null,
                    )
                }
            }.onFailure { t ->
                _state.update {
                    it.copy(isLoading = false, error = t.message ?: "Erro ao carregar dados")
                }
            }
        }
    }

    fun loadProducts() {
        val current = _state.value
        _state.update { it.copy(isLoadingProducts = true, productsError = null) }
        viewModelScope.launch {
            val result = repo.getRevenueByProduct(
                period = current.period.apiKey,
                from = current.customFrom,
                to = current.customTo,
            )
            result.onSuccess { resp ->
                _state.update {
                    it.copy(
                        isLoadingProducts = false,
                        products = resp.products,
                        productsTotal = resp.total,
                        productsError = null,
                    )
                }
            }.onFailure { t ->
                _state.update {
                    it.copy(isLoadingProducts = false, productsError = t.message ?: "Erro")
                }
            }
        }
    }

    private fun formatUpdatedAt(iso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val formatter = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).apply {
                timeZone = TimeZone.getDefault()
            }
            val date: Date = parser.parse(iso) ?: return iso
            formatter.format(date)
        } catch (_: Exception) {
            iso
        }
    }
}
