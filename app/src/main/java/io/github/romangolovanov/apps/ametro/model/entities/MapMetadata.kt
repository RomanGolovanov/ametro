package io.github.romangolovanov.apps.ametro.model.entities

import android.content.res.Resources

class MapMetadata(
    private val locale: MapLocale?,
    val id: String,
    val cityId: Int,
    val timestamp: Int,
    val latitude: Double,
    val longitude: Double,
    val schemes: Map<String, Scheme>,
    val transports: Map<String, TransportScheme>,
    val transportTypes: Array<String>,
    val delays: Array<MapDelay>,
    val locales: Array<String>,
    private val commentsTextId: Int?,
    private val descriptionTextId: Int?,
    val fileName: String
) {
    val comments: String? get() = if (commentsTextId != null) locale?.getText(commentsTextId) else null
    val description: String? get() = if (descriptionTextId != null) locale?.getText(descriptionTextId) else null

    fun getScheme(name: String): Scheme {
        return schemes[name] ?: throw Resources.NotFoundException("Not found metadata for scheme $name")
    }

    fun getTransport(name: String): TransportScheme {
        return transports[name] ?: throw Resources.NotFoundException("Not found metadata for transport $name")
    }

    class Scheme(
        private val locale: MapLocale?,
        val name: String,
        private val nameTextId: Int?,
        val typeName: String,
        private val typeTextId: Int?,
        val fileName: String,
        val transports: Array<String>,
        val defaultTransports: Array<String>,
        val isRoot: Boolean
    ) {
        val displayName: String? get() = if (nameTextId != null) locale?.getText(nameTextId) else null
        val typeDisplayName: String? get() = if (typeTextId != null) locale?.getText(typeTextId) else null
    }

    class TransportScheme(
        val name: String,
        val fileName: String,
        val typeName: String
    )
}
