package com.example.aplicacion_fronton.model.dto

data class DenunciaCreateRequest(
    val objetivo_tipo: String,
    val objetivo_id: Int,
    val motivo: String,
)
