package com.empresa.dashboard.data

import com.empresa.dashboard.data.models.ProductResponse
import com.empresa.dashboard.data.models.RevenueResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevenueRepository @Inject constructor(
    private val api: ApiService,
) {
    suspend fun getRevenueBySeller(
        period: String,
        from: String? = null,
        to: String? = null,
    ): Result<RevenueResponse> = runCatching {
        api.getRevenueBySeller(period, from, to)
    }

    suspend fun getRevenue(
        period: String,
        from: String? = null,
        to: String? = null,
    ): Result<RevenueResponse> = runCatching {
        api.getRevenue(period, from, to)
    }

    suspend fun getRevenueByProduct(
        period: String,
        from: String? = null,
        to: String? = null,
    ): Result<ProductResponse> = runCatching {
        api.getRevenueByProduct(period, from, to)
    }
}
