package com.empresa.dashboard.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RevenueResponse(
    val total: Double,
    val currency: String,
    val dealCount: Int,
    val sellers: List<SellerDto>,
    val period: PeriodDto,
    val updatedAt: String,
)

@JsonClass(generateAdapter = true)
data class SellerDto(
    val ownerId: String?,
    val name: String,
    val total: Double,
    val dealCount: Int,
)

@JsonClass(generateAdapter = true)
data class PeriodDto(
    val key: String,
    val label: String,
    val from: String,
    val to: String,
)
