package io.github.romangolovanov.apps.ametro.model.serialization

class MapSerializationException(message: String?, cause: Throwable?) : Exception(message, cause) {
    constructor(ex: Exception) : this(ex.message, ex)
}
