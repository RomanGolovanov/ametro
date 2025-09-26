package org.ametro.ng.ui.navigation;


import org.ametro.ng.model.entities.MapDelay;

public interface INavigationControllerListener {
    boolean onOpenMaps();

    boolean onOpenSettings();

    boolean onChangeScheme(String schemeName);

    boolean onToggleTransport(String source, boolean checked);

    boolean onDelayChanged(MapDelay delay);

    boolean onOpenAbout();
}
