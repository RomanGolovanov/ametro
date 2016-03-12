package org.ametro.utils;

import java.util.ArrayList;
import java.util.Collection;

public class ListUtils {

    public static <T> T firstOrDefault(Collection<T> target, T defaultValue) {
        if(target == null || target.size() == 0){
            return defaultValue;
        }
        return target.iterator().next();
    }

    public interface IPredicate<T> { boolean apply(T type); }

    public static <T> Collection<T> filter(Collection<T> target, IPredicate<T> predicate) {
        Collection<T> result = new ArrayList<>();
        for (T element: target) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }
}
