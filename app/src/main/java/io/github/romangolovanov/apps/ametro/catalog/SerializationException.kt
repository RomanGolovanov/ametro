package io.github.romangolovanov.apps.ametro.catalog

class SerializationException(message: String?, cause: Throwable?) : Exception(message, cause) {
    constructor(ex: Exception) : this(ex.message, ex)
}
