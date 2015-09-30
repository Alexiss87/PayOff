package com.simpson.o.alexis.payup.backup;


import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
//import android.util.Log;

import java.io.File;

/**
 * Created by Alexis on 11/4/2014.
 */
public class MyBackupAgent extends BackupAgentHelper {
    String DATABASE_NAME = "payup.sqlite";
    String DATABASE_FILE_NAME = "payup.sqlite.db";
    // The name of the SharedPreferences file
    static final String PREFS = "payup_preferences";

    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "com.simpson.o.alexis.payup.backup_prefs";

    // A key to uniquely identify the set of backup data
    static final String DBS_BACKUP_KEY = "com.simpson.o.alexis.payup.backup_dbs";

    @Override
    public void onCreate() {
        FileBackupHelper dbs = new FileBackupHelper(this, DATABASE_FILE_NAME);
        addHelper(DBS_BACKUP_KEY, dbs);

        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
      //  Log.d("Backup_Agent_called", "Adding backupagent...");
    }

    @Override
    public File getFilesDir() {
        File path = getDatabasePath(DATABASE_FILE_NAME);
        return path.getParentFile();
    }
}
