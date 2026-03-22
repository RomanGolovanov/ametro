package io.github.romangolovanov.apps.ametro.model.entities

data class MapTransportTransfer(
    val from: Int,
    val to: Int,
    val delay: Int,
    val isVisible: Boolean
)
