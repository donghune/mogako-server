package kr.donghune.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class SuccessResponse(
    val message: String
)