package org.model;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public enum ETradePerformanceTypes {
    NET_CASH_FLOWS("Net of Cash Flows"),
    TRADES("Trade only Equity Curve"),
    TRADES_CUMULATIVE("Trade Cumulative Equity Curve"),
    CASH_FLOW_ADJUSTED("Cash-Flow Adjusted Equity Curve");

    private final String description;

    ETradePerformanceTypes(String description) {
        this.description = description;
    }

    public static ArrayList<String> getDescriptions() {
        ETradePerformanceTypes[] types = ETradePerformanceTypes.values();
        ArrayList<String> values = new ArrayList<>();

        for (ETradePerformanceTypes type : types) {
            values.add(type.getDescription());
        }
        return values;
    }

    public static ETradePerformanceTypes fromDescription(String description) {
        ETradePerformanceTypes[] types = ETradePerformanceTypes.values();
        for (ETradePerformanceTypes type : types) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return null;
    }
}