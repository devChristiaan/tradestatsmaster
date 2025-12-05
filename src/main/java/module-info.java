module org.app {
    requires javafx.fxml;
    requires javafx.web;
    requires static lombok;
    requires jakarta.annotation;
    requires atlantafx.base;
    requires com.opencsv;
    requires org.xerial.sqlitejdbc;
    requires java.sql;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.material2;
    requires org.slf4j;

    exports org.app;
    exports org.controller;
    exports org.utilities;
    exports org.service;
    exports org.model;
    exports org.model.transaction;
    exports org.model.dailyPrep;
    exports org.model.symbol;
    exports org.model.account;
    exports org.model.journal;
    exports org.model.goal;
    exports org.context;
    exports org.manager;
    exports org.logging;
    opens org.app to javafx.fxml;
    opens org.controller to javafx.fxml;
    opens org.utilities to javafx.fxml;
    opens org.service to com.opencsv;
    opens org.logging to javafx.fxml;
}