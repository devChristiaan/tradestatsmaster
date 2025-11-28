package org.model.goal;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public enum ETimeHorizon {
    SHORT_TERM("Short Term"),
    MID_TERM("Mid Term"),
    LONG_TERM("Long Term");

    private final String description;

    ETimeHorizon(String description) {
        this.description = description;
    }

    public static ArrayList<String> getDescriptions() {
        ETimeHorizon[] horizons = ETimeHorizon.values();
        ArrayList<String> values = new ArrayList<>();

        for (ETimeHorizon horizon : horizons) {
            values.add(horizon.getDescription());
        }
        return values;
    }

    public static ETimeHorizon fromDescription(String description) {
        ETimeHorizon[] horizons = ETimeHorizon.values();
        for (ETimeHorizon horizon : horizons) {
            if (horizon.getDescription().equals(description)) {
                return horizon;
            }
        }
        return null;
    }
}
