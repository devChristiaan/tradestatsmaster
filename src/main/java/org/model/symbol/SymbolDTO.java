package org.model.symbol;

import java.io.Serializable;
import java.util.ArrayList;

public class SymbolDTO extends Symbol implements Serializable {
    private final ArrayList<Symbol> listOfSymbols = new ArrayList<>();

    public void addSymbol(Symbol symbol) {
        this.listOfSymbols.add(symbol);
    }

    public void addAllSymbols(ArrayList<Symbol> listOfSymbols) {
        this.listOfSymbols.addAll(listOfSymbols);
    }

    public void deleteSymbol(Symbol symbol) {
        Symbol symbolIndex = listOfSymbols.stream().filter(p -> p.getSymbol().equals(symbol.getSymbol())).findFirst().orElse(null);
        this.listOfSymbols.remove(symbolIndex);
    }

    public ArrayList<Symbol> getSymbols() {
        return listOfSymbols;
    }
}
