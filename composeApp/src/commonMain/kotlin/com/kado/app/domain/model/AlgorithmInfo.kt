package com.kado.app.domain.model

data class AlgorithmInfo(
    val id: String,
    val simpleExplanation: String,
    val parameters: List<ParameterInfo>,
    val technicalDetails: String
)

data class ParameterInfo(val key: String, val name: String, val description: String, val hint: String)
