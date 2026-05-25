package com.nina.namofiscal.model

data class License(
    val key: String,
    val expirationDate: Long,
    val enabledModules: List<String> = emptyList(),
    val isOfflineFallback: Boolean = false
)
