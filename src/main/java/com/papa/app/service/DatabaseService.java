package com.papa.app.service;

import org.springframework.stereotype.Service;

import com.papa.app.DBConnection;

import jakarta.annotation.PreDestroy;

@Service
public class DatabaseService {
    private final DBConnection dbConnection;

    public DatabaseService() {
        this.dbConnection = new DBConnection();
    }

    public DBConnection getConnection() {
        return dbConnection;
    }

    @PreDestroy
    public void cleanup() {
        if (dbConnection != null) {
            dbConnection.closeDBConnection();
        }
    }
}
