package org.manager.DBManager;

import org.service.SqliteConnection;

public class RepositoryFactory {
    private final SqliteConnection db;

    private TransactionRepository transactionRepository;

    public RepositoryFactory() {
        this.db = new SqliteConnection();
    }

    public TransactionRepository transactions() {
        if (transactionRepository == null)
            transactionRepository = new TransactionRepository(db);
        return transactionRepository;
    }
}
