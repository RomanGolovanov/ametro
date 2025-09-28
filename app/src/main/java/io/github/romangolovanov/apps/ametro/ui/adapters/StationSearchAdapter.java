package io.github.romangolovanov.apps.ametro.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;  // ✅ AndroidX CursorAdapter

import io.github.romangolovanov.apps.ametro.R;
import io.github.romangolovanov.apps.ametro.model.entities.MapScheme;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeLine;
import io.github.romangolovanov.apps.ametro.model.entities.MapSchemeStation;

import java.util.ArrayList;
import java.util.List;

public class StationSearchAdapter extends CursorAdapter {

    private final List<StationInfo> stations;

    public static StationSearchAdapter createFromMapScheme(Context context, MapScheme scheme, String query) {
        List<StationInfo> items = getStations(scheme, query);
        return new StationSearchAdapter(context, createStationNameCursor(items), items);
    }

    protected StationSearchAdapter(Context context, Cursor cursor, List<StationInfo> items) {
        super(context, cursor, false);
        this.stations = items;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((StationInfoHolder) view.getTag()).update(stations.get(cursor.getPosition()));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.station_search_view_list_item, parent, false);
        v.setTag(new StationInfoHolder(v));
        return v;
    }

    public MapSchemeStation getStation(int position) {
        return stations.get(position).getStation();
    }

    protected static class StationInfo {
        private final MapSchemeStation station;
        private final MapSchemeLine line;

        public StationInfo(MapSchemeStation station, MapSchemeLine line) {
            this.station = station;
            this.line = line;
        }

        public MapSchemeStation getStation() {
            return station;
        }

        public MapSchemeLine getLine() {
            return line;
        }
    }

    private static List<StationInfo> getStations(MapScheme scheme, String query) {
        List<StationInfo> stationNamesStartsWith = new ArrayList<>();
        List<StationInfo> stationNamesContains = new ArrayList<>();
        for (MapSchemeLine line : scheme.getLines()) {
            for (MapSchemeStation station : line.getStations()) {
                if (station.getPosition() == null) {
                    continue;
                }

                var queryLowerCase = query.toLowerCase();
                var allNames = station.getAllDisplayNames();

                if (allNames.stream().anyMatch(s -> s.toLowerCase().startsWith(queryLowerCase))) {
                    stationNamesStartsWith.add(new StationInfo(station, line));
                    stationNamesContains.add(new StationInfo(station, line));
                    continue;
                }

                if (allNames.stream().anyMatch(s -> s.toLowerCase().contains(queryLowerCase))) {
                    stationNamesContains.add(new StationInfo(station, line));
                }
            }
        }

        return !stationNamesStartsWith.isEmpty() ? stationNamesStartsWith : stationNamesContains;
    }

    private static MatrixCursor createStationNameCursor(List<StationInfo> items) {
        Object[] temp = new Object[]{0, "default"};
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "text"});
        for (StationInfo item : items) {
            final MapSchemeStation station = item.getStation();
            temp[0] = station.getUid();
            temp[1] = station.getDisplayName();
            cursor.addRow(temp);
        }
        return cursor;
    }

    private static class StationInfoHolder {

        private final TextView stationView;
        private final TextView lineView;
        private final ImageView iconView;

        public StationInfoHolder(View v) {
            stationView = v.findViewById(R.id.station);
            lineView = v.findViewById(R.id.line);
            iconView = v.findViewById(R.id.icon);
        }

        public void update(StationInfo station) {
            stationView.setText(station.getStation().getDisplayName());
            lineView.setText(station.getLine().getDisplayName());
            GradientDrawable drawable = (GradientDrawable) iconView.getBackground();
            drawable.setColor(station.getLine().getLineColor());
        }
    }
}
