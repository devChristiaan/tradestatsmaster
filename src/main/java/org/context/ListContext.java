package org.context;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

public class ListContext<T> {
    private final ObservableList<T> master = FXCollections.observableArrayList();
    private final FilteredList<T> filtered;

    public ListContext() {
        this.filtered = new FilteredList<>(master, p -> true);
    }

    public ObservableList<T> getMaster() {
        return master;
    }

    public void replaceMaster(List<T> newMaster) {
        master.clear();
        master.addAll(newMaster);
    }

    public void replaceItemInMaster(T newItem) {
        master.set(master.indexOf(newItem), newItem);
    }

    public FilteredList<T> getFiltered() {
        return filtered;
    }

    public void setAllMaster(List<T> list) {
        master.setAll(list);
    }

    public void addToMaster(T item) {
        master.add(item);
    }

    public void removeFromMaster(T item) {
        master.remove(item);
    }
}
