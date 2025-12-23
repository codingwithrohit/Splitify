package com.example.splitify.domain.model

data class SimplifiedDebt(
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Double
)