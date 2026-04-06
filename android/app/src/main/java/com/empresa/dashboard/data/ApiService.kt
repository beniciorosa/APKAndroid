package com.empresa.dashboard.data

import com.empresa.dashboard.data.models.ProductResponse
import com.empresa.dashboard.data.models.RevenueResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/revenue/by-seller")
    suspend fun getRevenueBySeller(
        @Query("period") period: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): RevenueResponse

    @GET("api/revenue")
    suspend fun getRevenue(
        @Query("period") period: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): RevenueResponse

    @GET("api/revenue/by-product")
    suspend fun getRevenueByProduct(
        @Query("period") period: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): ProductResponse
}
