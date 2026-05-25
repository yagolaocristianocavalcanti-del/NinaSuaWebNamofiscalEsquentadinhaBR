package com.nina.namofiscal.model

data class Vehicle(
    val plate: String,
    val color: String,
    val brand: String,
    val model: String,
    val prisma: String,
    val entryTime: Long = System.currentTimeMillis(),
    val manobristaId: String? = null
)
