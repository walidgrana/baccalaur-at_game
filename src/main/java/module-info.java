module com.bac {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.sql;
    requires java.net.http;
    
    requires com.google.gson;
    requires org.xerial.sqlitejdbc;
    requires org.hibernate.orm.community.dialects;
    
    opens com.bac to javafx.fxml;
    opens com.bac.controller to javafx.fxml;
    opens com.bac.model.entity to org.hibernate.orm.core, javafx.base;
    
    exports com.bac;
    exports com.bac.controller;
    exports com.bac.model.entity;
    exports com.bac.model.dao;
    exports com.bac.service;
    exports com.bac.network;
}
