package io.github.romangolovanov.apps.ametro.catalog.entities

object TransportTypeHelper {

    @JvmStatic
    fun parseTransportType(transportTypeName: String): TransportType = when (transportTypeName) {
        "Метро" -> TransportType.Subway
        "Трамвай" -> TransportType.Tram
        "Автобус" -> TransportType.Bus
        "Электричка" -> TransportType.Train
        "Речной трамвай" -> TransportType.WaterBus
        "Троллейбус" -> TransportType.TrolleyBus
        "Фуникулер" -> TransportType.CableWay
        else -> TransportType.Unknown
    }

    @JvmStatic
    fun parseTransportTypes(names: Array<String>): Array<TransportType> =
        names.mapTo(LinkedHashSet()) { parseTransportType(it) }.toTypedArray()
}
