package org.model.symbol;

import java.io.Serializable;
import java.util.ArrayList;

public class SymbolDTO extends Symbol implements Serializable {
    private final ArrayList<Symbol> listOfSymbols = new ArrayList<>();

    public void addSymbol(Symbol symbol) {
        this.listOfSymbols.add(symbol);
    }

    public ArrayList<Symbol> deleteSymbol(Symbol symbol) {
        this.listOfSymbols.remove(symbol);
        return listOfSymbols;
    }

    public ArrayList<Symbol> getSymbols() {
        return listOfSymbols;
    }
}
