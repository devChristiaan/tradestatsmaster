package org.manager;

import org.model.symbol.Symbol;
import org.model.symbol.SymbolDTO;
import org.service.DataObjectService;

import java.util.List;

public class DTOManager {
    public static List<Symbol> getAllSymbols() {
        SymbolDTO symbols = DataObjectService.loadObject(DataObjectService.DataObjectFileType.SYMBOLS);
        if (symbols == null) {
            DataObjectService.saveObject(new SymbolDTO(), DataObjectService.DataObjectFileType.SYMBOLS);
            symbols = DataObjectService.loadObject(DataObjectService.DataObjectFileType.SYMBOLS);
        }
        return symbols.getSymbols();
    }

    public static void addSymbol(Symbol symbol) {
        SymbolDTO symbols = DataObjectService.loadObject(DataObjectService.DataObjectFileType.SYMBOLS);
        if (symbols != null) {
            symbols.addSymbol(symbol);
            DataObjectService.saveObject(symbols, DataObjectService.DataObjectFileType.SYMBOLS);
        }
    }
}
