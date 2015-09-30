package com.simpson.o.alexis.payup.storage;

import android.net.Uri;

import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.Lender;
import com.simpson.o.alexis.payup.PayUpApplication;
import com.simpson.o.alexis.payup.PayUpDataBaseHelper;
import com.simpson.o.alexis.payup.Utils.AppLog;
import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public class StorageDataTransfer {

    private static final String TAG = StorageDataTransfer.class.getSimpleName();

    private static final AppStorage storage = Storage.getStorage();
    private static List<Borrower> borrowerList = new ArrayList<Borrower>();
    private static List<Lender> lenderList = new ArrayList<Lender>();
  //  private static List<AbstractNote> notesBackup;
    private static boolean backupPerformed = false;


    private static void backupCurrentStorage() {
        borrowerList = storage.getAllBorrowers();
        lenderList = storage.getAllLenders();
        backupPerformed = true;
    }

    private static void restoreBackup() {
        checkBackupPerformed();

    storage.sync();

        // restore borrowers
        for (Borrower b : borrowerList){
            storage.insertBorrower(b);
        }
        //restore lenders
       for (Lender l : lenderList){
           storage.insertLender(l);
       }

        /*for (AbstractNote note : notesBackup) {
            Serializable newId = storage.insertNote(note);
            notesOldToNewIdsMap.put(note.getId(), newId);
        }*/


    }

    private static void checkBackupPerformed() {
        if (!backupPerformed) {
            throw new IllegalStateException("Backup not performed!");
        }
    }

    private static void clearCurrentStorage() {
        checkBackupPerformed();
        storage.clear();
    }

    private static void clearBackup() {
        lenderList = null;
        borrowerList = null;
        backupPerformed = false;
    }

    public static synchronized void transferDataFromSqlToDropbox( boolean clearSqlStorage){
        PayUpDataBaseHelper SqlDatabase = new PayUpDataBaseHelper(PayUpApplication.getContext());
        borrowerList = SqlDatabase.getAllBorrowers();
        lenderList = SqlDatabase.getAllLenders();
        if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {

                for (Borrower b : borrowerList) {
                    storage.insertBorrower(b);
                }
                //restore lenders
                for (Lender l : lenderList) {
                    storage.insertLender(l);
                }
                clearBackup();
                if (clearSqlStorage) {
                    SqlDatabase.clear();
                }
        }

    }


    public static synchronized void changeStorageType(Storage.Type newStorageType, boolean clearCurrentStorage) {
        if (newStorageType == null) {
            throw new NullPointerException("New storage type is null");
        }
        if (Storage.getCurrentStorageType() == newStorageType) {
            return;
        }

        backupCurrentStorage();
        if (clearCurrentStorage) {
            clearCurrentStorage();
        }

        boolean newStorageInitialized = false;
        try {
            Storage.init(newStorageType);
            newStorageInitialized = true;
        } catch (Exception e) {
            AppLog.e(TAG, "Exception during storage initialization", e);
        }

        // restore data to new initialized storage or
        // to old storage if new storage has not been initialized and old has been cleared
        if (newStorageInitialized || clearCurrentStorage) {
            restoreBackup();
        }

        clearBackup();

    }
}
