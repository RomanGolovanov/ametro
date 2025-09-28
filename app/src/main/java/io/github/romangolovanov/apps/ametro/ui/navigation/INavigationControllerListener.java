package io.github.romangolovanov.apps.ametro.ui.navigation;


import io.github.romangolovanov.apps.ametro.model.entities.MapDelay;

public interface INavigationControllerListener {
    boolean onOpenMaps();

    boolean onOpenSettings();

    boolean onChangeScheme(String schemeName);

    boolean onToggleTransport(String source, boolean checked);

    boolean onDelayChanged(MapDelay delay);

    boolean onOpenAbout();
}
