package io.github.romangolovanov.apps.ametro.ui.navigation

import io.github.romangolovanov.apps.ametro.model.entities.MapMetadata

class SchemeNavigationListComparator : Comparator<MapMetadata.Scheme> {

    override fun compare(lhs: MapMetadata.Scheme, rhs: MapMetadata.Scheme): Int {
        val byType = getType(lhs).compareTo(getType(rhs))
        if (byType != 0) {
            return byType
        }
        return getDisplayName(lhs).compareTo(getDisplayName(rhs))
    }

    private fun getType(scheme: MapMetadata.Scheme): String {
        val type = if (scheme.typeName == "ROOT") scheme.displayName ?: "" else scheme.typeDisplayName ?: ""
        if (scheme.name == "metro" || scheme.typeName == "Метро") {
            return ""
        }
        if (type == "OTHER") {
            return "" + Char.MAX_VALUE
        }
        return type
    }

    private fun getDisplayName(scheme: MapMetadata.Scheme): String {
        return if (scheme.typeName == "ROOT") "" else scheme.displayName ?: ""
    }
}
