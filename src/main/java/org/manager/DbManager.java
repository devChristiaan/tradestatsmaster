package org.manager;

import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DBManager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.manager.DTOManager.getAllAccountTransactions;
import static org.manager.DTOManager.getAllSymbols;

public class DbManager {
    private final Logger log = LoggerFactory.getLogger(DbManager.class);
    private final StartUpRepository startUp;
    private final TransactionRepository tran;
    private final DailyPrepDataRepository dailyData;
    private final JournalRepository journals;
    private final GoalsRepository goals;

    public DbManager(RepositoryFactory repo) {
        this.startUp = repo.startUp();
        this.tran = repo.transactions();
        this.dailyData = repo.dailyPrepData();
        this.journals = repo.journals();
        this.goals = repo.goals();
        if (ControllerRegistry.get(RepositoryFactory.class) == null)
            ControllerRegistry.register(RepositoryFactory.class, repo);
    }

    public void instantiateData() {
        startUp.dbStartUpChecks();
        GlobalContext.getTransactions().setAllMaster(tran.getAllTransactions());
        GlobalContext.getDailyPrep().setAllMaster(dailyData.getAllDailyPrepData());
        GlobalContext.getJournals().setAllMaster(journals.getAllJournalEntries());
        GlobalContext.getGoals().setAllMaster(goals.getAllGoals());

        ///Serialized DTO Object
        GlobalContext.getSymbols().setAllMaster(getAllSymbols());
        GlobalContext.getAccounts().setAllMaster(getAllAccountTransactions());
    }
}
