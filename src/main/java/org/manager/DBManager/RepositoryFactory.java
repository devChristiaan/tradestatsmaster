package org.manager.DBManager;

import org.service.SqliteConnection;

public class RepositoryFactory {
    private final SqliteConnection db;

    private StartUpRepository startupRepo;
    private TransactionRepository transactionRepository;
    private DailyPrepDataRepository dailyPrepDataRepository;
    private JournalRepository journalRepository;
    private GoalsRepository goalsRepository;

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

    public JournalRepository journals() {
        if (journalRepository == null)
            journalRepository = new JournalRepository(db);
        return journalRepository;
    }

    public GoalsRepository goals() {
        if (goalsRepository == null)
            goalsRepository = new GoalsRepository(db);
        return goalsRepository;
    }
}
