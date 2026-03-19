package io.github.romangolovanov.apps.ametro.utils

object ListUtils {

    @JvmStatic
    fun <T> firstOrDefault(target: Collection<T>?, defaultValue: T): T {
        if (target == null || target.isEmpty()) {
            return defaultValue
        }
        return target.iterator().next()
    }

    fun interface IPredicate<T> {
        fun apply(type: T): Boolean
    }

    @JvmStatic
    fun <T> filter(target: Collection<T>, predicate: IPredicate<T>): Collection<T> {
        val result = ArrayList<T>()
        for (element in target) {
            if (predicate.apply(element)) {
                result.add(element)
            }
        }
        return result
    }
}
