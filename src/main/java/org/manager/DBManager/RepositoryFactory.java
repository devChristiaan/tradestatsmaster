package org.manager.DBManager;

import org.service.SqliteConnection;

public class RepositoryFactory {
    private final SqliteConnection db;

    private StartUpRepository startupRepo;
    private TransactionRepository transactionRepository;
    private DailyPrepDataRepository dailyPrepDataRepository;

    public RepositoryFactory() {
        this.db = new SqliteConnection();
    }

    public TransactionRepository transactions() {
        if (transactionRepository == null)
            transactionRepository = new TransactionRepository(db);
        return transactionRepository;
    }

    public StartUpRepository startUp() {
        if (startupRepo == null)
            startupRepo = new StartUpRepository(db);
        return startupRepo;
    }

    public DailyPrepDataRepository dailyPrepData() {
        if (dailyPrepDataRepository == null)
            dailyPrepDataRepository = new DailyPrepDataRepository(db);
        return dailyPrepDataRepository;
    }
}
